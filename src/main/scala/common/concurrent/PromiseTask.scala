package common.concurrent

import java.util.concurrent.{Callable, RunnableFuture}

import eventloop.EventExecutor
import common.concurrent.PromiseTask._

class PromiseTask[V](executor: EventExecutor, task: Callable[V]) extends DefaultPromise[V](executor) with RunnableFuture[V] {
  def this(eventExecutor: EventExecutor, task: Runnable, result: V) = {
    this(eventExecutor, toCallable(task, result))
  }

  override def run(): Unit = {
    try if (setUncancellableInternal) {
      setSuccessInternal(task.call())
    }
    catch {case e: Throwable => setFailureInternal(e)}
  }

  override def equals(obj: Any): Boolean = this eq obj.asInstanceOf

  override def hashCode(): Int = System identityHashCode this

  override def setFailure(cause: Throwable) = throw new IllegalStateException

  protected def setFailureInternal(cause: Throwable): Promise[V] = {
    super.setFailure(cause)
    this
  }

  override def tryFailure(cause: Throwable) = false

  protected def tryFailureInternal(cause: Throwable): Boolean = super.tryFailure(cause)

  override def setSuccess(result: V) = throw new IllegalStateException

  protected def setSuccessInternal(result: V): Promise[V] = {
    super.setSuccess(result)
    this
  }

  override def trySuccess(result: V) = false

  protected def trySuccessInternal(result: V): Boolean = super.trySuccess(result)

  override def setUncancellable = throw new IllegalStateException

  protected def setUncancellableInternal: Boolean = super.setUncancellable
}

object PromiseTask {
  def apply[V](executor: EventExecutor, task: Callable[V]): PromiseTask[V] = new PromiseTask(executor, task)

  def apply[V](eventExecutor: EventExecutor, task: Runnable, result: V): PromiseTask[V] =
    new PromiseTask(eventExecutor, task, result)

  private final class RunnableAdapter[T](task: Runnable, result: T) extends Callable[T] {
    override def call(): T = {
      task.run()
      result
    }

    override def toString: String = s"Callable(task: $task, result: $result)"
  }

  object RunnableAdapter {
    def apply[T](task: Runnable, result: T): RunnableAdapter[T] = new RunnableAdapter(task, result)
  }

  def toCallable[T](task: Runnable, result: T): Callable[T] = RunnableAdapter(task, result)
}