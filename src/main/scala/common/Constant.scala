package common

trait Constant[T <: Constant[T]] extends Comparable[T] {
  def id(): Int
  def name(): String
}
