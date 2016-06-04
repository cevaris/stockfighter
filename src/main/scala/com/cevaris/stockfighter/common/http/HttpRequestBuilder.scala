package com.cevaris.stockfighter.common.http

import com.cevaris.stockfighter.common.app.wrap
import com.cevaris.stockfighter.common.json.JsonMapper
import com.cevaris.stockfighter.{ApiError, ApiKey, StockFighterHost}
import com.google.inject.Inject
import com.twitter.util.{Future, FuturePool}
import java.net.URI
import java.util.concurrent.LinkedBlockingQueue
import javax.websocket.{ClientEndpointConfig, Endpoint, EndpointConfig, MessageHandler, Session}
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpEntity, HttpResponse, HttpStatus}
import org.glassfish.tyrus.client.ClientManager

case class HttpRequestBuilder @Inject()(
  apiKey: ApiKey,
  stockFighterHost: StockFighterHost
) {

  private val mapper = JsonMapper.mapper
  private val pool = FuturePool.unboundedPool

  // https://tyrus.java.net/documentation/1.12/index/getting-started.html
  def stream[A](
    path: String,
    transform: String => Option[A]
  ): Future[LinkedBlockingQueue[A]] = pool {
    val wssPath: String = s"wss://api.stockfighter.io/$path"
    val cec = ClientEndpointConfig.Builder.create().build()
    val client = ClientManager.createClient()
    val queue = new LinkedBlockingQueue[A]()

    client.connectToServer(new Endpoint() {
      @Override
      def onOpen(session: Session, config: EndpointConfig): Unit = {
        session.addMessageHandler(new MessageHandler.Whole[String]() {
          @Override
          def onMessage(message: String): Unit = transform(message).map(queue.add)
        })
      }
    }, cec, new URI(wssPath))

    queue
  }

  def get[A](path: String, clazz: Class[A]): Future[A] = wrap {
    val httpClient = newClient()
    val httpRequest: HttpGet = new HttpGet(s"${ stockFighterHost.value }/$path")
    httpRequest.setHeader("X-Starfighter-Authorization", apiKey.value)
    val response: HttpResponse = httpClient.execute(httpRequest)
    val entity: HttpEntity = response.getEntity
    if (response.getStatusLine.getStatusCode != HttpStatus.SC_OK) {
      throw mapper.readValue(EntityUtils.toString(entity, "UTF-8"), classOf[ApiError])
    }
    Option(mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz))
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
    Option(mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz))
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
    Option(mapper.readValue(EntityUtils.toString(entity, "UTF-8"), clazz))
  }

  private def newClient() =
    HttpClientBuilder
      .create()
      .build()
}
