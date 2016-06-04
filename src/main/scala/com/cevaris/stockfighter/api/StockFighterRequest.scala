package com.cevaris.stockfighter.api

import com.cevaris.stockfighter._
import com.cevaris.stockfighter.common.concurrency.{FutureState, FutureSuccess}
import com.cevaris.stockfighter.common.http.HttpRequestBuilder
import com.cevaris.stockfighter.common.json.JsonMapper
import com.google.inject.Inject
import com.twitter.util.Future
import java.io.StringWriter


case class StockFighterRequest @Inject()(
  httpRequestBuilder: HttpRequestBuilder
) {

  def apiHeartBeat(): Future[ApiHeartBeat] = {
    httpRequestBuilder.get(s"ob/api/heartbeat", classOf[ApiHeartBeat])
  }

  def accountOrders(account: String, venue: String): Future[AccountOrders] = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/accounts/$account/orders", classOf[AccountOrders]
    )
  }

  def accountOrders(account: String, venue: String, symbol: String): Future[AccountOrders] = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/accounts/$account/stocks/$symbol/orders", classOf[AccountOrders]
    )
  }

  def venueHeartBeat(venue: String): Future[VenueHeartBeat] = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/heartbeat", classOf[VenueHeartBeat])
  }

  def venueSymbols(venue: String): Future[VenueSymbols] = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks", classOf[VenueSymbols])
  }


  def symbolOrderBook(venue: String, symbol: String): Future[StockOrderBook] = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks/$symbol", classOf[StockOrderBook])
  }

  def stockQuote(venue: String, symbol: String): Future[StockQuote] = {
    httpRequestBuilder.get(s"ob/api/venues/$venue/stocks/$symbol/quote", classOf[StockQuote])
  }

  def stockOrderStatus(venue: String, symbol: String, orderId: Int): Future[StockOrder] = {
    httpRequestBuilder.get(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrderCancel(venue: String, symbol: String, orderId: Int): Future[StockOrder] = {
    httpRequestBuilder.delete(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrder(so: StockOrderRequest): Future[StockOrder] = {
    val out = new StringWriter
    JsonMapper.mapper.writeValue(out, so)
    httpRequestBuilder.post(
      s"ob/api/venues/${ so.venue }/stocks/${ so.symbol }/orders", classOf[StockOrder], out.toString
    )
  }

  trait StreamTransformer[A] extends (String => Option[A])

  object StreamQuoteTransformer extends StreamTransformer[StockQuote] {
    private val mapper = JsonMapper.mapper

    def apply(message: String): Option[StockQuote] = {
      val ticker = mapper.readValue(message, classOf[TickerStockQuote])
      if (ticker.ok) {
        Some(ticker.quote)
      } else {
        None
      }
    }
  }

  object StreamExecutionTransformer extends StreamTransformer[Execution] {
    private val mapper = JsonMapper.mapper

    def apply(message: String): Option[Execution] = {
      val obj = mapper.readValue(message, classOf[Execution])
      if (obj.ok) {
        Some(obj)
      } else {
        None
      }
    }
  }

  def streamQuotes(account: String, venue: String): Future[FutureSuccess] = {
    httpRequestBuilder.stream(
      s"ob/api/ws/$account/venues/$venue/tickertape",
      StreamQuoteTransformer
    )
      .map { tickerTape =>
        while (true) {
          println(tickerTape.take())
        }
      }
      .map(_ => FutureSuccess())
  }

  def streamExecutions(account: String, venue: String): Future[FutureState] = {

    httpRequestBuilder.stream(
      s"ob/api/ws/$account/venues/$venue/executions",
      StreamExecutionTransformer
    )
      .map { executions =>
        while (true) {
          println(executions.take())
        }
      }
      .map(_ => FutureSuccess())
  }

}
