package com.cevaris.stockfighter.apps

import com.cevaris.stockfighter.api.modules.{EnvConfigModule, GuiceModule}
import com.cevaris.stockfighter.{ApiKey, SessionConfig}
import com.google.inject.{Guice, Provides}


case class FirstStepsModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): SessionConfig =
    SessionConfig(apiKey, "account", "venue", "symbol")
}

object FirstSteps {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(EnvConfigModule(), FirstStepsModule())
    val session = injector.getInstance(classOf[SessionConfig])
    println(session)
  }
}
