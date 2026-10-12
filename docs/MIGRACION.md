# Migración de MVC a MVVM – pasos manuales

El proyecto ya quedó reestructurado. Como la herramienta no puede borrar archivos de su computador,
los archivos de la versión MVC se dejaron **vacíos** (solo un comentario), así no afectan la compilación.
Puede borrarlos cuando quiera; el respaldo completo está en la carpeta
`com.mycompany_perfumeriaYVI_war_1.0-SNAPSHOT_RESPALDO_MVC`.

## Carpetas y archivos para borrar

```
src/main/java/config/            (SpringConfig.java)
src/main/java/controller/        (todos los *Controller.java)
src/main/java/Filters/           (AuthFilter.java)
src/main/java/HashGenerator/     (HashGenerator.java)
src/main/java/model/             (entidades antiguas)
src/main/java/repository/        (repositorios antiguos)
src/main/java/service/           (servicios antiguos)
src/main/java/util/              (MigrarPasswords.java)
src/main/webapp/WEB-INF/lib/     (jars duplicados: Maven ya trae JSTL y el driver MySQL)
src/main/webapp/WEB-INF/beans.xml
src/main/resources/META-INF/persistence.xml
target/                          (se regenera con Clean and Build)
```

El código nuevo está en `src/main/java/com/mycompany/perfumeriayvi/`.

## Después de borrar

1. NetBeans → clic derecho al proyecto → **Clean and Build**.
2. Importe `database/perfumeriayvi_v2.sql` en phpMyAdmin (crea la base nueva y el usuario `perfumeria_app`).
3. **Run**.

## Si prefiere seguir usando la base antigua `perfumeriayvi`

La versión 2 necesita tres columnas nuevas en `users` para el bloqueo por intentos fallidos.
Ejecute en phpMyAdmin (pestaña SQL, con la base `perfumeriayvi` seleccionada):

```sql
ALTER TABLE users
  ADD COLUMN intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
  ADD COLUMN bloqueado_hasta DATETIME NULL,
  ADD COLUMN ultimo_acceso DATETIME NULL;
```

y arranque la aplicación con `DB_URL=jdbc:mysql://localhost:3306/perfumeriayvi?...`, `DB_USER=root`, `DB_PASSWORD=` (vacío).
Se recomienda la base v2 porque además trae las restricciones de integridad y el usuario de mínimo privilegio.
