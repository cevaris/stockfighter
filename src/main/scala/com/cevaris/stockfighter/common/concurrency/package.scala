package com.cevaris.stockfighter.common

package object concurrency {

  trait FutureState

  case class FutureSuccess() extends FutureState

  case class FutureFailure(exception: Throwable) extends FutureState

}
