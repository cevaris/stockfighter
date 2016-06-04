package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.api.modules.{ApiConfig, EnvConfigModule}
import com.cevaris.stockfighter.api.StockFighterRequest
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, StockOrderRequest}
import com.google.inject.{Module, Provides}
import com.twitter.util.{Await, Future}
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}


case class ApiExamplesModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): ApiConfig =
    ApiConfig(apiKey, "PAL34100354", "YVJEX", "TFI")

  //    SessionConfig(apiKey, "EXB123456", "TESTEX", "FOOBAR")
}

object ApiExamples extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(EnvConfigModule(), ApiExamplesModule())

  def appMain(args: Array[String]): Unit = {
    val session = injector.getInstance(classOf[ApiConfig])
    println(session)

    val requester = injector.getInstance(classOf[StockFighterRequest])
    println(Await.result(requester.apiHeartBeat()))
    println(Await.result(requester.accountOrders(session.account, session.venue)))
    println(Await.result(requester.accountOrders(session.account, session.venue, session.symbol)))
    println(Await.result(requester.venueHeartBeat(session.venue)))
    println(Await.result(requester.venueSymbols(session.venue)))
    println(Await.result(requester.stockQuote(session.venue, session.symbol)))
    println(Await.result(requester.symbolOrderBook(session.venue, session.symbol)))
    //    println(Await.result(requester.stockOrderStatus(session.venue, session.symbol, order.id)))
    //    println(Await.result(requester.stockOrderCancel(session.venue, session.symbol, order.id)))

    val timerPool = new ScheduledThreadPoolExecutor(3)
    timerPool.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val future = requester.stockQuote(session.venue, session.symbol)
          .flatMap { quote =>
            requester.stockOrder(StockOrderRequest(
              session.account, session.venue, session.symbol,
              quote.bid - 100, 10, "buy", "limit"
            ))
          }
        println(Await.result(future))
      }
    }, 2, 10, TimeUnit.SECONDS)

    Await.result(Future.collect(Seq(
      requester.streamExecutions(session.account, session.venue),
      requester.streamQuotes(session.account, session.venue)
    )))
  }

}
