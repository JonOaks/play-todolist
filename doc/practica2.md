_Jonatan Diego Poyato Hernández_

Práctica 2
=========

Vamos a crear los tests que prueben las funciones del modelo y del controlador.
Hemos comenzado creando los tests del modelo ***User***
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
