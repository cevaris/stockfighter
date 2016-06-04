package com.cevaris.stockfighter

import com.cevaris.stockfighter.api.{DirectionType, OrderType}
import com.google.inject.Inject
import org.joda.time.DateTime

package object api {
  type DirectionType = String
  type OrderType = String

  object DirectionType {

    val Buy: DirectionType = "buy"
    val Sell: DirectionType = "sell"
  }

  object OrderType {

    val Limit: OrderType = "limit"
    val Market: OrderType = "market"
    val FillOrKill: OrderType = "fill-or-kill"
    val ImmediateOrCancel: OrderType = "immediate-or-cancel"

  }

}

case class StockFighterHost @Inject()(value: String)

case class ApiKey @Inject()(value: String) {
  override def toString: String = value
}

class ApiException(message: String) extends RuntimeException(message)

case class ApiError(ok: Boolean, error: String) extends ApiException(error)


case class Symbol(name: String, symbol: String)

case class SymbolQuote(price: Int, qty: Int, isBuy: Boolean)

case class Fill(price: Int, qty: Int, ts: DateTime)

case class VenueHeartBeat(ok: Boolean, venue: String)

case class ApiHeartBeat(ok: Boolean, error: String)

case class VenueSymbols(ok: Boolean, symbols: Seq[Symbol])

case class StockOrderRequest(
  account: String,
  venue: String,
  symbol: String,
  price: Int,
  qty: Int,
  direction: DirectionType,
  orderType: OrderType
)

case class AccountOrders(ok: Boolean, venue: String, orders: Seq[StockOrder])

case class StockOrder(
  ok: Boolean,
  account: String,
  venue: String,
  symbol: String,
  direction: String,
  orderType: String,
  id: Int,
  originalQty: Int,
  qty: Int,
  price: Int,
  ts: DateTime,
  fills: Seq[Fill],
  totalFilled: Int,
  open: Boolean
)

case class StockOrderBook(
  ok: Boolean,
  venue: String,
  symbol: String,
  bids: Seq[SymbolQuote],
  asks: Seq[SymbolQuote],
  ts: DateTime
)

case class TickerStockQuote(
  ok: Boolean,
  quote: StockQuote
)

case class StockQuote(
  ok: Boolean,
  symbol: String,
  venue: String,
  bid: Int,
  ask: Int,
  bidSize: Int,
  askSize: Int,
  bidDepth: Int,
  askDepth: Int,
  last: Int,
  lastSize: Int,
  lastTrade: DateTime,
  quoteTime: DateTime
)

case class Execution(
  ok: Boolean,
  account: String,
  symbol: String,
  venue: String,
  order: StockOrder,
  standingId: Int,
  incomingId: Int,
  price: Int,
  filled: Int,
  filledAt: DateTime,
  standingComplete: Boolean,
  incomingComplete: Boolean
)