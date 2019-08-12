package common.concurrent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Callable, Delayed, TimeUnit}

import eventloop.{AbstractScheduledEventExecutor, EventExecutor}
import common.concurrent.PromiseTask.toCallable
import ScheduledFutureTask._

final class ScheduledFutureTask[V](e: AbstractScheduledEventExecutor, c: Callable[V], n: Long, p: Long)
  extends PromiseTask[V](e, c) with ScheduledFuture[V] {
  private val id = nextTaskId.getAndIncrement
  private var deadlineNanos: Long = n
  private val periodNanos: Long = p

  def this(e: AbstractScheduledEventExecutor, t: Runnable, r: V, n: Long) = this(e, toCallable(t, r), n, 0)
  def this(e: AbstractScheduledEventExecutor, c: Callable[V], n: Long) = this(e, c, n, 0)

  override def getDelay(unit: TimeUnit): Long = deadlineNanos - (System.nanoTime() - START_TIME)

  override def compareTo(o: Delayed): Int = {
    if (this == o) return 0
    val that = o.asInstanceOf[ScheduledFutureTask[_]]
    val d = this.deadlineNanos - that.deadlineNanos
    if (d < 0) -1
    else if(d > 0) 1
    else if (id < that.id) -1
    else if (id > that.id) 1
    else 1
  }

  override def run(): Unit = ???
}

object ScheduledFutureTask {
  private val nextTaskId = new AtomicLong
  private val START_TIME = System.nanoTime

  private[concurrent] def nanoTime = System.nanoTime - START_TIME

  private[concurrent] def deadlineNanos(delay: Long) = {
    val deadlineNanos = nanoTime + delay
    if (deadlineNanos < 0) Long.MaxValue else deadlineNanos
  }
}
