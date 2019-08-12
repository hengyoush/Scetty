package common.concurrent

trait ScheduledFuture[V] extends Future[V] with java.util.concurrent.ScheduledFuture[V]