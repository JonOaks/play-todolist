package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Task

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Application extends Controller {

   val html: String = "<html><head><style>body {background-color: #daecee;}#error {position: absolute;top: 50%;left: 50%;margin-top: -303px;margin-left: -303px;}</style></head><body><div id=\"error\"><a href=\"http://huwshimi.com/\"><img src=\"http://huwshimi.com/wp-content/themes/huwshimi/images/404.png\" alt=\"404 page not found\" id=\"error404-image\"></a></div></body></html>"

   /*implicit val json = new Writes[Task] {
      def writes(task: Task) = Json.obj (
         "id" -> task.id,
         "label" -> task.label
         )
   }*/

   implicit val taskWrites: Writes[Task] = (
      (JsPath \ "id").write[Long] and
      (JsPath \ "label").write[String]
   )(unlift(Task.unapply))

   def index = Action {
      Redirect(routes.Application.tasks)
   }

   val taskForm = Form(
      "label" -> nonEmptyText
   )

   def tasks = Action {
      val json = Json.toJson(Task.all())
      Ok(json)
   }

   def newTask = Action { 
      implicit request => taskForm.bindFromRequest.fold(
         errors => BadRequest(views.html.index(Task.all(), errors)),
         label => {
            val task = Task.create(label)
            Created(Json.toJson(task))
         }
      )
   }

   def getTask(id: Long) = Action {
      Task.task(id) match {
         case None => NotFound(html).as("text/html")
         case Some(t) => Ok(Json.toJson(t))
      }
   }

   def deleteTask(id: Long) = Action {
      Task.delete(id)
      Redirect(routes.Application.tasks)
   }
}



