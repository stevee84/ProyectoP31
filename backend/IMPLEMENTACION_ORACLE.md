# Backend Oracle — integrante 2 (Joshua)

## Inventario de archivos

Nuevos (rutas relativas a `backend/`):

```text
IMPLEMENTACION_ORACLE.md
src/main/java/accesodatos/ProveedorConexion.java
src/main/java/arranque/MainServidorOracle.java
src/main/java/ia/ExtractorReservaIA.java
src/main/java/ia/ExtractorReservaIAGemini.java
src/main/java/repositorio/FabricaRepositoriosOracle.java
src/main/java/repositorio/IRepositorioConsultas.java
src/main/java/repositorio/IRepositorioFuncionarios.java
src/main/java/repositorio/IRepositorioRecursos.java
src/main/java/repositorio/IRepositorioReservas.java
src/main/java/repositorio/RepositorioConsultasOracle.java
src/main/java/repositorio/RepositorioFuncionariosOracle.java
src/main/java/repositorio/RepositorioRecursosOracle.java
src/main/java/repositorio/RepositorioReservasOracle.java
src/main/java/servicio/ConsultasReservas.java
src/main/java/servicio/ErroresOracle.java
src/main/java/servicio/FabricaServiciosOracle.java
src/main/java/servicio/ServicioAplicacion.java
src/test/java/ia/ExtractorReservaIAGeminiTest.java
src/test/java/integracion/ServidorOracleIT.java
src/test/java/repositorio/RepositoriosOracleIT.java
src/test/java/repositorio/RepositoriosOracleTest.java
src/test/java/servicio/ConsultasReservasTest.java
src/test/java/servicio/ReservasTransaccionTest.java
src/test/java/servicio/ServicioAplicacionOracleIT.java
src/test/java/servicio/ServicioAplicacionTest.java
src/test/java/soporte/DatosOracle.java
```

Existentes modificados: `backend/pom.xml`,
`backend/src/main/java/accesodatos/ConexionOracle.java` (solo aclara el mensaje
de configuración), `README.md` y `prompts_proyecto2/PROMPTS_LLM.md`.
Los demás archivos existentes se conservaron, incluidos sus finales de línea.

## Implementación

- `src/main/java/accesodatos/ProveedorConexion.java`: punto pequeño para sustituir
  la apertura JDBC en pruebas; producción utiliza `ConexionOracle` y `DriverManager`.
- `src/main/java/repositorio/`: cuatro interfaces e implementaciones nuevas
  (`Funcionarios`, `Recursos`, `Reservas`, `Consultas`), además del repositorio
  de categorías existente. Autenticación y clave pertenecen a funcionarios;
  disponibilidad a recursos. Usan las firmas y columnas de `DB/02`.
  `RepositorioFuncionariosOracle` aplica el hash de claves (`seguridad.ClaveHash`,
  PBKDF2WithHmacSHA256 con sal por usuario) y lo verifica en Java con el valor que
  devuelve `obtenerCredencialesUsuario`; `insertarFuncionario` recibe el hash de la
  clave inicial (= ID) y `cambiarClaveUsuario` el de la nueva clave.
- `FabricaRepositoriosOracle`: crea los repositorios con la conexión de la operación.
  No conserva conexiones (de eso se encarga `ConexionCompartida`). Permite
  sustituir una dependencia en las pruebas.
- `src/main/java/servicio/ServicioAplicacion.java`: implementación completa de
  `IServicioAplicacion`; sesión de solo lectura, administración, reservas y consultas.
- `FabricaServiciosOracle`: una instancia por servidor; comparte el mismo bloqueo
  y una sola conexión JDBC reutilizada (`accesodatos.ConexionCompartida`) entre
  los servicios de todos los clientes; no hay conexión por cliente.
- `ErroresOracle`: traducción contextual; solo registra código y estado JDBC en el
  servidor. No expone SQL, mensajes del driver ni contraseñas al cliente.
- `ConsultasReservas`: construcción de calendario y agenda de lunes a domingo;
  actividad en cada intervalo que solapa, incluso horarios con minutos/segundos.
  Calendario carga recursos y reservas una vez; el repositorio completa recursos
  por reserva (no por celda). Las semanas sin filas se completan con cero.
- `src/main/java/ia/`: interfaz pequeña y adaptador Gemini REST con JSON, tiempos
  de espera y validación del catálogo. Adaptación del Proyecto 1 sin dependencia
  a ese módulo, sin Swing y sin copiar sus mensajes externos inseguros.
