package pl.edu.osp

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object Main  extends  App with BaseService {
  val serviceName = "Pierwsza applikacja"
  protected def log: LoggingAdapter = Logging(system, serviceName)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val route =
    get {
      pathSingleSlash {
        complete {
          <html>
            <body>Hello world!</body>
          </html>
        }
      } ~
        path("ping") {
          complete("PONG!")
        } ~
        path("crash") {
          sys.error("BOOM!")
        }
    }

  Http().bindAndHandle(route, "localhost", 8080)
}

