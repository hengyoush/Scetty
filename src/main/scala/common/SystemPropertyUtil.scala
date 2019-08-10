package common

import java.security.{AccessController, PrivilegedAction}

import common.logger.InternalLoggerFactory

class SystemPropertyUtil private

object SystemPropertyUtil {
  implicit def toPrivilegedAction(x: () => String): PrivilegedAction[String] = () => x.apply()

  private val logger = InternalLoggerFactory.getInstance(classOf[SystemPropertyUtil])

  def contains(key: String): Boolean = get(key) != null

  def get(key: String): String = get(key, null)

  def get(key: String, defaultValue: String): String = {
    key match {
      case null => throw new NullPointerException("key")
      case "" => throw new IllegalArgumentException("key must not be empty.")
      case _ =>
    }

    var value: String = null
    try
        if (System.getSecurityManager == null) value = System.getProperty(key)
        else value = AccessController doPrivileged toPrivilegedAction(() => System.getProperty(key))
    catch {
      case e: SecurityException =>
        logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e)
    }

    if (value == null) return defaultValue
    value
  }

  def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    var value = get(key)
    if (value == null) return defaultValue
    value = value.trim.toLowerCase
    if (value.isEmpty) return defaultValue
    if ("true" == value || "yes" == value || "1" == value) return true
    if ("false" == value || "no" == value || "0" == value) return false
    logger.warn("Unable to parse the boolean system property '{}':{} - using the default value: {}", key, value, defaultValue)
    defaultValue
  }

  def getInt(key: String, defaultValue: Int): Int = {
    var value = get(key)
    if (value == null) return defaultValue
    value = value.trim
    try
      return value.toInt
    catch {
      case e: Exception =>

      // Ignore
    }
    logger.warn("Unable to parse the integer system property '{}':{} - using the default value: {}", key, value, defaultValue)
    defaultValue
  }

  def getLong(key: String, defaultValue: Long): Long = {
    var value = get(key)
    if (value == null) return defaultValue
    value = value.trim
    try
      return Long(value)
    catch {
      case e: Exception =>

    }
    logger.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}", key, value, defaultValue)
    defaultValue
  }


}