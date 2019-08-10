package eventloop

trait EventLoop extends OrderedEventExecutor {
  def next: EventLoop
  def parent: EventLoopGroup
}
