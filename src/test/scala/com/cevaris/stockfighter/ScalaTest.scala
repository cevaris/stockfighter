package com.cevaris.stockfighter

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

trait ScalaTest extends WordSpec
  with MustMatchers
  with MockitoSugar
  with BeforeAndAfter
