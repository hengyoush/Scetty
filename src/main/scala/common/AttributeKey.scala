package common

final class AttributeKey[T] private (id: Int, name: String)
  extends AbstractConstant[AttributeKey[T]](id, name)

object AttributeKey {
  private val pool: ConstantPool[AttributeKey[AnyRef]] = new AttributeKey[AnyRef](_, _)

  def valueOf[T](name: String): AttributeKey[T] =  pool.valueOf(name).asInstanceOf[AttributeKey[T]]

  def valueOf[T](firstNameComponent: Class[_], secondNameComponent: String): AttributeKey[T] =
    pool.valueOf(firstNameComponent, secondNameComponent).asInstanceOf[AttributeKey[T]]

  def exists(name: String): Boolean = pool.exists(name)

  def instanceOf[T](name: String): AttributeKey[T] = pool.newInstance(name).asInstanceOf[AttributeKey[T]]
}
