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
    
    var dataMap = Map[Date, (Int, Int)]()
    dataMap = dateList.map(d => (d, (0,0))).toMap
    for {
      task <- tasks
    } yield {
      task.due_on.map { key =>
        dataMap = dataMap.map {
          case (k, v) => k -> {
            if (k == key || k.after(key))
              (v._1 + task.point.getOrElse(1), v._2)
            else
              v
          }
        }
      }

      task.completed_at.map { key =>
        dataMap = dataMap.map {
          case (k, v) => k -> {
            if (k == key || k.after(key))
              (v._1, v._2 + task.point.getOrElse(1))
            else
              v
          }
        }
      }
    }

    val plannedData = buildDataMap(tasks, (t => t.due_on))
    println("plannedData : " + plannedData)
    val doneData = buildDataMap(tasks, (t => t.completed_at))
    println("doneData : " + doneData)

    //val dataMap = addDataMap(plannedData, doneData)
    println("dataMap : " + dataMap)

    val data = dataMapToJson(dataMap)
    println("data : " + data)

    GraphData(minDate, maxDate, data.toString)

    //tasks.foldLeft(JsArray())((jsArray, task) => jsArray :+ Json.toJson(task))
  }
  
  private def getDateList(tasks: List[Task]) = {
    (tasks.map(t => t.due_on) ++ tasks.map(t => t.completed_at)).filter(x => x.isDefined).map(x => x.get)
  }

  def getMinDate(tasks: List[Task]): Date = {
    val minDate = tasks.foldLeft(new Date())((currentDate, task) => if (currentDate.before(task.due_on.getOrElse(new Date()))) currentDate else task.due_on.getOrElse(new Date()))
    val cal = Calendar.getInstance()
    cal.setTime(minDate)
    cal.add(Calendar.DAY_OF_YEAR, -7)
    cal.getTime()
  }
  /*
  def buildDataMap(tasks: List[Task]): Map[Date, (Int, Int)] = {
    tasks.foldLeft(Map[Date, (Int, Int)]())(
      (map, task) => {
        val mapWithNewTask = task.due_on.map { due_date =>
          val key = due_date
          val currentValue = map.get(key).getOrElse((0, 0))
          val value = (currentValue._1 + 1, currentValue._2)
          map.updated(key, value)
        }.getOrElse(map)

        task.completed_at.map { completed_date =>
          val key = completed_date
          val currentValue = mapWithNewTask.get(key).getOrElse((0, 0))
          val value = (currentValue._1, currentValue._2 + 1)
          mapWithNewTask.updated(key, value)
        }.getOrElse(mapWithNewTask)
      })
  }*/

  def buildDataMap(tasks: List[Task], taskToKey: Task => Option[Date]): Map[Date, Int] = {
    val emptyMap = Map[Date, Int]()
    val map = tasks.foldLeft(emptyMap)((map, task) => {
      taskToKey(task).map { key =>
        val currentValue = map.get(key).getOrElse(0)
        val newtValue = currentValue + 1
        map.updated(key, newtValue)
      }.getOrElse(map)
    })

    cumulateListValue(map.toList)
  }

  def cumulateListValue(list: List[(Date, Int)]): Map[Date, Int] = {
    def cumulateDataListValueWithAcc[Date](list: List[(Date, Int)], acc: Int): List[(Date, Int)] = {
      list match {
        case List() => List()
        case head :: tail => {
          val newAcc = head._2 + acc
          (head._1, newAcc) :: cumulateDataListValueWithAcc(tail, newAcc)
        }
      }
    }

    val sortedList = list.sortBy(x => x._1)
    cumulateDataListValueWithAcc(sortedList, 0).toMap
  }

  def addDataMap[A](originalDataMap: Map[A, Int], newDataMap: Map[A, Int]): Map[A, (Int, Int)] = {
    val originalDataMap2 = originalDataMap.map { case (k, v) => k -> (v, 0) }
    originalDataMap2 ++ newDataMap.map { case (k, v) => k -> ((originalDataMap.getOrElse(k, 0), v)) }
  }

  def dataMapToJson(dataMap: Map[Date, (Int, Int)]): JsArray = {
    dataMap.foldLeft(JsArray())((jsArray, mapElem) =>
      jsArray :+ dataToJson(mapElem._1, mapElem._2))
  }

  def dataToJson(date: Date, values: (Int, Int)): JsObject = {
    Json.obj(
      "day" -> Json.toJson(date),
      "planned" -> Json.toJson(values._1),
      "done" -> Json.toJson(values._2))
  }

}