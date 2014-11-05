package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.Task

import java.util.Date
import java.text.SimpleDateFormat

import org.h2.jdbc.JdbcSQLException


class TaskSpec extends Specification {
   
   val a = new SimpleDateFormat("dd-MM-yyyy")
   val b: Date = a.parse("14-10-1990")
   val c: Date = a.parse("22-10-2014112133")
   val correctDate: Option[Date] = Some(b)
   val incorrectDate: Option[Date] = Some(c)

   "Tasks" should{
      "return all tasks" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            val tasks:List[Task] = Task.all()
            //Como existen dos tareas, la longitud de la lista debe ser 2
            tasks.length must equalTo(2)
         }
      }

      "return task" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            val task = Task.task(1)
            task must beSome
         }
      }

      "not return task" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            val task = Task.task(3)
            task must beNone
         }
      }

      "create task without user and date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            val task = Task.create("task")
            task.label must equalTo("task")
         }
      }

      "create task with a correct date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

            val task = Task.createWithDate("task",correctDate)
            task.label must equalTo("task")
            task.deadline must equalTo(correctDate)
         }
      }

      "create task with an incorrect date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

            val task = Task.createWithDate("task",incorrectDate)
            task.label must equalTo("task")
            //La inserción en la base de datos no se lleva a cabo pero si se instancia la clase Task
            //con dicha fecha
            task.deadline must equalTo(incorrectDate)
         }
      }

      "delete task" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Existen dos tareas en la base de datos, borramos la primera
            val success = Task.delete(1)
            success must equalTo(1)
         }
      }

      "not delete task" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Existen dos tareas en la base de datos, borramos la primera
            val success = Task.delete(3)
            success must equalTo(0)
         }
      }

      "return user's tasks list" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario existente con dos tareas asociadas
            val tasks:List[Task] = Task.tasks("McQuack")
            tasks.length must equalTo(2)
         }
      }

      "return user's tasks list empty" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            val tasks:List[Task] = Task.tasks("Fake_user")
            tasks.length must equalTo(0)
         }
      }

      "create task with an existent user" in{
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario existente
            //Devuelve una tarea con un newid autoincrementado, label = "tarea" y una fecha vacia
            val task = Task.createWithUser("task","McQuack")
            task.label must equalTo("task")
            task.deadline must beNone
         }
      }

      "throw JbdcSQLException in a task's creation with a nonexistent user" in{
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            Task.createWithUser("task","Fake_user") must throwA[JdbcSQLException]
         }
      }

      "create task with date and an existent user" in{
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            val task = Task.createWithUserAndDate("task","McQuack",correctDate)
            task.label must equalTo("task")
            task.deadline must equalTo(correctDate)
         }
      }
   }
}