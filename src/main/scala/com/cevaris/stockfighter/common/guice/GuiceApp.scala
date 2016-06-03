package com.cevaris.stockfighter.common.guice

import com.google.inject.{Guice, Injector, Module}

abstract class GuiceApp {

  protected val modules: Seq[Module] = Seq.empty[Module]
  private var optInjector: Option[Injector] = None

  def main(args: Array[String]): Unit = {
    optInjector = Some(Guice.createInjector(modules: _*))
    appMain(args)
  }

  protected def injector: Injector = optInjector.get

  protected def appMain(args: Array[String]): Unit

}
