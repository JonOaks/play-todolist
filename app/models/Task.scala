package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Date
import java.text.SimpleDateFormat

case class Task(id: Long, label: String, deadline: Option[Date])

object Task {

   val task = {
      get[Long]("id") ~ 
      get[String]("label") ~
      get[Option[Date]]("deadline") map {
         case id~label~deadline => Task(id, label, deadline)
      }

   }

   implicit val taskWrites = new Writes[Task] {
      def writes(task: Task) = Json.obj (
         "id" -> task.id,
         "label" -> task.label,
         "deadline" -> {
            task.deadline match {
               case None => ""
               case Some(t) => {
                  val dateformat = new SimpleDateFormat("dd-MM-yyyy")
                  dateformat.format(t)
               }
            }
         }
      )
   }

   /*implicit val taskWrites: Writes[Task] = (
      (JsPath \ "id").write[Long] and
      (JsPath \ "label").write[String] and
      (JsPath \ "deadline").write[Option[Date]]
   )(unlift(Task.unapply))*/

   def all(): List[Task] = DB.withConnection { 
      implicit c => SQL("select * from task where author_login = 'McQuack' ").as(task *)
   }
  
   def create(label: String): Task = {
      var newid = 0L
      DB.withConnection { implicit c => newid = SQL("insert into task (label,author_login) values ({label},'McQuack')").on('label -> label).executeInsert().get
      }
      
      return Task(newid,label,None)
   }

   def createWithDate(label: String, deadline: Option[Date]): Task = {
      var newid = 0L
      DB.withConnection { implicit c => newid = SQL("insert into task (label,author_login,deadline) values ({label},'McQuack',{deadline})").on('label -> label).on('deadline -> deadline).executeInsert().get
      }
      
      return Task(newid,label,deadline)
   }

   def delete(id: Long): Long = {
      DB.withConnection { implicit c => SQL("delete from task where id = {id}").on('id -> id).executeUpdate()
      }
   }

   def task(id: Long): Option[Task] = DB.withConnection{
      implicit c => SQL("select * from task where id = {id}").on('id -> id).as(task.singleOpt)
   }

   def tasks(login: String): List[Task] = DB.withConnection{
      implicit c => SQL("select * from task where author_login = {login}").on('login -> login).as(task *)
   }

   def createWithUser(label: String, login: String): Task = {
      var newid = 0L
      DB.withConnection {implicit c => newid = SQL("insert into task (label,author_login) values ({label},{login})").on('label -> label).on('login -> login).executeInsert().get
      }
      
      return Task(newid,label,None)
   }

   def createWithUserAndDate(label: String, login: String, deadline: Option[Date]): Task = {
      var newid = 0L
      DB.withConnection {implicit c => newid = SQL("insert into task (label,author_login,deadline) values ({label},{login},{deadline})").on('label -> label).on('login -> login).on('deadline -> deadline).executeInsert().get
      }
      
      return Task(newid,label,deadline)
   }

   def deleteTasksSameDate(date_to_delete: Option[Date]): Long = {
      DB.withConnection {implicit c => SQL("delete from task where deadline = {date_to_delete}").on('date_to_delete -> date_to_delete).executeUpdate()}
   }


   def deleteTasksUserBeforeDate(login: String, date_to_delete: Option[Date]): Long = {
      DB.withConnection {implicit c => SQL("delete from task where deadline < {date_to_delete} and author_login  = {login}").on('date_to_delete -> date_to_delete).on('login -> login)executeUpdate()}
   }

   def newCategory(login: String, category: String): String = {
      var newid = 0L
      DB.withConnection {implicit c => newid = SQL("insert into category (category) values ({category})").on('category -> category).executeInsert().get
      }
      DB.withConnection {implicit c => newid = SQL("insert into user_category (login,category) values ({login},{category})").on('login -> login).on('category -> category).executeInsert().get
      }
      return "CREADA"
   }

   def getTasksCategory(login: String, category: String): List[Task] = DB.withConnection{
      // la comprobación de si el usuario tiene la categoría pasada por parámetro asociada a él
      // la hacemos en el controlador
      implicit c => SQL("select t.* from task t inner join task_category tc on tc.task_id = t.id inner join category c on c.category = tc.category where t.author_login = {login} and c.category = {category}").on('login -> login).on('category -> category).as(task *)
   }

   def addTaskToCategory(category: String, id: Long): String = {
      // la comprobación de si el usuario tiene la categoría pasada por parámetro asociada a él
      // la hacemos en el controlador
      DB.withConnection {implicit c => SQL("insert into task_category (task_id,category) values ({id},{category})").on('id -> id).on('category -> category).executeInsert().get
      }
      return "AÑADIDA"
   }

   def addCategoryToUser(login: String, category: String): String = {
      // la comprobación de si el usuario tiene la categoría pasada por parámetro asociada a él
      // la hacemos en el controlador
      DB.withConnection {implicit c => SQL("insert into user_category (login,category) values ({login},{category})").on('login -> login).on('category -> category).executeInsert().get
      }
      return "AÑADIDA"
   }

   def taskBelongToUser(id: Long, login: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from task where id = {id} and author_login = {login}").on('login -> login).on('id -> id).as(scalar[Long].single)
      }

      return cantidad
   }

   def taskBelongToCategory(task_id: Long, category: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from task_category where task_id = {task_id} and category = {category}").on('category -> category).on('task_id -> task_id).as(scalar[Long].single)
      }

      return cantidad
   }
}