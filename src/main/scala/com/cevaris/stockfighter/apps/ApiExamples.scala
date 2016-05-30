package com.cevaris.stockfighter.apps

import com.cevaris.stockfighter.api.StockFighterRequest
import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, SessionConfig, StockOrderRequest}
import com.google.inject.{Module, Provides}
import com.twitter.util.Await


case class ApiExamplesModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): SessionConfig =
    SessionConfig(apiKey, "EXB123456", "TESTEX", "FOOBAR")
}

object ApiExamples extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(EnvConfigModule(), ApiExamplesModule())

  def appMain(args: Array[String]): Unit = {
    val session = injector.getInstance(classOf[SessionConfig])
    println(session)

    val requester = injector.getInstance(classOf[StockFighterRequest])
    println(Await.result(requester.apiHeartBeat()))
    println(Await.result(requester.accountOrders(session.account, session.venue)))
    println(Await.result(requester.accountOrders(session.account, session.venue, session.symbol)))
    println(Await.result(requester.venueHeartBeat(session.venue)))
    println(Await.result(requester.venueSymbols(session.venue)))
    println(Await.result(requester.stockQuote(session.venue, session.symbol)))
    println(Await.result(requester.symbolOrderBook(session.venue, session.symbol)))
    val order = Await.result(
      requester.stockOrder(
        StockOrderRequest(session.account, session.venue, session.symbol, 100, 10, "buy", "limit")
      )
    )
    println(order)
    println(Await.result(requester.stockOrderStatus(session.venue, session.symbol, order.id)))
    println(Await.result(requester.stockOrderCancel(session.venue, session.symbol, order.id)))
  }

}
