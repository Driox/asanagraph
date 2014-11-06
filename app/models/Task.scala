package models

import java.util.Date


case class Task(id: Long, name: String, due_on:Option[Date], completed_on:Option[Date])
//case class Task(id: Long, name: String)

object Tasks {

}