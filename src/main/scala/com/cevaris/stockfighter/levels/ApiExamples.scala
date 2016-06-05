package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.api.{SFConfig, SFRequest}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, Execution, StockOrderRequest, StockQuote}
import com.google.inject.{Module, Provides}
import com.twitter.util.{Await, Future}
import java.util.concurrent.{BlockingQueue, ScheduledThreadPoolExecutor, TimeUnit}


case class ApiExamplesModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): SFConfig =
    SFConfig(apiKey, "PAL34100354", "YVJEX", "TFI")

  //    SessionConfig(apiKey, "EXB123456", "TESTEX", "FOOBAR")
}

object ApiExamples extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(EnvConfigModule(), ApiExamplesModule())

  def appMain(args: Array[String]): Unit = {
    val session = injector.getInstance(classOf[SFConfig])
    println(session)

    val requester = injector.getInstance(classOf[SFRequest])
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

    def printer[A](q: BlockingQueue[A]): Future[Unit] = {
      while (true) {
        println(q.take())
      }
      Future.Done
    }

    Await.result(Future.collect(Seq(
      requester.streamExecutions(session.account, session.venue)(printer[Execution]),
      requester.streamQuotes(session.account, session.venue)(printer[StockQuote])
    )))
  }

}
