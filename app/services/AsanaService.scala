package services

import play.Configuration
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent.Future
import play.api.libs.functional.syntax._
import models._
import java.util.Date

/**
 * API : http://developer.asana.com/documentation/#tasks
 * WS : https://www.playframework.com/documentation/2.2.x/ScalaWS
 * Json : https://www.playframework.com/documentation/2.2.x/ScalaJson
 */
object AsanaService {

  import Mapper._

  val baseUrl = "https://app.asana.com/api/1.0"

  val API_KEY = play.api.Play.current.configuration.getString("asana.api.key").getOrElse("")
  val WORKSPACE_ID = play.api.Play.current.configuration.getString("asana.api.workspace.id").getOrElse("")
  val ASSIGNEE = play.api.Play.current.configuration.getString("asana.api.assignee").getOrElse("")

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

  def taskJson(id: String) = {
    val url = s"$baseUrl/tasks/$id"
    WS.url(url).withAuth(API_KEY, "", AuthScheme.BASIC).get.map(response => response.json)
  }

  def task(id: String): Future[Task] = {
    taskJson(id).map { json =>
      (json \ "data").as[Task]
    }
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
