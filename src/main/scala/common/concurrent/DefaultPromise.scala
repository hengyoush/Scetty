package common.concurrent

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

import common.SystemPropertyUtil
import common.logger.{InternalLogger, InternalLoggerFactory}
import eventloop.EventExecutor
import DefaultPromise._

import scala.runtime.VolatileObjectRef

class DefaultPromise[T](eventExecutor: EventExecutor) extends AbstractFuture[T] with Promise[T] {
  require(eventExecutor != null, "eventExecutor不能为空")

  private var result: VolatileObjectRef[AnyRef] = _

  private var listeners: AnyRef = _
  private var waiters: Short = _
  private var notifyingListeners: Boolean = _

  protected def this() = this(null)

  override def setSuccess(result: T): Promise[T] = {

  }

  private def setSuccess0(result: T): Boolean = {

  }

  private def setValue0(result: AnyRef): Boolean = {
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

  private def checkNotifyWaiters: Boolean = {
    synchronized(this) {
      if (waiters > 0) notifyAll()
      listeners != null
    }
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
  }

  private def notifyListenersNow(): Unit = {

  }
}

object DefaultPromise {
  private val logger: InternalLogger = InternalLoggerFactory getInstance DefaultPromise.getClass

  private val MAX_LISTENER_STACK_DEPTH = math.min(8,
    SystemPropertyUtil.getInt("io.netty.defaultPromise.maxListenerStackDepth", 8))
  private val currStackDepth = ThreadLocal.withInitial[Int](() => 0)

  private val RESULT_UPDATER: AtomicReferenceFieldUpdater[DefaultPromise[_], AnyRef] =
    AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.getClass, AnyRef.getClass, "result")
  private val SUCCESS = new AnyRef
  private val UNCANCELLABLE = new AnyRef

}
