# PROMPTS_LLM — Proyecto 2

Bitácora de prompts del Proyecto 2 (Sistema de Reserva de Recursos: Frontend, Backend y base de datos) de EIF206 Programación 3. La bitácora del Proyecto 1 está en `bitacora_prompts.txt`, en la raíz del repositorio.

Repositorio: https://github.com/stevee84/ProyectoP31

---

# Steven — Integrante 1: Comunicación, servidor, Proxy y alertas

Este trabajo se hizo como una conversación continua con Claude Code. Yo planteaba el problema o la decisión, Claude proponía y escribía el código, y revisábamos juntos qué fallaba antes de seguir. Todo se validó contra el enunciado, la división de trabajo del equipo y el estilo de código del profesor. En varios puntos se usaron equipos de agentes: revisores en paralelo, cada uno con un área, y un agente líder que decidía qué arreglar y a quién asignarlo. Cada entrada indica la herramienta, el prompt, qué se obtuvo y qué hubo que corregir.

| # | Tema |
| --- | --- |
| 1 | Punto de partida: división del trabajo, estilo del profesor y spec-kit |
| 2 | Revisar el avance del equipo y decidir el estilo visual |
| 3 | Poner la rama al día con main y con el trabajo de Joshua |
| 4 | Implementar la parte del Integrante 1 |
| 5 | Revisión ponytail y equipo de revisores con líder |
| 6 | Estilo visual con capturas antes y después |
| 7 | Mensajes de integración para los compañeros |
| 8 | Auditoría contra el enunciado, un agente por punto |
| 9 | Aplicar los ajustes de cumplimiento con un equipo orquestado |
| 10 | Publicar y actualizar los mensajes al equipo |
| 11 | Integrar el frontend de Scott y rondas de pruebas orquestadas |
| 12 | QA externo y una duda de diseño: ¿para qué sockets si la base es local? |
| 13 | Bloques A y B: endurecer seguridad y servidor, y cierre para la entrega |

## Prompt 1 — Punto de partida: división del trabajo, estilo del profesor y spec-kit

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Ya recibimos la segunda parte del proyecto; hay que aplicar sobre el proyecto este archivo [la división del trabajo] y yo estoy encargado de todo lo del Integrante 1. En `Codigo Profe` hay diferentes ejemplos de cómo le gusta a mi profesor el código: siempre haremos código como a él le gusta, y si un cambio tiene sentido y es elegante y ordenado, lo hacemos. Quiero ver si es posible aplicar spec-kit de GitHub: explícame qué hace, cómo se usa y si conviene aplicarlo aquí.”

**Qué se obtuvo.**
- Una explicación de spec-kit y sus fases: constitution, specify, clarify, plan, tasks e implement.
- La recomendación de no usarlo. El proyecto ya existe, la tarea es acotada y agregaría `.specify/` y documentos al repositorio del grupo. El modo plan y la memoria de Claude Code cubren lo mismo.
- Un análisis del código del profesor:
  - Oracle con procedimientos almacenados y `CallableStatement`/`REF_CURSOR`.
  - Excepciones propias y nombres en español.
  - Observer con `agregar`/`eliminar`/`notificar`/`actualizar` y Singleton con `getInstancia()`.
  - No tiene ejemplos de sockets.
- Un levantamiento de todo lo que la interfaz le pide a los servicios, como base del contrato remoto.
- Las decisiones tomadas:
  - Seguir con el flujo de plan aprobado antes de programar.
  - Enviar los mensajes por socket como **objetos serializados**.

**Correcciones sobre la marcha**

- El PDF de la división no se podía leer en el equipo por falta de herramientas para PDF. Se pasó a una versión `.md`.

## Prompt 2 — Revisar el avance del equipo y decidir el estilo visual

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Revisemos este repo: Joshua (Integrante 2) ya subió código de su parte, vale la pena ver qué subió. Luego tengo algunos cambios que quiero hacer a la vista del proyecto: ¿esto puede afectar o no vale la pena modificarlo ahorita?” Y sobre la vista: “estilo de botones y demás, no tanto lógica sino cómo se ve; no se ve como un programa real.”

