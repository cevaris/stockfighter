package com.cevaris.stockfighter.levels

import com.cevaris.stockfighter.api.modules.EnvConfigModule
import com.cevaris.stockfighter.api.{OrderType, SFConfig, SFRequest, SFSession, SFTrader}
import com.cevaris.stockfighter.common.app.{AppFailure, AppShutdownState, AppSuccess}
import com.cevaris.stockfighter.common.concurrency.{Result, SuccessResult}
import com.cevaris.stockfighter.common.guice.{GuiceApp, GuiceModule}
import com.cevaris.stockfighter.common.util.Retry
import com.cevaris.stockfighter.{ApiKey, StockOrder}
import com.google.inject.{Inject, Module, Provides}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.logging.Logger
import com.twitter.util.{Await, Duration, Future, NonFatal}

case class FirstStepsLevel @Inject()(
  config: SFConfig,
  request: SFRequest,
  trader: SFTrader,
  session: SFSession
) {

  private val log = Logger.get

  private implicit val timer = DefaultTimer.twitter
  private val retryPolicy = Retry.stopOnceReached(3)(
    Retry.exponentialRetry(Duration.fromSeconds(1), 2)
  )

  def startLevel(): Future[AppShutdownState] =
    placeOrder()
      .flatMap(blockOnOrder)
      .map(_ => AppSuccess())
      .rescue {
        case NonFatal(t) =>
          log.error(t, "app failed")
          Future.value(AppFailure(t))
      }

  private def blockOnOrder(order: StockOrder): Future[Result] = {
    def go(): Future[Result] = {
      request.stockOrderStatus(config.venue, config.symbol, order.id)
        .flatMap { status =>
          if (status.open) {
            log.info(s"Order ${ order.id } still open")
            Future.sleep(Duration.fromSeconds(1)).flatMap(_ => go())
          } else {
            Future.value(SuccessResult())
          }
        }
    }

    go()
  }

  private def placeOrder(): Future[StockOrder] = {
    Retry(retryPolicy)
      .onRetry((attempt: Int, t: Throwable) => log.error(t, s"failed: attempt $attempt"))
      .apply(
        request.stockQuote(config.venue, config.symbol)
          .flatMap { quote =>
            trader.buy(quote.bid, 100, OrderType.Market)
          }
      )
  }
}

case class FirstStepsModule() extends GuiceModule {
  @Provides
  def providesSessionConfig(apiKey: ApiKey): SFConfig =
    SFConfig(apiKey, "FTB42042940", "AYBCEX", "ZICO")
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
