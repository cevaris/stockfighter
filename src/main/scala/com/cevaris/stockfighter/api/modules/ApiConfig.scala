package com.cevaris.stockfighter.api.modules

import com.cevaris.stockfighter.ApiKey
import com.google.inject.Inject

case class ApiConfig @Inject()(
  apiKey: ApiKey,
  account: String,
  venue: String,
  symbol: String
)
