package controllers

import play.api._
import play.api.mvc._
import services.AsanaService
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.StatisticService

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
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
  
  def ioi = Action.async {
    StatisticService.filterByProjectName("IOI").map(json => Ok(Json.toJson(json)))
  }
}