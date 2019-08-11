package eventloop

import common.concurrent.{Future, Promise}

trait EventExecutor extends EventExecutorGroup {
  def next: EventExecutor
  def parent: EventExecutorGroup

  def inEventLoop: Boolean = inEventLoop(Thread.currentThread)
  def inEventLoop(thread: Thread): Boolean

  def newPromise[V]: Promise[V]
  def newSucceededFuture[V](result: V): Future[V]
  def newFailedFuture[V](cause: Throwable): Future[V]
}
