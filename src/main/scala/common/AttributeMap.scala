package common

trait AttributeMap {
  def attr[T](key: AttributeKey[T]): Attribute[T]

  def hasAttr[T](key: AttributeKey[T]): Boolean
}
