package channel

import common.concurrent.{Future, GenericFutureListener, Promise}

trait ChannelPromise extends ChannelFuture with Promise[Unit]{
  def channel(): Channel

  def setSuccess(result: Unit): ChannelPromise
  def setSuccess(): ChannelPromise
  def trySuccess(): Boolean
  def setFailure(cause: scala.Throwable): ChannelPromise

  def addListener(listener: GenericFutureListener[_ <: Future[_ >: Unit]]): ChannelPromise
  def addListeners(listener: GenericFutureListener[_ <: Future[_ >: Unit]]*): ChannelPromise
  def removeListener(listener: GenericFutureListener[_ <: Future[_ >: Unit]]): ChannelPromise
  def removeListeners(listener: GenericFutureListener[_ <: Future[_ >: Unit]]*): ChannelPromise

  def sync: ChannelPromise
  def syncUninterruptibly: ChannelPromise
  def await: ChannelPromise
  def awaitUninterruptibly: ChannelPromise

  def unvoid: ChannelPromise
}
