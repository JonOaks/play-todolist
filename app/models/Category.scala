package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

case class Category(id: Long, category: String)

object Category {

   val category = {
      get[Long]("id") ~ 
      get[String]("category") map {
         case id~category => Category(id, category)
      }
   }

   def existCategory(category_name: String): Option[Category] = DB.withConnection{
      implicit c => SQL("select * from category where category = {category_name}").on('category_name -> category_name).as(category.singleOpt)
   }

   def categoryBelongToUser(category_name: String, login: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from user_category where category = {category_name} and login = {login}").on('login -> login).on('category_name -> category_name).as(scalar[Long].single)
      }

      return cantidad
   }

   def newCategory(login: String, category: String): String = {
      var newid = 0L
      DB.withConnection {implicit c => newid = SQL("insert into category (category) values ({category})").on('category -> category).executeInsert().get
      }
      DB.withConnection {implicit c => newid = SQL("insert into user_category (login,category) values ({login},{category})").on('login -> login).on('category -> category).executeInsert().get
      }
      return "CREADA"
   }

   def addTaskToCategory(category: String, id: Long): String = {
      // la comprobación de si el usuario tiene la categoría pasada por parámetro asociada a él
      // la hacemos en el controlador
      DB.withConnection {implicit c => SQL("insert into task_category (task_id,category) values ({id},{category})").on('id -> id).on('category -> category).executeInsert().get
      }
      return "AÑADIDA"
   }
}