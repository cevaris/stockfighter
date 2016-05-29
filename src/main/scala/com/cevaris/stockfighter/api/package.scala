package com.cevaris.stockfighter

import com.google.inject.Inject
import org.joda.time.DateTime

package object api {

}

case class ApiKey @Inject()(
  value: String
)

case class SessionConfig @Inject()(
  apiKey: ApiKey,
  account: String,
  venue: String,
  symbol: String
)

case class StockQuote(
  ok: Boolean,
  symbol: String,
  Venue: String,
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
