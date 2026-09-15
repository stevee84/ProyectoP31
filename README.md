# Sistema de Reserva de Recursos

Proyecto del curso EIF206 Programación 3 — Universidad Nacional de Costa Rica.

Aplicación de escritorio en Java Swing para gestionar la reserva de recursos
universitarios (salas, equipos, laptops) por parte de funcionarios.

## Requisitos

- Java 17+
- Maven 3.8+

## Cómo ejecutar

```bash
mvn compile exec:java -Dexec.mainClass="Main"
```

## Credenciales por defecto

| Usuario | Contraseña | Rol           |
|---------|------------|---------------|
| ADMIN   | ADMIN      | Administrador |

Los funcionarios se crean desde el panel de administración. Al primer login,
el sistema solicita cambio de contraseña.

## Funcionalidades

### Administrador
- Gestión de funcionarios (CRUD)
- Gestión de categorías de recursos (CRUD)
- Gestión de recursos (CRUD con filtro por categoría)

### Funcionario
- Crear reservaciones seleccionando categorías, fecha y horario
- Extracción de datos con IA (Gemini, requiere `GEMINI_API_KEY`)
- Cancelar reservaciones propias

### Común (todos los roles)
- Calendarización: grilla de disponibilidad por categoría, fecha y hora
- Agenda semanal: vista de actividades por día y hora con navegación
- Estadísticas de actividades: conteo por semana con gráfico de barras
- Estadísticas de recursos: uso por categoría en rango de fechas
- Exportación a PDF en todos los reportes
- Búsqueda en tiempo real en todas las tablas

## Arquitectura

```
model/        → Entidades de dominio + Fachadas XxxModel (wrappean services)
repository/   → Persistencia XML con DOM
service/      → Lógica de negocio, validación de sesión, auto-persistencia
controller/   → Solo 2 campos: view + modelo (XxxModel)
view/         → Swing (EstiloUI, BarraBusquedaTabla, BotonTablaRenderer, Iconos)
consulta/     → Interfaces y adaptadores para consultas desacopladas
exception/    → Excepciones de dominio
```

El patrón de capas es: **Service ← Model (fachada) ← Controller (view + modelo)**.
Los controllers nunca importan `service.*` directamente.

## Tecnologías

- Java 17, Maven
- Swing (interfaz gráfica)
- DOM/XML (persistencia en `data/`)
- Graphics2D (iconos generados por código)
- iTextPDF 5.5.13.4 (exportación PDF)
- JUnit Jupiter 5.10.2 (54 tests)
- org.json + Gemini API (extracción con IA, opcional)

## Tests

```bash
mvn test
```
