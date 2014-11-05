_Jonatan Diego Poyato Hernández_

Práctica 2
=========

Vamos a crear los tests que prueben las funciones del modelo y del controlador.
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
Seguidamente los tests del modelo ***Task***. Hay que resaltar los métodos en los que la invocación de estos provoca una excepción. Estos son: createWithUser(String,String), createWithUserAndDate(String,String,Option[Date]):

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