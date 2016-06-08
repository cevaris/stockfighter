package com.cevaris.stockfighter.common.util

import com.cevaris.stockfighter.ScalaTest

class MinMaxRangeTest extends ScalaTest {

  private val intTest = MinMaxRange(-10, 10)

  "MinMaxRangeTest" should {
    "false includes on lower min" in {
      intTest.includes(intTest.min - 1) mustBe false
    }

    "false includes on higher max" in {
      intTest.includes(intTest.max + 1) mustBe false
    }

    "true includes on exact min" in {
      intTest.includes(intTest.min) mustBe true
    }

    "true includes on exact max" in {
      intTest.includes(intTest.max) mustBe true
    }
  }
}
