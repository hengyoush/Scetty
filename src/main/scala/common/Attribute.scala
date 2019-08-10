package common

trait Attribute[T] {
  def key: AttributeKey[T]
  def get: T
  def set(value: T)
  def getAndSet(value: T): T
  def compareAndSet(oldValue: T, newValue: T): Boolean

  def getAndRemove(value: T): T
  def remove()
}
