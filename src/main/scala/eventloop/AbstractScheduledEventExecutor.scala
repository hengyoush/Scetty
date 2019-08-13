package eventloop

import java.util.concurrent.{Callable, TimeUnit}

import common.concurrent.PromiseTask.RunnableAdapter
import common.concurrent.{ScheduledFuture, ScheduledFutureTask}

import scala.collection.mutable

abstract class AbstractScheduledEventExecutor extends AbstractEventExecutor {
  val queue: mutable.PriorityQueue[ScheduledFutureTask[_]]  = mutable.PriorityQueue.empty[ScheduledFutureTask[_]]

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] =
    schedule0(ScheduledFutureTask[_](this, RunnableAdapter(command, null), unit.toNanos(delay), 0))

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] =
    schedule0(ScheduledFutureTask[V](this, callable, unit.toNanos(delay), 0))

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] =
    schedule0(ScheduledFutureTask[_](this, RunnableAdapter(command, null), unit.toNanos(initialDelay), unit.toNanos(period)))

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] =
    schedule0(ScheduledFutureTask(this, RunnableAdapter(command, null), unit.toNanos(initialDelay), -unit.toNanos(delay)))

  protected[concurrent] def schedule0[V](task: ScheduledFutureTask[V]): ScheduledFuture[V] = {
    queue.enqueue(task)
    task
  }
}
