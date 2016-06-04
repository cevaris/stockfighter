package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.ApiKey
import com.cevaris.stockfighter.api.StockFighterRequest
import com.cevaris.stockfighter.api.modules.{ApiConfig, EnvConfigModule}
import com.cevaris.stockfighter.common.app.{AppShutdownState, AppSuccess}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.google.inject.{Inject, Module, Provides, Singleton}
import com.twitter.util.{Await, Future}


case class ChockABlockModule() extends GuiceModule {
  @Provides
  @Singleton
  def providesSessionConfig(apiKey: ApiKey): ApiConfig =
    ApiConfig(apiKey, "MA58350224", "DFKEX", "RWL")
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
  apiConfig: ApiConfig,
  request: StockFighterRequest
) {

  def startLevel(): Future[AppShutdownState] = {
    Future.value(AppSuccess())
  }

}