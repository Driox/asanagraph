package services

import play.Configuration
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent.Future
import play.api.libs.functional.syntax._

case class Project(id: Long, name: String)

case class Task(id: Long, name: String)

/**
 * API : http://developer.asana.com/documentation/#tasks
 * WS : https://www.playframework.com/documentation/2.2.x/ScalaWS
 * Json : https://www.playframework.com/documentation/2.2.x/ScalaJson
 */
object AsanaService {

  val baseUrl = "https://app.asana.com/api/1.0"

  val API_KEY = play.api.Play.current.configuration.getString("asana.api.key").getOrElse("")
  val WORKSPACE_ID = play.api.Play.current.configuration.getString("asana.api.workspace.id").getOrElse("")
  val ASSIGNEE = play.api.Play.current.configuration.getString("asana.api.assignee").getOrElse("")

  implicit val projectReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String])(Project)

  implicit val taskReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String])(Task)

  def getWorkspaceId() = {
    me().map(me => me \ "data" \ "workspaces" \ "id" toString)
  }

  def getAssigneeId() = {
    me().map(me => me \ "data" \ "id" toString)
  }

  def tasks() = {
    val url = s"$baseUrl/tasks?workspace=$WORKSPACE_ID&assignee=me"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json)
  }

  def task(id: String) = {
    val url = s"$baseUrl/tasks/$id"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json)
  }

  def me() = {
    val url = s"$baseUrl/users/me"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json)
  }

  def projects() = {
    val url = s"$baseUrl/projects"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json)
  }

  def tasksOnProject(projectId: Long) = {
    val url = s"$baseUrl/projects/$projectId/tasks"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json).map(t => (t \ "data").as[List[Task]])
  }

  def projectList() = projects().map { p => (p \ "data").as[List[Project]] }

}
