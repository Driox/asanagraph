package models

import java.util.Date

case class Task(id: Long, name: String, due_on:Option[Date], completed_at:Option[Date], point:Option[Int])
//case class Task(id: Long, name: String)

object Tasks {

}