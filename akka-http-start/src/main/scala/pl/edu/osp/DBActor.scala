package pl.edu.osp

import java.sql.DriverManager

import akka.actor.Actor

class DBActor extends Actor {
  Class.forName("org.h2.Driver")
  val conn = DriverManager.getConnection("jdbc:h2:./test", "sa", "")

  def receive = {
    case LastWeather => sender ! getLatest
  }

  override def postStop() {
    conn.close()
  }

  private def getLatest() = {
    val stat = conn.prepareStatement(
      "Select temp, press, humi, wind, sun FROM weather Order By date Desc Limit 1")
    val res = stat.executeQuery()
    val rArr = new Array[Int](5)
    var i = 0
    res.next()
      rArr(0) = res.getInt(1)
      rArr(1) = res.getInt(2)
      rArr(2) = res.getInt(3)
      rArr(3) = res.getInt(4)
      rArr(4) = res.getInt(5)
    rArr
  }
}
