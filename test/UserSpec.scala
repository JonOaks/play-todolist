package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.User

class UserSpec extends Specification {
   "Models" should {
      "find user" in {
         running(FakeApplication()) {
            // La BD contiene dos usarios ("McQuack" y "Jonatan")
            // Comprobamos que existe uno de estos dos
            val user = User.existUser("McQuack")
            user must beSome
         }
      }
      "not find user" in {
         running(FakeApplication()) {
            val user = User.existUser("Prueba")
            user must beNone
         }
      }
   }
}