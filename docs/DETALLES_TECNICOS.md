# Detalles técnicos

Documentación de referencia para quien revisa el código. Para instalar y ejecutar el
proyecto basta el [README](../README.md).

## Paquetes

Backend:

| Paquete | Responsabilidad |
|---------|-----------------|
| `app` | `MainServidor`: arranca el servidor con Oracle |
| `red` | Servidor, conexiones, despacho y publicación de eventos |
| `servicio` | Contexto de sesión, fábrica y servicios de aplicación |
| `datos` | Conexión JDBC y repositorios Oracle (procedimientos y funciones) |
| `ia` | Extracción de datos de una reserva con Gemini |
| `utilitarios` | `ClaveHash` (PBKDF2) |

Paquetes del Frontend:

| Paquete | Responsabilidad |
|---------|-----------------|
| `app` | `MainCliente`: conecta el Proxy y arranca el MVC |
| `proxy` | `ProxyServicio`, el Proxy del servicio remoto |
| `controller` · `model` · `view` | MVC de las pantallas y el área de alertas |
| `utilitarios` | `Tarea`: llamadas al servidor fuera del hilo de Swing |

El Backend real implementa el contrato completo con repositorios Oracle de
categorías, funcionarios y autenticación, recursos y disponibilidad, reservas
y estadísticas. `ServicioAplicacion` aplica permisos, validaciones y transacciones;
`FabricaServiciosOracle` comparte el bloqueo entre los clientes del servidor.
La extracción Gemini se ejecuta en el Backend, sin mantener conexiones ni bloqueos.

## Configuración de red

`protocolo.Configuracion` lee las siguientes propiedades del sistema:

| Propiedad | Valor predeterminado | Uso |
|-----------|----------------------|-----|
| `servidor.host` | `localhost` | Dirección del Backend |
| `servidor.puerto` | `5000` | Puerto del servidor |
| `servidor.timeout` | `10000` | Espera máxima de una respuesta, en milisegundos |
| `servidor.maxClientes` | `100` | Solo servidor: conexiones atendidas a la vez |
| `servidor.timeoutSinSesion` | `60000` | Solo servidor: milisegundos que espera a que una conexión inicie sesión |

Las conexiones que exceden `servidor.maxClientes` reciben un aviso y se
cierran. Una conexión sin sesión se cierra tras `servidor.timeoutSinSesion` ms;
con sesión iniciada no hay límite de inactividad.

La deserialización utiliza `Configuracion.FILTRO_MENSAJES` para aceptar las
clases permitidas del protocolo y los DTO.

## Reglas de usuarios y categorías

Los funcionarios se registran con su identificación como clave inicial.
En el primer ingreso deben cambiarla. El servidor lo exige: mientras el
cambio esté pendiente (también para `ADMIN`) rechaza toda operación salvo
cambiar la clave y cerrar sesión, con el código `SESION` y el mensaje
"Debe cambiar su clave antes de continuar.". La clave nueva no puede ser la
actual ni la identificación del usuario.

El teléfono del funcionario es obligatorio y solo admite dígitos, espacios,
`+` y `-`, con 7 a 20 caracteres y al menos 7 dígitos (por ejemplo
`8888-1111` o `+506 8888 1111`). La descripción de cada categoría es única,
sin distinguir mayúsculas ni espacios al inicio o al final.

Las claves se guardan con hash PBKDF2WithHmacSHA256 y sal aleatoria por
usuario (`utilitarios.ClaveHash`, formato `pbkdf2$iteraciones$sal$hash` en
`TUSUARIOS.CLAVE`). La verificación se hace en Java; Oracle nunca recibe ni
guarda una clave en texto plano, y una restricción `CHECK` lo impide.

## Base de datos Oracle

Se trabaja con Oracle Database 11g Express Edition, versión 11.2.0.2.0.

Esquema del proyecto:

```text
RESERVAS_P2
```

La conexión de SQL Developer puede llamarse `ReservasRecursos`.
Ese nombre es una etiqueta local y no sustituye al usuario ni al SID.

### Scripts

