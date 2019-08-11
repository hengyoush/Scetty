package common.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

import common.SystemPropertyUtil
import common.concurrent.DefaultPromise._
import common.logger.{InternalLogger, InternalLoggerFactory}
import eventloop.EventExecutor

import scala.concurrent.CancellationException
import scala.concurrent.duration.TimeUnit

class DefaultPromise[T](eventExecutor: EventExecutor) extends AbstractFuture[T] with Promise[T] {
  require(eventExecutor != null, "eventExecutor不能为空")

  @volatile private var result: Any = _

  private var listeners: AnyRef = _
  private var waiters: Short = _
  private var notifyingListeners: Boolean = _

  protected def this() = this(null)

  override def setSuccess(result: T): Promise[T] =
    if (setSuccess0(result)) this else throw new IllegalStateException("operation already success")

  override def trySuccess(result: T): Boolean = setSuccess0(result)

  override def setFailure(cause: Throwable): Promise[T] =
    if (setFailure0(cause)) this else throw new IllegalStateException("operation already success")

  override def tryFailure(cause: Throwable): Boolean = setFailure0(cause)

  override def setUncancellable: Boolean = {
    if (RESULT_UPDATER.compareAndSet(this, null, UNCANCELLABLE))
      true
    else {
      val result = this.result
      !isDone0(result) || !isCancelled0(result)
    }
  }

  override def isDone: Boolean = isDone0(result)

  override def isSuccess: Boolean = {
    val result = this.result
    result != null && result != UNCANCELLABLE && !result.isInstanceOf[CauseHolder]
  }

  override def isCancelled: Boolean = isCancelled0(this.result)

  override def isCancellable: Boolean = result == null

  override def cause: Throwable = {
    val result = this.result
    result match {
      case r: CauseHolder => r.cause
      case _ => null
    }
  }

  override def addListener(listener: GenericFutureListener[_ <: Future[_ >: T]]): Promise[T] = {
    synchronized(this)(addListener0(listener))
    if (isDone) notifyListeners()
    this
  }

  override def addListeners(listeners: GenericFutureListener[_ <: Future[_ >: T]]*): Promise[T] = {
    synchronized(this)(listeners.filter(_ != null).foreach(addListener0))
    if (isDone) notifyListeners()
    this
  }

  override def removeListener(listener: GenericFutureListener[_ <: Future[_ >: T]]): Promise[T] = {
    synchronized(this)(removeListener0(listener))
    this
  }

  override def removeListeners(listeners: GenericFutureListener[_ <: Future[_ >: T]]*): Promise[T] = {
    synchronized(this)(listeners.filterNot(_ == null).foreach(removeListener0))
    this
  }

  override def await: Promise[T] = {
    if (isDone) return this
    if (Thread.interrupted()) throw new InterruptedException

    checkDeadLock()

    synchronized(this) {
      while (!isDone) {
        incWaiters()
        try wait()
        finally decWaiters()
      }
    }
    this
  }

  override def awaitUninterruptibly: Promise[T] = {
    if (isDone) return this

    checkDeadLock()

    var interrupted = false
    synchronized(this) {
      while (!isDone) {
        incWaiters()
        try wait()
        catch {
          case InterruptedException => interrupted = true
        }
        finally decWaiters()
      }
    }

    if (interrupted) Thread.currentThread().interrupt()
    this
  }

  override def await(timeout: Long, unit: TimeUnit): Boolean = await0(unit.toNanos(timeout), interruptable = true)

  override def await(timeoutMills: Long): Boolean = await0(MILLISECONDS.toNanos(timeoutMills), interruptable = true)

  override def awaitUninterruptibly(timeoutMills: Long, unit: TimeUnit): Boolean =
    await0(unit.toNanos(timeoutMills), interruptable = false)

  override def awaitUninterruptibly(timeoutMills: Long): Boolean =
    await0(MILLISECONDS.toNanos(timeoutMills), interruptable = false)

  override def getNow: T = {
    this.result match {
      case CauseHolder => _
      case SUCCESS => _
      case UNCANCELLABLE => _
      case _ => result.asInstanceOf[T]
    }
  }

  override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
    if (RESULT_UPDATER.get(this) == null &&
      RESULT_UPDATER.compareAndSet(this, null, CauseHolder(new CancellationException))) {
      if (checkNotifyWaiters) notifyListeners()
      return true
    }

