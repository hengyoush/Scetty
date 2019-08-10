package common.logger

import scala.runtime.VolatileObjectRef

abstract class InternalLoggerFactory {

  def newInstance(name: String): InternalLogger
}

object InternalLoggerFactory {
  private[common] var defaultFactory: VolatileObjectRef[InternalLoggerFactory] = _

  def getInstance(clazz: Class[_]): InternalLogger = getInstance(clazz.getName)

  def getInstance(name: String): InternalLogger = {
    getDefaultFactory.newInstance(name)
  }

  def getDefaultFactory: InternalLoggerFactory = {
    if (defaultFactory == null)
      defaultFactory = VolatileObjectRef.create(newDefaultFactory(InternalLoggerFactory.getClass.getName))
    defaultFactory.elem
  }

  def newDefaultFactory(name: String): InternalLoggerFactory = ???
}
