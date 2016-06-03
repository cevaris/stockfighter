package com.cevaris.stockfighter.common.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule


object JsonMapper {

  val mapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .registerModule(new JodaModule())

}
