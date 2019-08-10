package common.concurrent

import scala.concurrent.duration.TimeUnit

trait Future[V] extends java.util.concurrent.Future[V] {

  def isSuccess: Boolean
  def isCancellable: Boolean
  def cause: Throwable

  def addListener(listener: GenericFutureListener[_ <: Future[_ >: V]]): Future[V]
  def addListeners(listener: GenericFutureListener[_ <: Future[_ >: V]]*): Future[V]
  def removeListener(listener: GenericFutureListener[_ <: Future[_ >: V]]): Future[V]
  def removeListeners(listener: GenericFutureListener[_ <: Future[_ >: V]]*): Future[V]

  def sync: Future[V]
  def syncUninterruptibly: Future[V]
  def await: Future[V]
  def awaitUninterruptibly: Future[V]
  def await(timeout: Long, unit: TimeUnit): Boolean
  def await(timeoutMills: Long): Boolean
  def awaitUninterruptibly(timeoutMills: Long, unit: TimeUnit): Boolean
  def awaitUninterruptibly(timeoutMills: Long): Boolean

  def getNow: V
  def cancel(mayInterruptIfRunning: Boolean)
}
