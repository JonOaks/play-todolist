package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Task

object Application extends Controller {

   def index = Action {
      Redirect(routes.Application.tasks)
   }

   val taskForm = Form(
      "label" -> nonEmptyText
   )

   def tasks = Action {
      Ok(views.html.index(Task.all(), taskForm))
   }
   def newTask = TODO
   def deleteTask(id: Long) = TODO
}