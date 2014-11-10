package models

import java.util.Date

case class Tag(id: Long, name: String)
case class Task(id: Long, name: String, due_on: Option[Date], completed_at: Option[Date], tags: Option[List[Tag]] = None) {

  def point(): Double = {
    tags.map { list =>
      list match {
        case List() => 1
        case head :: tail => head.name.toDouble
      }
    }.getOrElse(1)
  }

}

object Tasks {

}