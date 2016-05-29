package com.cevaris.stockfighter.apps

import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, SessionConfig}
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
  }

}
