import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.Category

import org.h2.jdbc.JdbcSQLException


class CategorySpec extends Specification {
   "Category" should{
      "create category of an existent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Category.newCategory("McQuack","testing") must equalTo("CREADA")
         }
      }

      "throw JbdcSQLException in a category creation of a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Category.newCategory("testing","testing") must throwA[JdbcSQLException]
         }
      }

      "add task to one particular category" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Category.addTaskToCategory("Adventure",1) must equalTo("AÑADIDA")
         }
      }

      "throw JbdcSQLException trying to add one task to a category that doesn't exist" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Category.addTaskToCategory("testing",1) must throwA[JdbcSQLException]
         }
      }
   }
}