**Qué se obtuvo.**
- Estado de las ramas:
  - `Josua` solo tenía los scripts Oracle: tablas, secuencias y CRUD de categorías con procedimientos, en el estilo del profesor.
  - `Steve` estaba atrás de `main`.
  - `Scott` solo tenía commits viejos del Proyecto 1.
- Los tipos del esquema de Joshua quedaron anotados para que los DTO coincidan con ellos: ids de texto, rol, estado e id de reserva entero.
- La conclusión de que cambiar el estilo visual no afecta la red y casi no afecta al Integrante 3, siempre que no se reestructuren `VentanaPrincipal` ni `AplicacionController`.
- La causa de que "no se vea real": la app nunca configuraba un Look & Feel y corría con Metal. Se eligió el **tema del sistema** de Windows, sin dependencias externas.

## Prompt 3 — Poner la rama al día con main y con el trabajo de Joshua

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Hagamos merge de Joshua a mi rama, para que eso quede listo de una vez y trabajar con lo que él ya ha hecho. Borremos los builders: eran para otra tarea, si no son útiles no hace falta rescatarlos; si un patrón creacional como Factory vale la pena, lo hacemos, pero que funcione para el proyecto. Pongamos Steve al día con main y luego con Joshua; después empezamos a programar en la rama.”

**Qué se obtuvo.**
- La rama `Steve` quedó al día con `main` y con la rama `Josua`. Las dos actualizaciones fueron avance rápido, sin conflictos, y los 54 tests del Proyecto 1 siguieron pasando.
- Se decidió no forzar un patrón creacional: el proyecto ya necesita uno real. `IFabricaServicios` es una Abstract Factory que crea un servicio por conexión, falso para pruebas u Oracle. `Configuracion` es un Singleton, como en el ejemplo del profesor.

**Correcciones sobre la marcha**

- El borrado directo de los Builders fue bloqueado por seguridad, así que se guardaron con `git stash`, que se puede revertir.
- Al cambiar de rama se perdieron los XML con datos de demostración: `data/` está en `.gitignore` y la rama vieja los sobrescribió. Se recuperaron del historial de la conversación, incluida la clave ADMIN.

## Prompt 4 — Implementar la parte del Integrante 1

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Empecemos con la parte del Integrante 1; haz push primero y empezamos.”

**Qué se obtuvo.**
- **Maven multimódulo:** los módulos son `comun`, `backend`, `frontend` y `proyecto1`. Este último es temporal y compila el código del Proyecto 1 sin moverlo.
- **Módulo `comun`:**
  - El contrato `IServicioAplicacion`, que cubre todo lo que usan las fachadas `XxxModel`.
  - 13 DTO sin claves.
  - El protocolo: `Peticion`, `Respuesta`, `EventoReservaCreada`, `Operacion`, `CodigoError` y `ServicioException`.
  - `Configuracion`, con host y puerto configurables.
- **Backend:**
  - `Servidor` concurrente, con un hilo por cliente.
  - `ManejadorCliente`, con su propia sesión por conexión.
  - `Despachador`, que exige sesión y traduce errores.
  - `PublicadorEventos` (Observer), que difunde cada reserva confirmada a los demás usuarios.
  - Un servicio falso en memoria para avanzar sin Oracle.
- **Frontend:** `ProxyServicio`, con un solo hilo lector que separa las respuestas, emparejadas por id de petición, de las alertas. También tiene timeout y manejo de caídas.
- **Pruebas:** 21 nuevas, entre protocolo, Proxy e integración con sockets reales. Cubren respuestas sin mezclar entre clientes, alertas a los demás y no al autor, competencia por el último recurso, permisos, caídas y timeout. Se corrieron 5 veces seguidas sin fallos intermitentes.

