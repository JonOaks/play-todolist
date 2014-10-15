package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Task
import models.User

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Date
import java.text.SimpleDateFormat

object Application extends Controller {

   val html_404: String = "<html><head><style>body {background-color: #daecee;}#error {position: absolute;top: 50%;left: 50%;margin-top: -303px;margin-left: -303px;}</style></head><body><div id=\"error\"><img src=\"http://huwshimi.com/wp-content/themes/huwshimi/images/404.png\" alt=\"404 page not found\" id=\"error404-image\"></div></body></html>"

   def index = Action {
      Redirect(routes.Application.tasks)
   }

   val taskForm = Form(
      mapping(
      "id" -> ignored(0L),
      "label" -> nonEmptyText,
      "deadline" -> optional(date("dd-MM-yyyy"))
      )(Task.apply)(Task.unapply)
   )

   def tasks = Action {
      val json = Json.toJson(Task.all())
      Ok(json)
   }

   def newTask = Action { 
      implicit request => taskForm.bindFromRequest.fold(
         errors => BadRequest,
         task => {
            task.deadline match {
               case None => {
                  val task_without_date = Task.create(task.label)
                  Created(Json.toJson(task_without_date))
               }
               case Some(t) => {
                  val task_with_date = Task.createWithDate(task.label,task.deadline)
                  Created(Json.toJson(task_with_date))
               }
            }
         }
      )
   }

   def getTask(id: Long) = Action {
      Task.task(id) match {
         case None => NotFound(html_404).as("text/html")
         case Some(t) => Ok(Json.toJson(t))
      }
   }

   def deleteTask(id: Long) = Action {
      if(Task.delete(id) > 0)
      {
         Ok
      }
      else
      {
         NotFound(html_404).as("text/html")
      }
   }

   def getTasksUser(login: String) = Action {
      User.existUser(login) match {
         case None => NotFound(html_404).as("text/html")
         case Some(t) => {
            val json = Json.toJson(Task.tasks(login))
            Ok(json)
         }
      }
   }

   def newTaskUser(login: String) = Action {
      implicit request => taskForm.bindFromRequest.fold(
         errors => BadRequest,
         task => {
            User.existUser(login) match {
               case None => NotFound(html_404).as("text/html")
               case Some(t) => {
                  task.deadline match {
                     case None => {
                        val task_without_date = Task.createWithUser(task.label,login)
                        Created(Json.toJson(task_without_date))
                     }
                     case Some(t) => {
                        val task_with_date = Task.createWithUserAndDate(task.label,login,task.deadline)
                        Created(Json.toJson(task_with_date))
                     }
                  }
               }
            }
         }
      )
   }
   
}



