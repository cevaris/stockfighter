package com.cevaris.stockfighter.apps

import com.cevaris.stockfighter.api.StockFighterRequest
import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, SessionConfig, StockOrderRequest}
import com.google.inject.{Module, Provides}


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
    println(requester.apiHeartBeat())
    println(requester.accountOrders("EXB123456", "TESTEX"))
    println(requester.accountOrders("EXB123456", "TESTEX", "FOOBAR"))
    println(requester.venueHeartBeat("TESTEX"))
    println(requester.venueSymbols("TESTEX"))
    println(requester.stockQuote("TESTEX", "FOOBAR"))
    println(requester.symbolOrderBook("TESTEX", "FOOBAR"))
    val order = requester.stockOrder(
      StockOrderRequest("EXB123456", "TESTEX", "FOOBAR", 100, 10, "buy", "limit")
    )
    println(order)
    println(requester.stockOrderStatus("TESTEX", "FOOBAR", order.id))
    println(requester.stockOrderCancel("TESTEX", "FOOBAR", order.id))
  }

}
