_Jonatan Diego Poyato Hernández_
Práctica 1
=========

En esta primera iteración vamos a hacer una primera iteración en la que convertiremos la aplicación en una API REST que trabaja con objetos JSON y añadiremos algunos datos adicionales que las tareas gestiona la API: usuario que crea la tarea y fecha opcional de finalización.
Feature 1
----

####Consulta de una tarea
Devuelve la representación JSON de la tarea cuyo identificador se pasa por la URI
```
/tasks/id
```
######Explicación
Primeramente hemos añadido la ruta correspondiente a la consulta de una tarea:
```
GET /tasks/$id<[0-9]+>  controllers.Application.getTask(id: Long)
```
Tras esto, hemos implementado la acción asociada al nuevo método consultaTarea:
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
/tasks
```
######Explicación
La ruta referente a la creación de una nueva tarea ya fue añadida en la práctica introductoria:
```
POST     /tasks                     controllers.Application.newTask
```
Lo que hemos hecho ha sido modificar tanto el método create de la clase Task para que además de insertar una nueva tarea en la base de datos nos devuelva su id:
```
   def create(label: String): Task = {
      var newid = 0L
      DB.withConnection { implicit c => newid = SQL("insert into task (label) values ({label})").on('label -> label).executeInsert().get
      }
      
      return Task(newid,label)
   }
```
como el método newTask de la clase Application para que devuelva el JSON con la descripción de la nueva tarea creada
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