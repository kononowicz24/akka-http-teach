package pl.edu.osp

import akka.actor.{Props, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import spray.json._
import akka.http.scaladsl.model.StatusCodes.MovedPermanently


case class LastWeather()

object ShowInfo {
  val sun = Array("bardzo duże", "duże", "średnie", "małe", "bardzo małe")
  val wind = Array("bardzo duży", "duży", "średni", "mały", "bardzo mały")
  def getSun(i:Int):String = if(i < 0 || i > 4) "Błąd" else sun(i)
  def getWind(i:Int):String = if(i < 0 || i > 4) "Błąd" else wind(i)
}

final case class WeatherData(temp:Int, pass: Int, humi: Int, wind: Int, sun: Int)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val itemFormat = jsonFormat5(WeatherData)
}

object Main  extends  App with BaseService with JsonSupport {

  val serviceName = "Pierwsza applikacja"
  protected def log: LoggingAdapter = Logging(system, serviceName)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val dbAct = system.actorOf(Props[DBActor], name = "DBActor")
  val route =
    get {
      pathSingleSlash {
        getFromResource("html/index.html")
      } ~
        path("ping") {
          complete("PONG!")
        } ~
        (path("api" / IntNumber ) & parameter('o)) { (l, o) =>
         complete(s"API for $l parm o =  $o" )
      } ~
        path("api" / "json") {
         complete {
           WeatherData(25, 1000, 89, 2, 3)
         }
        } ~
       path("weather") {
         implicit val timeout = Timeout(10 seconds)
         val f:Future[Array[String]] = ask(dbAct, LastWeather).mapTo[Array[String]]
         var arr = Await.result(f, 10 seconds)
         if(arr.length < 5) arr = Array("", "", "", "", "", "")
         complete {
           <html>
             <body>
               <p><img src="img/press.png" style="width:25px;height:25px;" /> Ciśnienie: {arr(0)} hPa</p>
               <p><img src="img/temp.png" style="width:25px;height:25px;" />Temperatura: {arr(1)} °C</p>
               <p><img src="img/humi.png" style="width:25px;height:25px;" />Wilgotność: {arr(2)} %</p>
               <p><img src="img/sun.png" style="width:25px;height:25px;" />Nasłonecznienie: {ShowInfo.getSun(arr(3).toInt)} </p>
               <p><img src="img/wind.png" style="width:25px;height:25px;" />Wiatr: {ShowInfo.getWind(arr(4).toInt)} </p>
             </body>
           </html>
         }
       } ~
      pathPrefix("img") {
          getFromResourceDirectory("img")
      } ~
        path("redirect" / Rest) { pathRest =>
          redirect("http://xxlo.osp.edu.pl/" + pathRest, MovedPermanently )
        }
    }

  Http().bindAndHandle(route, "localhost", httpPort)
}

