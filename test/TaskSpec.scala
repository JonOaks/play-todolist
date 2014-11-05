package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.Task

import java.util.Date
import java.text.SimpleDateFormat


class TaskSpec extends Specification {
   
   val a = new SimpleDateFormat("dd-MM-yyyy")
   val b: Date = a.parse("14-10-1990")
   val c: Date = a.parse("22-10-2014112133")
   val correctDate: Option[Date] = Some(b)
   val incorrectDate: Option[Date] = Some(c)

   "Tasks" should{
      "return all tasks" in {
         running(FakeApplication()){
            val tasks:List[Task] = Task.all()
            //Como existen dos tareas, la longitud de la lista debe ser 2
            tasks.length must equalTo(2)
         }
      }

      "return task" in {
         running(FakeApplication()){
            val task = Task.task(1)
            task must beSome
         }
      }

      "not return task" in {
         running(FakeApplication()){
            val task = Task.task(3)
            task must beNone
         }
      }

      "create task without user and date" in {
         running(FakeApplication()){
            val task = Task.create("")
            task.label must equalTo("")
         }
      }

      "create task with a correct date" in {
         running(FakeApplication()){

            val task = Task.createWithDate("",correctDate)
            task.label must equalTo("")
            task.deadline must equalTo(correctDate)
         }
      }

      "create task with an incorrect date" in {
         running(FakeApplication()){

            val task = Task.createWithDate("",incorrectDate)
            task.label must equalTo("")
            //La inserción en la base de datos no se lleva a cabo pero si se instancia la clase Task
            //con dicha fecha
            task.deadline must equalTo(incorrectDate)
         }
      }

      "delete task" in {
         running(FakeApplication()){
            //Existen dos tareas en la base de datos, borramos la primera
            val success = Task.delete(1)
            success must equalTo(1)
         }
      }
   }
}