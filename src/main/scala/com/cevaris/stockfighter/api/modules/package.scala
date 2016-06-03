package com.cevaris.stockfighter.api

import com.cevaris.stockfighter.ApiKey
import com.google.inject.Inject

package object modules {

}

case class ApiConfig @Inject()(apiKey: ApiKey, account: String, venue: String, symbol: String)