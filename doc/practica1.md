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
GET /tasks/$id<[0-9]+>  controllers.Application.consultaTarea(id: Long)
```
Tras esto, hemos implementado la acción asociada al nuevo método consultaTarea:
```
   def consultaTarea(id: Long) = Action {
      try{
         Ok(Json.toJson(Task.tarea(id)))
      } catch {
         case e: RuntimeException => NotFound(html).as("text/html")
      }
   }
```
Y la query correspondiente a la consulta de una tarea:
```
   def tarea(id: Long): Task = DB.withConnection{
      implicit c => SQL("select * from task where id = {id}").on('id -> id).as(task.single)
   }
```