**Correcciones sobre la marcha**

- En `Servidor`, un arreglo usado como truco para referenciar al manejador desde su propio callback se reemplazó por un `Consumer`.
- Capturar `NullPointerException` como "petición mal formada" escondía errores reales del servicio, así que se quitó.

## Prompt 5 — Revisión ponytail y equipo de revisores con líder

**Herramienta:** Claude Code (Claude Opus) con agentes en paralelo

> Prompt “Pasemos ponytail al terminar y pongamos a orquestar un equipo a revisar: visuales, errores, caídas, excepciones, errores de proxy ya sea de caída, timeout, etc. Que sea una organización donde cada uno reporta lo que encuentra y formas de mejorarlo, y otro agente decide qué es mejor y quién lo hace.” Luego: “pon a un agente a arreglarlo de una vez y repite hasta que no haya errores.”

**Qué se obtuvo.**
- Ponytail encontró un método sin uso, que se eliminó.
- Trabajaron cuatro revisores en paralelo (red, excepciones, contrato e integración, visual) y un líder que verificó en el código los hallazgos graves y repartió el trabajo.
- Se corrigió lo siguiente:
  - **Un cliente que no lee congelaba al autor de la reserva y a todos los demás.** El revisor lo reprodujo a las 125 reservas. Ahora cada conexión envía sus alertas por un hilo propio, y una prueba con 400 reservas lo confirma.
  - **Una reserva confirmada se reportaba como error** si fallaba la alerta posterior, y el cliente podía reintentarla y duplicarla. Ahora el fallo al publicar no afecta la respuesta.
  - **Una respuesta que no se podía serializar tumbaba la sesión.** Ahora se verifica antes de enviarla y, si falla, se responde con un error normal.
  - **Deserialización sin control.** Se agregó un filtro, con límites de tamaño y clases permitidas, en cliente y servidor, y un timeout en el saludo inicial.
  - **Mensajes de error.** El detalle técnico queda en el servidor y el usuario recibe un mensaje claro en español.
  - **Proxy:** la reconexión ya no se rompe y los nulos se manejan antes de enviar.
  - **Contrato:** la sesión y la fábrica pasaron al paquete neutral `servicio`. Quedó documentado quién escribe la sesión, la clave inicial de un funcionario y el código de error de cada caso. La alerta lleva la hora en que se emitió.
  - **Para el Integrante 3:** el helper `Tarea`, que hace las llamadas fuera del hilo de Swing para que la interfaz no se congele.
- El líder descartó lo que era sobreingeniería para un proyecto del curso.

**Correcciones sobre la marcha**

- Una validación nueva usaba `List.of(...).contains(null)`, que lanza una excepción en las listas inmutables, y 6 pruebas fallaron. Se cambió por `anyMatch(Objects::isNull)`.

## Prompt 6 — Estilo visual con capturas antes y después

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Dale, haz el commit y sigamos con lo visual.” Después de ver las capturas: “Dejémoslo así, se ve bien.”

**Qué se obtuvo.**
- Una herramienta temporal, fuera del repositorio, que abre la app real, inicia sesión sola y guarda capturas de cada pestaña y de un diálogo, con el tema actual y con el de Windows.
- La aplicación ahora arranca con el tema del sistema. Encabezados de tabla y selección usan los colores nativos, y las filas alternas son gris claro. Los cambios quedaron solo en `Main` y `EstiloUI`.
- Commits separados para la red y para lo visual, como se pidió.

**Correcciones sobre la marcha**

- Con solo activar el tema de Windows, los títulos de las columnas desaparecían: Windows ignora el fondo azul del encabezado y el texto blanco quedaba sobre blanco. Se pasó al encabezado nativo.
- El revisor visual había dicho que los botones perderían el azul. Las capturas mostraron que no era así, porque el código ya los pintaba de una forma que Windows respeta.
- La herramienta de capturas se cerró sin guardar, para no modificar los XML de datos.

