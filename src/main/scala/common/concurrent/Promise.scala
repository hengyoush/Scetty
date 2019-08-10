package common.concurrent

trait Promise[V] extends Future[V] {
  def setSuccess(result: V): Promise[V]
  def trySuccess(result: V): Boolean
  def setFailure(cause: scala.Throwable): Promise[V]
  def tryFailure(cause: scala.Throwable): Boolean
  def setUncancellable: Boolean

  def addListener(listener: GenericFutureListener[_ <: Future[_ >: V]]): Promise[V]
  def addListeners(listener: GenericFutureListener[_ <: Future[_ >: V]]*): Promise[V]
  def removeListener(listener: GenericFutureListener[_ <: Future[_ >: V]]): Promise[V]
  def removeListeners(listener: GenericFutureListener[_ <: Future[_ >: V]]*): Promise[V]

  def sync: Promise[V]
  def syncUninterruptibly: Promise[V]
  def await: Promise[V]
  def awaitUninterruptibly: Promise[V]
}
