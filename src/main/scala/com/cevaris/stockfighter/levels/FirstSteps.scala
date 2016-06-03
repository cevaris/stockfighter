package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.api.{ApiConfig, StockFighterRequest}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, StockOrderRequest}
import com.google.inject.{Module, Provides, Singleton}
import com.twitter.util.Await


case class FirstStepsModule() extends GuiceModule {
  @Provides
  @Singleton
  def providesSessionConfig(apiKey: ApiKey): ApiConfig =
    ApiConfig(apiKey, "MA58350224", "DFKEX", "RWL")
}

object FirstSteps extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(EnvConfigModule(), FirstStepsModule())

  def appMain(args: Array[String]): Unit = {
    val session = injector.getInstance(classOf[ApiConfig])
    val request = injector.getInstance(classOf[StockFighterRequest])

    val future = request.stockQuote(session.venue, session.symbol)
      .flatMap { quote =>
        request.stockOrder(StockOrderRequest(
          session.account, session.venue, session.symbol,
          quote.bid, 101, "buy", "market"
        ))
      }
    println(Await.result(future))
  }

}
