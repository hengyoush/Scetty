package eventloop

import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.{AbstractExecutorService, TimeUnit}

import common.concurrent.Future
import common.logger.InternalLoggerFactory
import AbstractEventExecutor._

abstract class AbstractEventExecutor(_parent: EventExecutorGroup)
  extends AbstractExecutorService with EventExecutor {

  private val selfIte: Iterator[AbstractEventExecutor] = List(this).iterator

  def this() = this(_)

  override def parent: EventExecutorGroup = _parent
  override def next: EventExecutor = this

  override def shutdownGracefully: Future[_] = shutdownGracefully(defaultTimeout, defaultQuietPeriod, SECONDS)
}

object AbstractEventExecutor {
  private val logger = InternalLoggerFactory.getInstance(AbstractEventExecutor.getClass)
  private val defaultQuietPeriod: Long = 2
  private val defaultTimeout: Long = 15
}
