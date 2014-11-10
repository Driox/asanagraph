package services

import play.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent.Future
import models._
import java.util.Date
import java.util.Calendar

object DataTransformer {

  import Mapper._

  def formatTaskToJson(tasks: List[Task]) = {
    val minDate = getMinDate(tasks)
    val maxDate = tasks.foldLeft(new Date())((currentDate, task) => if (currentDate.after(task.due_on.getOrElse(new Date()))) currentDate else task.due_on.getOrElse(new Date()))

    val dateList = getDateList(tasks)
    var dataMap = initMap(dateList)

    for {
      task <- tasks
    } yield {
      dataMap = addTaskPlannedInfoToDataMap(task, dataMap)
      dataMap = addTaskDoneInfoToDataMap(task, dataMap)
    }

    val data = dataMapToJson(dataMap)

    GraphData(minDate, maxDate, data.toString)
  }

  def getMinDate(tasks: List[Task]): Date = {
    val minDate = tasks.foldLeft(new Date())((currentDate, task) => if (currentDate.before(task.due_on.getOrElse(new Date()))) currentDate else task.due_on.getOrElse(new Date()))
    val cal = Calendar.getInstance()
    cal.setTime(minDate)
    cal.add(Calendar.DAY_OF_YEAR, -7)
    cal.getTime()
  }

  private def getDateList(tasks: List[Task]): List[Date] = {
    (tasks.map(t => t.due_on) ++ tasks.map(t => t.completed_at)).filter(x => x.isDefined).map(x => x.get)
  }

  private def initMap(dateList: List[Date]): Map[Date, (Double, Double)] = {
    dateList.map(d => (d, (0.0, 0.0))).toMap
  }

  private def addTaskPlannedInfoToDataMap(task: Task, inputDataMap: Map[Date, (Double, Double)]) = {
    var dataMap = inputDataMap

    task.due_on.map { key =>
      dataMap = dataMap.map {
        case (k, v) => k -> {
          if (k == key || k.after(key))
            (v._1 + task.point, v._2)
          else
            v
        }
      }
    }

    dataMap
  }

  private def addTaskDoneInfoToDataMap(task: Task, inputDataMap: Map[Date, (Double, Double)]) = {
    var dataMap = inputDataMap

    task.completed_at.map { key =>
      dataMap = dataMap.map {
        case (k, v) => k -> {
          if (k == key || k.after(key))
            (v._1, v._2 + task.point)
          else
            v
        }
      }
    }

    dataMap
  }

  private def dataMapToJson(dataMap: Map[Date, (Double, Double)]): JsArray = {
    dataMap.foldLeft(JsArray())((jsArray, mapElem) =>
      jsArray :+ dataToJson(mapElem._1, mapElem._2))
  }

  def dataToJson(date: Date, values: (Double, Double)): JsObject = {
    Json.obj(
      "day" -> Json.toJson(date),
      "planned" -> Json.toJson(values._1),
      "done" -> Json.toJson(values._2))
  }

}