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
}