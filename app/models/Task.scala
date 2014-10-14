package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Task(id: Long, label: String)

object Task {

   val task = {
      get[Long]("id") ~ 
      get[String]("label") map {
         case id~label => Task(id, label)
      }
   }

   /*implicit val taskWrites = new Writes[Task] {
      def writes(task: Task) = Json.obj (
         "id" -> task.id,
         "label" -> task.label
         )
   }*/

   implicit val taskWrites: Writes[Task] = (
      (JsPath \ "id").write[Long] and
      (JsPath \ "label").write[String]
   )(unlift(Task.unapply))

   def all(): List[Task] = DB.withConnection { 
      implicit c => SQL("select * from task where author_login = 'McQuack' ").as(task *)
   }
  
   def create(label: String): Task = {
      var newid = 0L
      DB.withConnection { implicit c => newid = SQL("insert into task (label,author_login) values ({label},'McQuack')").on('label -> label).executeInsert().get
      }
      
      return Task(newid,label)
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
      
      return Task(newid,label)
   }
}