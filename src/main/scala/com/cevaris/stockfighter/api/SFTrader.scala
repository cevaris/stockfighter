package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.{StockOrder, StockOrderRequest}
import com.google.inject.{Inject, Singleton}
import com.twitter.logging.Logger
import com.twitter.util.Future

@Singleton
case class SFTrader @Inject()(
  apiConfig: SFConfig,
  sFSession: SFSession,
  sFRequest: SFRequest
) {

  private val logger = Logger.get()

  def buy(
    price: Int,
    qty: Int,
    orderType: OrderType
  ): Future[StockOrder] = {
    newOrder(DirectionType.Buy, price, qty, orderType)
  }

  def sell(
    price: Int,
    qty: Int,
    orderType: OrderType
  ): Future[StockOrder] = {
    newOrder(DirectionType.Sell, price, qty, orderType)
  }

  private def newOrder(
    direction: DirectionType,
    price: Int,
    qty: Int,
    orderType: OrderType
  ): Future[StockOrder] = {
    val soReq = StockOrderRequest(
      account = apiConfig.account,
      venue = apiConfig.venue,
      symbol = apiConfig.symbol,
      price,
      qty,
      direction,
      orderType
    )
    logger.info(soReq.toString)

    sFRequest.stockOrder(soReq)
  }


}
