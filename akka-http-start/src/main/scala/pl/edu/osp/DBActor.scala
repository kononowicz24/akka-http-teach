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
    val stat = conn.prepareStatement("Select * FROM weather Order By date Desc Limit 1")
    val res = stat.executeQuery()
    val rArr = new Array[String](5)
    var i = 0
    res.next()
      rArr(0) = res.getDouble(2).toString
      rArr(1) = res.getDouble(3).toString
      rArr(2) = res.getDouble(4).toString
      rArr(3) = res.getString(5)
      rArr(4) = res.getString(6)
    rArr
  }
}
