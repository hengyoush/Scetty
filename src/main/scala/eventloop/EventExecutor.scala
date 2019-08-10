package eventloop

trait EventExecutor extends EventExecutorGroup {
  def next: EventExecutor
  def parent: EventExecutorGroup

  def inEventLoop: Boolean = inEventLoop(Thread.currentThread)
  def inEventLoop(thread: Thread): Boolean
}
