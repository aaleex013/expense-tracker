# Expense Tracker

API REST para gestionar gastos e ingresos personales. Estoy desarrollando este proyecto para practicar Java y Spring Boot, desde la conexión con la base de datos hasta la autenticación y las reglas de negocio.

Cada usuario puede gestionar sus categorías, movimientos, presupuestos y gastos recurrentes. El proyecto contiene el backend; las peticiones se pueden probar con Swagger, Postman o curl.

## Funcionalidades

- Registro e inicio de sesión con JWT.
- Consulta del perfil del usuario autenticado.
- Categorías de ingresos y gastos, con opción de crear categorías propias.
- Creación, consulta, edición y eliminación de movimientos.
- Listado de movimientos con filtros y paginación.
- Presupuestos mensuales por categoría, con el importe gastado y el restante.
- Gastos recurrentes mensuales y anuales.
- Generación de vencimientos y confirmación de pagos.
- Prevención de vencimientos y pagos duplicados.
- Informe mensual con ingresos, gastos, balance y gastos por categoría.
- Acceso a los datos según el usuario autenticado.

## Tecnologías

- Java 21 y Spring Boot 4.1.2.
- Spring Web MVC, Spring Data JPA y Hibernate.
- Spring Security y JWT.
- PostgreSQL 17 y Flyway.
- Docker Compose.
- Maven Wrapper.
- JUnit y Mockito.
- OpenAPI y Swagger UI.

Las contraseñas se guardan como hashes con PBKDF2. Los tokens JWT se firman con una clave privada RSA y se validan con la clave pública.

## Organización del código

El código está organizado por funcionalidad. Dentro de cada paquete están las clases que necesita esa parte de la aplicación.

```text
com.alejandro.expense_tracker
├── auth          Registro e inicio de sesión
├── user          Usuarios y consulta del perfil
├── category      Categorías
├── movement      Movimientos y filtros
├── budget        Presupuestos mensuales
├── recurring     Gastos recurrentes
│   └── occurrence  Vencimientos y pagos
├── report        Informes
├── security      Configuración de seguridad y JWT
├── config        Configuración general
└── exceptions    Excepciones y respuestas de error
```

El controller recibe la petición, el service aplica las reglas de negocio y el repository accede a la base de datos. Los DTO definen los datos que se reciben y se devuelven en la API.

## Cómo ejecutar el proyecto

Necesitas tener Java 21, Docker con Docker Compose y OpenSSL. No hace falta instalar Maven porque el proyecto incluye Maven Wrapper.

### 1. Descargar el proyecto

```bash
git clone https://github.com/aaleex013/expense-tracker.git
cd expense-tracker
```

Si ya tienes el proyecto en tu ordenador, entra directamente en su carpeta.

### 2. Configurar las variables de entorno

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

Edita `.env` y establece los valores para tu entorno local:

```dotenv
POSTGRES_DB=expense_tracker
POSTGRES_USER=expense_tracker
POSTGRES_PASSWORD=replace_with_local_password
POSTGRES_PORT=5432
```

Sustituye la contraseña de ejemplo por la que quieras utilizar. El archivo `.env` está excluido de Git.

### 3. Crear las claves para JWT

Ejecuta estos comandos desde la raíz del proyecto la primera vez que lo prepares:

```bash
mkdir -p secrets
openssl genpkey -algorithm RSA -out secrets/jwt-private.pem -pkeyopt rsa_keygen_bits:2048
openssl pkey -in secrets/jwt-private.pem -pubout -out secrets/jwt-public.pem
```

La aplicación lee las claves desde la carpeta `secrets`, que también está excluida de Git. Si ya tienes las claves creadas, puedes reutilizarlas.

### 4. Levantar PostgreSQL

Con Docker en ejecución:

```bash
docker compose up -d
docker compose ps
```

El servicio `postgres` debería aparecer como `healthy`. La base de datos estará disponible en `localhost:5432`, salvo que hayas cambiado el puerto.

Los datos se guardan en el volumen `postgres_data`. Cambiar la contraseña en `.env` después de inicializar la base de datos no cambia la contraseña que PostgreSQL ya tiene guardada.

### 5. Arrancar la aplicación

Docker Compose lee `.env`, pero Spring Boot no lo carga automáticamente. En una terminal bash o zsh, puedes cargar las variables antes de arrancar:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Estos comandos suponen que tu archivo `.env` local contiene asignaciones válidas para la shell, como las del ejemplo.

También puedes abrir el proyecto en IntelliJ, configurar el plugin EnvFile para cargar `.env` y ejecutar `ExpenseTrackerApplication`. Usa la raíz del proyecto como directorio de trabajo para que encuentre las claves.

Al arrancar, Flyway aplica las migraciones pendientes. Hibernate comprueba que las entidades coinciden con las tablas mediante `ddl-auto=validate`.

