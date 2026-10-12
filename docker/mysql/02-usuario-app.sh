#!/bin/sh
# RNF-S8: dentro de Docker la aplicación se conecta desde otro contenedor,
# por eso se crea el usuario perfumeria_app para cualquier host ('%'),
# con los mismos permisos mínimos que en XAMPP.
set -e
mysql -uroot -p"$MARIADB_ROOT_PASSWORD" <<SQL
CREATE USER IF NOT EXISTS 'perfumeria_app'@'%' IDENTIFIED BY '${APP_DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE         ON perfumeriayvi_v2.users            TO 'perfumeria_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.cliente          TO 'perfumeria_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.proveedor        TO 'perfumeria_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.producto         TO 'perfumeria_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.venta            TO 'perfumeria_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.detalle_venta    TO 'perfumeria_app'@'%';
GRANT SELECT                         ON perfumeriayvi_v2.auditoria_ventas TO 'perfumeria_app'@'%';
FLUSH PRIVILEGES;
SQL
