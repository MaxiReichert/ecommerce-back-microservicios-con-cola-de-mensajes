# ecommerce-back-microservicios

Backend de una aplicación de ecommerce (tienda de comida) implementado como arquitectura de **microservicios** con **Java 17** y **Spring Boot 3.5.14**. Cada microservicio es una aplicación independiente con su propia base de datos H2 en memoria.

---

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
  - [1. Instalar Java 17](#1-instalar-java-17)
  - [2. Instalar Maven](#2-instalar-maven)
- [Configurar RabbitMQ](#configurar-rabbitmq)
  - [Opción A — CloudAMQP (nube)](#opción-a--cloudamqp-nube)
  - [Opción B — Docker local](#opción-b--docker-local)
- [Levantar los microservicios](#levantar-los-microservicios)
- [Bases de datos H2](#bases-de-datos-h2)

---

## Arquitectura

```
┌─────────────────────────────────────────────────────┐
│              Gateway  :8080                         │
│  /api/productos/**  →  ms-producto                  │
│  /api/pagos/**      →  ms-pago                      │
│  /api/descuentos/** →  ms-pago                      │
│  /api/pedidos/**    →  ms-pedido                    │
└──────────┬──────────┬──────────────┬────────────────┘
           │          │              │
     :8081 │    :8082 │        :8083 │
    ms-producto   ms-pago       ms-pedido
    (H2: productodb) (H2: pagodb) (H2: pedidodb)
```

Cada microservicio corre en su propio proceso y expone su propia API REST. El **Gateway** es el único punto de entrada desde el frontend (Angular en `localhost:4200`).

---

## Tecnologías

| Componente | Tecnología | Puerto |
|---|---|---|
| Gateway | Spring Cloud Gateway 3.5.4 | 8080 |
| ms-producto | Spring Boot 3.5.14 + H2 | 8081 |
| ms-pago | Spring Boot 3.5.14 + H2 | 8082 |
| ms-pedido | Spring Boot 3.5.14 + H2 | 8083 |
| Cola de mensajes | RabbitMQ (CloudAMQP o Docker) | 5672 |

---

## Requisitos previos

### 1. Instalar Java 17

> **Verificar si ya lo tenés instalado** (PowerShell o CMD):
> ```powershell
> java -version
> ```
> Si la salida muestra `openjdk 17` o `java version "17"`, podés saltar este paso.

1. Ir a [https://openjdk.org/install/](https://openjdk.org/install/) y descargar el `.zip` de **Java 17** para **Windows x64**.
2. Descomprimir el `.zip` en una carpeta fija, por ejemplo:
   ```
   C:\java\jdk-17
   ```
3. Configurar las variables de entorno:
   - Abrir el menú inicio y buscar **"Editar las variables de entorno del sistema"**.
   - Hacer clic en **"Variables de entorno..."**.
   - En la sección **Variables del sistema**, hacer clic en **"Nueva..."** y completar:
     - Nombre: `JAVA_HOME`
     - Valor: `C:\java\jdk-17`
   - Luego, seleccionar la variable **`Path`**, hacer clic en **"Editar..."** y agregar una nueva entrada:
     - `%JAVA_HOME%\bin`
4. Aceptar todos los cuadros de diálogo, **abrir una nueva terminal** y verificar:
   ```powershell
   java -version
   # Esperado: openjdk version "17.x.x" ...
   ```

---

### 2. Instalar Maven

> Cada microservicio incluye el wrapper **`mvnw.cmd`**, por lo que **Maven no es obligatorio** — podés usarlo directamente (ver [Levantar los microservicios](#levantar-los-microservicios)).  
> Si preferís tener Maven instalado globalmente, seguí estos pasos:

> **Verificar si ya lo tenés instalado** (PowerShell o CMD):
> ```powershell
> mvn -version
> ```

1. Ir a [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Descargar el binario `apache-maven-3.x.x-bin.zip`.
3. Descomprimir en una carpeta fija, por ejemplo: `C:\tools\maven`.
4. Agregar `C:\tools\maven\bin` a la variable de entorno `Path`:
   - Buscar **"Variables de entorno"** en el menú inicio.
   - En **Variables del sistema**, editar `Path` y agregar la ruta.
5. Reiniciar la terminal y verificar:
   ```powershell
   mvn -version
   # Esperado: Apache Maven 3.x.x ...
   ```

---

## Configurar RabbitMQ

Todos los microservicios se conectan a un broker RabbitMQ para la comunicación asíncrona (descuento de stock, resultado de pago, etc.). Tenés dos opciones:

### Opción A — CloudAMQP (nube)

1. Crear una cuenta gratuita en [https://www.cloudamqp.com](https://www.cloudamqp.com).
2. Crear una nueva instancia → elegir el plan **Little Lemur** (gratuito).
3. Desde el dashboard de la instancia, copiar la **AMQP URL**, que tiene el formato:
   ```
   amqps://USUARIO:PASSWORD@HOST.rmq.cloudamqp.com/USUARIO
   ```
4. En cada microservicio (`ms-producto`, `ms-pago`, `ms-pedido`), abrir:
   ```
   src/main/resources/application.properties
   ```
   Y reemplazar el placeholder con tu URL:
   ```properties
   spring.rabbitmq.addresses=amqps://TU_USUARIO:TU_PASSWORD@TU_HOST.rmq.cloudamqp.com/TU_USUARIO
   ```

---

### Opción B — Docker local

Si preferís correr el broker localmente sin crear una cuenta externa:

```powershell
docker run -d `
  --name rabbitmq `
  -p 5672:5672 `
  -p 15672:15672 `
  rabbitmq:3-management
```

Esto levanta RabbitMQ con:
- **Puerto 5672** → conexión AMQP usada por los microservicios.
- **Puerto 15672** → panel de administración web en [http://localhost:15672](http://localhost:15672) (usuario: `guest`, contraseña: `guest`).

Luego, en cada `application.properties`, **comentá** la línea `addresses` y agregá las propiedades individuales:

```properties
# Comentar esta línea:
# spring.rabbitmq.addresses=amqps://TU_USUARIO:TU_PASSWORD@TU_HOST.rmq.cloudamqp.com/TU_USUARIO

# Agregar estas en su lugar:
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

> **Nota:** `spring.rabbitmq.addresses` tiene precedencia sobre `host`/`port` individuales. Si usás Docker, la línea `addresses` debe estar comentada o eliminada.

---

## Levantar los microservicios

> **Importante:** asegurate de que RabbitMQ esté corriendo **antes** de iniciar los microservicios (ver [Configurar RabbitMQ](#configurar-rabbitmq)).

> Cada microservicio es un proyecto independiente. Hay que abrir una terminal **por cada uno** y ejecutar el comando desde su carpeta raíz. Se recomienda levantarlos en el siguiente orden.

### 1 — ms-producto (puerto 8081)

```powershell
cd ms-producto
.\mvnw.cmd spring-boot:run
```

### 2 — ms-pago (puerto 8082)

```powershell
cd ms-pago
.\mvnw.cmd spring-boot:run
```

### 3 — ms-pedido (puerto 8083)

```powershell
cd ms-pedido
.\mvnw.cmd spring-boot:run
```

### 4 — Gateway (puerto 8080)

```powershell
cd gateway
.\mvnw.cmd spring-boot:run
```

Una vez que los cuatro estén corriendo, el punto de entrada principal es:

```
http://localhost:8080
```

---

## Bases de datos H2

Cada microservicio tiene su propia base de datos en memoria, independiente de las demás. Los datos se crean al iniciar y se pierden al apagar cada servicio.

La consola web de H2 está habilitada en cada uno:

| Microservicio | Consola H2 | JDBC URL |
|---|---|---|
| ms-producto | http://localhost:8081/h2-console | `jdbc:h2:mem:productodb` |
| ms-pago | http://localhost:8082/h2-console | `jdbc:h2:mem:pagodb` |
| ms-pedido | http://localhost:8083/h2-console | `jdbc:h2:mem:pedidodb` |

En todos los casos:
- **Usuario:** `sa`
- **Contraseña:** *(vacía)*