## Prompt 7 — Mensajes de integración para los compañeros

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Haz push de lo que tenemos y dame un mensaje para Joshua (Integrante 2) y otro para Scott (Integrante 3) con las cosas de cada uno, lo que tienen que tener en cuenta y los cambios que hicimos que los pueden afectar, todo luego de que hagan merge con mi rama.”

**Qué se obtuvo.**
- Un mensaje para Joshua:
  - Cómo hacer el merge y dónde va su código.
  - Qué implementar: `IFabricaServicios` y el servicio real.
  - Reglas de sesión y errores, que devuelva solo DTO, el mapeo con su esquema Oracle y las reservas atómicas con bloqueo.
- Un mensaje para Scott:
  - Crear una rama nueva desde la de Steve, porque la suya estaba muy atrás.
  - Los cambios visuales que no debe deshacer.
  - Cómo probar contra el servidor falso y usar `Tarea` para no congelar la interfaz.
  - Errores por código, consultas que cambiaron y todo lo que pide el panel de alertas.

## Prompt 8 — Auditoría contra el enunciado, un agente por punto

**Herramienta:** Claude Code (Claude Opus) con 7 agentes en paralelo

> Prompt “Este sería el enunciado del proyecto: revisemos que los cambios hechos por nosotros hoy estén de acuerdo con las indicaciones. Pon un agente para cada punto del proyecto y que busque detenidamente, tómate tu tiempo.” Aclaración durante la revisión: “nuestro profesor nos indica el uso de Oracle y no MySQL.”

**Qué se obtuvo.**
- Se revisaron 7 puntos:
  1. Arquitectura distribuida
  2. Backend y notificaciones
  3. Frontend y MVC
  4. Alertas
  5. Patrón Proxy
  6. Funcionalidades 1 a 8 con validación
  7. Base de datos, pruebas y entrega
- Lo de hoy no contradice el enunciado.
- El revisor de arquitectura comprobó la arquitectura distribuida de verdad: levantó el servidor en un proceso y dos clientes en otros, uno por la IP de red y con solo `comun` y `frontend` en el classpath.
- Ajustes que le tocaban al Integrante 1:
  - La prueba de integración estaba en el frontend y le habría traído el driver de base de datos.
  - El backend no tenía pruebas propias.
  - Faltaban reglas en el contrato: cancelar solo reservas futuras, cambio de clave, rangos de fechas.
  - Las estadísticas por categoría hacían una llamada al servidor por cada categoría.
  - Hacía falta explicitar el patrón Proxy en el Javadoc.
  - El README todavía describía el Proyecto 1.
- La ambigüedad que hay que consultar con el profesor: si la alerta debe llegar a las otras ventanas del mismo usuario.

**Correcciones sobre la marcha**

- Varios revisores marcaron MySQL como incumplimiento. Se les informó que Oracle es indicación del profesor y quedó como algo resuelto, que se anota en el README.
- Un revisor dijo que `data/` no estaba en `.gitignore`. Se verificó con `git check-ignore` que sí lo estaba.

## Prompt 9 — Aplicar los ajustes de cumplimiento con un equipo orquestado

**Herramienta:** Claude Code (Claude Opus) con agentes trabajadores y un líder

> Prompt “Dale, aplica del 1 al 7 en un commit. Igual orquesta agentes para cada uno, un líder y una conversación que funcione hasta que quede resuelto, siempre cumpliendo lo que piden los enunciados.”

