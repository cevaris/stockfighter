package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.ApiKey
import com.google.inject.Inject

case class SFConfig @Inject()(
  apiKey: ApiKey,
  account: String,
  venue: String,
  symbol: String
)
