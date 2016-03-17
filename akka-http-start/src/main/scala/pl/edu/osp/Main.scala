package pl.edu.osp

import akka.actor.{Props, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import spray.json._

case class LastWeather()

object ShowInfo {
  val sun = Array("bardzo duże", "duże", "średnie", "małe", "bardzo małe")
  val wind = Array("bardzo duży", "duży", "średni", "mały", "bardzo mały")
  def getSun(i:Int):String = if(i < 0 || i > 4) "Błąd" else sun(i)
  def getWind(i:Int):String = if(i < 0 || i > 4) "Błąd" else wind(i)
}

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
       path("weather") {
         implicit val timeout = Timeout(10 seconds)
         val f:Future[Array[String]] = ask(dbAct, LastWeather).mapTo[Array[String]]
         var arr = Await.result(f, 10 seconds)
         if(arr.length < 5) arr = Array("", "", "", "", "", "")
         complete {
           <html>
             <body>
               <p><img src="/img/press.png" /> Ciśnienie: {arr(0)} hPa</p>
               <p><img src="/img/temp.png" />Temperatura: {arr(1)} °C</p>
               <p><img src="/img/humi.png" />Wilgotność: {arr(2)} %</p>
               <p><img src="/img/sun.png" />Nasłonecznienie: {ShowInfo.getSun(arr(3).toInt)} </p>
               <p><img src="/img/wind.png" />Wiatr: {ShowInfo.getWind(arr(4).toInt)} </p>
             </body>
           </html>
         }
       }
    }

  Http().bindAndHandle(route, "localhost", httpPort)
}

