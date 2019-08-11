package common

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import common.ConstantPool.checkNotNullAndNotEmpty

abstract class ConstantPool[T <: Constant[T]] {
  private val constants: ConcurrentHashMap[String, T] = new ConcurrentHashMap
  private val _nextId: AtomicInteger = new AtomicInteger(1)

  def valueOf(firstNameComponent: Class[_], secondNameComponent: String): T = {
    if (firstNameComponent == null) throw new NullPointerException("firstNameComponent")
    if (secondNameComponent == null) throw new NullPointerException("secondNameComponent")

    valueOf(firstNameComponent.getName + '#' + secondNameComponent)
  }

  def valueOf(name: String): T = getOrCreate(checkNotNullAndNotEmpty(name))

  def exists(name: String): Boolean = constants.contains(checkNotNullAndNotEmpty(name))

  def newInstance(name: String): T = createOrThrow(checkNotNullAndNotEmpty(name))

  private def getOrCreate(name: String): T = constants.computeIfAbsent(name, newConstant(nextId, _))

  private def createOrThrow(name: String): T = {
    if (constants.contains(name))
      throw new IllegalArgumentException(s"$name is already in use")

    constants.computeIfAbsent(name, newConstant(nextId, _))
  }

  def newConstant(id: Int, name: String): T
  def nextId: Int = _nextId.getAndIncrement
}

object ConstantPool {
  private def checkNotNullAndNotEmpty(name: String): String = {
    if (name == null || name.isEmpty) throw new IllegalArgumentException("empty name")
    name
  }
}