Abrir cada archivo en SQL Developer, seleccionar todo su contenido y
ejecutarlo con F5: `00` en una conexión `SYSTEM` y el resto en la conexión del
proyecto (`RESERVAS_P2`).

| Archivo | Contenido | Cuándo ejecutarlo |
|---------|-----------|-------------------|
| `DB/00_crear_usuario.sql` | Usuario `RESERVAS_P2` y sus permisos | Una sola vez, como `SYSTEM` |
| `DB/01_tablas_y_secuencias.sql` | Cinco tablas y dos secuencias | Una sola vez en un esquema vacío |
| `DB/02_procedimientos_y_funciones.sql` | Cursores, CRUD, acceso, reservas y estadísticas | Después de `01`; puede repetirse para actualizar las rutinas |
| `DB/03_pruebas_categorias.sql` | Prueba de categorías | Opcional, después de `02` |
| `DB/04_pruebas_recursos.sql` | Pruebas de recursos y relaciones | Opcional, después de `02` |
| `DB/05_pruebas_usuarios.sql` | Pruebas de funcionarios y claves | Opcional, después de `02` |
| `DB/06_datos_iniciales.sql` | Administrador y categorías iniciales | Después de `02` |
| `DB/07_pruebas_reservas.sql` | Pruebas de reservas, disponibilidad y estadísticas | Opcional, después de `02` |

Los scripts de pruebas deshacen sus datos temporales mediante rollback.

El script `06` guarda sus cambios mediante commit.

Si `01` muestra `ORA-00955` en un esquema ya preparado, los objetos ya
existen. Si el esquema se creó con una versión anterior de `01` (sin el
índice único de categorías ni la restricción de claves con hash), hay que
borrar los objetos del esquema y ejecutar de nuevo `01`, `02` y `06`.

Los números consumidos por las secuencias no se recuperan mediante
rollback. Los saltos en los identificadores son normales.

### Datos iniciales

El script `06` crea:

- Usuario: `ADMIN`.
- Clave inicial: `ADMIN`.
- Rol: `ADMINISTRADOR`.
- Primer ingreso pendiente.
- Categorías: Sala para 10 personas, Laptop Windows y Sala de juntas.

La clave de `ADMIN` se escribe ya con hash (literal PBKDF2 generado con
`jshell --class-path backend/target/classes`, y dentro `utilitarios.ClaveHash.generar("ADMIN")`).
Si `ADMIN` ya existe, el script conserva su clave; si la tiene en texto plano
(base anterior), la restablece a `ADMIN` con hash y primer ingreso pendiente.

Las categorías iniciales se agregan solamente si no existe una con la misma
descripción.

### Transacciones

Los procedimientos y funciones de `02` no ejecutan commit ni rollback.

El servicio Java controla la transacción completa.

La creación de una reserva debe realizarse con la misma conexión JDBC,
incluyendo la cabecera y todas las asignaciones de recursos.

El servicio debe confirmar todos los cambios o deshacerlos completamente.

### Configuración JDBC

El driver se declara únicamente en `backend/pom.xml`.

Se utiliza `ojdbc11:23.6.0.24.10`, tomado del ejemplo del profesor.

Las pruebas de integración se ejecutan con ese driver y Oracle XE 11.2.0.2.0.
Eso no certifica todas las combinaciones de versiones: compilar o ejecutar
pruebas sin Oracle no valida la persistencia.

