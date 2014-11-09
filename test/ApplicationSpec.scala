import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{Json,JsValue,JsArray}

import controllers.Application
import models.Task

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with JsonMatchers {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)
      redirectLocation(home) must beSome("/tasks")
    }

    "return task json format" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val task = Task.create("prueba")
        val Some(resultTask) = route(FakeRequest(GET, "/tasks/"+task.id))
 
        status(resultTask) must equalTo(OK)
        contentType(resultTask) must beSome.which(_ == "application/json")
 
        val resultJson: JsValue = contentAsJson(resultTask)
        val resultString = Json.stringify(resultJson) 
 
        resultString must /("id" -> task.id)
        resultString must /("label" -> "prueba")
        resultString must /("deadline" -> "")
      }
    }

    "return anonymous user tasks json format" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(resultTasks) = route(FakeRequest(GET, "/tasks"))

        status(resultTasks) must equalTo(OK)
        contentType(resultTasks) must beSome.which(_ == "application/json")

        /* El resultado de la petición es una colección JSON
        ** con la lista de tareas de mi usuario anónimo ("McQuack")
        */
        val resultJson: JsValue= contentAsJson(resultTasks)
        //Como quiero contar el número de elementos en el JsValue
        //lo mapeo en un JsArray y verifico su tamaño
        resultJson.as[JsArray].value.size must equalTo(3)
      }
    }
  }
}
