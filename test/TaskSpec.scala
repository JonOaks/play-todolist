package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.Task

class TaskSpec extends Specification {
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
   }
}