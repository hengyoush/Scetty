package common

import java.util.concurrent.atomic.AtomicLong

import common.AbstractConstant.uniqueIdGenerator

abstract class AbstractConstant[T <: AbstractConstant[T]](_id: Int, _name: String) extends Constant[T] {
  private val uniquifier = uniqueIdGenerator.getAndIncrement()

  override def id(): Int = _id

  override def name(): String = _name

  override def compareTo(o: T): Int = {
    if (this eq o) return 0

    val other: AbstractConstant[T]  = o
    val returnCode: Int = hashCode() - other.hashCode()

    if (returnCode != 0) return returnCode

    if (uniquifier < other.uniquifier) return -1

    if (other.uniquifier > uniquifier) return 1

    throw new Error("failed to compare two different constants")
  }
}

object AbstractConstant {
  private val uniqueIdGenerator = new AtomicLong()
}
