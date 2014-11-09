import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{Json,JsValue,JsArray}
import play.api.libs.Files._

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

    "return task in json format" in {
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

    "return anonymous user tasks in json format" in {
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

    "send 404 on a nonexistent task request" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/tasks/4"))

        status(result) must equalTo(NOT_FOUND)
        contentType(result) must beSome.which(_ == "text/html")
      }
    }

    "send 201 and return task in json format on a task creation" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(POST, "/tasks", FakeHeaders(), Map("label" -> Seq("testing"))))

        status(result) must equalTo(CREATED)
        contentType(result) must beSome.which(_ == "application/json")

        val resultJson: JsValue = contentAsJson(result)
        val resultString = Json.stringify(resultJson) 
 
        resultString must /("label" -> "testing")
        resultString must /("deadline" -> "")
      }
    }

    "send 201 and return task in json format on a task creation with an user login" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(POST, "/McQuack/tasks", FakeHeaders(), Map("label" -> Seq("testing"))))

        status(result) must equalTo(CREATED)
        contentType(result) must beSome.which(_ == "application/json")

        val resultJson: JsValue = contentAsJson(result)
        val resultString = Json.stringify(resultJson) 
 
        resultString must /("label" -> "testing")
        resultString must /("deadline" -> "")

        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        //Hay 3 tareas ya creadas asociadas a mi usuario anónimo
        //como aquí creamos una más, el total es 4
        resultJson2.as[JsArray].value.size must equalTo(4)
      }
    }

    "send 404 on a nonexistent user task list request" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/Fake_user/tasks"))

        status(result) must equalTo(NOT_FOUND)
        contentType(result) must beSome.which(_ == "text/html")
      }
    }
  }
}
