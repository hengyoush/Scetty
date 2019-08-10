package channel

import java.net.SocketAddress

import buffer.ByteBufAllocator
import common.AttributeMap
import eventloop.EventLoop

trait Channel extends ChannelOutboundInvoker
  with AttributeMap with Comparable[Channel] {
  def id: ChannelId

  def eventLoop: EventLoop

  def parent: Channel

  def config: ChannelConfig

  def isOpen: Boolean
  def isRegistered: Boolean
  def isActive: Boolean

  def metadata: ChannelMetadata

  def localAddress: SocketAddress
  def remoteAddress: SocketAddress

  def closeFuture: ChannelFuture
  def isWritable: Boolean
  def bytesBeforeUnwritable: Long
  def unsafe: Unsafe
  def pipeline: ChannelPipeline
  def alloc: ByteBufAllocator
  def read: Channel
  def flush: Channel

  trait Unsafe {

    def recvBufAllocHandle: RecvByteBufAllocator.Handle

    def localAddress: SocketAddress

    def remoteAddress: SocketAddress

    def register(eventLoop: EventLoop, promise: ChannelPromise): Unit

    def bind(localAddress: SocketAddress, promise: ChannelPromise): Unit

    def connect(remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise): Unit

    def disconnect(promise: ChannelPromise): Unit

    def close(promise: ChannelPromise): Unit

    def closeForcibly(): Unit

    def deregister(promise: ChannelPromise): Unit

    def beginRead(): Unit

    def write(msg: Any, promise: ChannelPromise): Unit

    def flush(): Unit

    def voidPromise: ChannelPromise

    def outboundBuffer: ChannelOutboundBuffer
  }
}


