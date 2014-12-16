package controllers

import play.api.Play.current
import play.api.libs.ws.{WS, WSResponseHeaders}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by thont on 12/15/14.
 */
object ProxyController extends Controller {
  val IgnoredHeaders = ("Transfer-Encoding")

  def reverseProxy = Action.async(parse.raw) {
    request: Request[RawBuffer] =>
      // Create the request to the upstream server
        val proxyRequest = WS.url(request.headers("HttpUrl"))
          .withFollowRedirects(follow = true)
          .withMethod(request.method)
          .withHeaders(request.headers.toMap.mapValues(_.head).toSeq: _*)
          .withQueryString(request.queryString.mapValues(_.head).toSeq: _*)
          .withBody(request.body.asBytes().get)

      // Stream the response to the client:
      proxyRequest.stream().map {
        case (headers: WSResponseHeaders, enum) => Result(
          ResponseHeader(headers.status, (headers.headers - IgnoredHeaders).mapValues(_.head)),
          enum)
      }
  }
}
