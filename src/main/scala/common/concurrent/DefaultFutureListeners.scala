package common.concurrent

/**
  * thread not safe
  */
final class DefaultFutureListeners {
  private var _listeners: Array[GenericFutureListener[_ <: Future[_]]] = _
  private var size: Int = _listeners.length

  def this(first: GenericFutureListener[_ <: Future[_]], second: GenericFutureListener[_ <: Future[_]]) = {
    this()
    _listeners = Array(first, second)
    size = _listeners.length
  }

  def add(l: GenericFutureListener[_ <: Future[_]]): Unit = {
    if (size == _listeners.length) {
      this._listeners = Array.copyOf(this._listeners, size << 1)
    }
    this._listeners(size) = l
    this.size += 1
  }

  def remove(toRemove: GenericFutureListener[_ <: Future[_]]): Unit = {
    _listeners indexOf toRemove match {
      case -1 =>
      case i =>
        val listenersToMove = size - i - 1
        // listenersToMove可能为0，因为移除的listener在数组的最后一个
        if (listenersToMove > 0) {
          Array.copy(_listeners, 1 + i, _listeners, i, listenersToMove)
        }
        size -= 1
        _listeners(size) = null
    }
  }

  def listeners: Array[GenericFutureListener[_ <: Future[_]]] = _listeners
}

object DefaultFutureListeners {
  def apply(first: GenericFutureListener[_], second: GenericFutureListener[_]) = new DefaultFutureListeners(first, second)
}
