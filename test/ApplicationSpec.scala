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

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        //Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        //Como justo antes hemos creado una más, el total es 4
        resultJson2.as[JsArray].value.size must equalTo(4)
      }
    }

    "send 404 on a nonexistent user's task list request" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/Fake_user/tasks"))

        status(result) must equalTo(NOT_FOUND)
        //Como devuelvo un html compruebo que es así
        contentType(result) must beSome.which(_ == "text/html")
      }
    }

    "send 400 on a task creation with an incorrect date" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        //Lo pruebo tanto con usuario como sin él
        val Some(result) = route(FakeRequest(POST, "/McQuack/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("1212-2014"))))
        val Some(result2) = route(FakeRequest(POST, "/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("1212-2014"))))

        status(result) must equalTo(BAD_REQUEST)
        status(result2) must equalTo(BAD_REQUEST)
      }
    }

    "send 201 and return task in json format on a task creation with or without an user login and a correct date" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(POST, "/McQuack/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("14-10-1990"))))
        val Some(result2) = route(FakeRequest(POST, "/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("14-10-1990"))))

        status(result) must equalTo(CREATED)
        contentType(result) must beSome.which(_ == "application/json")

        val resultJson: JsValue = contentAsJson(result)
        val resultString = Json.stringify(resultJson) 
 
        resultString must /("label" -> "testing")
        resultString must /("deadline" -> "14-10-1990")

        status(result2) must equalTo(CREATED)
        contentType(result2) must beSome.which(_ == "application/json")

        val resultJson2: JsValue = contentAsJson(result2)
        val resultString2 = Json.stringify(resultJson2)

        resultString2 must /("label" -> "testing")
        resultString2 must /("deadline" -> "14-10-1990") 
      }
    }

    "return OK deleting a task" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(DELETE, "/tasks/3"))

        status(result) must equalTo(OK)
      }
    }

    "send 404 deleting an nonexistent task" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(DELETE, "/tasks/4"))

        status(result) must equalTo(NOT_FOUND)
      }
    }

    "return OK deleting tasks with the same date passed by url" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/tasks/05-11-2014"))

        status(result) must equalTo(OK)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Como justo antes hemos borrado las que tienen como fecha de finalización 05-11-2014
        **y solo había una, ahora el total debe ser 2 (iniciamos la base de datos con 3 asociadas
        **a nuestro usuario anónimo)
        */
        resultJson2.as[JsArray].value.size must equalTo(2)
      }
    }

    "send 400 deleting tasks with the same date passed by url, that is incorrect" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/tasks/0511-2014"))

        status(result) must equalTo(BAD_REQUEST)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Hemos intentado borrar las que tienen como fecha de finalización 0511-2014.
        **Como no se borra ninguna al ser una petición errónea, el total debe ser 3
        */
        resultJson2.as[JsArray].value.size must equalTo(3)
      }
    }

    "return OK trying to deleting tasks with the same date passed by url" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/tasks/06-11-2014"))

        status(result) must equalTo(OK)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Hemos intentado borrar las que tienen como fecha de finalización 06-11-2014.
        **Como no hay ninguna, el total debe seguir siendo 3
        */
        resultJson2.as[JsArray].value.size must equalTo(3)
      }
    }

    "return OK deleting tasks with the date before than the date passed by url" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/McQuack/tasks/06-11-2014"))

        status(result) must equalTo(OK)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Como justo antes hemos borrado las que tienen como fecha de finalización
        **una fecha anterior a 06-11-2014 y solo había una,
        **ahora el total debe ser 2 (iniciamos la base de datos con 3 asociadas a nuestro usuario anónimo)
        */
        resultJson2.as[JsArray].value.size must equalTo(2)
      }
    }

    "send 400 deleting tasks with the date before than the date passed by url, that is incorrect" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/McQuack/tasks/0611-2014"))

        status(result) must equalTo(BAD_REQUEST)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Hemos intentado borrar las que tienen como fecha de finalización una anterior a 0611-2014.
        **Como no se borra ninguna al ser una petición errónea, el total debe ser 3
        */
        resultJson2.as[JsArray].value.size must equalTo(3)
      }
    }

    "return OK trying to deleting tasks with a date before than the date passed by url" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/McQuack/tasks/04-11-2014"))

        status(result) must equalTo(OK)

        //Comprobación extra para ver si está todo correcto
        val Some(result2) = route(FakeRequest(GET, "/McQuack/tasks"))
        status(result2) must equalTo(OK)
        contentType(result2) must beSome.which(_ == "application/json")
        val resultJson2: JsValue = contentAsJson(result2)
        /*Hay 3 tareas ya creadas asociadas a mi usuario anónimo.
        **Hemos intentado borrar las que tienen como fecha de finalización una anterior a 06-11-2014.
        **Como no hay ninguna, el total debe seguir siendo 3
        */
        resultJson2.as[JsArray].value.size must equalTo(3)
      }
    }
  }
}
