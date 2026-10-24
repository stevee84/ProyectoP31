# Sistema de Reserva de Recursos — Proyecto 2

Curso EIF206 Programación 3 — Universidad Nacional de Costa Rica.

Evolución del Proyecto 1 (aplicación Swing con persistencia XML) a una **aplicación
distribuida**: varios clientes Swing (Frontend, MVC) se comunican por **sockets** con un
servidor (Backend, sin interfaz gráfica) que aplica las reglas de negocio y persiste en
**Oracle**. Cuando alguien crea una reserva, los demás usuarios conectados reciben una
**alerta asincrónica**.

> **Base de datos:** se usa **Oracle por indicación del profesor** (el enunciado menciona MySQL).

## Integrantes y responsabilidades

| Integrante | Responsabilidad |
|------------|-----------------|
| 1 — Steve  | Comunicación por sockets, servidor, patrón Proxy, publicación de alertas |
| 2 — Joshua | Servicios, reglas de negocio, repositorios, base de datos Oracle |
| 3 — Scott  | MVC del Frontend, pantallas, reportes, alertas en la interfaz |

## Arquitectura distribuida

```
 Máquina A                 Máquina B                      Máquina C
┌──────────────┐        ┌──────────────────────┐        ┌──────────┐
│ Frontend MVC │──TCP──▶│ Backend (sin UI)     │──JDBC─▶│  Oracle  │
│ (Swing)      │◀─alerta│ Servidor + servicios │        └──────────┘
└──────────────┘        │ + reglas de negocio  │
┌──────────────┐        │                      │
│ Frontend MVC │──TCP──▶│                      │
└──────────────┘        └──────────────────────┘
```

- Varios Frontend se conectan a un único Backend; pueden correr en máquinas distintas.
- El Frontend no accede a la base de datos ni conoce las clases del servidor: solo usa el
  contrato y los DTO del módulo `comun`.
- El servidor decide el autor de cada operación a partir de la sesión de la conexión
  (`ContextoSesion`), nunca a partir de datos enviados por el cliente.

## Patrón Proxy

```
Controlador → IServicioAplicacion ← ProxyServicio ──socket (Peticion/Respuesta)──> ManejadorCliente → Despachador → IServicioAplicacion (servicio real)
```

| Rol | Clase |
|-----|-------|
| Subject | `contrato.IServicioAplicacion` |
| Proxy | `proxy.ProxyServicio` (Frontend) |
| Contraparte del servidor | `red.Despachador` (traduce cada `Peticion` a la llamada real) |
| RealSubject | Servicio del Backend creado por `servicio.IFabricaServicios` |

Los controladores trabajan contra la interfaz, sin saber que hay red de por medio. Las
llamadas lentas se ejecutan fuera del hilo de Swing con `util.Tarea`.

## Alertas asincrónicas

- **Publicación** (`red.PublicadorEventos`): solo cuando se crea con éxito una reserva nueva
  se publica un `EventoReservaCreada` a los **demás usuarios autenticados**; el autor no lo
  recibe. Un observador que falla se retira sin afectar a los otros.
- **Recepción**: el Frontend registra un `contrato.IReceptorEventos`; el `ProxyServicio`
  entrega los eventos que llegan por el socket. Como llegan en un hilo de red, la vista debe
  actualizarse con `SwingUtilities.invokeLater(...)`.

## Estructura de módulos y paquetes

| Módulo | Paquetes | Contenido |
|--------|----------|-----------|
| `comun` | `contrato` | `IServicioAplicacion`, `IReceptorEventos` |
| | `dto` | DTO serializables (`UsuarioDTO`, `ReservaDTO`, `RecursoDTO`, `CategoriaDTO`, `MatrizCalendarioDTO`, …) |
| | `protocolo` | `Peticion`, `Respuesta`, `Mensaje`, `Operacion`, `EventoReservaCreada`, `CodigoError`, `ServicioException`, `Configuracion` |
| `backend` | `red` | `MainServidor`, `Servidor`, `ManejadorCliente`, `Despachador`, `PublicadorEventos` |
| | `servicio` | `ContextoSesion`, `IFabricaServicios` |
| | `falso` | Servicio en memoria para pruebas e integración temprana |
| `frontend` | `proxy`, `util` | `ProxyServicio`, `Tarea` (MVC en migración por el Integrante 3) |
| `proyecto1` | — | Módulo temporal que compila la app del Proyecto 1 desde `src/` hasta completar la migración |
| `DB/` | — | Scripts SQL de Oracle |

`protocolo.Configuracion` lee propiedades del sistema:

| Propiedad | Defecto | Uso |
|-----------|---------|-----|
| `servidor.host` | `localhost` | Dirección del Backend (cliente) |
| `servidor.puerto` | `5000` | Puerto de escucha / conexión |
| `servidor.timeout` | `10000` ms | Tiempo máximo de espera de una respuesta |

