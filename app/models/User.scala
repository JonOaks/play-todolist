package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

case class User(id: Long, login: String)

object User {

   val user = {
      get[Long]("id") ~ 
      get[String]("login") map {
         case id~login => User(id, login)
      }
   }

   def existUser(login: String): Option[User] = DB.withConnection{
      implicit c => SQL("select * from task_user where login = {login}").on('login -> login).as(user.singleOpt)
   }

   def addCategoryToUser(login: String, category: String): String = {
      // la comprobación de si el usuario tiene la categoría pasada por parámetro asociada a él
      // la hacemos en el controlador
      DB.withConnection {implicit c => SQL("insert into user_category (login,category) values ({login},{category})").on('login -> login).on('category -> category).executeInsert().get
      }
      return "AÑADIDA"
   }
}