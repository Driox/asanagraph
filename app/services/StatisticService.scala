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

  def filterByProjectNameJson(projectName: String) = {
    val projects = AsanaService.projectList()
    projects.flatMap { list =>
      val ioi = list.filter(p => p.name == projectName).head
      val rawTasks = AsanaService.tasksOnProject(ioi.id)
      val detailTasks = rawTasks.flatMap {
        Future.traverse(_) { t =>
          AsanaService.taskJson(t.id.toString)
        }
      }
      detailTasks
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