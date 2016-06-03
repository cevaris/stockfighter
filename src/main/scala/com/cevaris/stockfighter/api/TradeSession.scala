package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.common.concurrency.ReadWriter
import com.cevaris.stockfighter.{AccountOrders, Direction, StockQuote}
import com.google.inject.Inject

case class TradeSession @Inject()(
  var cash: Int = 0,
  var nav: Int = 0,
  var position: Int = 0,
  var latestQuote: Option[StockQuote] = None
) extends ReadWriter {

  def setLatestQuote(stockQuote: StockQuote): Unit = write {
    latestQuote = Some(stockQuote)
  }

  def update(accountStatus: AccountOrders): Unit = {

    if (!accountStatus.ok) {
      return
    }

    val okOrders = accountStatus.orders.filter(_.ok)

    val buyOrders = okOrders.filter(_.direction == Direction.Buy).filter(_.ok)
    val sellOrders = okOrders.filter(_.direction == Direction.Sell).filter(_.ok)

    val buyFills = buyOrders.flatMap(_.fills)
    val sellFills = sellOrders.flatMap(_.fills)

    val (bCash, bPos) = buyFills.foldLeft(0, 0) { case (acc, fill) =>
      (acc._1 + (fill.price * fill.qty), acc._1 + fill.qty)
    }

    val (sCash, sPos) = sellFills.foldLeft(0, 0) { case (acc, fill) =>
      (acc._1 + (fill.price * fill.qty), acc._1 + fill.qty)
    }

    write {
      cash = sCash - bCash
      position = sPos - bPos
      nav = cash + (position * latestQuote.map(_.last).getOrElse(0))
    }
    println(this)
  }

}
