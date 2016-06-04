package com.cevaris.stockfighter.common

import com.twitter.util.{Future, Return, Throw, Try}

package object app {

  def wrap[A](f: => Option[A]): Future[A] = {
    Try(f) match {
      case Return(v) => v match {
        case Some(vv) => Future.value(vv)
        case None => Future.exception(new RuntimeException("nil returned"))
      }
      case Throw(e) => Future.exception(e)
    }
  }

  trait AppShutdownState

  case class AppSuccess() extends AppShutdownState

  case class AppFailure(exception: Throwable) extends AppShutdownState
}
