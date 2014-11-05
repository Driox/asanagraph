package services

import play.Configuration
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent.Future

object StatisticService {

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
          println(s"t : $t")
          AsanaService.task(t.id.toString)
        }
      }
      detailTasks
    }
  }
}