package controllers

import play.api._
import play.api.mvc._
import services.AsanaService
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.StatisticService
import scala.concurrent.Future
import models.Project

object Application extends Controller {

  def index = Action.async {
    AsanaService.projectList.map(projects => 
      Ok(views.html.index(projects)))
      
//    val projects= List(Project(1, "IOI"), Project(2, "Migration"))
//      Future(Ok(views.html.index(projects)))
  }

  def me = Action.async {
    AsanaService.me.map(json => Ok(json))
  }

  def tasks = Action.async {
    StatisticService.tasksByProject.map(json => Ok(Json.toJson(json)))
  }
  
  def projects = Action.async {
    AsanaService.projects.map(json => Ok(json))
  }
  
  def byProject(project:String) = Action.async {
    StatisticService.filterByProjectName(project).map(StatisticService.formatTaskToJson).map(json => Ok(Json.toJson(json)))
      
      //Ok(views.html.project(project, Json.toJson(json).toString)))
  }
}