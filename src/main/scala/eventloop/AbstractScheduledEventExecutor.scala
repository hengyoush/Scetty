package eventloop

import java.util.concurrent.TimeUnit

import scala.collection.mutable

abstract class AbstractScheduledEventExecutor extends AbstractEventExecutor {
  val queue: mutable.PriorityQueue[String]  = mutable.PriorityQueue.empty

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): Nothing = {

  }
}