**Qué se obtuvo.**
- Los trabajadores se organizaron en rondas para no editar los mismos archivos:
  - **Pruebas:** `RedIT` pasó al backend y el frontend quedó dependiendo solo de `comun`. Se agregaron `DespachadorTest` y la verificación de que la alerta llega completa.
  - **README:** reescrito para el Proyecto 2, con arquitectura, diagrama del Proxy, alertas, módulos, nota sobre Oracle, orden de los scripts, ejecución, pruebas y códigos de error.
  - **Contrato y reglas:**
    - Cancelar solo reservas propias, activas y futuras.
    - Cambio de clave con sesión, que desactiva el primer ingreso.
    - Reservas entre las 7:00 y las 18:00 y nunca en el pasado.
    - Validación de rangos de fechas.
    - La nueva operación `contarPorCategoria`.
    - Los roles del patrón Proxy en el Javadoc.
    - La validación de negocio salió del Proxy y la hace el servidor.
- El líder revisó todo contra el enunciado, ejecutó los comandos del README levantando servidor y cliente, y resolvió las dudas de diseño de los trabajadores.
- Un solo commit con 99 pruebas en verde, 13 de ellas de integración.

**Correcciones sobre la marcha**

- **Estadísticas.** El líder rechazó la primera versión porque contaba reservas canceladas, incoherente con la calendarización. Ahora cuentan solo reservas activas y hay una prueba que lo verifica.
- **Javadoc.** Se reacomodaron líneas demasiado largas.
- **Sangría.** Se corrigió una línea mal sangrada en `RedIT`.
- **README.** Faltaba mencionar una prueba en la lista y se agregó.

## Prompt 10 — Publicar y actualizar los mensajes al equipo

**Herramienta:** Claude Code (Claude Opus)

> Prompt “Dale, haz push y actualiza los mensajes para Joshua y Scott.”

**Qué se obtuvo.**
- La rama `Steve` quedó publicada con tres commits: red del Integrante 1, estilo visual y ajustes de cumplimiento.
- Los mensajes a Joshua y Scott se actualizaron con lo nuevo:
  - Las reglas que el servicio real debe validar igual que el servicio falso, con sus pruebas como referencia.
  - Que las estadísticas cuentan solo reservas activas.
  - La operación `contarPorCategoria`.
  - Que el frontend depende solo de `comun`.
  - Que las validaciones ahora las hace el servidor.
  - Que los controladores deben recibir `IServicioAplicacion` y no el Proxy concreto.
  - La configuración de host y puerto para correr en máquinas distintas.

## Prompt 11 — Integrar el frontend de Scott y rondas de pruebas orquestadas

**Herramienta:** Claude Code (Claude Opus) con un equipo de agentes: testers por área, un corrector y un revisor de calidad con ponytail.

> Prompt “Scott ha trabajado su parte, esto fue lo que me envió [resumen de su rama Integrante3-frontend].” Luego: “Termina los tests con los agentes que quedaron pendientes.”

**Qué se obtuvo.**
- El frontend de Scott se integró en `Steve` por avance rápido, sin conflictos.
- Se organizó un ciclo que se repite hasta que no queden errores:
  1. Tres testers en paralelo: red (Steve), frontend (Scott) y backend con Oracle (Joshua).
  2. Un corrector aplica los arreglos, cada uno con su prueba.
  3. Un revisor de calidad con ponytail confirma y simplifica el diff.
  4. Una nueva ronda de testers.
- **Ronda 1 (8 arreglos):**
  - Red:
    - Las conexiones se cortaban cerca de 1 MB de tráfico, porque el límite del filtro de deserialización se acumulaba en toda la sesión.
    - `conectar` se podía colgar sin timeout.
    - El ciclo de `accept` podía quedar girando sin pausa.
  - Frontend:
    - Doble clic en "Guardar" enviaba dos solicitudes y duplicaba categorías.
    - Avisos de error repetidos.
    - Una respuesta vieja podía reemplazar a la más reciente.
    - Faltaba validar el rango de fechas en estadísticas.
    - Un error dentro de `Tarea` quedaba sin manejar.
- **Ronda 2 (5 arreglos en el backend):**
  - El arranque de Oracle muestra mensajes claros y sale con código de error cuando falla.
  - Los errores fuera de reservas ya no mencionan "el horario".
  - Se corrigió la concordancia de género en los mensajes.
