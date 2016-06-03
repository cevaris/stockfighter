package com.cevaris.stockfighter.common.time

import org.joda.time.{DateTime, DateTimeZone}

object DateTimeUtils {

  def now: DateTime = DateTime.now(DateTimeZone.UTC).toDateTimeISO

}
