package com.cevaris.stockfighter.api

import com.cevaris.stockfighter._
import com.cevaris.stockfighter.common.concurrency.{Result, SuccessResult}
import com.cevaris.stockfighter.common.http.HttpRequest
import com.cevaris.stockfighter.common.json.JsonMapper
import com.google.inject.Inject
import com.twitter.util.Future
import java.io.StringWriter


case class SFRequest @Inject()(
  httpRequest: HttpRequest
) {

  def apiHeartBeat(): Future[ApiHeartBeat] = {
    httpRequest.get(s"ob/api/heartbeat", classOf[ApiHeartBeat])
  }

  def accountOrders(account: String, venue: String): Future[AccountOrders] = {
    httpRequest.get(
      s"ob/api/venues/$venue/accounts/$account/orders", classOf[AccountOrders]
    )
  }

  def accountOrders(account: String, venue: String, symbol: String): Future[AccountOrders] = {
    httpRequest.get(
      s"ob/api/venues/$venue/accounts/$account/stocks/$symbol/orders", classOf[AccountOrders]
    )
  }

  def venueHeartBeat(venue: String): Future[VenueHeartBeat] = {
    httpRequest.get(s"ob/api/venues/$venue/heartbeat", classOf[VenueHeartBeat])
  }

  def venueSymbols(venue: String): Future[VenueSymbols] = {
    httpRequest.get(s"ob/api/venues/$venue/stocks", classOf[VenueSymbols])
  }


  def symbolOrderBook(venue: String, symbol: String): Future[StockOrderBook] = {
    httpRequest.get(s"ob/api/venues/$venue/stocks/$symbol", classOf[StockOrderBook])
  }

  def stockQuote(venue: String, symbol: String): Future[StockQuote] = {
    httpRequest.get(s"ob/api/venues/$venue/stocks/$symbol/quote", classOf[StockQuote])
  }

  def stockOrderStatus(venue: String, symbol: String, orderId: Int): Future[StockOrder] = {
    httpRequest.get(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrderCancel(venue: String, symbol: String, orderId: Int): Future[StockOrder] = {
    httpRequest.delete(
      s"ob/api/venues/$venue/stocks/$symbol/orders/$orderId", classOf[StockOrder]
    )
  }

  def stockOrder(so: StockOrderRequest): Future[StockOrder] = {
    val out = new StringWriter
    JsonMapper.mapper.writeValue(out, so)
    httpRequest.post(
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

  def streamQuotes[A](
    account: String,
    venue: String
  )(
    f: (StockQuote) => A
  ): Future[Result] = {
    httpRequest.stream(
      s"ob/api/ws/$account/venues/$venue/tickertape",
      StreamQuoteTransformer
    )
      .map { queue =>
        while (true) {
          f(queue.take())
        }
        SuccessResult()
      }
  }

  def streamExecutions[A](
    account: String,
    venue: String
  )(
    f: (Execution) => A
  ): Future[Result] = {
    httpRequest.stream(
      s"ob/api/ws/$account/venues/$venue/executions",
      StreamExecutionTransformer
    )
      .map { queue =>
        while (true) {
          f(queue.take())
        }
        SuccessResult()
      }
  }

}
