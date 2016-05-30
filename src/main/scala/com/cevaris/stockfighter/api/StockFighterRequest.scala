package com.cevaris.stockfighter.api

import com.cevaris.stockfighter._
import com.cevaris.stockfighter.http.HttpRequestBuilder
import com.cevaris.stockfighter.json.JsonMapper
import com.google.inject.Inject
import java.io.StringWriter


case class StockFighterRequest @Inject()(
  httpRequestBuilder: HttpRequestBuilder
) {

  def apiHeartBeat(): ApiHeartBeat = {
    httpRequestBuilder.get(s"ob/api/heartbeat", classOf[ApiHeartBeat])
  }

  def accountOrders(account: String, venue: String): AccountOrders = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/accounts/$account/orders", classOf[AccountOrders]
    )
  }

  def accountOrders(account: String, venue: String, symbol: String): AccountOrders = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/accounts/$account/stocks/$symbol/orders", classOf[AccountOrders]
    )
  }

  def venueHeartBeat(venue: String): VenueHeartBeat = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/heartbeat", classOf[VenueHeartBeat])
  }

  def venueSymbols(venue: String): VenueSymbols = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks", classOf[VenueSymbols])
  }


  def symbolOrderBook(venue: String, symbol: String): StockOrderBook = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks/$symbol", classOf[StockOrderBook])
  }

  def stockQuote(venue: String, symbol: String): StockQuote = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks/$symbol/quote", classOf[StockQuote])
  }

  def stockOrderStatus(venue: String, symbol: String, orderId: Int): StockOrder = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrderCancel(venue: String, symbol: String, orderId: Int): StockOrder = {
    httpRequestBuilder.delete(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrder(so: StockOrderRequest): StockOrder = {
    val out = new StringWriter
    JsonMapper.mapper.writeValue(out, so)
    httpRequestBuilder.post(
      s"ob/api/venues/${ so.venue }/stocks/${ so.symbol }/orders", classOf[StockOrder], out.toString
    )
  }

}
