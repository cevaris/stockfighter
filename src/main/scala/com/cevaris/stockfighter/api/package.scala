package com.cevaris.stockfighter

import com.google.inject.Inject
import org.joda.time.DateTime

package object api {

}

case class StockFighterHost @Inject()(value: String)

case class ApiKey @Inject()(value: String) {
  override def toString: String = value
}

case class SessionConfig @Inject()(
  apiKey: ApiKey,
  account: String,
  venue: String,
  symbol: String
)

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
  direction: String,
  orderType: String
)

case class AccountOrders(
  ok: Boolean,
  venue: String,
  orders: Seq[StockOrder]
)

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

case class StockQuote(
  ok: Boolean,
  symbol: String,
  venue: String,
  bid: Integer,
  ask: Integer,
  bidSize: Integer,
  askSize: Integer,
  bidDepth: Integer,
  askDepth: Integer,
  last: Integer,
  lastSize: Integer,
  lastTrade: DateTime,
  quoteTime: DateTime
)
