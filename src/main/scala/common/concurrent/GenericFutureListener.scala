package common.concurrent

import java.util.EventListener


trait GenericFutureListener[F <: Future[_]] extends EventListener {

  def operationComplete(future: F)
}
