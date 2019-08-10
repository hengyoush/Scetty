package common.concurrent

import java.util.concurrent.TimeUnit

import scala.concurrent.{CancellationException, TimeoutException}

abstract class AbstractFuture[T] extends Future[T]{

  override def get: T = {
    await
    cause match {
      case null => getNow
      case _: CancellationException => _
      case _ =>
    }
  }

  override def get(timeout: Long, unit: TimeUnit): T = {
    if (await(timeout, unit)) {
      cause match {
        case null => getNow
        case _: CancellationException => _
        case _ =>
      }
    }

    throw new TimeoutException
  }
}