- `src/main/java/arranque/MainServidorOracle.java`: utiliza `red.Servidor` original,
  comprueba la conexión y proporciona la fábrica real.
- `pom.xml`: conserva ojdbc11 23.6.0.24.10; agrega JSON y copia las dependencias
  de ejecución en la fase `package`.

## Decisiones importantes

Cada fábrica reutiliza **una sola conexión física** (`ConexionCompartida`): abrir
una conexión nueva por operación agotaba el listener de Oracle XE (ORA-12519).
Es seguro porque el bloqueo de la fábrica deja una sola operación a la vez y cada
operación termina con commit o rollback. Cada operación pide la conexión con
autocommit desactivado y la "cierra" con try-with-resources; ese cierre no cierra
la conexión física, y si deja de ser válida (`isValid`) se abre otra. Todos los
repositorios de la operación reciben la misma conexión; no hacen commit, rollback
ni close de esa conexión. Las listas devueltas
son inmutables. Un fallo durante una sentencia o commit provoca rollback; un
fallo de cierre posterior a un commit exitoso se registra sin anunciar que falló
la reserva ya confirmada. Un fallo de comunicación durante commit puede tener
resultado incierto: consultar antes de reintentar.

La reserva valida todas las categorías antes de seleccionar, preserva repeticiones
y ordena los recursos seleccionados por código antes de llamar a las asignaciones
con bloqueo de Oracle. Si falta alguno, todavía no ha escrito nada. El bloqueo
compartido dura desde disponibilidad hasta commit/rollback y cubre las demás
operaciones JDBC. Es intencionalmente sencillo y serializa también consultas.
Está diseñado para **un servidor Backend**, no para coordinar distintos procesos;
Oracle revalida cada asignación mediante sus bloqueos de fila.

El servicio no llama a `ContextoSesion.iniciar/cerrar`, ni publica alertas.
`cerrarSesion` verifica sesión y deja la limpieza al despachador. Solo el ID del
contexto se utiliza para autor y cambio de clave. El CRUD administrativo exige
administrador; categorías, recursos, calendario, agenda y estadísticas permiten
sesión de funcionario. Las consultas de funcionarios requieren administrador.

Las claves nunca se guardan en texto plano: `TUSUARIOS.CLAVE` contiene
`pbkdf2$iteraciones$salBase64$hashBase64` y una restricción `CHECK` rechaza otro
formato. `DB/06` escribe el hash precalculado de `ADMIN`. La descripción de
categoría es única mediante el índice `UQ_TCATEGORIAS_DESCRIPCION` sobre
`UPPER(TRIM(DESCRIPCION))`; `ErroresOracle` reconoce esa restricción en ORA-00001
y responde "Ya existe una categoría con esa descripción." (al registrar y al
modificar).

Las longitudes se validan conforme a los tamaños de `DB/01`. Si la instalación
utiliza semántica BYTE y un texto multibyte supera los bytes permitidos, Oracle
lo rechaza con ORA-12899, traducido a VALIDACION (sin alterar el esquema). Las
fechas admitidas son años 1–9998 para poder calcular el fin exclusivo de días y
semanas sin desbordar Oracle. Inicio y fin usan hora local, sin conversión de zona:
mantener el reloj y la zona horaria del Backend y Oracle coordinados.

Las estadísticas reutilizan `COUNT(DISTINCT reserva.ID)` por categoría de `DB/02`;
fechas inclusivas para categoría y semanas completas de lunes a domingo. Solo
cuentan activas. Cancelar conserva cabecera y relaciones. Las eliminaciones con
historial se rechazan por clave foránea; no se borran reservas para permitirlas.

La agenda conserva la semántica completa del Proyecto 1 (cada franja ocupada),
no la simplificación del servicio falso que agrupaba solo por hora de inicio.
Si dos actividades contiguas ocupan partes de una misma hora, el calendario
muestra ambas, sin confundir el límite exclusivo del intervalo.

## Pruebas y datos

Unitarias nuevas:

- `repositorio.RepositoriosOracleTest`: llamadas JDBC, posiciones y tipos de
  parámetros, columnas de cursores, mapeo de DTO y cierre de recursos.
- `servicio.ServicioAplicacionTest`: todos los métodos protegidos por sesión,
  permisos, campos, longitudes, fechas, autenticación, clave y extracción simulada.
- `servicio.ReservasTransaccionTest`: transacción única, orden de asignación,
  categorías repetidas, falta normal, rollback de segunda asignación, commit y
  rollback fallidos, y cierre fallido después de confirmar.
