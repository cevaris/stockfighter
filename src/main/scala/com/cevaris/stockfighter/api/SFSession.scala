package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.common.concurrency.ReadWriter
import com.cevaris.stockfighter.{AccountOrders, StockOrder, StockQuote}
import com.google.inject.{Inject, Singleton}

@Singleton
case class SFSession @Inject()(
  var cash: Int = 0,
  var nav: Int = 0,
  var position: Int = 0,
  var latestQuote: Option[StockQuote] = None,
  var orders: Map[Int, StockOrder] = Map.empty[Int, StockOrder]
) extends ReadWriter {

  def setLatestQuote(stockQuote: StockQuote): Unit = write {
    latestQuote = Some(stockQuote)
  }

  def update(accountStatus: AccountOrders): Unit = {

    if (!accountStatus.ok) {
      return
    }

    val okOrders = accountStatus.orders.filter(_.ok)

    var sumCash = 0
    var sumPosition = 0
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
    }

    write {
      cash = sumCash
      position = sumPosition
      orders = okOrders.map(o => o.id -> o).toMap
      nav = latestQuote.map(q => cash + (position * q.last)).getOrElse(0)
    }
  }

}
