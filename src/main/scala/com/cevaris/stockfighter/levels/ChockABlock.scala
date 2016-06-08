package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.ApiKey
import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.api.{OrderType, SFConfig, SFRequest, SFSession, SFTrader}
import com.cevaris.stockfighter.common.app.{AppFailure, AppShutdownState, AppSuccess}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.common.util.MinMaxRange
import com.google.inject.{Inject, Module, Provides, Singleton}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.logging.Logger
import com.twitter.util.{Await, Duration, Future, NonFatal}


case class ChockABlockModule() extends GuiceModule {
  @Provides
  @Singleton
  def providesSessionConfig(apiKey: ApiKey): SFConfig =
    SFConfig(apiKey, "SAK60094393", "KDUEX", "PIXE")
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
  private implicit val timer = DefaultTimer.twitter
  private val log = Logger.get
  private val posRange = MinMaxRange(-399, 399)

  def startLevel(): Future[AppShutdownState] = {
    val futureObserve = session.observe()

    val futureLogic =
      request.stockQuote(config.venue, config.symbol).flatMap { quote =>
        Future.whileDo(session.position < 100000) {
          log.info(s"current position: ${ session.position } current quote: $quote.last")
          val quoteBid = math.min(session.latestQuote.getOrElse(quote).bid, quote.bid)
          trader.buy(quoteBid, 500, OrderType.Limit)
            .flatMap { _ =>
              Future.sleep(Duration.fromSeconds(2))
            }
        }
      }

    Future.collect(Seq(futureObserve, futureLogic))
      .map(_ => AppSuccess())
      .rescue {
        case NonFatal(t) =>
          log.error(t, "app failed")
          Future.value(AppFailure(t))
      }
  }

}