- `servicio.ConsultasReservasTest`: intervalos parciales/contiguos, agenda completa,
  canceladas y semanas con cero.
- `ia.ExtractorReservaIAGeminiTest`: transporte simulado; sin llamadas externas,
  clave en cabecera, fallos sanitizados, JSON, datos incompletos e interrupciones.

Integración activada exclusivamente con `ORACLE_IT=true`:

- `repositorio.RepositoriosOracleIT`: funcionarios, acceso, recursos y reserva con
  rollback; ninguna confirmación oculta en los repositorios.
- `servicio.ServicioAplicacionOracleIT`: commit real observado desde otra conexión,
  falta normal sin residuos, repeticiones, conflictos, cancelaciones, historial,
  estadísticas/consultas y competencia entre dos servicios de la misma fábrica.
  La prueba de fallo delega la primera asignación a Oracle e induce un código de
  recurso inexistente en la segunda llamada REAL, comprobando rollback total.
- `integracion.ServidorOracleIT`: servidor y Proxy originales, sesiones reales,
  persistencia, una alerta, rechazo de reserva ajena, consultas y cancelación.
- Se mantiene `repositorio.RepositorioCategoriasOracleIT` sin modificar.

`src/test/java/soporte/DatosOracle.java` identifica cada ejecución mediante UUID.
Confirma únicamente sus datos y elimina relaciones, reservas, recursos, categorías
y usuarios por IDs exactos. La limpieza se ejecuta incluso si fallan aserciones.
Las estadísticas globales se comparan con su valor previo o la consulta real,
sin suponer que las tablas del proyecto estén vacías. Una terminación abrupta o
caída de BD durante limpieza requiere revisar manualmente esos IDs identificables.

## Ejecución y alcance de la validación

Consultar comandos, variables y orden de scripts en el [README raíz](../README.md).
No volver a ejecutar `DB/01` sobre el esquema existente. `DB/02` no necesita
cambios para este trabajo. El nuevo arranque incluye driver y dependencias.

Este entorno WSL no heredó `DB_PASSWORD`, `ORACLE_IT` ni `GEMINI_API_KEY` de IntelliJ.
No se buscó ni inventó la contraseña; no se hicieron llamadas reales a Gemini.
Las pruebas Oracle nuevas necesitan ejecución local con esas variables y scripts
ya preparados. Sus omisiones no constituyen una validación de Oracle ni del
arranque completo con la base conectada.

Resultados de las verificaciones realizadas con Java 17 y
`-Dmaven.compiler.release=17`:

| Comprobación | Resultado |
|-------------|-----------|
| `mvn test` | 116 unitarias aprobadas, sin fallos ni omisiones |
| `mvn verify` | Las mismas 116 unitarias y 13 IT de red aprobadas; 17 IT Oracle omitidas |
| `mvn -pl backend -am package -DskipTests` | Empaquetado correcto; dependencias `comun`, `json` y `ojdbc11` presentes |
| Arranque con classpath documentado, sin `DB_PASSWORD` | Detecta falta de conexión/configuración antes de aceptar clientes |
| Comprobación aislada de sockets sin Oracle | Driver y JSON cargan; servidor original con fábrica real devuelve PERSISTENCIA por falta de configuración, sin detalles privados |

Las 17 IT omitidas son las tres de categorías existentes, tres de repositorios
nuevos, diez del servicio Oracle y una de sockets con Oracle. Hay 30 unitarias
nuevas y 14 IT Oracle nuevas. No hubo ejecución real contra Oracle en este entorno.
La comprobación aislada de sockets **no sustituye** `ServidorOracleIT` ni valida
persistencia. El primer intento de esa comprobación coincidió con una recompilación
de Maven y no encontró una clase; repetido con la compilación terminada pasó.

La bitácora `prompts_proyecto2/PROMPTS_LLM.md` registra el trabajo y sus límites.

## Integración con el equipo

No se cambian `comun/`, `backend/src/main/java/red/`, `falso/`, pruebas de Steve ni
Frontend. No hay incompatibilidad del contrato que requiera cambiarlo. Steve
puede usar directamente el nuevo main o sustituir una construcción de fábrica en
su main. Scott debe mantener controladores contra `IServicioAplicacion`, procesar
`ResultadoReservaDTO.fracaso` como resultado normal y ejecutar red fuera del EDT.
Pantallas, PDF, Proxy y presentación de alertas siguen siendo responsabilidad del
equipo correspondiente. No se hicieron commit ni push.
