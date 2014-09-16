package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.tasks)
  }

   val taskForm = Form(
      "label" -> nonEmptyText
   )

  def tasks = TODO
  def newTask = TODO
  def deleteTask(id: Long) = TODO
}