- **Ronda final:** 0 errores nuevos en las tres áreas. Una sola conexión aguantó 100 000 peticiones, y el backend pasó 87 verificaciones contra Oracle.

**Correcciones sobre la marcha**

- Las pruebas contra Oracle no conectaban porque la clave dada era la del administrador de la base, no la del usuario del proyecto. Con autorización, se creó el usuario `RESERVAS_P2` en el Oracle local y se cargaron los scripts `01`, `02` y `06`.
- Tres pruebas de concurrencia fallaban con `ORA-12519`, porque Oracle XE se quedaba sin procesos. Se subió `processes` a 150 en la base local.
- Bajo carga (6000 consultas seguidas), cerca del 30 % fallaba porque el backend abría una conexión por operación. Se reutiliza una conexión por fábrica (`ConexionCompartida`): 0 fallos en la misma prueba.

## Prompt 12 — QA externo y una duda de diseño: ¿para qué sockets si la base es local?

**Herramienta:** Claude Code (Claude Opus) con un agente de QA externo

> Prompt “Pon un agente de QA sin conocimiento del tema a revisarlo. Y tengo una duda del diseño: ¿cuál es el punto de los sockets y el Proxy si el hilo hacia la base de datos siempre es local?” Luego: “Joshua tiene una base de datos local para esto y yo tengo otra; no existe una sola base. ¿En qué parte del diseño estamos fallando?” y “¿Qué dice el documento original sobre este problema?”

**Qué se obtuvo.**
- Un QA externo, que partió solo del README como lo haría el profesor. Compiló y ejecutó todo en una copia limpia y recorrió el sistema como usuario. Lo más visible que encontró:
  - El cambio de clave del primer ingreso solo lo exigía la interfaz.
  - Las claves se guardaban en texto plano.
  - Quedaban datos de prueba en la base.
  - El README tenía notas internas.
  - Se aceptaba como clave nueva la misma clave inicial.
- La explicación del diseño:
  - Los sockets separan a los clientes, que nunca tocan la base, del servidor, el único que habla con Oracle.
  - Así las credenciales quedan solo en el servidor, las reglas y la concurrencia se resuelven en un solo lugar y muchos usuarios trabajan desde distintas máquinas.
  - Tener una base por integrante sirve para desarrollar, pero en la defensa debe haber un solo Backend con una sola base, y los demás conectados como clientes.
- Lo que dice el enunciado: pide máquinas separadas dentro de una red y un único Backend, pero no impone una forma de despliegue. Ante la idea de una VM en Azure o Kubernetes, se recomendó la VM. Kubernetes rompe la idea de un único Backend con estado en memoria.

## Prompt 13 — Bloques A y B: endurecer seguridad y servidor, y cierre para la entrega

**Herramienta:** Claude Code (Claude Opus) con corrector, revisor de calidad con ponytail y tres testers

> Prompt “Pasemos los dos bloques seguidos, con lo que sería el servidor. Mi idea es poner el servidor en una máquina virtual de Microsoft Azure; es una milla extra pero funciona, o podemos extenderlo un poquito más con Kubernetes. Dime qué ves mejor.”

**Qué se obtuvo.**
- **Bloque A:**
  - El servidor exige el cambio de clave del primer ingreso para cualquier cliente, incluso uno que se salte la interfaz. Mientras tanto, esa conexión tampoco recibe alertas.
  - La clave nueva no puede ser el id ni la clave actual.
  - El teléfono tiene un formato validado.
  - Se reemplazó `printStackTrace` por logs.
  - Las pruebas limpian sus datos.
  - El README quedó sin notas internas.
- **Bloque B:**
  - Las claves se guardan con PBKDF2 y sal (`seguridad/ClaveHash`), sin dependencias nuevas, y la base rechaza claves en texto plano.
  - El login tarda lo mismo con usuarios inexistentes, para no revelar cuáles existen.
  - La descripción de categoría es única.
  - El servidor tiene un máximo de clientes con aviso y un plazo para las conexiones que no inician sesión.