La API se ejecuta en `http://localhost:8080`.

Para detener PostgreSQL conservando los datos:

```bash
docker compose down
```

## Probar la API

La documentación está disponible con la aplicación en ejecución:

- [Swagger UI](http://localhost:8080/swagger-ui.html).
- [Definición OpenAPI](http://localhost:8080/v3/api-docs).

Primero registra un usuario con `POST /api/auth/register`:

```json
{
  "username": "Alejandro",
  "email": "alejandro@example.com",
  "password": "MiPasswordDePrueba123!"
}
```

Después inicia sesión con `POST /api/auth/login`:

```json
{
  "email": "alejandro@example.com",
  "password": "MiPasswordDePrueba123!"
}
```

Copia el campo `accessToken` de la respuesta. En Swagger pulsa **Authorize** y pega solo el token. En Postman selecciona **Bearer Token**. Si utilizas curl, añade esta cabecera:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer TU_TOKEN"
```

El token tiene una duración configurada de 15 minutos. Cuando caduque, vuelve a iniciar sesión.

## Endpoints principales

Salvo el registro y el login, los endpoints de esta tabla necesitan un token válido.

| Método | Ruta | Función |
| --- | --- | --- |
| POST | `/api/auth/register` | Registrar un usuario |
| POST | `/api/auth/login` | Iniciar sesión |
| GET | `/api/users/me` | Consultar mi perfil |
| GET / POST | `/api/categories` | Listar o crear categorías |
| GET / POST | `/api/movements` | Listar o crear movimientos |
| GET / PUT / DELETE | `/api/movements/{id}` | Consultar, editar o borrar un movimiento |
| GET / POST | `/api/budgets` | Consultar o crear presupuestos |
| PATCH / DELETE | `/api/budgets/{id}` | Cambiar el límite o borrar un presupuesto |
| GET / POST | `/api/recurring-expenses` | Listar o crear gastos recurrentes |
| PUT / DELETE | `/api/recurring-expenses/{id}` | Editar o borrar un gasto recurrente |
| PATCH | `/api/recurring-expenses/{id}/active` | Activar o desactivar un gasto recurrente |
| POST | `/api/expense-occurrences/generate` | Generar los vencimientos de un mes |
| GET | `/api/expense-occurrences` | Consultar los vencimientos de un mes |
| POST | `/api/expense-occurrences/{id}/pay` | Confirmar un pago |
| GET | `/api/reports/monthly` | Consultar el informe mensual |

Ejemplo de listado de movimientos con filtros:

```text
GET /api/movements?type=EXPENSE&fromDate=2026-11-01&toDate=2026-11-30&page=0&size=10
```

También se puede filtrar por `categoryId`. Las páginas empiezan en cero y `size` indica cuántos movimientos se solicitan por página. El listado se ordena por fecha descendente y, si coinciden las fechas, por ID descendente.

Los presupuestos, vencimientos e informes utilizan el formato `AAAA-MM` para consultar un mes:

```text
GET /api/budgets?month=2026-11
GET /api/expense-occurrences?month=2026-11
GET /api/reports/monthly?month=2026-11
```

El usuario se obtiene del JWT. No hace falta enviar un `userId` para consultar sus datos.

## Gastos recurrentes y vencimientos

Un gasto recurrente guarda la configuración de un pago que se repite, como una cuota mensual o un seguro anual. Un vencimiento representa un pago concreto de ese gasto.

Por ejemplo, la cuota de la moto puede repetirse cada día 2. Al generar diciembre, se crea su vencimiento para ese mes. La generación se solicita con `POST /api/expense-occurrences/generate`:

```json
{
  "month": "2026-12"
}
```

El vencimiento queda pendiente. Al confirmar el pago se crea el movimiento de gasto y se marca como pagado. Los vencimientos pendientes todavía no cuentan como gastos en los presupuestos ni en los informes.

La generación se realiza mediante esta petición; actualmente no hay una tarea programada que la ejecute automáticamente.

## Pruebas

El proyecto incluye pruebas unitarias con JUnit y Mockito para los servicios de movimientos y presupuestos. Comprueban reglas de negocio como la categoría del movimiento, la propiedad de los datos y el cálculo de los presupuestos.

Puedes ejecutar esas pruebas con:

```bash
./mvnw -Dtest=MovementServiceTest,BudgetServiceTest test
```

Para ejecutar todas las pruebas:

```bash
./mvnw test
```

La prueba que carga el contexto de Spring necesita la base de datos, las variables de entorno y las claves configuradas como se explica en los pasos de arranque.

## Pendiente

- Ampliar las pruebas de autenticación, categorías, gastos recurrentes, vencimientos e informes.
- Añadir más pruebas de persistencia y de endpoints.
- Crear un frontend para utilizar la aplicación desde una interfaz.
