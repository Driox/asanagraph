package services

import play.Configuration
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent.Future
import models._
import java.util.Date
import java.util.Calendar

object StatisticService {

  import Mapper._

  def formatTaskToJson(tasks: List[Task]) = {
    val minDateWithDelai = getMinDate(tasks)
    val maxDate = tasks.foldLeft(new Date())((currentDate, task) => if (currentDate.after(task.due_on)) currentDate else task.due_on)

    val aMap = buildDataMap(tasks)
    

  }

  def getMinDate(tasks: List[Task]): Date = {
    val minDate = tasks.foldLeft(new Date())((currentDate, task) => if (currentDate.before(task.due_on)) currentDate else task.due_on)
    val cal = Calendar.getInstance()
    cal.setTime(minDate)
    cal.add(Calendar.DAY_OF_YEAR, -7)
    cal.getTime()
  }
  
  def buildDataMap(tasks:List[Task]):Map[Date, (Int, Int)] = {
    tasks.foldLeft(Map[Date, (Int, Int)]())(
      (map, task) => {
        task.due_on.map { due_date =>
          val key = due_date
          val currentValue = map.get(key).getOrElse((0, 0))
          val value = (currentValue._1 + 1, currentValue._2)
          map.updated(key, value)
        }.flatMap { map =>
          task.completed_on.map { completed_date =>
            val key = completed_date
            val currentValue = map.get(key).getOrElse((0, 0))
            val value = (currentValue._1, currentValue._2 + 1)
            map.updated(key, value)
          }
        }.getOrElse(map)
      })
  }
  
  def buildDataMap(dataMap:Map[Date, (Int, Int)]):String = {
    dataMap.tol
  }
  
  def formatDateToJson(date:Date, values:(Int, Int)) = {
    """
    {
	  "day" : "${date}"
    }
    """
     Json.obj(
          "day" -> Json.toJson(date),
          "values" -> Json.arr() aobj(
            "id" -> tasks.id,
            "name" -> tasks.name))
  }

  def tasksByProject() = {
    AsanaService.projectList().flatMap { Future.traverse(_) { project => projectIdToTasks(project) } }
  }

  def projectIdToTasks(project: Project) = {
    AsanaService.tasksOnProject(project.id).map { list =>
      list.map { tasks =>
        Json.obj(
          "project" -> Json.obj(
            "id" -> project.id,
            "name" -> project.name),
          "tasks" -> Json.obj(
            "id" -> tasks.id,
            "name" -> tasks.name))
      }
    }
  }

  def filterByProjectName(projectName: String) = {
    val projects = AsanaService.projectList()
    projects.flatMap { list =>
      val ioi = list.filter(p => p.name == projectName).head
      val rawTasks = AsanaService.tasksOnProject(ioi.id)
      val detailTasks = rawTasks.flatMap {
        Future.traverse(_) { t =>
          AsanaService.task(t.id.toString)
        }
      }
      detailTasks
    }
  }
}