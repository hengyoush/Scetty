package eventloop

import channel.{Channel, ChannelFuture, ChannelPromise}

trait EventLoopGroup extends EventExecutorGroup {

  def next: EventLoop

  def register(channel: Channel): ChannelFuture
  def register(promise: ChannelPromise): ChannelFuture
}