    false
  }

  override def sync: Promise[T] = {
    await
    if (cause == null) this else throw cause
  }

  override def syncUninterruptibly: Promise[T] = {
    awaitUninterruptibly
    if (cause == null) this else throw cause
  }

  private def setSuccess0(result: T): Boolean = setValue0(if (result == null) SUCCESS else result)

  private def setFailure0(cause: Throwable): Boolean = setValue0(CauseHolder(cause))

  private def setValue0(result: Any): Boolean = {
    if (RESULT_UPDATER.compareAndSet(this, null, result)
      || RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, result)) {
      if (checkNotifyWaiters) {
        currStackDepth set 0
        notifyListeners()
      }
      return true
    }

    false
  }

  private def checkNotifyWaiters: Boolean = synchronized(this) {
    if (waiters > 0) notifyAll()
    listeners != null
  }

  private def notifyListeners(): Unit = {
    if (eventExecutor.inEventLoop) {
      val _currStackDepth = currStackDepth.get
      if (_currStackDepth < MAX_LISTENER_STACK_DEPTH) {
        currStackDepth.set(_currStackDepth + 1)
        try notifyListenersNow()
        finally currStackDepth set _currStackDepth
      }
    }

    safeExecute(this.eventExecutor, () => notifyListenersNow())
  }

  private def notifyListenersNow(): Unit = {
    var listeners: AnyRef = null
    synchronized(this) {
      if (notifyingListeners || this.listeners == null)
        return
      notifyingListeners = true
      listeners = this.listeners
      this.listeners = null
    }

    while (true) {
      listeners match {
        case _: DefaultFutureListeners => notifyListeners0 _
        case _: GenericFutureListener[_ <: Future[_ >: T]] => notifyListener0(this, _)
        case _ => throw new IllegalStateException("cant reach here")
      }

      synchronized(this) {
        listeners = this.listeners
        this.listeners = null

        if (listeners == null) {
          notifyingListeners = false
          return
        }
      }
    }
  }

  private def notifyListeners0(l: DefaultFutureListeners): Unit = l.listeners.foreach(notifyListener0(this, _))

  private def addListener0(l: GenericFutureListener[_]): Unit = {
    listeners match {
      case null => this.listeners = l
      case listeners: DefaultFutureListeners => listeners add l
      case listener: GenericFutureListener[_] => this.listeners = DefaultFutureListeners(listener, l)
      case _ =>
    }
  }

  private def removeListener0(l: GenericFutureListener[_]): Unit = {
    listeners match {
      case listeners: DefaultFutureListeners => listeners.remove(l)
      case `l` => this.listeners = null
      case _ =>
    }
  }

  private def checkDeadLock(): Unit = {
    if (eventExecutor != null && eventExecutor.inEventLoop) throw new IllegalStateException("dead lock")
  }

  private def incWaiters(): Unit =
    if (waiters == Short.MaxValue) throw new IllegalStateException("too many waiters") else waiters += 1

  private def decWaiters(): Unit = waiters -= 1

  private def await0(timeoutNano: Long, interruptable: Boolean): Boolean = {
    if (isDone) return true
    if (timeoutNano <= 0) return isDone
    if (interruptable && Thread.interrupted()) throw new InterruptedException

    checkDeadLock()

    var interrupted, break = false
    var waitTime = timeoutNano
    val startTime = System.nanoTime()
    try {
      while (!break) {
        synchronized(this) {
          incWaiters()
          try wait(waitTime / 1000000, Int(waitTime % 1000000))
          catch {
            case InterruptedException => if (interruptable) throw _ else interrupted = true
          }
          finally decWaiters()
        }
        if (isDone) break = true
        waitTime = timeoutNano - (System.nanoTime() - startTime)
        if (waitTime < 0) break = true
      }
      isDone
    } finally if (interrupted) Thread.currentThread().interrupt()

  }
}

object DefaultPromise {
  private val logger: InternalLogger = InternalLoggerFactory getInstance DefaultPromise.getClass

  private val MAX_LISTENER_STACK_DEPTH = math.min(8,
    SystemPropertyUtil.getInt("io.netty.defaultPromise.maxListenerStackDepth", 8))
  private val currStackDepth = ThreadLocal.withInitial[Int](() => 0)

  private val RESULT_UPDATER: AtomicReferenceFieldUpdater[DefaultPromise[_], Any] =
    AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.getClass, AnyRef.getClass, "result")
  private val SUCCESS = new AnyRef
  private val UNCANCELLABLE = new AnyRef

  def apply[V](eventExecutor: EventExecutor): DefaultPromise[V] = new DefaultPromise(eventExecutor)

  private def notifyListener0[V](f: Future[_ >: V], l: GenericFutureListener[_ <: Future[_ >: V]]): Unit = l operationComplete f

  private def isDone0(result: Any): Boolean = result != null && result != UNCANCELLABLE

  private def isCancelled0(result: Any): Boolean =
    result match {
      case r: CauseHolder => r.cause.isInstanceOf[CancellationException]
      case _ => false
    }

  private def safeExecute(executor: EventExecutor, task: Runnable) = {
    executor.execute(task)
  }

  private class CauseHolder(_cause: Throwable) {
    def cause: Throwable = _cause
  }

  object CauseHolder {
    def apply(cause: Throwable): CauseHolder = new CauseHolder(cause)
  }

}
