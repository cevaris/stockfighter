package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.ApiKey
import com.cevaris.stockfighter.api.SFRequest
import com.cevaris.stockfighter.api.modules.{ApiConfig, EnvConfigModule}
import com.cevaris.stockfighter.common.app.{AppShutdownState, AppSuccess}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.google.inject.{Inject, Module, Provides, Singleton}
import com.twitter.util.{Await, Future}

case class FirstStepsLevel @Inject()(
  apiConfig: ApiConfig,
  request: SFRequest
) {

  def startLevel(): Future[AppShutdownState] = {
    Future.value(AppSuccess())
  }

}

case class FirstStepsModule() extends GuiceModule {
  @Provides
  @Singleton
  def providesSessionConfig(apiKey: ApiKey): ApiConfig =
    ApiConfig(apiKey, "MA58350224", "DFKEX", "RWL")
}

object FirstSteps extends GuiceApp {
  override protected val modules: Seq[Module] = Seq(
    EnvConfigModule(),
    FirstStepsModule()
  )

  def appMain(args: Array[String]): Unit = {
    val request = injector.getInstance(classOf[FirstStepsLevel])
    Await.result(request.startLevel())
  }

}
