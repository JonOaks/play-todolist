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
   val d: Date = a.parse("05-11-2014")
   val e: Date = a.parse("06-11-2014")
   val correctDate: Option[Date] = Some(b)
   val incorrectDate: Option[Date] = Some(c)
   val sameDate: Option[Date] = Some(d)
   val beforeDate: Option[Date] = Some(e)

   "Tasks" should{
      "return all tasks" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            val tasks:List[Task] = Task.all()
            //Como existen dos tareas, la longitud de la lista debe ser 2
            tasks.length must equalTo(3)
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
            val task = Task.task(4)
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
            val success = Task.delete(4)
            success must equalTo(0)
         }
      }

      "return user's tasks list" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario existente con dos tareas asociadas
            val tasks:List[Task] = Task.tasks("McQuack")
            tasks.length must equalTo(3)
         }
      }

      "return user's tasks list empty" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            val tasks:List[Task] = Task.tasks("Fake_user")
            tasks.length must equalTo(0)
         }
      }

      "create task with an existent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario existente
            //Devuelve una tarea con un newid autoincrementado, label = "tarea" y una fecha vacia
            val task = Task.createWithUser("task","McQuack")
            task.label must equalTo("task")
            task.deadline must beNone
         }
      }

      "throw JbdcSQLException in a task creation with a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            Task.createWithUser("task","Fake_user") must throwA[JdbcSQLException]
         }
      }

      "create task with date and an existent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            val task = Task.createWithUserAndDate("task","McQuack",correctDate)
            task.label must equalTo("task")
            task.deadline must equalTo(correctDate)
         }
      }

      "throw JbdcSQLException in a task creation with date and a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            Task.createWithUserAndDate("task","Fake_user",correctDate) must throwA[JdbcSQLException]
         }
      }

      "delete tasks same date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Task.deleteTasksSameDate(sameDate) must equalTo(1)
         }
      }

      "try to delete tasks with a date with not the same date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Task.deleteTasksSameDate(correctDate) must equalTo(0)
         }
      }

      "delete tasks with a previous date of an existent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Task.deleteTasksUserBeforeDate("McQuack",beforeDate) must equalTo(1)
         }
      }

      "try to delete tasks with a previous date of a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Task.deleteTasksUserBeforeDate("Fake_user",beforeDate) must equalTo(0)
         }
      }

      "try to delete tasks with a date with a previous date" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            Task.deleteTasksUserBeforeDate("McQuack",correctDate) must equalTo(0)
         }
      }
   }
}