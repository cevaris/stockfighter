package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.common.concurrency.{ReadWriter, Result, SuccessResult}
import com.cevaris.stockfighter.{AccountOrders, StockOrder, StockQuote}
import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.logging.Logger
import com.twitter.util.{Await, Duration, Future, FuturePool, Timer}
import java.util.concurrent.Executors

@Singleton
case class SFSession @Inject()(
  config: SFConfig,
  request: SFRequest
) extends ReadWriter {

  var cash: Int = 0
  var nav: Int = 0
  /**
   * Total number of filled shares
   */
  var position: Int = 0
  /**
   * Total number of unfilled shares
   * Open orders with shares left outstanding
   */
  var unfilledPosition: Int = 0
  var latestQuote: Option[StockQuote] = None
  var orders: Map[Int, StockOrder] = Map.empty[Int, StockOrder]

  private implicit val timer = DefaultTimer.twitter
  private val observerPool = FuturePool(Executors.newFixedThreadPool(1))
  private val log = Logger.get

  def setLatestQuote(stockQuote: StockQuote): Unit = write {
    latestQuote = Some(stockQuote)
  }

  def totalPosition: Int = read {
    position + unfilledPosition
  }

  def update(accountStatus: AccountOrders): Unit = {
    log.debug(s"updating session: $accountStatus")

    if (!accountStatus.ok) {
      return
    }

    val okOrders = accountStatus.orders.filter(_.ok)

    var sumCash = 0
    var sumPosition = 0
    var sumUnfilledPosition = 0
    for (order <- okOrders) {
      for (fill <- order.fills) {

        if (order.direction == DirectionType.Buy) {
          sumCash -= fill.price * fill.qty
          sumPosition += fill.qty
        }
        if (order.direction == DirectionType.Sell) {
          sumCash += fill.price * fill.qty
          sumPosition -= fill.qty
        }

      }

      if (order.open) {
        sumUnfilledPosition = order.qty
      }
    }

    write {
      cash = sumCash
      position = sumPosition
      unfilledPosition = sumUnfilledPosition
      orders = okOrders.map(o => o.id -> o).toMap
      nav = latestQuote.map(q => cash + (position * q.last)).getOrElse(0)
    }
  }

  def observe()(implicit timer: Timer): Future[Result] = observerPool {
    val quoteFuture = request.streamQuotes(config.account, config.venue) { queue =>
      while (true) {
        val quote = queue.take()
        write {
          latestQuote = Some(quote)
        }
      }
      SuccessResult()
    }

    val fillFuture = request.streamExecutions(config.account, config.venue) { queue =>
      while (true) {
        val execution = queue.take()
        log.info(execution.toString)
      }
      SuccessResult()
    }

    val statusFuture = Future.whileDo(p = true) {
      request.accountOrders(config.account, config.venue)
        .map(update)
        .flatMap(_ => Future.sleep(Duration.fromSeconds(1)))
    }.map(_ => SuccessResult())

    val futureTasks =
      Future.collect(Seq(quoteFuture, fillFuture, statusFuture))
        .map(_ => SuccessResult())

    Await.result(futureTasks)
  }

}
