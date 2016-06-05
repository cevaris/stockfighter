package com.cevaris.stockfighter.api

import com.cevaris.stockfighter._
import com.cevaris.stockfighter.common.http.HttpRequest
import com.cevaris.stockfighter.common.time.DateTimeUtils
import org.mockito.Mockito._

class SFSessionTest extends ScalaTest {

  val accountOrders: AccountOrders = mock[AccountOrders]
  val ts = DateTimeUtils.now

  before {
    reset(accountOrders)
    when(accountOrders.ok).thenReturn(true)
  }

  "TradeSessionTest" should {
    "have correct default values" in {
      val session = newSession()
      session.cash mustBe 0
      session.nav mustBe 0
      session.position mustBe 0
    }

    "handle empty orders" in {
      val session = newSession()
      when(accountOrders.orders).thenReturn(Seq.empty[StockOrder])
      session.update(accountOrders)

      session.cash mustBe 0
      session.nav mustBe 0
      session.position mustBe 0
      session.orders mustBe Map.empty[Int, StockOrder]
    }

    "handle buy and sell orders" in {
      val quote = mock[StockQuote]
      when(quote.last).thenReturn(100)
      val session = newSession()
      session.setLatestQuote(quote)

      val so1 = mock[StockOrder]
      when(so1.ok).thenReturn(true)
      when(so1.id).thenReturn(1)
      when(so1.qty).thenReturn(0)
      when(so1.open).thenReturn(false)
      when(so1.direction).thenReturn(DirectionType.Buy)
      when(so1.fills).thenReturn(Seq(Fill(100, 10, ts), Fill(100, 5, ts)))

      val so2 = mock[StockOrder]
      when(so2.ok).thenReturn(true)
      when(so2.id).thenReturn(2)
      when(so2.open).thenReturn(true)
      when(so2.qty).thenReturn(5)
      when(so2.direction).thenReturn(DirectionType.Sell)
      when(so2.fills).thenReturn(Seq(Fill(200, 10, ts)))

      when(accountOrders.orders).thenReturn(Seq(so1, so2))
      session.update(accountOrders)

      session.cash mustBe 500
      session.position mustBe 5
      session.unfilledPosition mustBe 5
      session.nav mustBe 1000
      session.orders mustBe Map(1 -> so1, 2 -> so2)
    }

    "handle invalid orders" in {
      val session = newSession()

      val so1 = mock[StockOrder]
      when(so1.ok).thenReturn(false)
      when(so1.id).thenReturn(1)
      when(so1.direction).thenReturn(DirectionType.Buy)
      when(so1.fills).thenReturn(Seq(Fill(100, 10, ts), Fill(100, 5, ts)))

      when(accountOrders.orders).thenReturn(Seq(so1))
      session.update(accountOrders)

      session.cash mustBe 0
      session.position mustBe 0
      session.nav mustBe 0
      session.orders.size mustBe 0
    }

    "handle invalid account status" in {
      val session = newSession()

      val so1 = mock[StockOrder]
      when(so1.ok).thenReturn(true)

      when(accountOrders.ok).thenReturn(false)
      session.update(accountOrders)
      verify(accountOrders, times(0)).orders

      session.cash mustBe 0
      session.position mustBe 0
      session.nav mustBe 0
      session.orders.size mustBe 0
    }
  }

  def newSession() = {
    val apikey = ApiKey("apikey")
    SFSession(
      SFConfig(apikey, "account", "venue", "symbol"),
      SFRequest(HttpRequest(apikey, SFHost("localhost")))
    )
  }


}
