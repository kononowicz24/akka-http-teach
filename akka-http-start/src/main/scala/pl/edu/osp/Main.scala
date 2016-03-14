package pl.edu.osp

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json._



object Main  extends  App with BaseService {
  val serviceName = "Pierwsza applikacja"
  protected def log: LoggingAdapter = Logging(system, serviceName)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val dbAct = system.actorOf(Props[DBActor], name = "DBActor")
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
        (path("api" / IntNumber ) & parameter('o)) { (l, o) =>
         complete(s"API for $l parm o =  $o" )
      } ~
        path("api" / RestPath) { (pe) =>
         complete(s"API with end $pe ")
        }
    }

  Http().bindAndHandle(route, "localhost", 8080)
}