Referencia:
[Documentación de compatibilidad JDBC de Oracle](https://www.oracle.com/database/technologies/faq-jdbc.html).

Definir estos valores en `oracle.properties` (raíz del proyecto) o como variables
de entorno de la terminal o el IDE donde se ejecuta el Backend o sus pruebas;
las variables de entorno tienen prioridad:

| Variable | Valor |
|----------|-------|
| `DB_URL` | `jdbc:oracle:thin:@localhost:1521:XE` |
| `DB_USER` | `RESERVAS_P2` |
| `DB_PASSWORD` | Clave del usuario `RESERVAS_P2` |
| `ORACLE_IT` | `true` para activar las pruebas de integración Oracle |

La URL utiliza:

- Host: `localhost`.
- Puerto: `1521`.
- SID: `XE`.

Si SQL Developer utiliza otros valores, ajustar `DB_URL`.

`DB_PASSWORD` corresponde al usuario del proyecto, no a `SYSTEM`.

Cada integrante configura sus propias variables. La contraseña no se guarda
en el código ni en archivos compartidos en Git.

### Conexiones y repositorios

`ConexionOracle.abrir()` devuelve una conexión nueva; quien la solicita debe
cerrarla. El servicio no la usa por operación: cada fábrica reutiliza una sola
conexión (`ConexionCompartida`, descrita más abajo).

Los repositorios reciben esa conexión y cierran sus propias sentencias y
cursores. No cierran la conexión ni confirman la transacción.

Esto permite que varios repositorios participen en la misma operación.

`app.MainServidor` arranca `red.Servidor` con la fábrica Oracle.

La aplicación reutiliza una conexión por fábrica (`ConexionCompartida`) y cada operación
trabaja con autocommit desactivado. Abrir una conexión nueva por operación agotaba el
listener de Oracle XE (ORA-12519) con muchas operaciones seguidas; si la conexión deja de
ser válida, se abre otra.
Confirma antes de devolver éxito y revierte ante errores. Al reservar,
consulta disponibilidad y selecciona recursos antes de insertar; la falta
normal de recursos no deja cabecera ni asignaciones parciales. Las categorías
repetidas solicitan recursos distintos. Se asignan en orden de código para
los bloqueos de fila de Oracle.

Una sola fábrica por servidor serializa las operaciones JDBC, también las
modificaciones administrativas y cancelaciones. Es una solución sencilla para
el servidor único del proyecto, no un bloqueo distribuido entre varios
procesos Backend. Las rutinas Oracle mantienen además sus bloqueos y la
revalidación de solapamientos. Evitar modificar datos manualmente mientras
se realizan pruebas concurrentes.

## Gemini (opcional)

| Variable del Backend | Uso |
|----------------------|-----|
| `GEMINI_API_KEY` | Clave privada de Google AI Studio; no se envía al Frontend |
| `GEMINI_MODEL` | Modelo disponible para su cuenta; por defecto `gemini-3.5-flash-lite` |
| `GEMINI_TIMEOUT_SECONDS` | Entre 1 y 120; por defecto 8 segundos |

Sin clave, sin cuota, sin conexión o con respuesta inválida se devuelve un
`ResultadoExtraccionDTO` fallido y comprensible, sin cuerpos de errores externos.
El modelo predeterminado figura en la documentación oficial consultada durante
este trabajo; la disponibilidad y facturación dependen de Google y de su cuenta.
Se puede cambiar sin recompilar. No se ejecutan llamadas reales de pago en pruebas.

Se usa REST `generateContent`, respuesta JSON y cabecera `x-goog-api-key` (no
clave en la URL). Solo se aceptan IDs del catálogo real; se admiten campos
incompletos y se conservan las cantidades de categorías repetidas.

Si se aumenta el tiempo de Gemini, el timeout del Proxy del cliente debe ser
mayor que la operación completa (por ejemplo, `-Dservidor.timeout=40000` si
la construcción del Proxy utiliza esa configuración). Un timeout de red tras
una reserva no implica que Oracle la haya deshecho: consultar antes de reintentar.

Referencias oficiales: [modelos](https://ai.google.dev/gemini-api/docs/models),
[generateContent](https://ai.google.dev/api/generate-content).

## Pruebas contra Oracle

Clases:

```text
datos.RepositorioCategoriasOracleIT
datos.RepositoriosOracleIT
servicio.ServicioAplicacionOracleIT
integracion.ServidorOracleIT
```

Requiere:

- Scripts `01` y `02` ejecutados en Oracle.
- Variables JDBC configuradas.
- `ORACLE_IT=true`.

Comprueba:

- Conexión con el usuario configurado.
- Inserción, búsqueda, listado, modificación y eliminación de categorías.
- Listas inmutables.
- Rollback de una inserción.
- CRUD de funcionarios, autenticación, primer ingreso y conservación de clave.
- Claves guardadas con hash (la columna nunca contiene la clave) y rechazo de
  una clave nueva igual a la actual o al ID.
- Descripción de categoría única al registrar y al modificar.
- Primer ingreso exigido por el servidor a través de sockets.
- Recursos, disponibilidad y mapeo de cursores.
- Reservas confirmadas, falta normal sin residuos, categorías repetidas,
  solapamientos y horarios contiguos.
- Rollback real tras la primera asignación, fallo en la segunda.
- Cancelación propia, ajena, repetida e iniciada; restauración de disponibilidad.
- Restricciones de borrado sin eliminar historial, estadísticas, agenda y calendario.
- Dos servicios de la misma fábrica compitiendo por el último recurso.
- Sockets y Proxy originales con servicio y fábrica Oracle, incluyendo alertas.

Las pruebas antiguas de categorías usan rollback. Las nuevas pruebas que hacen
commit crean usuarios temporales `J2IT-...`, categorías y recursos propios
(`soporte.DatosOracle`); limpian exclusivamente sus IDs en `finally`/`@AfterEach`,
en orden de claves foráneas, también si el proceso termina a mitad de una
prueba. No cambian la clave del administrador. Si quedan residuos (proceso
matado o caída de Oracle), en la base local de desarrollo:

```sql
DELETE FROM TRESERVA_RECURSO WHERE ID_RESERVA IN (SELECT ID FROM TRESERVAS WHERE ID_USUARIO LIKE 'J2IT-%');
DELETE FROM TRESERVAS WHERE ID_USUARIO LIKE 'J2IT-%';
DELETE FROM TRECURSOS WHERE CODIGO LIKE 'J2IT-%' OR DESCRIPCION LIKE '%J2IT-%';
DELETE FROM TCATEGORIAS WHERE DESCRIPCION LIKE 'J2IT-%';
DELETE FROM TUSUARIOS WHERE ID LIKE 'J2IT-%';
COMMIT;
```

Nunca vaciar tablas.

Desde la raíz, en PowerShell con las variables JDBC ya configuradas:

```powershell
$env:ORACLE_IT = 'true'
./mvnw -pl backend -am verify
```

En Bash: `export ORACLE_IT=true` y ejecutar el mismo comando Maven.
Para ejecutar todo el reactor, usar `./mvnw test` y `./mvnw verify`.
Sin `ORACLE_IT=true`, las pruebas Oracle se omiten.
Que Maven termine correctamente con esas pruebas omitidas no valida la
conexión a Oracle.

Las pruebas unitarias nuevas utilizan dependencias simuladas (conexión,
repositorios o extractor), sin Oracle ni consumo de Gemini. Comprueban sesión,
permisos, límites, horarios, transacciones, errores y matrices de consultas.

**Verificación de este trabajo:** los resultados reales y las limitaciones del
entorno están registrados en [Implementación Oracle](IMPLEMENTACION_ORACLE.md).

## Protocolo y errores

Cada llamada viaja como `Peticion` y devuelve una `Respuesta`.

El servicio informa los errores al cliente con `ServicioException` y un
`CodigoError`.

| Código | Significado |
|--------|-------------|
| `VALIDACION` | Datos inválidos, duplicados o inexistentes |
| `SESION` | Credenciales incorrectas, sesión no iniciada o cambio de clave pendiente |
| `PERMISO` | Operación no permitida para el usuario |
| `RECURSO_EN_USO` | Eliminación de un registro con asociados |
| `DISPONIBILIDAD` | Horario no disponible, fuera del resultado normal de reserva fallida |
| `PERSISTENCIA` | Fallo de Oracle y operación revertida |
| `COMUNICACION` | Fallo de red informado por el Proxy, servidor lleno o conexión cerrada por inactividad |

La falta normal de recursos al reservar se devuelve mediante
`ResultadoReservaDTO.fracaso(...)`.

Las operaciones sin retorno terminan normalmente cuando tienen éxito.
