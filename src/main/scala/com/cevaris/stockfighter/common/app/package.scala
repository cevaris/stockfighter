package com.cevaris.stockfighter.common

package object app {

  trait AppShutdownState

  case object AppSucess extends AppShutdownState

  case object AppFailure extends AppShutdownState

}
