package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Task
import models.User
import models.Category

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Date
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.regex.Matcher

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
   
   def okDate(date: String): Boolean = {
      val pat = Pattern compile("(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)[0-9][0-9]")
      val mat = pat.matcher(date)
      if(mat.matches())
      {
         true
      }
      else
      {
         false
      }
   }

   def deleteTasksSameDate(date_to_delete: String) = Action {
      if(okDate(date_to_delete))
      {
         val a = new SimpleDateFormat("dd-MM-yyyy")
         val b: Date = a.parse(date_to_delete)
         val to_delete: Option[Date] = Some(b)
         
         Task.deleteTasksSameDate(to_delete)
         Ok
      }
      else
      {
         BadRequest
      }
   }

   def deleteTasksUserBeforeDate(login: String, date_to_delete: String) = Action {
      if(okDate(date_to_delete))
      {
         User.existUser(login) match {
            case None => NotFound(html_404).as("text/html")
            case Some(t) => {
               val a = new SimpleDateFormat("dd-MM-yyyy")
               val b: Date = a.parse(date_to_delete)
               val to_delete: Option[Date] = Some(b)

               Task.deleteTasksUserBeforeDate(login, to_delete)
               Ok
            }
         }
      }
      else
      {
         BadRequest
      }
   }

   // Métodos Feature 4 (CATEGORÍAS)
   def newCategory(login: String, category: String) = Action {
      User.existUser(login) match {
         case None => NotFound(html_404).as("text/html")
         case Some(t) => {
            // Si ya existe la categoria lo que hacemos es vincularla también a otro usuario (case Some(t))
            Category.existCategory(category) match {
               case None => {
                  if(Category.newCategory(login,category) == "CREADA")
                  {
                     Ok("CATEGORY " + category + " HAS BEEN CREATED AND VINCULATED TO THE USER NAMED " + login).as("text/html")
                  }
                  else
                  {
                     Ok("CATEGORY" + category + " HASN'T BEEN CREATED").as("text/html")
                  }
               }
               case Some(t) =>
               {
                  if(Category.categoryBelongToUser(category,login) == 0)
                  {
                     User.addCategoryToUser(login,category)
                     Ok("CATEGORY " + category + " HAS BEEN VINCULATED TO THE USER NAMED " + login).as("text/html")
                  }
                  // Si ya existe y está vinculada al usuario facilitado por parámetro, no se hace nada
                  // y se devuelve un error 400
                  else
                  {
                     BadRequest("CATEGORY " + category + " ALREADY EXISTS AND BELONG TO THE USER NAMED " + login).as("text/html")
                  }
               }
            }

         }
      }
   }

   def getTasksCategory(login: String, category: String) = Action {
      User.existUser(login) match {
         case None => NotFound(html_404).as("text/html")
         case Some(t) => {
            Category.existCategory(category) match {
               case None => NotFound(html_404).as("text/html")
               case Some(u) => {
                  //Comprobamos que la categoria pertenece al usuario especificado por parámetro
                  if(Category.categoryBelongToUser(category,login) > 0)
                  {
                     val json = Json.toJson(Task.getTasksCategory(login,category))
                     Ok(json)
                  }
                  else
                  {
                     BadRequest
                  }
               }
            }
         }
      }
   }

   def addTaskToCategory(login: String, category: String, id: Long) = Action {
      User.existUser(login) match {
         case None => BadRequest
         case Some(t) => {
            //Comprobamos que la tarea pertenece al usuario especificado por parámetro
            if(Task.taskBelongToUser(id,login) > 0)
            {
               Category.existCategory(category) match {
                  case None => BadRequest
                  case Some(u) => {
                     //Comprobamos que la categoria pertenece al usuario especificado por parámetro
                     if(Category.categoryBelongToUser(category,login) > 0)
                     {
                        if(Task.taskBelongToCategory(id,category) > 0)
                        {
                           BadRequest("THE TASK NUMBER " + id + " ALREADY BELONGS TO CATEGORY " + category).as("text/html")
                        }
                        else
                        {
                           if(Category.addTaskToCategory(category,id) == "AÑADIDA")
                           {
                              Ok("TASK NUMBER " + id + " HAS BEEN ADDED TO CATEGORY " + category).as("text/html")
                           }
                           else
                           {
                              Ok("TASK NUMBER " + id + " HASN'T BEEN ADDED TO CATEGORY " + category).as("text/html")
                           }
                        }
                     }
                     else
                     {
                        BadRequest
                     }
                  }
               }
            }
            else
            {
               BadRequest
            }
         }
      }
   }
}



