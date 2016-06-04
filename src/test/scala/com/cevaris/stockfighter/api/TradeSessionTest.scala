package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.common.time.DateTimeUtils
import com.cevaris.stockfighter.{AccountOrders, Direction, Fill, ScalaTest, StockOrder, StockQuote}
import org.mockito.Mockito._

class TradeSessionTest extends ScalaTest {

  val accountOrders: AccountOrders = mock[AccountOrders]

  before {
    reset(accountOrders)
    when(accountOrders.ok).thenReturn(true)
  }

  "TradeSessionTest" should {
    "have correct default values" in {
      val session = TradeSession()
      session.cash mustBe 0
      session.nav mustBe 0
      session.position mustBe 0
    }

    "handle empty orders" in {
      val session = TradeSession()
      when(accountOrders.orders).thenReturn(Seq.empty[StockOrder])
      session.update(accountOrders)

      session.cash mustBe 0
      session.nav mustBe 0
      session.position mustBe 0
      session.orders mustBe Map.empty[Int, StockOrder]
    }

    "handle update correctly" in {
      val ts = DateTimeUtils.now

      val quote = mock[StockQuote]
      when(quote.last).thenReturn(100)
      val session = TradeSession(latestQuote = Some(quote))

      val so1 = mock[StockOrder]
      when(so1.ok).thenReturn(true)
      when(so1.id).thenReturn(1)
      when(so1.direction).thenReturn(Direction.Buy)
      when(so1.fills).thenReturn(Seq(Fill(100, 10, ts), Fill(100, 5, ts)))

      val so2 = mock[StockOrder]
      when(so2.ok).thenReturn(true)
      when(so2.id).thenReturn(2)
      when(so2.direction).thenReturn(Direction.Sell)
      when(so2.fills).thenReturn(Seq(Fill(200, 10, ts)))

      // Should be ignored
      val so3 = mock[StockOrder]
      when(so3.ok).thenReturn(false)
      when(so3.id).thenReturn(3)
      when(so3.direction).thenReturn(Direction.Sell)
      when(so3.fills).thenReturn(Seq(Fill(99, 99, ts)))

      when(accountOrders.orders).thenReturn(Seq(so1, so2, so3))
      session.update(accountOrders)

      session.cash mustBe 500
      session.position mustBe 5
      session.nav mustBe 1000
      session.orders mustBe Map(1 -> so1, 2 -> so2)
    }
  }


}
