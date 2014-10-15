_Jonatan Diego Poyato Hernández_

Práctica 1
=========

En esta primera iteración vamos a hacer una primera iteración en la que convertiremos la aplicación en una API REST que trabaja con objetos JSON y añadiremos algunos datos adicionales que las tareas gestiona la API: usuario que crea la tarea y fecha opcional de finalización.

Feature 1
----

####Consulta de una tarea
Devuelve la representación JSON de la tarea cuyo identificador se pasa por la URI
```
GET /tasks/id
```

######Explicación
Primeramente hemos añadido la ruta correspondiente a la consulta de una tarea:
```
GET /tasks/$id<[0-9]+>  controllers.Application.getTask(id: Long)
```
Y definido nuestro propio conversor implícito:
```
   implicit val taskWrites = new Writes[Task] {
      def writes(task: Task) = Json.obj (
         "id" -> task.id,
         "label" -> task.label
         )
   }
```
Tras esto, hemos implementado la acción asociada al nuevo método ***consultaTarea***:
```
   def getTask(id: Long) = Action {
      Task.task(id) match {
         case None => NotFound(html).as("text/html")
         case Some(t) => Ok(Json.toJson(t))
      }
   }
```
Y la query correspondiente a la consulta de una tarea:
```
   def task(id: Long): Option[Task] = DB.withConnection{
      implicit c => SQL("select * from task where id = {id}").on('id -> id).as(task.singleOpt)
   }
```

####Creación de nueva tarea
Recibe el dato de la nueva tarea a crear (su descripción) en un formulario. Devuelve un JSON con la descripción de la nueva tarea creada y el código HTTP 201 (CREATED).

```
POST /tasks
```

######Explicación
La ruta referente a la creación de una nueva tarea ya fue añadida en la práctica introductoria:
```
POST     /tasks                     controllers.Application.newTask
```
Lo que hemos hecho ha sido modificar, tanto el método ***create*** de la clase ***Task*** para que además de insertar una nueva tarea en la base de datos nos devuelva su id:
```
   def create(label: String): Task = {
      var newid = 0L
      DB.withConnection { implicit c => newid = SQL("insert into task (label) values ({label})").on('label -> label).executeInsert().get
      }
      
      return Task(newid,label)
   }
```
como el método ***newTask*** de la clase ***Application*** para que devuelva el JSON con la descripción de la nueva tarea creada:
```
   def newTask = Action { 
      implicit request => taskForm.bindFromRequest.fold(
         errors => BadRequest(views.html.index(Task.all(), errors)),
         label => {
            val task = Task.create(label)
            Created(Json.toJson(task))
         }
      )
   }
```

####Listado de tareas
Devuelve una colección JSON con la lista de tareas
```
GET /tasks
```
Respuesta (ejemplo en el que tenemos dos tareas ya creadas):
```
[{"id":1,"label":"testing"},{"id":2,"label":"testing2"}]
```

######Explicación
Ya disponemos de la ruta correspondiente para resolver esta petición (fue añadida en la práctica anterior) y, por lo tanto, y aún pudiendo hacer uso del conversor implícito visto anteriormente, lo que hemos hecho ha sido modificar nuestro conversor implícito usando el _patrón combinator_ por mero interés educativo.
```
   implicit val taskWrites: Writes[Task] = (
      (JsPath \ "id").write[Long] and
      (JsPath \ "label").write[String]
   )(unlift(Task.unapply))
```
y el método ***tasks***, sustituyendo el formulario por la respuesta esperada en esta práctica (colección JSON con la lista de tareas):
```
   def tasks = Action {
      val json = Json.toJson(Task.all())
      Ok(json)
   }
```

####Borrado de una tarea
Borra una tarea cuyo identificador se pasa en la URI. Si la tarea no existe se debe devolver un código HTTP 404 (NOT FOUND).
```
DELETE /tasks/id
```

