package com.cevaris.stockfighter.http

import com.cevaris.stockfighter.json.JsonMapper
import com.cevaris.stockfighter.{ApiError, ApiKey, StockFighterHost}
import com.google.inject.Inject
import com.twitter.util.{Future, Return, Throw, Try}
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpEntity, HttpResponse, HttpStatus}

case class HttpRequestBuilder @Inject()(
  apiKey: ApiKey,
  stockFighterHost: StockFighterHost
) {

  private val mapper = JsonMapper.mapper

  def get[A](path: String, clazz: Class[A]): Future[A] = wrap {
    val httpClient = newClient()
    val httpRequest: HttpGet = new HttpGet(s"${ stockFighterHost.value }/$path")
    httpRequest.setHeader("X-Starfighter-Authorization", apiKey.value)
    val response: HttpResponse = httpClient.execute(httpRequest)
    val entity: HttpEntity = response.getEntity
    if (response.getStatusLine.getStatusCode != HttpStatus.SC_OK) {
      throw mapper.readValue(EntityUtils.toString(entity, "UTF-8"), classOf[ApiError])
    }
    mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz)
  }

  def delete[A](path: String, clazz: Class[A]): Future[A] = wrap {
    val httpClient = newClient()
    val httpRequest: HttpDelete = new HttpDelete(s"${ stockFighterHost.value }/$path")
    httpRequest.setHeader("X-Starfighter-Authorization", apiKey.value)
    val response: HttpResponse = httpClient.execute(httpRequest)
    val entity: HttpEntity = response.getEntity
    if (response.getStatusLine.getStatusCode != HttpStatus.SC_OK) {
      throw mapper.readValue(EntityUtils.toString(entity, "UTF-8"), classOf[ApiError])
    }
    mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz)
  }

  def post[A](path: String, clazz: Class[A], jsonBody: String): Future[A] = wrap {
    val httpClient = newClient()
    val httpRequest = new HttpPost(s"${ stockFighterHost.value }/$path")
    httpRequest.setHeader("X-Starfighter-Authorization", apiKey.value)

    val jsonEntity = new StringEntity(jsonBody)
    jsonEntity.setContentType("application/json")
    httpRequest.setEntity(jsonEntity)

    val httpResponse: HttpResponse = httpClient.execute(httpRequest)
    val entity: HttpEntity = httpResponse.getEntity
    if (httpResponse.getStatusLine.getStatusCode != HttpStatus.SC_OK) {
      throw mapper.readValue(EntityUtils.toString(entity, "UTF-8"), classOf[ApiError])
    }
    mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz)
  }

  private def wrap[A](f: => A): Future[A] = {
    Try(f) match {
      case Return(v) => Future.value(v)
      case Throw(e) => Future.exception(e)
    }
  }

  private def newClient() =
    HttpClientBuilder
      .create()
      .build()
}