- **Reglas compartidas:** quedaron en `comun/contrato/Reglas`, para que el servidor las aplique y el cliente las adelante.
- **Calidad:** corrigió tres errores reales antes de los testers:
  - Alertas a conexiones con cambio de clave pendiente.
  - Diferencia de tiempo en el login.
  - Un rechazo lento que podía bloquear la aceptación de conexiones.
- **Ronda final:** con los tres testers, cada uno con su propio puerto y prefijo de datos, y limpiando lo que creaba. Sin hallazgos bloqueantes ni altos.
  - El plazo sin sesión se reiniciaba con cada petición; ahora es total y hay una prueba que lo verifica.
  - El rechazo por servidor lleno espera como máximo 200 ms.
  - Se aclararon los mensajes de Oracle.
  - La interfaz quedó con tildes y ñ.
- **Decisión del profesor:** la base es local en la computadora del servidor, sin VM. Así quedó documentado en el README, junto con el uso de Oracle en lugar de MySQL.
- **Cierre para la entrega:**
  - Se retiró el módulo temporal del Proyecto 1.
  - Se ensayó el despliegue: servidor con Oracle y dos clientes en procesos separados, conectados por la IP de red y con solo el JAR del frontend. La reserva de uno llegó como alerta al otro.
  - Se armó un ZIP con fuentes, scripts y documentación, que compila desde cero.
- **Resultado:** 126 pruebas unitarias y 36 de integración con Oracle real, todas en verde.

**Correcciones sobre la marcha**

- Al recargar el esquema, el corrector borró todos los datos de la base local, no solo los de prueba. La base quedó en su estado inicial (ADMIN con primer ingreso pendiente).
- Una ronda de testers se cortó por el límite de uso y dejó datos `R2-`/`B2-` y la clave de ADMIN cambiada. Se borraron esos datos y se restauró ADMIN antes de repetir la ronda.

---

# Joshua — Integrante 2: Servicios, repositorios y Oracle

## Prompt 11 — Backend Oracle del integrante 2 (Joshua)

**Fecha:** 24 de octubre de 2026.

**Herramienta:** OpenCode (GPT-6.1 Sol).

> Completar directamente en la rama `Josua` los repositorios, servicio real,
> transacciones, reservas concurrentes, consultas y extracción mediante Gemini,
> reutilizando el contrato y servidor de Steve. Mantener Java 17 y JDBC con
> procedimientos y cursores Oracle, agregar pruebas y documentación, sin modificar
> contrato, red, Frontend ni servicio falso; no hacer commit ni push automático.

**Qué se obtuvo.**

- Se examinó la rama, el estado local, los commits, scripts SQL, contrato, DTO,
  servicio falso y pruebas de red. Los cambios locales iniciales de 63 archivos
  eran de finales de línea, sin diferencias de contenido; se conservaron.
- Repositorios e interfaces de funcionarios/autenticación, recursos/disponibilidad,
  reservas/asignaciones y consultas/estadísticas. Reutilizan las rutinas de `DB/02`
  sin cambiar sus firmas ni recrear objetos de la base.
- `ServicioAplicacion` implementa los 28 métodos del contrato, valida sesión y rol,
  campos y fechas, y utiliza `ServicioException` con traducción contextual de errores.
  El servicio solo lee el contexto de sesión.
- Reservas en una conexión y transacción: conserva categorías repetidas, selecciona
  recursos distintos, ordena las asignaciones y confirma antes de devolver éxito.
  Si falta disponibilidad no inserta cabecera. El bloqueo de una única fábrica se
  comparte entre clientes y cubre también las mutaciones administrativas.
- Agenda y calendario respetan solapamientos de intervalos y excluyen canceladas;
  estadísticas con categorías en cero, conteo distinto por categoría y semanas de
  lunes a domingo completadas con cero.
