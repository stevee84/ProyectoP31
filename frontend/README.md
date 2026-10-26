# Frontend — MVC, reportes y alertas (Integrante 3)

Cliente Swing del sistema de reservas. Solo depende del módulo `comun`: no tiene
driver, credenciales ni referencias a la base de datos. Todo lo persistente y todas
las reglas (permisos, solapes, horario) viven en el Backend.

## Ejecución

Primero se compila todo desde la raíz:

```bash
mvn clean install
```

### Cliente real (contra el servidor)

```bash
# desde la raíz
mvn -pl frontend exec:java -Dservidor.host=IP_DEL_SERVIDOR -Dservidor.puerto=5000

# o con el jar empaquetado (las dependencias quedan en frontend/target/dependencias)
java -Dservidor.host=IP_DEL_SERVIDOR -Dservidor.puerto=5000 -jar frontend/target/frontend-1.0-SNAPSHOT.jar
```

| Propiedad | Valor por defecto | Uso |
|-----------|-------------------|-----|
| `servidor.host` | `localhost` | IP o nombre de la máquina del Backend |
| `servidor.puerto` | `5000` | Puerto del `ServerSocket` |
| `servidor.timeout` | `10000` | Milisegundos de espera por respuesta |

Si el servidor no está disponible al arrancar, el cliente avisa y vuelve a intentar
la conexión al presionar *Ingresar*. Se pueden abrir varios clientes a la vez, en la
misma máquina o en otras.

### Demo sin servidor

Para revisar las pantallas de ambos roles sin Backend ni Oracle:

```bash
mvn -pl frontend exec:java -Dexec.mainClass=cliente.MainClienteDemo -Ddemo.alertas=8
```

Usa `ServicioClienteFalso` (datos en memoria) y `FuenteEventosSimulada`, que genera
una alerta de otro usuario cada `demo.alertas` segundos. Usuarios (clave = id):
`ADMIN`, `F001`, `F002`, `F003`.

## Estructura

| Paquete | Contenido |
|---------|-----------|
| `cliente` | `MainCliente` (conecta el Proxy y arranca el MVC) y `MainClienteDemo` |
| `controller` | `AplicacionController` (login → ventana → cierre de sesión) y un controlador por pantalla |
| `model` | Fachadas `XxxModel` que delegan en `IServicioAplicacion`; `AlertasModel`, `Alerta`, `TablaDatos` |
| `view` | Pantallas Swing del Proyecto 1, `AlertasPanel`, `AlertaDetalleDialog`, `GeneradorPdf` |
| `proxy` | `ProxyServicio` (Integrante 1) |
| `util` | `Tarea`: ejecuta la llamada remota fuera del EDT y devuelve el resultado en el EDT |
| `falso` | `ServicioClienteFalso` y `FuenteEventosSimulada`, para pruebas y demo |

Flujo de una acción:
`View → Controller → XxxModel → IServicioAplicacion (ProxyServicio) → socket → Backend`.

Los controladores reciben el contrato `IServicioAplicacion`, no el Proxy concreto:
en ejecución se inyecta `ProxyServicio`; en pruebas, `ServicioClienteFalso`.

## Interfaz sin bloqueos

Toda llamada al servidor pasa por `util.Tarea` (un `SwingWorker`): la red se espera
fuera del Event Dispatch Thread y la vista se actualiza dentro de él. Mientras una
petición está en curso, la ventana sigue respondiendo y pueden llegar alertas.

Los errores remotos llegan como `ServicioException` con su `CodigoError` y se
muestran con el mensaje que envía el Backend. Si se pierde la conexión al crear una
reserva, el cliente no la reintenta (pudo haber quedado registrada): avisa y recarga
"Mis reservas".

Calendarización, agenda y estadísticas piden la respuesta completa en una sola
llamada (`MatrizCalendarioDTO`, `AgendaSemanaDTO`, listas de conteos); los modelos
la convierten en filas con `TablaDatos` y la vista solo pinta.

## Alertas

- `AlertasPanel` está en `BorderLayout.EAST` de `VentanaPrincipal`, fuera del
  `JTabbedPane`: sigue visible al cambiar de pestaña.
- `AlertasController` implementa `IReceptorEventos` y se registra en el Proxy al abrir
  la ventana principal. `actualizar()` llega en el hilo lector del socket; el cambio
  del modelo se pasa al EDT con `SwingUtilities.invokeLater`.
- El servidor no envía al autor su propia alerta; el controlador lo revisa también.
- **Ver** abre `AlertaDetalleDialog` (encargado, fecha, desde, hasta, recursos).
- **Descartar** pide confirmación; si se acepta, solo se quita de la lista local. No
  se envía nada al Backend ni se modifica la reserva.
- Al cerrar sesión se quita el receptor y se vacía la lista; la próxima sesión
  empieza sin alertas. Una alerta que venía en camino al cerrar sesión se descarta.

## Reportes PDF

Cada pantalla con tabla conserva su botón *Generar PDF* (`GeneradorPdf`, iText 5).
Los datos son los que ya están en la tabla, obtenidos por el Proxy: el reporte no
consulta la base de datos.

## Pruebas

| Clase | Tipo | Qué comprueba |
|-------|------|---------------|
| `model.AlertasModelTest` | unitaria | orden, duplicados, descarte, limpieza, notificación |
| `controller.AlertasControllerTest` | unitaria | alerta de otro usuario, filtro del autor, ver, confirmación sí/no, cierre de sesión, cambios en el EDT |
| `view.AlertasPanelTest` | unitaria | filas, íconos Ver/Desc., celdas no editables, panel a la derecha |
| `model.ConversionTablasTest` | unitaria | DTO → filas de reservas, calendario y agenda |
| `model.FachadasConServicioFalsoTest` | unitaria | login, primer ingreso, permisos, reservas sin disponibilidad, cancelación ajena, matriz en una llamada, IA falsa |
| `integracion.AlertasProxyIT` | integración | MVC + `ProxyServicio` real: alerta durante una petición y ciclo de sesión |

```bash
mvn -pl frontend -am test      # unitarias (Surefire)
mvn -pl frontend -am verify    # + integración (Failsafe)
```

Las pruebas corren con `java.awt.headless=true`, sin abrir ventanas. La revisión
visual se hace con `MainClienteDemo` entrando como `ADMIN` y como `F001`.