######Explicación
Hemos modificado la ruta haciendo caso a la nueva especificación quedando esta misma del siguiente modo:
```
DELETE   /tasks/:id                 controllers.Application.deleteTask(id: Long)
```
Y además también hemos modificado el código del método ***deleteTask*** de la clase ***Application*** (hemos hecho que el método ***delete*** de la clase ***Task*** devuelva un long para poder comprobar si se ha encontrado y borrado la tarea)
```
   def deleteTask(id: Long) = Action {
      if(Task.delete(id) > 0)
      {
         Ok
      }
      else
      {
         NotFound(html_404).as("text/html")
      }
   }
```

Feature 2
----
Ahora podemos, tanto listar las tareas asociadas a un usuario concreto
```
GET      /:login/tasks              controllers.Application.getTasksUser(login: String)
```
como añadir una nueva
```
POST     /:login/tasks              controllers.Application.newTaskUser(login: String)
```

######Explicación

En esta segunda feature hemos añadido el login referente al usuario creador de la tarea. Primeramente hemos añadido una nueva evolución (2.sql) en la que hemos modificado la tabla task y hemos añadido una nueva tabla para almacenar el login de los usuarios:
```
# Add User
 
# --- !Ups

ALTER TABLE task ADD author_login VARCHAR(255);

CREATE SEQUENCE task_user_id_seq;
CREATE TABLE task_user (
      id integer NOT NULL DEFAULT nextval('task_user_id_seq'),
      login varchar(255) NOT NULL,
      constraint pk_task_user PRIMARY KEY (login)

);

ALTER TABLE task ADD constraint fk_task_task_user_1 FOREIGN KEY (author_login) REFERENCES task_user (login) ON DELETE restrict ON UPDATE restrict;

INSERT into task_user (login) values ('McQuack');
INSERT into task_user (login) values ('Jonatan');

INSERT into task (label,author_login) values ('Launchpad','McQuack');
INSERT into task (label,author_login) values ('Threshold','McQuack');

# --- !Downs
ALTER TABLE task DROP author_login;

DROP TABLE task_user;
DROP SEQUENCE task_user_id_seq;
```
Hemos insertado dos usuarios, McQuack (identificador usuario anónimo)  y Jonatan, así como algunas tareas asociadas al usuario identificado como anónimo.

Seguidamente hemos creado dos métodos nuevos en modelo (***getTasks*** y ***newTasksUser***) y controlador(***tasks*** y ***createWithUser***) para añadir las dos nuevas funcionalidades y modificado las funciones anteriores para que sean compatibles con la nueva, es decir, apra que sea compatible con la funcionalidad "Usuario creador de la tabla".

Feature 3
----
Gracias a los cambios realizados ahora podemos, o no, añadir fechas de terminación en las tareas. Además hemos añadidod dos nuevas funcionalidades:

- Dada una fecha concreta se borraran todas las tareas con fecha de terminación igual

```
GET      /tasks/:date               controllers.Application.deleteTasksSameDate(date: String)

```

- Dado un determinado usuario y una fecha se borraran todas las tareas del usuario mencionado con fecha de terminación anterior a la fecha dada

```
GET      /:login/tasks/:date        controllers.Application.deleteTasksUserBeforeDate(login: String, date: String)
```

######Explicación

Hemos añadido las dos rutas anteriores en nuestro archivo de rutas e implementado los métodos correspodientes en el controlador y, por consiguiente, dos nuevos métodos en el modelo.
Además hemos implementado un nuevo método en el controlador para comprobar si lo que se nos pasa es una fecha correcta mediante el uso de expreiones regulares
```
   def okDate(date: String): Boolean = {
      val pat = Pattern compile("(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)[0-9][0-9]")
      val mat = pat.matcher(date)
      if(mat.matches())
      {
         true
      }
      else
      {
         false
      }
   }
```

Lo primero que hacemos en los nuevos métodos (las dos nuevas funcionalidades) del controlador es realizar esta comprobación. Si la fecha es correcta, la parseamos de la siguiente forma (esto lo hacemos para disponer la fecha de tal forma que no haya problemas de tipos):
```
      if(okDate(date_to_delete))
      {
         val a = new SimpleDateFormat("dd-MM-yyyy")
         val b: Date = a.parse(date_to_delete)
         val to_delete: Option[Date] = Some(b)
         
         .
         .
         .
      }
```
En el caso de que la fecha introducida no sea correcta devolvemos un error HTTP (***BadRequest***).