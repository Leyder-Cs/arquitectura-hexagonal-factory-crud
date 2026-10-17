### Nota sobre la Edición de Productos en el Frontend ###################3

Al hacer clic en el botón **"Editar"** de cualquier producto en la tabla (tanto en MySQL como en MongoDB):

- Los datos del producto se cargan automáticamente en el **formulario principal ubicado en la parte superior de la página** (el título del formulario cambia a `Editar producto #ID`).
- Si estás viendo la lista o los filtros más abajo en la pantalla, simplemente **sube al formulario principal** en la parte superior para modificar los campos (`Nombre`, `Precio`, `Descripción`) y presionar **"Guardar cambios"** (o **"Cancelar"**).

# #########GUÍA DE EJECUCIÓN DEL PROYECTO

### 1. Requisitos Previos

- **MySQL** corriendo en el puerto `3306`.
- **MongoDB** corriendo en el puerto `27017`.
- **Node.js** (versión 18 o superior) y **npm**.

---

### 2. Preparación de Bases de Datos

#### A. MySQL

1. Abre tu gestor de base de datos preferido (MySQL Workbench, phpMyAdmin, DBeaver, etc.).
2. Ejecuta el script ubicado en:
   ```text
   Spring_boot_crud/dbs/productoscruddb.sql
   ```
   _(Crea la base de datos `productoscruddb`, la tabla `products` y los registros iniciales)._

#### B. MongoDB

1. Abre MongoDB Compass o la terminal `mongosh`.
2. Conéctate a `mongodb://localhost:27017`.
3. Ejecuta el script ubicado en:
   ```text
   Spring_boot_crud/dbs/productoscruddb.js
   ```
   _(Inserta los productos iniciales y configura la colección `counters` con el último ID utilizado)._

---

### 3. Ejecución del Backend (Spring Boot)

1. Abre una terminal en la carpeta `Spring_boot_crud`:
   ```powershell
   cd Spring_boot_crud
   ```
2. _(Opcional)_ Si tu usuario o contraseña de MySQL no son `root` y vacía, configúralas en `src/main/resources/application.properties` o mediante variables de entorno:
   ```powershell
   $env:DB_USER = "root"
   $env:DB_PASSWORD = "TU_PASSWORD"
   ```
3. Inicia la aplicación con el wrapper de Maven:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. El backend estará disponible en: **`http://localhost:8081`**

---

### 4. Ejecución del Frontend (React + Vite)

1. Abre otra terminal en la carpeta `react_crud`:
   ```powershell
   cd react_crud
   ```
2. Instala las dependencias:
   ```powershell
   npm install
   ```
3. Inicia el servidor de desarrollo:
   ```powershell
   npm run dev
   ```
4. Abre en tu navegador la URL que indique la consola (usualmente **`http://127.0.0.1:5173/`**).

---

### 5. Rutas de la API (Endpoints)

- **CRUD SQL (MySQL):** `http://localhost:8081/productos`
- **CRUD NoSQL (MongoDB):** `http://localhost:8081/nosql/productos`
- **Filtros con Agregación (MongoDB):**
  - Precio mínimo: `GET /nosql/productos/filtros/precio-minimo?minimo=50000`
  - Más caros: `GET /nosql/productos/filtros/mas-caros?limite=3`
  - Más baratos: `GET /nosql/productos/filtros/mas-baratos?limite=3`
  - Estadísticas: `GET /nosql/productos/filtros/estadisticas`

### RESUMEN DE LO QHE SE REALIZO

## PASO 1

Primero se agrego la dependecia de mongoDB en el archivo pom.xml

## PASO 2

Luego se agrego la conexion con la base de datos mongoDb en aplication.properties

## PASO 3

Se creo la persistencia Mongo

Se creo la clase de ProductRepositoryMongo en infrestructure/persistence/mongodb que hereda de la clases asbtracta ProductRepository
Se implementaron los 5 metodos del contrato sin cambiar el contrato ni el servicio, como el contrato usa id de tipo int y mongo no lo autoincrementa, se usa una colección de counters con findAndModify para generar el siguiente id. y los productos se guardan en la colección products.

## PASO 4

Se registraron los do servicios en app.java, al existir dos clases que heredan de ProductRepository, springt boot no sabria cual inyectar entonces se cambio el bean por uno para cada persistencia.

## PASO 5

Se crearon los controladores

Al productController que ya existia se le indico con @Qualifier que use el servicio SQL
Se creó ProducMongoController, que es copia del anterior, con otra ruta y servicio de mongo

## PASO 6

Realice prueba del CRUD con Mysql y MongoDb, se ejecuto la aplicación en el puerto 8081 y se probaron en postman los 5 endpoints de cada ruta GET listar todos, GET por id, POST, PUT Y DELETE y se verificaron los datos en PhpMyAdmin y DBgate

## PASO 7

filtros de agregación
7.1 Se creó domain/model/ResultadoFiltro.java, un record con dos campos: resultados (lista de mapas con lo que devuelve el pipeline) y pipeline (el texto de la consulta ejecutada). Va en domain.model porque lo usa el contrato, y el contrato no puede depender de infrastructure. Se usan mapas y no Product porque los pipelines con $project y $group no devuelven productos completos.
7.2 Se agregaron 4 métodos al contrato ProductRepository: listarPorPrecioMinimo, obtenerEstadisticasPrecio, listarMasCaros y listarMasBaratos. No se declararon abstract, para no obligar a ProductRepositoryMySQL a implementarlos: por defecto lanzan UnsupportedOperationException y solo Mongo los sobrescribe. Se importó ResultadoFiltro del dominio.
7.3 En productRepositoryMongo se escrbienron 4 metodos con los pipelines de agtegación
7.4 Se agregaron los 4 metodos que spolo delegan al contrado como el CRUD, asi que el servicio no sabe lo que hay detras
7.5 En productMongoController se agregaron los 4 enpoins que llaman al servicio, estos el controller de SQL no los tiene
7.6 Se probaron los 4 endpoints en postamn y cada uno devolvio los resultados correspondientes a la consulta.

## PASO 8

por ultimo se implementeto todo en el frontend con una interfaz de mongo y sus filtros. separamos el crud de sql con un boton para usar mongo. con su respectivo endpoint y frontend, pruebas satisfactorias.