La deserialización usa `Configuracion.FILTRO_MENSAJES` (`ObjectInputFilter`) para aceptar
solo las clases del protocolo y los DTO.

## Funcionalidades

**Administrador:** CRUD de funcionarios, categorías y recursos (filtro por categoría).
**Funcionario:** crear reservaciones (categorías, fecha y horario), extracción de datos con IA
(Gemini, opcional con `GEMINI_API_KEY`), cancelar reservaciones propias.
**Todos:** calendarización por categoría/fecha/hora, agenda semanal, estadísticas de actividades
y de recursos, exportación a PDF, búsqueda en tablas, alertas de nuevas reservas.

Al crear un funcionario su clave inicial es su identificación y en el primer ingreso se pide
cambiarla.

## Requisitos

- JDK 17 o superior
- Maven 3.8+
- Oracle Database (p. ej. Oracle XE) para el Backend definitivo

## Base de datos

Ejecutar en Oracle, en este orden, con el usuario del esquema:

1. `DB/01_tablas_y_secuencias.sql` — tablas y secuencias
2. `DB/02_procedimientos_y_funciones.sql` — procedimientos y funciones
3. `DB/03_pruebas_categorias.sql` — pruebas / datos de categorías (opcional)

## Ejecución

### Compilar

```bash
mvn clean install
```

### Servidor (Backend)

Mientras se integra la fábrica Oracle (Integrante 2), `MainServidor` usa el servicio en
memoria `falso` con los usuarios `ADMIN/ADMIN`, `F001/F001`, `F002/F002`, `F003/F003`.

```bash
# Windows (en Linux/macOS usar ':' como separador)
java -Dservidor.puerto=5000 -cp "comun/target/classes;backend/target/classes" red.MainServidor
```

Debe mostrar `Servidor de reservas escuchando en el puerto 5000`. Abrir ese puerto en el
firewall si los clientes están en otras máquinas.

### Clientes (Frontend)

Cada cliente se apunta al servidor con `-Dservidor.host=<IP del servidor>` (y
`-Dservidor.puerto` si no es 5000). El `MainCliente` del Frontend está **pendiente**
(Integrante 3).

Mientras tanto, la aplicación del Proyecto 1 (`Main`, módulo `proyecto1`) se ejecuta
**desde la raíz del repositorio**, porque usa el directorio relativo `data/`:

```bash
mvn -pl proyecto1 exec:java -Dexec.mainClass=Main
```

## Pruebas

| Tipo | Patrón | Plugin | Comando |
|------|--------|--------|---------|
| Unitarias | `*Test` | Surefire | `mvn test` |
| Integración | `*IT` | Failsafe | `mvn verify` |

- **Unitarias:** `ProtocoloTest` (comun), `ProxyServicioTest` (frontend),
  `DespachadorTest` (traducción de peticiones, sesión y errores) y
  `ServicioAplicacionFalsoTest` (reglas del contrato: cancelar, cambio de clave, validación
  de reservas y rangos) en backend, y las pruebas heredadas del Proyecto 1 (`proyecto1`).
- **Integración:** `backend/src/test/java/red/RedIT.java` levanta un `Servidor` real con el
  servicio falso y conecta clientes `ProxyServicio` por sockets: login, operaciones, errores
  y entrega de alertas a otros usuarios.

## Protocolo y errores

Cada llamada viaja como `Peticion` (operación + argumentos) y vuelve como `Respuesta`. Toda
falla se informa con `ServicioException` y un `CodigoError`:

| Código | Significado |
|--------|-------------|
| `VALIDACION` | Datos inválidos, duplicados o inexistentes |
| `SESION` | Credenciales incorrectas o sesión no iniciada |
| `PERMISO` | El rol no permite la operación o la reserva es de otro usuario |
| `RECURSO_EN_USO` | Borrar algo que tiene recursos o reservas asociados |
| `DISPONIBILIDAD` | Horario no disponible (fuera del caso normal de `ResultadoReservaDTO.fracaso`) |
| `PERSISTENCIA` | Oracle falló y la operación se revirtió |
| `COMUNICACION` | La red falló (solo lo genera el Proxy) |

Las operaciones sin retorno terminan normalmente si tuvieron éxito.

## Bitácora de prompts

Registro del uso de asistentes de IA durante el desarrollo:

- Proyecto 2: [`prompts_proyecto2/PROMPTS_LLM.md`](prompts_proyecto2/PROMPTS_LLM.md). Cada prompt indica la herramienta, qué se obtuvo y qué hubo que corregir.
- Proyecto 1: [`bitacora_prompts.txt`](bitacora_prompts.txt).

## Repositorio

https://github.com/stevee84/ProyectoP31
