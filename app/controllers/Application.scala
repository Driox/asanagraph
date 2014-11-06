package controllers

import play.api._
import play.api.mvc._
import services.AsanaService
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.StatisticService
import scala.concurrent.Future
import models.Project
import services.DataTransformer

object Application extends Controller {

  def index = Action.async {
    AsanaService.projectList.map(projects =>
      Ok(views.html.index(projects)))
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

  def byProject(project: String) = Action.async {
    StatisticService.filterByProjectName(project)
      .map(DataTransformer.formatTaskToJson)
//      .map(json => Ok(Json.toJson(json)))
      .map(json => Ok(views.html.project(project, json)))

//    StatisticService.filterByProjectNameJson(project)
//      .map(json => Ok(Json.toJson(json)))
  }
}