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
