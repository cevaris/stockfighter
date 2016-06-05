package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.ApiKey
import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.api.{OrderType, SFConfig, SFRequest, SFSession, SFTrader}
import com.cevaris.stockfighter.common.app.{AppShutdownState, AppSuccess}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.google.inject.{Inject, Module, Provides, Singleton}
import com.twitter.util.{Await, Future}


case class ChockABlockModule() extends GuiceModule {
  @Provides
  @Singleton
  def providesSessionConfig(apiKey: ApiKey): SFConfig =
    SFConfig(apiKey, "MA58350224", "DFKEX", "RWL")
}

object ChockABlock extends GuiceApp {

  override protected val modules: Seq[Module] = Seq(
    EnvConfigModule(), ChockABlockModule()
  )

  def appMain(args: Array[String]): Unit = {
    val level = injector.getInstance(classOf[ChockABlockLevel])
    println(Await.result(level.startLevel()))
  }

}

case class ChockABlockLevel @Inject()(
  config: SFConfig,
  request: SFRequest,
  trader: SFTrader,
  session: SFSession
) {

  def startLevel(): Future[AppShutdownState] = {

    Future.whileDo(session.nav < 100000) {
      request.stockQuote(config.venue, config.symbol)
        .flatMap { quote =>
          trader.buy(quote.bid, 100, OrderType.Limit)
        }
    }
    Future.value(AppSuccess())
  }

}