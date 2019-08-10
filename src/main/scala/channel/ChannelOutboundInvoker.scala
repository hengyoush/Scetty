package channel

import java.net.SocketAddress

trait ChannelOutboundInvoker {
  def bind(socketAddress: SocketAddress): ChannelFuture
  def bind(socketAddress: SocketAddress, channelPromise: ChannelPromise): ChannelFuture

  def connect(remoteAddress: SocketAddress): ChannelFuture
  def connect(remoteAddress: SocketAddress, channelPromise: ChannelPromise): ChannelFuture
  def connect(remoteAddress: SocketAddress, socketAddress: SocketAddress): ChannelFuture
  def connect(remoteAddress: SocketAddress, socketAddress: SocketAddress, channelPromise: ChannelPromise): ChannelFuture
  def disconnect: ChannelFuture
  def disconnect(channelPromise: ChannelPromise): ChannelFuture

  def close: ChannelFuture
  def close(channelPromise: ChannelPromise): ChannelFuture
  def deregister(): ChannelFuture
  def deregister(channelPromise: ChannelPromise): ChannelFuture

  def read: ChannelOutboundInvoker
  def write(msg: AnyRef): ChannelFuture
  def write(msg: AnyRef, channelPromise: ChannelPromise): ChannelFuture
  def flush: ChannelOutboundInvoker
  def writeAndFlush(msg: AnyRef): ChannelFuture
  def writeAndFlush(msg: AnyRef, channelPromise: ChannelPromise): ChannelFuture

  def newPromise: ChannelPromise
  def newSucceededFuture: ChannelFuture
  def newFailureFuture(cause: Throwable): ChannelFuture
  def voidPromise: ChannelPromise
}
