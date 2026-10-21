# MediCitas · Módulo de especialidades con JDBC

Evidencia **GA7-220501096-AA2-EV01 — Codificación de módulos del software** · SENA, Análisis y Desarrollo de Software, ficha 3186702.
Autores: Carlas Elenys Montalvo Mendoza y Freyder López.

Módulo del sistema **MediCitas** (gestión de citas médicas) que administra el catálogo de
**especialidades médicas**: crear, listar, buscar, actualizar y eliminar (CRUD), conectándose por
**JDBC** a la base de datos PostgreSQL del proyecto (tabla `specialties`).

## Estructura

```
src/main/java/co/edu/sena/medicitas/
├── modelo/Especialidad.java          entidad con sus validaciones (nombre ≤ 80, descripción ≤ 255)
├── datos/ConexionBD.java             conexión JDBC; lee config/db.properties o variables de entorno
├── datos/EspecialidadDao.java        interfaz DAO con las operaciones CRUD
├── datos/EspecialidadDaoJdbc.java    implementación con PreparedStatement y try-with-resources
├── datos/DatosException.java         errores de datos con mensajes comprensibles
└── app/MenuEspecialidades.java       menú de consola (punto de entrada)
src/test/java/co/edu/sena/medicitas/
└── PruebaCrudEspecialidades.java     prueba del CRUD completo contra la base real
config/db.properties.ejemplo          plantilla de configuración (la real no se versiona)
lib/postgresql-42.7.14.jar            driver JDBC oficial (Maven Central, SHA-1 verificado)
pom.xml                               para compilar con Maven, si se prefiere
ejecutar.sh                           compila y ejecuta sin Maven
```

## Cómo ejecutarlo

Requisitos: Java 17 o superior y la base de datos de MediCitas en marcha (`docker compose up` en el proyecto MediCitas).

```bash
cp config/db.properties.ejemplo config/db.properties   # y ajuste los datos si hace falta
./ejecutar.sh            # menú de especialidades
./ejecutar.sh prueba     # prueba automática del CRUD
```

## Operaciones CRUD

| Operación | Método del DAO | SQL |
|---|---|---|
| Inserción | `insertar` | `INSERT INTO specialties (name, description) VALUES (?, ?)` |
| Consulta | `listarTodas`, `buscarPorId`, `buscarPorNombre` | `SELECT … ORDER BY name` / `WHERE id = ?` / `WHERE name ILIKE ?` |
| Actualización | `actualizar` | `UPDATE specialties SET name = ?, description = ? WHERE id = ?` |
| Eliminación | `eliminar` | `DELETE FROM specialties WHERE id = ?` |

Reglas que respeta: el nombre es obligatorio y único (SQLSTATE 23505 → «Ya existe una especialidad…»),
y una especialidad con médicos asignados no se puede eliminar (llave foránea, SQLSTATE 23503 →
«No se puede eliminar: hay médicos asignados…»).

## Estándares de codificación aplicados

| Elemento | Convención | Ejemplos |
|---|---|---|
| Paquetes | minúsculas, dominio invertido | `co.edu.sena.medicitas.datos` |
| Clases e interfaces | PascalCase, sustantivos | `Especialidad`, `EspecialidadDao`, `ConexionBD` |
| Métodos | camelCase, verbos | `insertar`, `buscarPorNombre`, `listarTodas` |
| Variables y atributos | camelCase, descriptivas | `especialidadDao`, `nombreLimpio`, `totalInicial` |
| Constantes | MAYÚSCULAS_CON_GUION_BAJO | `LONGITUD_MAXIMA_NOMBRE`, `SQL_INSERTAR` |
| Documentación | Javadoc en clases y métodos públicos | `/** Inserta la especialidad… */` |

Además: consultas parametrizadas (sin concatenar SQL), cierre de recursos con try-with-resources,
credenciales fuera del código y del repositorio, separación en capas modelo · datos · aplicación.

## Resultado de la prueba

`./ejecutar.sh prueba` (21/10/2026, PostgreSQL 16 de MediCitas): **11 verificaciones aprobadas** —
inserción con id generado, consulta por id y por nombre, listado, actualización, rechazo de nombre
repetido, bloqueo de eliminación con médicos asignados, eliminación y base de datos igual que al inicio.
