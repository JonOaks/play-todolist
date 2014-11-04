package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.Task

import java.util.Date

class TaskSpec extends Specification {
   val correctDate:Option[Date] = Some(new Date)

   "Tasks" should{
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
   }
}