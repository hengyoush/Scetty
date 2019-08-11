package eventloop

import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.{AbstractExecutorService, Callable, RunnableFuture, TimeUnit}

import common.concurrent.{DefaultPromise, Future, Promise, PromiseTask}
import common.logger.InternalLoggerFactory
import eventloop.AbstractEventExecutor._

abstract class AbstractEventExecutor(_parent: EventExecutorGroup)
  extends AbstractExecutorService with EventExecutor {

  private val selfIte: Iterator[AbstractEventExecutor] = List(this).iterator

  def this() = this(_)

  override def parent: EventExecutorGroup = _parent
  override def next: EventExecutor = this
  override def iterator: Iterator[EventExecutor] = selfIte

  override def shutdownGracefully: Future[_] = shutdownGracefully(defaultTimeout, defaultQuietPeriod, SECONDS)

  override def newPromise[V]: Promise[V] = DefaultPromise(eventExecutor = this)

  override def submit[T](task: Callable[T]): Future[T] = super.submit(task).asInstanceOf
  override def submit(task: Runnable): Future[_] = super.submit(task).asInstanceOf
  override def submit[T](task: Runnable, result: T): Future[T] = super.submit(task, result).asInstanceOf

  override def newTaskFor[T](callable: Callable[T]): RunnableFuture[T] = PromiseTask(this, callable)
  override def newTaskFor[T](runnable: Runnable, value: T): RunnableFuture[T] = PromiseTask(this, runnable, value)

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit) =
    throw new UnsupportedOperationException

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit) =
    throw new UnsupportedOperationException

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit) =
    throw new UnsupportedOperationException

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) =
    throw new UnsupportedOperationException
}

object AbstractEventExecutor {
  private val logger = InternalLoggerFactory.getInstance(AbstractEventExecutor.getClass)
  private val defaultQuietPeriod: Long = 2
  private val defaultTimeout: Long = 15

  protected def safeExecute(task: Runnable): Unit = task.run()
}
