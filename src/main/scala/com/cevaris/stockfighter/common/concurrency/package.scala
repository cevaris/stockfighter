package com.cevaris.stockfighter.common

package object concurrency {

  trait Result

  case class SuccessResult() extends Result

  case class FailureResult(exception: Throwable) extends Result

}
