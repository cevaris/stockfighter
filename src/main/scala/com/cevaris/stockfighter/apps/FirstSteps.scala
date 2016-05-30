package com.cevaris.stockfighter.apps

import com.cevaris.stockfighter.api.StockFighterRequest
import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, SessionConfig, StockOrderRequest}
import com.google.inject.{Module, Provides}
import com.twitter.util.Await


case class FirstStepsModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): SessionConfig =
    SessionConfig(apiKey, "account", "venue", "symbol")
}

object FirstSteps extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(EnvConfigModule(), FirstStepsModule())

  def appMain(args: Array[String]): Unit = {
    val session = injector.getInstance(classOf[SessionConfig])
    println(session)

    val requester = injector.getInstance(classOf[StockFighterRequest])
    println(Await.result(requester.apiHeartBeat()))
    println(Await.result(requester.accountOrders("EXB123456", "TESTEX")))
    println(Await.result(requester.accountOrders("EXB123456", "TESTEX", "FOOBAR")))
    println(Await.result(requester.venueHeartBeat("TESTEX")))
    println(Await.result(requester.venueSymbols("TESTEX")))
    println(Await.result(requester.stockQuote("TESTEX", "FOOBAR")))
    println(Await.result(requester.symbolOrderBook("TESTEX", "FOOBAR")))
    val order = Await.result(
      requester.stockOrder(
        StockOrderRequest("EXB123456", "TESTEX", "FOOBAR", 100, 10, "buy", "limit")
      )
    )
    println(order)
    println(Await.result(requester.stockOrderStatus("TESTEX", "FOOBAR", order.id)))
    println(Await.result(requester.stockOrderCancel("TESTEX", "FOOBAR", order.id)))
  }

}
