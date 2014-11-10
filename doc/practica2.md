_Jonatan Diego Poyato Hernández_

Práctica 2
=========

Vamos a crear los tests que prueban las funciones del modelo y del controlador.
Hemos comenzado creando los tests del modelo ***User***:

```
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
```

Seguidamente los tests del modelo ***Task***. Hay que resaltar los métodos en los que la invocación de estos provocan una excepción. Estos son: createWithUser(String,String), createWithUserAndDate(String,String,Option[Date]):

```
      "throw JbdcSQLException in a task creation with a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            Task.createWithUser("task","Fake_user") must throwA[JdbcSQLException]
         }
      }

      "throw JbdcSQLException in a task creation with date and a nonexistent user" in {
         running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
            //Usuario no existente
            Task.createWithUserAndDate("task","Fake_user",correctDate) must throwA[JdbcSQLException]
         }
      }
```

El resto de los tests del modelo ***Task*** no esconden gran misterio. Con ellos probamos todas las combinaciones posibles a la hora de crear, borrar o consultar tareas de la base de datos y de este modo verificamos el comportamiento de la misma.

A continuación hemos creado los tests correspondientes a nuestro controlador. Vamos a repasar aquellos en los que hemos tenido que lidiar con alguna dificultad.

Al consultar cualquier tarea, ya sea mediante el método sin usuario como con el que si lo especficamos, nuestro controlador debe devolvernos la misma en formato JSON. Este es el ejemplo en el que si se especifica el usuario:

```
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
```

Así mismo, también hemos comprobado que la consulta fallida, al pedir una tarea inexistente, debe enviar un 404 y devolver nuestro html "customizado":

```
    "send 404 on a nonexistent task request" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(result) = route(FakeRequest(GET, "/tasks/4"))

        status(result) must equalTo(NOT_FOUND)
        contentType(result) must beSome.which(_ == "text/html")
      }
    }
```

A la hora de crear una tarea, hemos realizado el enviado del form-data en una FakeRequest. Lo hemos hecho de la siguiente forma:

```
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
```

En el test anterior no hemos comprobado que se ha creado correctamente ya que no hemos vuelto a pedir todas las tareas asociadas a nuestro usuario anónimo ("McQuack") porque no lo hemos visto necesario, pero a partir del siguiente test si que lo hemos comprobado en prácticamente todos. El siguiente test  es similar al anterior salvando que si hemos especificado el usuario creador de la tarea:

```
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
```

Cuando intentamos crear una tarea con una fecha incorrecta nuestro controlador debe devolver un error 400 (BAD_REQUEST):

```
    "send 400 on a task creation with an incorrect date" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        //Lo pruebo tanto con usuario como sin él
        val Some(result) = route(FakeRequest(POST, "/McQuack/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("1212-2014"))))
        val Some(result2) = route(FakeRequest(POST, "/tasks", FakeHeaders(), Map("label" -> Seq("testing"), "deadline" -> Seq("1212-2014"))))

        status(result) must equalTo(BAD_REQUEST)
        status(result2) must equalTo(BAD_REQUEST)
      }
    }
```

Respecto al borrado, hemos realizado la comprobación extra (cuando decimos extra no queremos decir no necesario, ya que en estos casos si lo es para corroborar las acciones pertinentes) de la que hablabamos anteriormente en las funcionalidades "especiales" (de la práctica 1) de las fechas. Los tests asociados al borrado simple son los siguientes:

```
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
```

Y los tests asociados al borrado que se realizan en las funcionalidades "especiales" son estos:

```
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
```

Nueva feature usando TDD(Categorías)
----

Hemos añadido una nueva característica usando TDD: **categorías**. Hemos ampliado el API para que se puedan crear categorías asociadas a un usuario y que se puedan añadir, modificar y listar las tareas de un usuario dentro de una determinada categoría.

Lo primero que hemos hecho ha sido crear una nueva evolución en la que hemos creado tres tablas, una para almacenar las categorías y dos más que representan las relaciones "muchos a muchos" existentes entre categoría y tarea y entre categoría y usuario, y hemos hecho algunas inserciones para probar la nueva funcionalidad.

```
CREATE SEQUENCE category_id_seq;
CREATE TABLE category (
      id integer NOT NULL DEFAULT nextval('category_id_seq'),
      category varchar(255) NOT NULL,
      constraint pk_category PRIMARY KEY (category)
);

CREATE SEQUENCE user_category_id_seq;
CREATE TABLE user_category (
      id integer NOT NULL DEFAULT nextval('user_category_id_seq'),
      login varchar(255) NOT NULL,
      category varchar(255) NOT NULL,
      constraint pk_user_category PRIMARY KEY (login,category)
);

CREATE SEQUENCE task_category_id_seq;
CREATE TABLE task_category (
      id integer NOT NULL DEFAULT nextval('task_category_id_seq'),
      task_id varchar(255) NOT NULL,
      category varchar(255) NOT NULL,
      constraint pk_task_category PRIMARY KEY(task_id,category)
);

ALTER TABLE user_category ADD constraint fk_user_category_task_user FOREIGN KEY (login) REFERENCES task_user (login) ON DELETE restrict ON UPDATE restrict;
ALTER TABLE user_category ADD constraint fk_user_category_category FOREIGN KEY (category) REFERENCES category (category) ON DELETE restrict ON UPDATE restrict;

ALTER TABLE task_category ADD constraint fk_task_category_task FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE restrict ON UPDATE restrict;
ALTER TABLE task_category ADD constraint fk_task_category_category FOREIGN KEY (category) REFERENCES category (category) ON DELETE restrict ON UPDATE restrict;
```

Seguidamente, lo que hemos hecho ha sido crear todos los tests que comprueban la funcionalidad que queríamos añadir al sistema. Como es lógico, fallaban. Tras crearlos todos hemos escrito un código que hacía que funcionasen para después refactorizar el código de la nueva funcionalidad tanto en controlador como en el modelo.

Destacar un par de métodos del modelo en los que contabamos las filas obtenidas tras realizar una query a nuestra base de datos:

```
   def taskBelongToUser(id: Long, login: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from task where id = {id} and author_login = {login}").on('login -> login).on('id -> id).as(scalar[Long].single)
      }

      return cantidad
   }

   def taskBelongToCategory(task_id: Long, category: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from task_category where task_id = {task_id} and category = {category}").on('category -> category).on('task_id -> task_id).as(scalar[Long].single)
      }

      return cantidad
   }
```

También hemos creado el fichero ***Category.scala*** el cual representa el modelo categoría:

```
case class Category(id: Long, category: String)

object Category {

   val category = {
      get[Long]("id") ~ 
      get[String]("category") map {
         case id~category => Category(id, category)
      }
   }

   def existCategory(category_name: String): Option[Category] = DB.withConnection{
      implicit c => SQL("select * from category where category = {category_name}").on('category_name -> category_name).as(category.singleOpt)
   }

   def categoryBelongToUser(category_name: String, login: String): Long = {
      var cantidad = 0L
      val rowParser = scalar[Long]
      val rsParser = scalar[Long].single

      DB.withConnection {implicit c => cantidad = SQL("select count(*) from user_category where category = {category_name} and login = {login}").on('login -> login).on('category_name -> category_name).as(scalar[Long].single)
      }

      return cantidad
   }
}
```

En él definimos la clase y algunos métodos asociados a la misma que hemos necesitado en el controlador.

De esta forma, la nueva característica queda añadida.