- Extractor Gemini real en Backend con API key del entorno, modelo configurable,
  timeout, JSON y errores sanitizados; una interfaz permite simularlo sin consumir
  la API en pruebas. No mantiene transacción ni bloqueo durante la llamada externa.
- `arranque.MainServidorOracle` reutiliza `red.Servidor` con la fábrica real. Maven
  copia las dependencias de ejecución, incluido el driver Oracle, al empaquetar.
- Se agregaron pruebas unitarias y pruebas Oracle optativas: commit y rollback real,
  fallo en segunda asignación, falta normal, cancelación, estadísticas, competencia
  entre dos servicios y sockets con fábrica y servicio reales. Los datos temporales
  se identifican con UUID y se limpian por sus IDs, sin utilizar el ADMIN del proyecto.
- README y `backend/IMPLEMENTACION_ORACLE.md` documentan arquitectura, comandos,
  variables, responsabilidades y límites de la validación.

**Correcciones y límites durante el trabajo.**

- La terminal WSL no tenía Java/Maven en PATH. Se utilizaron herramientas temporales
  fuera del repositorio y Java 17 con `-Dmaven.compiler.release=17`; no se cambió
  la configuración de IntelliJ ni el driver JDBC.
- La primera ejecución de Maven agotó el límite del comando durante la preparación
  y pruebas; se repitió sin ese límite. Las ejecuciones posteriores de `test` y
  `verify` terminaron correctamente; se registran los totales finales a continuación.
- Se agregó protección para no anunciar fracaso si falla el cierre de una conexión
  después de un commit exitoso; si también falla el rollback, el error es PERSISTENCIA.
- No se heredaron `DB_PASSWORD`, `ORACLE_IT` ni `GEMINI_API_KEY` de IntelliJ. No se
  buscaron contraseñas ni se hicieron llamadas reales de pago. Las IT Oracle nuevas
  requieren ejecución local; un resultado Maven exitoso con ellas omitidas no
  demuestra persistencia real ni arranque con Oracle conectado.
- No se modificaron archivos del contrato, red, Frontend, servicio falso ni pruebas
  de Steve. No se hicieron commit, push, rebase, merge ni limpiezas de trabajo.

**Resultados de verificación.**

- `mvn test -Dmaven.compiler.release=17`: 116 pruebas unitarias aprobadas,
  cero fallos, errores u omisiones (30 unitarias nuevas).
- `mvn verify -Dmaven.compiler.release=17`: las 116 unitarias y las 13 IT
  existentes de red aprobadas. Se omitieron 17 IT Oracle: 3 de categorías,
  3 de repositorios nuevos, 10 del servicio real y 1 de sockets con Oracle.
- `mvn -pl backend -am package -DskipTests -Dmaven.compiler.release=17`:
  empaquetado correcto; se verificaron `comun`, JSON y el driver Oracle en
  `backend/target/dependencias/`.
- El main con el classpath documentado detectó la falta de configuración de
  Oracle antes de aceptar clientes. Una comprobación temporal fuera del repositorio
  cargó driver/JSON y usó un socket contra `red.Servidor` con la fábrica real:
  obtuvo PERSISTENCIA sanitizado al faltar la configuración. El primer intento
  coincidió con una recompilación y no encontró una clase; al terminar la
  compilación se repitió correctamente. No demuestra acceso real a Oracle.
- La comparación de huellas de archivos respecto al inicio confirmó que las áreas
  protegidas y pruebas de Steve no cambiaron. Se revisaron los 28 métodos y no
  quedaron métodos simulados, TODO ni dependencias de Swing en el código nuevo.
- Pendiente local: ejecutar las 17 IT con Oracle y comprobar el arranque conectado;
  probar opcionalmente Gemini con clave/cuota configuradas. No se informa esas
  comprobaciones como aprobadas.
