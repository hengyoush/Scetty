package eventloop

import java.util.concurrent.{Callable, ScheduledExecutorService, ScheduledFuture}

import common.concurrent.Future

import scala.concurrent.duration.TimeUnit

trait EventExecutorGroup extends ScheduledExecutorService with Iterable[EventExecutor] {

  def isShuttingDown: Boolean

  def shutdownGracefully: Future[_]

  /**
    * 当此方法执行时，isShuttingDown返回true。
    * 如果在quietPeriod没有任务提交的话那么进行shutdwon，否则重新开始计算quietPeriod
    *
    * @param timeout     超时时间
    * @param quietPeriod 等待任务提交的时间，详细如上所示
    * @param unit        时间单位
    */
  def shutdownGracefully(timeout: Long, quietPeriod: Long, unit: TimeUnit): Future[_]

  def terminationFuture: Future[_]

  def next: EventExecutor

  override def iterator: Iterator[EventExecutor]

  def submit(task: Runnable): Future[_]
  def submit[T](task: Runnable, result: T): Future[T]
  def submit[T](task: Callable[T]): Future[T]

  def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V]
  def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_]
  def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_]
  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_]
}
