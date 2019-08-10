package channel


import common.concurrent.{Future, GenericFutureListener}


trait ChannelFuture extends Future[()]{
  def channel(): Channel

  def addListener(listener: GenericFutureListener[_ <: Future[_ >: Unit]]): ChannelFuture
  def addListeners(listener: GenericFutureListener[_ <: Future[_ >: Unit]]*): ChannelFuture
  def removeListener(listener: GenericFutureListener[_ <: Future[_ >: Unit]]): ChannelFuture
  def removeListeners(listener: GenericFutureListener[_ <: Future[_ >: Unit]]*): ChannelFuture

  def sync: ChannelFuture
  def syncUninterruptibly: ChannelFuture
  def await: ChannelFuture
  def awaitUninterruptibly: ChannelFuture

  def isVoid: Boolean
}
