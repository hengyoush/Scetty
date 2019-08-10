package common.logger

trait InternalLogger {
  def warn(value: String, key: String, e: Throwable)
  def warn(format: String, arguments: Any*): Unit
  def info(msg: String)
}
