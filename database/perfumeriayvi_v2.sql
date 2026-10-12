-- =====================================================================
--  PERFUMERÍA YVI - BASE DE DATOS v2 (DATOS FICTICIOS)
--  Sistema de inventario y ventas - Arquitectura MVVM
--
--  Uso en XAMPP: phpMyAdmin > Importar > seleccionar este archivo > Continuar
--  (importar como root). Crea la base perfumeriayvi_v2; NO toca la base
--  perfumeriayvi original.
--
--  Mejoras frente a la versión 1:
--   * Integridad (RNF-F3): llaves primarias, foráneas con RESTRICT, valores
--     únicos (documento, NIT, usuario) y reglas CHECK en la propia base
--     (precios > 0, venta >= compra, stock >= 0, cantidad > 0).
--   * Dinero en DECIMAL(12,2) en lugar de INT; fechas de creación y cambio.
--   * Seguridad (RNF-S1, RNF-S2, RNF-S7, RNF-S8):
--       - contraseñas solo como hash BCrypt (CHECK de formato);
--       - columnas intentos_fallidos / bloqueado_hasta / ultimo_acceso;
--       - la auditoría registra LOGIN, LOGIN_FAIL y BLOQUEO;
--       - la tabla de auditoría es inmutable (no se puede editar ni borrar);
--       - la aplicación se conecta con el usuario perfumeria_app, que solo
--         tiene los permisos mínimos y NO puede modificar la auditoría.
--   * Índices para las consultas frecuentes (RNF-R1).
--
--  Usuarios de prueba (contraseñas ficticias, cámbielas en un entorno real):
--     admin           / Admin2026*     (ADMIN)
--     ana.martinez    / Vendedor2026*  (VENDEDOR - todos los módulos)
--     carlos.rios     / Vendedor2026*  (VENDEDOR - clientes y ventas)
--     sofia.leon      / Vendedor2026*  (VENDEDOR - productos y proveedores)
--     pedro.gil       / Vendedor2026*  (VENDEDOR - inactivo)
--  Usuario de conexión de la aplicación: perfumeria_app / PerfumeriaApp2026*
-- =====================================================================

SET NAMES utf8mb4;
SET time_zone = '-05:00';

DROP DATABASE IF EXISTS perfumeriayvi_v2;
CREATE DATABASE perfumeriayvi_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE perfumeriayvi_v2;

-- ---------------------------------------------------------------------
-- USUARIOS DEL SISTEMA (RF01, RF03, RF09, RF10, RNF-S1, RNF-S2)
-- ---------------------------------------------------------------------
CREATE TABLE users (
  id                 INT          NOT NULL AUTO_INCREMENT,
  username           VARCHAR(30)  NOT NULL,
  full_name          VARCHAR(150) NOT NULL,
  role               ENUM('ADMIN','VENDEDOR') NOT NULL,
  active             TINYINT(1)   NOT NULL DEFAULT 1,
  password_hash      CHAR(60)     NOT NULL COMMENT 'Hash BCrypt, nunca la contraseña',
  acceso_clientes    TINYINT(1)   NOT NULL DEFAULT 0,
  acceso_productos   TINYINT(1)   NOT NULL DEFAULT 0,
  acceso_proveedores TINYINT(1)   NOT NULL DEFAULT 0,
  acceso_ventas      TINYINT(1)   NOT NULL DEFAULT 0,
  intentos_fallidos  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  bloqueado_hasta    DATETIME     NULL,
  ultimo_acceso      DATETIME     NULL,
  created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_users_username UNIQUE (username),
  CONSTRAINT ck_users_hash     CHECK (password_hash LIKE '$2%'),
  CONSTRAINT ck_users_intentos CHECK (intentos_fallidos <= 5)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- CLIENTES (RF04)
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
  id_cliente       INT          NOT NULL AUTO_INCREMENT,
  nombre           VARCHAR(100) NOT NULL,
  tipo_documento   VARCHAR(3)   NOT NULL,
  numero_documento VARCHAR(10)  NOT NULL,
  telefono         VARCHAR(15)  NOT NULL,
  email            VARCHAR(100) NULL,
  ciudad           VARCHAR(50)  NOT NULL,
  barrio           VARCHAR(100) NULL,
  tipo_via         VARCHAR(20)  NULL,
  numero_via       VARCHAR(10)  NULL,
  complemento      VARCHAR(50)  NULL,
  created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_cliente),
  CONSTRAINT uq_cliente_documento UNIQUE (numero_documento),
  CONSTRAINT ck_cliente_tipo_doc  CHECK (tipo_documento IN ('CC','TI','CE','PAS')),
  CONSTRAINT ck_cliente_documento CHECK (numero_documento REGEXP '^[0-9]{1,10}$'),
  CONSTRAINT ck_cliente_telefono  CHECK (telefono REGEXP '^[0-9]{7,15}$'),
  CONSTRAINT ck_cliente_nombre    CHECK (CHAR_LENGTH(nombre) >= 3)
) ENGINE=InnoDB;

CREATE INDEX ix_cliente_nombre ON cliente (nombre);

-- ---------------------------------------------------------------------
-- PROVEEDORES (RF05)
-- ---------------------------------------------------------------------
CREATE TABLE proveedor (
  id_proveedor   INT          NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(100) NOT NULL,
  nit            VARCHAR(20)  NOT NULL,
  ciudad         VARCHAR(100) NOT NULL,
  barrio         VARCHAR(100) NULL,
  tipo_via       VARCHAR(30)  NOT NULL,
  numero_via     VARCHAR(30)  NOT NULL,
  complemento    VARCHAR(100) NULL,
  tipo_proveedor VARCHAR(50)  NOT NULL,
  telefono       VARCHAR(15)  NOT NULL,
  email          VARCHAR(100) NOT NULL,
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_proveedor),
  CONSTRAINT uq_proveedor_nit      UNIQUE (nit),
  CONSTRAINT ck_proveedor_nit      CHECK (CHAR_LENGTH(nit) BETWEEN 5 AND 20),
  CONSTRAINT ck_proveedor_telefono CHECK (telefono REGEXP '^[0-9]{7,15}$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- PRODUCTOS E INVENTARIO (RF06, RF07)
-- ---------------------------------------------------------------------
CREATE TABLE producto (
  id_producto   INT           NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(100)  NOT NULL,
  marca         VARCHAR(100)  NOT NULL,
  tipo          VARCHAR(50)   NOT NULL,
  categoria     VARCHAR(50)   NOT NULL,
  descripcion   VARCHAR(255)  NOT NULL,
  precio_compra DECIMAL(12,2) NOT NULL,
  precio_venta  DECIMAL(12,2) NOT NULL,
  stock         INT           NOT NULL DEFAULT 0,
  stock_minimo  INT           NOT NULL DEFAULT 5,
  id_proveedor  INT           NOT NULL,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_producto),
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor)
      REFERENCES proveedor (id_proveedor) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT ck_producto_precio_compra CHECK (precio_compra > 0),
  CONSTRAINT ck_producto_precio_venta  CHECK (precio_venta > 0),
  CONSTRAINT ck_producto_utilidad      CHECK (precio_venta >= precio_compra),
  CONSTRAINT ck_producto_stock         CHECK (stock >= 0),
  CONSTRAINT ck_producto_stock_minimo  CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

CREATE INDEX ix_producto_nombre ON producto (nombre);

-- ---------------------------------------------------------------------
-- VENTAS (RF07, RF08)
-- ---------------------------------------------------------------------
CREATE TABLE venta (
  id_venta   INT           NOT NULL AUTO_INCREMENT,
  fecha      DATE          NOT NULL,
  id_cliente INT           NOT NULL,
  total      DECIMAL(12,2) NOT NULL DEFAULT 0,
  id_user    INT           NOT NULL,
  created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_venta),
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
      REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_venta_user FOREIGN KEY (id_user)
      REFERENCES users (id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT ck_venta_total CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE INDEX ix_venta_fecha ON venta (fecha);

CREATE TABLE detalle_venta (
  id_detalle  INT           NOT NULL AUTO_INCREMENT,
  id_venta    INT           NOT NULL,
  id_producto INT           NOT NULL,
  cantidad    INT           NOT NULL,
  subtotal    DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_detalle),
  CONSTRAINT uq_detalle_producto UNIQUE (id_venta, id_producto),
  CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta)
      REFERENCES venta (id_venta) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
      REFERENCES producto (id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- AUDITORÍA (RF11, RF12, RNF-S7, RNF-S8)
-- ---------------------------------------------------------------------
CREATE TABLE auditoria_ventas (
  id_auditoria   BIGINT       NOT NULL AUTO_INCREMENT,
  tabla_afectada VARCHAR(50)  NOT NULL,
  operacion      VARCHAR(10)  NOT NULL,
  id_registro    INT          NULL,
  usuario        VARCHAR(100) NOT NULL,
  fecha          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  detalle        TEXT         NULL,
  PRIMARY KEY (id_auditoria),
  CONSTRAINT ck_auditoria_operacion
      CHECK (operacion IN ('INSERT','UPDATE','DELETE','LOGIN','LOGIN_FAIL','BLOQUEO'))
) ENGINE=InnoDB;

CREATE INDEX ix_auditoria_fecha ON auditoria_ventas (fecha);
CREATE INDEX ix_auditoria_tabla_op ON auditoria_ventas (tabla_afectada, operacion);
CREATE INDEX ix_auditoria_usuario ON auditoria_ventas (usuario);


-- ---------------------------------------------------------------------
-- TRIGGERS DE AUDITORÍA
-- La aplicación guarda el usuario en la variable de sesión @app_user
-- antes de cada cambio (RepositorioBase.registrarUsuarioAuditoria).
-- ---------------------------------------------------------------------

DELIMITER $$
CREATE TRIGGER trg_insert_cliente AFTER INSERT ON cliente FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('cliente', 'INSERT', NEW.id_cliente, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Cliente registrado -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', Documento: ', COALESCE(NEW.numero_documento, ''), ', Telefono: ', COALESCE(NEW.telefono, ''), ', Email: ', COALESCE(NEW.email, ''), ', Ciudad: ', COALESCE(NEW.ciudad, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_update_cliente AFTER UPDATE ON cliente FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('cliente', 'UPDATE', NEW.id_cliente, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Antes -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', Documento: ', COALESCE(OLD.numero_documento, ''), ', Telefono: ', COALESCE(OLD.telefono, ''), ', Email: ', COALESCE(OLD.email, ''), ', Ciudad: ', COALESCE(OLD.ciudad, ''), ' | Después -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', Documento: ', COALESCE(NEW.numero_documento, ''), ', Telefono: ', COALESCE(NEW.telefono, ''), ', Email: ', COALESCE(NEW.email, ''), ', Ciudad: ', COALESCE(NEW.ciudad, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_cliente AFTER DELETE ON cliente FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('cliente', 'DELETE', OLD.id_cliente, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Cliente eliminado -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', Documento: ', COALESCE(OLD.numero_documento, ''), ', Telefono: ', COALESCE(OLD.telefono, ''), ', Email: ', COALESCE(OLD.email, ''), ', Ciudad: ', COALESCE(OLD.ciudad, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_insert_proveedor AFTER INSERT ON proveedor FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('proveedor', 'INSERT', NEW.id_proveedor, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Proveedor registrado -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', NIT: ', COALESCE(NEW.nit, ''), ', Ciudad: ', COALESCE(NEW.ciudad, ''), ', Tipo: ', COALESCE(NEW.tipo_proveedor, ''), ', Telefono: ', COALESCE(NEW.telefono, ''), ', Email: ', COALESCE(NEW.email, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_update_proveedor AFTER UPDATE ON proveedor FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('proveedor', 'UPDATE', NEW.id_proveedor, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Antes -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', NIT: ', COALESCE(OLD.nit, ''), ', Ciudad: ', COALESCE(OLD.ciudad, ''), ', Tipo: ', COALESCE(OLD.tipo_proveedor, ''), ', Telefono: ', COALESCE(OLD.telefono, ''), ', Email: ', COALESCE(OLD.email, ''), ' | Después -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', NIT: ', COALESCE(NEW.nit, ''), ', Ciudad: ', COALESCE(NEW.ciudad, ''), ', Tipo: ', COALESCE(NEW.tipo_proveedor, ''), ', Telefono: ', COALESCE(NEW.telefono, ''), ', Email: ', COALESCE(NEW.email, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_proveedor AFTER DELETE ON proveedor FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('proveedor', 'DELETE', OLD.id_proveedor, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Proveedor eliminado -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', NIT: ', COALESCE(OLD.nit, ''), ', Ciudad: ', COALESCE(OLD.ciudad, ''), ', Tipo: ', COALESCE(OLD.tipo_proveedor, ''), ', Telefono: ', COALESCE(OLD.telefono, ''), ', Email: ', COALESCE(OLD.email, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_insert_producto AFTER INSERT ON producto FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('producto', 'INSERT', NEW.id_producto, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Producto registrado -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', Marca: ', COALESCE(NEW.marca, ''), ', Precio Compra: ', COALESCE(NEW.precio_compra, ''), ', Precio Venta: ', COALESCE(NEW.precio_venta, ''), ', Stock: ', COALESCE(NEW.stock, ''), ', Stock Minimo: ', COALESCE(NEW.stock_minimo, ''), ', ID Proveedor: ', COALESCE(NEW.id_proveedor, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_update_producto AFTER UPDATE ON producto FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('producto', 'UPDATE', NEW.id_producto, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Antes -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', Marca: ', COALESCE(OLD.marca, ''), ', Precio Compra: ', COALESCE(OLD.precio_compra, ''), ', Precio Venta: ', COALESCE(OLD.precio_venta, ''), ', Stock: ', COALESCE(OLD.stock, ''), ', Stock Minimo: ', COALESCE(OLD.stock_minimo, ''), ', ID Proveedor: ', COALESCE(OLD.id_proveedor, ''), ' | Después -> ', 'Nombre: ', COALESCE(NEW.nombre, ''), ', Marca: ', COALESCE(NEW.marca, ''), ', Precio Compra: ', COALESCE(NEW.precio_compra, ''), ', Precio Venta: ', COALESCE(NEW.precio_venta, ''), ', Stock: ', COALESCE(NEW.stock, ''), ', Stock Minimo: ', COALESCE(NEW.stock_minimo, ''), ', ID Proveedor: ', COALESCE(NEW.id_proveedor, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_producto AFTER DELETE ON producto FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('producto', 'DELETE', OLD.id_producto, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Producto eliminado -> ', 'Nombre: ', COALESCE(OLD.nombre, ''), ', Marca: ', COALESCE(OLD.marca, ''), ', Precio Compra: ', COALESCE(OLD.precio_compra, ''), ', Precio Venta: ', COALESCE(OLD.precio_venta, ''), ', Stock: ', COALESCE(OLD.stock, ''), ', Stock Minimo: ', COALESCE(OLD.stock_minimo, ''), ', ID Proveedor: ', COALESCE(OLD.id_proveedor, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_insert_venta AFTER INSERT ON venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('venta', 'INSERT', NEW.id_venta, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Venta registrado -> ', 'Fecha: ', COALESCE(NEW.fecha, ''), ', Cliente ID: ', COALESCE(NEW.id_cliente, ''), ', Total: ', COALESCE(NEW.total, ''), ', Usuario ID: ', COALESCE(NEW.id_user, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_update_venta AFTER UPDATE ON venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('venta', 'UPDATE', NEW.id_venta, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Antes -> ', 'Fecha: ', COALESCE(OLD.fecha, ''), ', Cliente ID: ', COALESCE(OLD.id_cliente, ''), ', Total: ', COALESCE(OLD.total, ''), ', Usuario ID: ', COALESCE(OLD.id_user, ''), ' | Después -> ', 'Fecha: ', COALESCE(NEW.fecha, ''), ', Cliente ID: ', COALESCE(NEW.id_cliente, ''), ', Total: ', COALESCE(NEW.total, ''), ', Usuario ID: ', COALESCE(NEW.id_user, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_venta AFTER DELETE ON venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('venta', 'DELETE', OLD.id_venta, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Venta eliminado -> ', 'Fecha: ', COALESCE(OLD.fecha, ''), ', Cliente ID: ', COALESCE(OLD.id_cliente, ''), ', Total: ', COALESCE(OLD.total, ''), ', Usuario ID: ', COALESCE(OLD.id_user, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_insert_detalle_venta AFTER INSERT ON detalle_venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('detalle_venta', 'INSERT', NEW.id_detalle, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Detalle registrado -> ', 'Venta ID: ', COALESCE(NEW.id_venta, ''), ', Producto ID: ', COALESCE(NEW.id_producto, ''), ', Cantidad: ', COALESCE(NEW.cantidad, ''), ', Subtotal: ', COALESCE(NEW.subtotal, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_update_detalle_venta AFTER UPDATE ON detalle_venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('detalle_venta', 'UPDATE', NEW.id_detalle, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Antes -> ', 'Venta ID: ', COALESCE(OLD.id_venta, ''), ', Producto ID: ', COALESCE(OLD.id_producto, ''), ', Cantidad: ', COALESCE(OLD.cantidad, ''), ', Subtotal: ', COALESCE(OLD.subtotal, ''), ' | Después -> ', 'Venta ID: ', COALESCE(NEW.id_venta, ''), ', Producto ID: ', COALESCE(NEW.id_producto, ''), ', Cantidad: ', COALESCE(NEW.cantidad, ''), ', Subtotal: ', COALESCE(NEW.subtotal, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_detalle_venta AFTER DELETE ON detalle_venta FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('detalle_venta', 'DELETE', OLD.id_detalle, COALESCE(@app_user, 'Sistema'), NOW(),
        CONCAT('Detalle eliminado -> ', 'Venta ID: ', COALESCE(OLD.id_venta, ''), ', Producto ID: ', COALESCE(OLD.id_producto, ''), ', Cantidad: ', COALESCE(OLD.cantidad, ''), ', Subtotal: ', COALESCE(OLD.subtotal, '')));
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_insert_users AFTER INSERT ON users FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('users', 'INSERT', NEW.id, COALESCE(@app_user, 'Sistema'), NOW(),
            CONCAT('Usuario creado: ', NEW.username, ' (', NEW.role, ')'));
END$$
DELIMITER ;

-- RNF-S7: la tabla users registra inicios de sesión, intentos fallidos,
-- bloqueos, activación/bloqueo por el administrador y cambios de permisos.
DELIMITER $$
CREATE TRIGGER trg_update_users AFTER UPDATE ON users FOR EACH ROW BEGIN
    DECLARE cambios TEXT DEFAULT '';
    DECLARE op VARCHAR(10) DEFAULT 'UPDATE';

    IF NOT (OLD.ultimo_acceso <=> NEW.ultimo_acceso) THEN
        SET op = 'LOGIN';
        SET cambios = 'Inicio de sesión exitoso; ';
    END IF;

    IF NEW.bloqueado_hasta IS NOT NULL AND NOT (OLD.bloqueado_hasta <=> NEW.bloqueado_hasta) THEN
        SET op = 'BLOQUEO';
        SET cambios = CONCAT(cambios, 'Cuenta bloqueada hasta ', NEW.bloqueado_hasta, ' por intentos fallidos; ');
    ELSEIF NEW.intentos_fallidos > OLD.intentos_fallidos THEN
        SET op = 'LOGIN_FAIL';
        SET cambios = CONCAT(cambios, 'Intento fallido número ', NEW.intentos_fallidos, '; ');
    END IF;

    IF OLD.active <> NEW.active THEN
        SET op = 'UPDATE';
        SET cambios = CONCAT(cambios, IF(NEW.active = 1, 'Se activó el usuario; ', 'Se bloqueó el usuario; '));
    END IF;
    IF OLD.acceso_clientes <> NEW.acceso_clientes THEN
        SET op = 'UPDATE';
        SET cambios = CONCAT(cambios, 'Permiso clientes: ', IF(NEW.acceso_clientes = 1, 'SI', 'NO'), '; ');
    END IF;
    IF OLD.acceso_productos <> NEW.acceso_productos THEN
        SET op = 'UPDATE';
        SET cambios = CONCAT(cambios, 'Permiso productos: ', IF(NEW.acceso_productos = 1, 'SI', 'NO'), '; ');
    END IF;
    IF OLD.acceso_proveedores <> NEW.acceso_proveedores THEN
        SET op = 'UPDATE';
        SET cambios = CONCAT(cambios, 'Permiso proveedores: ', IF(NEW.acceso_proveedores = 1, 'SI', 'NO'), '; ');
    END IF;
    IF OLD.acceso_ventas <> NEW.acceso_ventas THEN
        SET op = 'UPDATE';
        SET cambios = CONCAT(cambios, 'Permiso ventas: ', IF(NEW.acceso_ventas = 1, 'SI', 'NO'), '; ');
    END IF;

    IF cambios <> '' THEN
        INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
        VALUES ('users', op, NEW.id, COALESCE(@app_user, 'Sistema'), NOW(),
                CONCAT('Usuario ', NEW.username, ' | ', cambios));
    END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_delete_users AFTER DELETE ON users FOR EACH ROW BEGIN
    INSERT INTO auditoria_ventas (tabla_afectada, operacion, id_registro, usuario, fecha, detalle)
    VALUES ('users', 'DELETE', OLD.id, COALESCE(@app_user, 'Sistema'), NOW(),
            CONCAT('Usuario eliminado: ', OLD.username));
END$$
DELIMITER ;

-- RNF-S8: el historial de auditoría no se puede modificar ni borrar.
DELIMITER $$
CREATE TRIGGER trg_auditoria_sin_cambios BEFORE UPDATE ON auditoria_ventas FOR EACH ROW BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoría es de solo lectura';
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_auditoria_sin_borrado BEFORE DELETE ON auditoria_ventas FOR EACH ROW BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoría es de solo lectura';
END$$
DELIMITER ;


-- =====================================================================
-- DATOS FICTICIOS
-- Los cambios de la carga inicial quedan en la auditoría como 'carga_inicial'.
-- =====================================================================
SET @app_user = 'carga_inicial';

INSERT INTO users (id, username, full_name, role, active, password_hash, acceso_clientes, acceso_productos, acceso_proveedores, acceso_ventas) VALUES
(1, 'admin', 'Administrador General', 'ADMIN', 1, '$2a$10$1iQP7wCAEdgLsnozfsZ/Se3tUQQuvULmh9Rn70FWATEP/8qfwgfQK', 1, 1, 1, 1),
(2, 'ana.martinez', 'Ana María Martínez', 'VENDEDOR', 1, '$2a$10$CqL6p8y2pzYEAdMFddU5eOwLIlD1Nxw5kNnFj6cciAn9fsjXWiRV6', 1, 1, 1, 1),
(3, 'carlos.rios', 'Carlos Andrés Ríos', 'VENDEDOR', 1, '$2a$10$8ivAr.u/7aqT1/bTDh/DCu.W2FcLCYA1lhW9Dssv4aLtR8kHFK0.G', 1, 0, 0, 1),
(4, 'sofia.leon', 'Sofía León Duarte', 'VENDEDOR', 1, '$2a$10$220zlClOx6fV/xftLFhvgee14cnJO5MbnjQ2JPzrq/5j17qmMIMdS', 0, 1, 1, 0),
(5, 'pedro.gil', 'Pedro José Gil', 'VENDEDOR', 0, '$2a$10$sr6lE/ygHiZqmPXuIOkHqO16f3kr5ZKI/Efqplv.u97W/u0n74TNG', 1, 0, 0, 1);

INSERT INTO proveedor (nombre, nit, ciudad, barrio, tipo_via, numero_via, complemento, tipo_proveedor, telefono, email) VALUES
('Aromas del Oriente S.A.S.', '900512340-1', 'Bucaramanga', 'Cabecera del Llano', 'Carrera', '35', '#48-12 Local 3', 'Perfumes', '6076451230', 'contacto1@aromas-proveedor.example'),
('Fragancias Andinas Ltda.', '900623451-2', 'Bogotá', 'Chapinero', 'Calle', '57', '#9-21 Of. 402', 'Perfumería', '6013456789', 'contacto2@fragancias-proveedor.example'),
('Esencias del Caribe S.A.S.', '901734562-3', 'Barranquilla', 'El Prado', 'Carrera', '54', '#68-90', 'Distribuidor', '6053214567', 'contacto3@esencias-proveedor.example'),
('Cosmética Natural Colombia', '901845673-4', 'Medellín', 'El Poblado', 'Calle', '10', '#43-55', 'Natural', '6044567812', 'contacto4@cosmetica-proveedor.example'),
('Joyas y Detalles Santander', '900956784-5', 'Floridablanca', 'Cañaveral', 'Avenida', 'Bucarica', '#4-30 Local 12', 'Accesorios', '6076789012', 'contacto5@joyas-proveedor.example'),
('Belleza Integral S.A.S.', '901067895-6', 'Cali', 'Granada', 'Avenida', '6N', '#23-40', 'Cosméticos', '6028901234', 'contacto6@belleza-proveedor.example'),
('Distribuidora La Esencia', '900178906-7', 'Bucaramanga', 'Centro', 'Calle', '36', '#15-22', 'Distribuidor', '6076012345', 'contacto7@distribuidora-proveedor.example'),
('Cuidado Personal Premium', '901289017-8', 'Pereira', 'Álamos', 'Carrera', '12', '#7-45', 'Cuidado personal', '6063123456', 'contacto8@cuidado-proveedor.example'),
('Perfumes Importados del Norte', '900390128-9', 'Cúcuta', 'Caobos', 'Avenida', '0', '#11-50', 'Perfumes', '6075234567', 'contacto9@perfumes-proveedor.example'),
('Naturaleza Viva S.A.S.', '901401239-0', 'Girón', 'Poblado', 'Transversal', '8', '#20-14', 'Natural', '6076345678', 'contacto10@naturaleza-proveedor.example'),
('Accesorios Glam Ltda.', '900512351-1', 'Bogotá', 'Usaquén', 'Carrera', '7', '#120-35', 'Accesorios', '6014567890', 'contacto11@accesorios-proveedor.example'),
('Mixtos Santander S.A.S.', '901623462-2', 'Piedecuesta', 'Centro', 'Calle', '8', '#6-18', 'Mixto', '6076456789', 'contacto12@mixtos-proveedor.example');

INSERT INTO cliente (nombre, tipo_documento, numero_documento, telefono, email, ciudad, barrio, tipo_via, numero_via, complemento) VALUES
('Laura Pérez Vargas', 'CE', '1067446522', '3190668968', 'laura.perez1@correo.example', 'Bucaramanga', 'Sotomayor', 'Diagonal', '101', '#74-71'),
('Andrés Suárez Cárdenas', 'CC', '1059202768', '3110340220', 'andres.suarez2@correo.example', 'Bucaramanga', 'Centro', 'Calle', '37', '#13-58'),
('Daniela Gómez Suárez', 'CC', '1091186040', '3238215876', 'daniela.gomez3@correo.example', 'Piedecuesta', 'Lagos', 'Avenida', '119', '#46-49'),
('Juan Mendoza Rojas', 'CC', '1097054446', '3222034628', 'juan.mendoza4@correo.example', 'Bucaramanga', 'Sotomayor', 'Avenida', '37', '#59-19'),
('Camila Cárdenas Castro', 'CC', '1003320452', '3258793812', 'camila.cardenas5@correo.example', 'Piedecuesta', 'Real de Minas', 'Calle', '52', '#75-71'),
('Felipe Suárez Pérez', 'CC', '1057102035', '3563438575', 'felipe.suarez6@correo.example', 'Bucaramanga', 'Lagos', 'Diagonal', '73', '#62-66'),
('Mariana Mendoza Gómez', 'CC', '1077244482', '3127313932', 'mariana.mendoza7@correo.example', 'Bucaramanga', 'Sotomayor', 'Calle', '92', '#80-55'),
('Carlos Suárez Torres', 'CC', '1064708126', '3126376341', 'carlos.suarez8@correo.example', 'Bucaramanga', 'La Concordia', 'Diagonal', '92', '#70-33'),
('Sofía Quintero Jiménez', 'CC', '1089892850', '3283571250', 'sofia.quintero9@correo.example', 'Bucaramanga', 'Sotomayor', 'Diagonal', '104', '#52-56'),
('Luis Díaz Castro', 'CC', '1036109392', '3014443850', 'luis.diaz10@correo.example', 'Floridablanca', 'El Prado', 'Diagonal', '103', '#67-63'),
('Andrea Moreno Díaz', 'PAS', '103491144', '3578621065', 'andrea.moreno11@correo.example', 'Piedecuesta', 'Real de Minas', 'Transversal', '85', '#5-38'),
('David Vargas Ortiz', 'CC', '1013203533', '3579145394', 'david.vargas12@correo.example', 'Girón', 'La Concordia', 'Carrera', '55', '#94-62'),
('Natalia Rojas Ramírez', 'CC', '1066896901', '3222973827', 'natalia.rojas13@correo.example', 'Floridablanca', 'Real de Minas', 'Carrera', '58', '#2-12'),
('Jorge Gómez Ortiz', 'CC', '1077932180', '3091890761', 'jorge.gomez14@correo.example', 'Floridablanca', 'Provenza', 'Calle', '80', '#6-97'),
('Paula Quintero Ramírez', 'CC', '1089721481', '3230771657', 'paula.quintero15@correo.example', 'Bucaramanga', 'Cañaveral', 'Transversal', '74', '#99-63'),
('Miguel Torres Quintero', 'CC', '1029920845', '3588911235', 'miguel.torres16@correo.example', 'Floridablanca', 'El Prado', 'Transversal', '38', '#11-54'),
('Valentina Herrera Torres', 'CC', '1078655506', '3278142594', 'valentina.herrera17@correo.example', 'Piedecuesta', 'Real de Minas', 'Carrera', '67', '#26-49'),
('Santiago Gómez Pérez', 'CC', '1052257400', '3016895485', 'santiago.gomez18@correo.example', 'Girón', 'Cañaveral', 'Calle', '4', '#66-61'),
('Isabella Vargas Castro', 'CC', '1060416819', '3226290387', 'isabella.vargas19@correo.example', 'Piedecuesta', 'La Concordia', 'Transversal', '80', '#22-20'),
('Mateo Cárdenas Mendoza', 'CC', '1027781143', '3593663689', 'mateo.cardenas20@correo.example', 'Girón', 'La Concordia', 'Transversal', '30', '#87-91'),
('Gabriela Rojas Quintero', 'CE', '1053573955', '3196869801', 'gabriela.rojas21@correo.example', 'Floridablanca', 'Cabecera', 'Transversal', '120', '#7-58'),
('Sebastián Díaz Suárez', 'CC', '1095421488', '3027332847', 'sebastian.diaz22@correo.example', 'Bucaramanga', 'Sotomayor', 'Carrera', '101', '#67-18'),
('Lucía Herrera Pineda', 'CC', '1052341080', '3267090084', 'lucia.herrera23@correo.example', 'Bucaramanga', 'Real de Minas', 'Calle', '50', '#40-24'),
('Tomás Moreno Herrera', 'CC', '1035358813', '3576787065', 'tomas.moreno24@correo.example', 'Piedecuesta', 'Lagos', 'Diagonal', '23', '#42-67'),
('Manuela Suárez Ortiz', 'CC', '1016120303', '3222692439', 'manuela.suarez25@correo.example', 'Floridablanca', 'Provenza', 'Calle', '12', '#59-83'),
('Nicolás Vargas Ruiz', 'CC', '1073156735', '3113648678', 'nicolas.vargas26@correo.example', 'Girón', 'San Alonso', 'Avenida', '72', '#88-74'),
('Juliana Quintero Díaz', 'CC', '1014722812', '3284598180', 'juliana.quintero27@correo.example', 'Bucaramanga', 'Provenza', 'Transversal', '38', '#73-35'),
('Alejandro Quintero Torres', 'CC', '1013617158', '3222942714', 'alejandro.quintero28@correo.example', 'Piedecuesta', 'San Alonso', 'Calle', '88', '#8-98'),
('Sara Moreno Herrera', 'CC', '1086697188', '3077998284', 'sara.moreno29@correo.example', 'Girón', 'Lagos', 'Transversal', '108', '#9-38'),
('Esteban Vargas Herrera', 'CC', '1035011808', '3218439487', 'esteban.vargas30@correo.example', 'Bucaramanga', 'Real de Minas', 'Avenida', '54', '#2-8'),
('Laura Pérez Sánchez', 'PAS', '100308401', '3022599037', 'laura.perez31@correo.example', 'Bucaramanga', 'Sotomayor', 'Transversal', '120', '#45-41'),
('Andrés Ortiz Martínez', 'CC', '1054249537', '3291575609', 'andres.ortiz32@correo.example', 'Floridablanca', 'El Prado', 'Diagonal', '55', '#87-16'),
('Daniela Moreno Mendoza', 'CC', '1091456455', '3262217045', 'daniela.moreno33@correo.example', 'Girón', 'Cañaveral', 'Calle', '95', '#92-56'),
('Juan Gómez Castro', 'CC', '1093321670', '3248863507', 'juan.gomez34@correo.example', 'Bucaramanga', 'Centro', 'Avenida', '99', '#54-41'),
('Camila Cárdenas Gómez', 'CC', '1072767058', '3236501061', 'camila.cardenas35@correo.example', 'Bucaramanga', 'Cabecera', 'Calle', '21', '#80-16'),
('Felipe Pérez Martínez', 'CC', '1016868746', '3290121624', 'felipe.perez36@correo.example', 'Bucaramanga', 'Lagos', 'Calle', '55', '#75-17'),
('Mariana Moreno Ruiz', 'CC', '1051553823', '3542119104', 'mariana.moreno37@correo.example', 'Piedecuesta', 'Provenza', 'Carrera', '24', '#70-4'),
('Carlos Quintero Pineda', 'CC', '1053265435', '3229821727', 'carlos.quintero38@correo.example', 'Floridablanca', 'Cabecera', 'Calle', '36', '#70-62'),
('Sofía Ruiz Ramírez', 'CC', '1042244317', '3194124685', 'sofia.ruiz39@correo.example', 'Bucaramanga', 'Centro', 'Carrera', '71', '#5-84'),
('Luis Mendoza Ramírez', 'CC', '1045729726', '3217550517', 'luis.mendoza40@correo.example', 'Bucaramanga', 'Cabecera', 'Carrera', '61', '#8-29');

INSERT INTO producto (nombre, marca, tipo, categoria, descripcion, precio_compra, precio_venta, stock, stock_minimo, id_proveedor) VALUES
('Aqua Brisa 100 ml', 'Aromas YVI', 'Perfume', 'Réplica Triple A', 'Fragancia fresca acuática para hombre', 28000.00, 45000.00, 10, 5, 1),
('Noche Dorada 100 ml', 'Aromas YVI', 'Perfume', 'Réplica Triple A', 'Fragancia oriental dulce para mujer', 28000.00, 45000.00, 13, 5, 1),
('Madera Salvaje 100 ml', 'Aromas YVI', 'Perfume', 'Réplica Triple A', 'Fragancia amaderada intensa', 30000.00, 48000.00, 10, 5, 1),
('Flor de Cereza 100 ml', 'Aromas YVI', 'Perfume', 'Réplica Triple A', 'Fragancia floral frutal juvenil', 27000.00, 44000.00, 12, 5, 9),
('Cuero Imperial 100 ml', 'Aromas YVI', 'Perfume', 'Réplica Triple A', 'Fragancia de cuero y especias', 32000.00, 52000.00, 36, 5, 9),
('Vainilla Suave 60 ml', 'Esencia Pura', 'Perfume', 'Preparado', 'Perfume preparado de vainilla', 15000.00, 28000.00, 14, 5, 7),
('Coco Tropical 60 ml', 'Esencia Pura', 'Perfume', 'Preparado', 'Perfume preparado de coco', 15000.00, 28000.00, 3, 5, 7),
('Frutos Rojos 60 ml', 'Esencia Pura', 'Perfume', 'Preparado', 'Perfume preparado de frutos rojos', 15000.00, 28000.00, 37, 5, 3),
('Chocolate Intenso 60 ml', 'Esencia Pura', 'Perfume', 'Preparado', 'Perfume preparado gourmand', 15000.00, 28000.00, 38, 5, 3),
('Brisa Marina 60 ml', 'Esencia Pura', 'Perfume', 'Preparado', 'Perfume preparado cítrico marino', 15000.00, 28000.00, 9, 5, 7),
('Elegance Homme 100 ml', 'Maison Lumière', 'Perfume', 'Original', 'Eau de parfum original importada', 210000.00, 320000.00, 17, 5, 2),
('Rose Velours 90 ml', 'Maison Lumière', 'Perfume', 'Original', 'Eau de parfum floral original', 230000.00, 345000.00, 28, 5, 2),
('Ocean Sport 100 ml', 'Blue Coast', 'Perfume', 'Original', 'Eau de toilette deportiva original', 180000.00, 275000.00, 16, 5, 9),
('Ámbar Real 75 ml', 'Royal Scents', 'Perfume', 'Original', 'Eau de parfum ambarada original', 250000.00, 380000.00, 0, 5, 2),
('Crema Hidratante Karité', 'NaturaPiel', 'Cosmético', 'Cuidado facial', 'Crema hidratante con manteca de karité', 18000.00, 32000.00, 14, 5, 6),
('Sérum Vitamina C', 'NaturaPiel', 'Cosmético', 'Cuidado facial', 'Sérum antioxidante 30 ml', 25000.00, 45000.00, 33, 5, 6),
('Protector Solar FPS 50', 'SolCare', 'Cosmético', 'Cuidado facial', 'Protector solar toque seco 50 ml', 22000.00, 38000.00, 13, 5, 6),
('Labial Mate Rubí', 'Glam Color', 'Cosmético', 'Maquillaje', 'Labial de larga duración tono rubí', 9000.00, 18000.00, 18, 5, 6),
('Base Líquida Natural', 'Glam Color', 'Cosmético', 'Maquillaje', 'Base líquida acabado natural 30 ml', 21000.00, 36000.00, 24, 5, 12),
('Shampoo Argán 400 ml', 'CabelloSano', 'Cuidado personal', 'Cabello', 'Shampoo nutritivo con aceite de argán', 14000.00, 26000.00, 9, 5, 8),
('Acondicionador Argán 400 ml', 'CabelloSano', 'Cuidado personal', 'Cabello', 'Acondicionador reparador', 14000.00, 26000.00, 0, 5, 8),
('Desodorante Roll-on Fresh', 'DermaFresh', 'Cuidado personal', 'Higiene', 'Desodorante sin alcohol 50 ml', 7000.00, 13000.00, 12, 5, 8),
('Jabón Artesanal Avena', 'Naturaleza Viva', 'Cuidado personal', 'Higiene', 'Jabón artesanal de avena y miel', 5000.00, 11000.00, 15, 5, 10),
('Aceite de Coco Orgánico', 'Naturaleza Viva', 'Natural', 'Aceites', 'Aceite de coco extra virgen 250 ml', 16000.00, 29000.00, 29, 5, 10),
('Aceite Esencial Lavanda', 'Naturaleza Viva', 'Natural', 'Aceites', 'Aceite esencial puro 15 ml', 12000.00, 24000.00, 17, 5, 4),
('Colágeno Hidrolizado', 'VitalPlus', 'Suplemento', 'Bienestar', 'Colágeno hidrolizado en polvo 500 g', 42000.00, 68000.00, 27, 5, 4),
('Biotina 5000 mcg', 'VitalPlus', 'Suplemento', 'Bienestar', 'Suplemento para cabello y uñas', 20000.00, 36000.00, 38, 5, 4),
('Omega 3 Premium', 'VitalPlus', 'Suplemento', 'Bienestar', 'Omega 3 de aceite de pescado', 26000.00, 44000.00, 1, 5, 4),
('Magnesio Citrato', 'VitalPlus', 'Suplemento', 'Bienestar', 'Magnesio de alta absorción', 19000.00, 33000.00, 28, 5, 10),
('Vitamina C 1000 mg', 'VitalPlus', 'Suplemento', 'Bienestar', 'Vitamina C con zinc', 15000.00, 27000.00, 37, 5, 10),
('Pulsera Acero Clásica', 'Glam Accesorios', 'Accesorio', 'Joyería', 'Pulsera en acero inoxidable', 18000.00, 35000.00, 23, 5, 5),
('Collar Corazón Plata', 'Glam Accesorios', 'Accesorio', 'Joyería', 'Collar con dije de corazón', 25000.00, 48000.00, 33, 5, 11),
('Aretes Dorados Aro', 'Glam Accesorios', 'Accesorio', 'Joyería', 'Aretes de aro bañados en oro', 16000.00, 30000.00, 23, 5, 11),
('Anillo Ajustable Luna', 'Glam Accesorios', 'Accesorio', 'Joyería', 'Anillo ajustable con diseño de luna', 12000.00, 24000.00, 40, 5, 5),
('Estuche Regalo Perfume', 'Aromas YVI', 'Accesorio', 'Empaques', 'Caja de regalo con moño', 6000.00, 12000.00, 2, 5, 12),
('Atomizador Viajero 10 ml', 'Esencia Pura', 'Accesorio', 'Empaques', 'Atomizador recargable de bolsillo', 5000.00, 10000.00, 34, 5, 12),
('Kit Spa Relajante', 'Naturaleza Viva', 'Cuidado personal', 'Kits', 'Sales, aceite y vela aromática', 38000.00, 65000.00, 10, 5, 10),
('Body Splash Frutal 250 ml', 'Esencia Pura', 'Perfume', 'Body splash', 'Body splash de frutas tropicales', 13000.00, 25000.00, 39, 5, 7),
('Body Splash Floral 250 ml', 'Esencia Pura', 'Perfume', 'Body splash', 'Body splash de flores blancas', 13000.00, 25000.00, 23, 5, 7),
('Crema Corporal Almendras', 'NaturaPiel', 'Cuidado personal', 'Cuerpo', 'Crema corporal con aceite de almendras', 17000.00, 30000.00, 19, 5, 8);

INSERT INTO venta (id_venta, fecha, id_cliente, total, id_user) VALUES
(1, '2026-01-10', 10, 132000.00, 3),
(2, '2026-01-13', 23, 181000.00, 3),
(3, '2026-01-15', 11, 48000.00, 3),
(4, '2026-01-18', 34, 345000.00, 3),
(5, '2026-01-21', 24, 150000.00, 3),
(6, '2026-01-26', 20, 136000.00, 3),
(7, '2026-01-31', 34, 196000.00, 2),
(8, '2026-02-05', 17, 75000.00, 2),
(9, '2026-02-07', 11, 60000.00, 2),
(10, '2026-02-11', 13, 182000.00, 1),
(11, '2026-02-16', 2, 40000.00, 2),
(12, '2026-02-17', 33, 133000.00, 1),
(13, '2026-02-22', 10, 174000.00, 3),
(14, '2026-02-23', 25, 119000.00, 2),
(15, '2026-02-28', 34, 79000.00, 2),
(16, '2026-03-01', 6, 102000.00, 3),
(17, '2026-03-05', 33, 148000.00, 3),
(18, '2026-03-07', 13, 22000.00, 3),
(19, '2026-03-11', 38, 56000.00, 3),
(20, '2026-03-16', 9, 82000.00, 3),
(21, '2026-03-20', 17, 127000.00, 2),
(22, '2026-03-23', 19, 339000.00, 3),
(23, '2026-03-26', 27, 144000.00, 2),
(24, '2026-03-31', 34, 37000.00, 2),
(25, '2026-04-02', 1, 237000.00, 2),
(26, '2026-04-06', 31, 189000.00, 3),
(27, '2026-04-07', 18, 760000.00, 2),
(28, '2026-04-12', 18, 145000.00, 3),
(29, '2026-04-13', 7, 113000.00, 2),
(30, '2026-04-15', 20, 157000.00, 1),
(31, '2026-04-16', 18, 1035000.00, 3),
(32, '2026-04-21', 15, 113000.00, 3),
(33, '2026-04-25', 21, 48000.00, 2),
(34, '2026-04-26', 31, 604000.00, 2),
(35, '2026-05-01', 26, 25000.00, 2),
(36, '2026-05-05', 5, 167000.00, 3),
(37, '2026-05-10', 39, 65000.00, 2),
(38, '2026-05-11', 36, 96000.00, 2),
(39, '2026-05-16', 14, 1230000.00, 1),
(40, '2026-05-21', 9, 54000.00, 3),
(41, '2026-05-25', 9, 369000.00, 3),
(42, '2026-05-30', 13, 142000.00, 2),
(43, '2026-05-31', 18, 56000.00, 2),
(44, '2026-06-05', 23, 1305000.00, 2),
(45, '2026-06-07', 10, 138000.00, 3),
(46, '2026-06-12', 24, 81000.00, 3),
(47, '2026-06-16', 10, 30000.00, 2),
(48, '2026-06-20', 1, 33000.00, 1),
(49, '2026-06-23', 22, 30000.00, 1),
(50, '2026-06-24', 28, 320000.00, 3),
(51, '2026-06-26', 32, 676000.00, 2),
(52, '2026-06-30', 30, 1284000.00, 2),
(53, '2026-07-01', 14, 56000.00, 2),
(54, '2026-07-06', 11, 1140000.00, 1),
(55, '2026-07-10', 13, 10000.00, 3),
(56, '2026-07-13', 19, 205000.00, 2),
(57, '2026-07-16', 23, 28000.00, 3),
(58, '2026-07-21', 15, 147000.00, 2),
(59, '2026-07-22', 36, 110000.00, 2),
(60, '2026-07-27', 34, 36000.00, 3);

INSERT INTO detalle_venta (id_venta, id_producto, cantidad, subtotal) VALUES
(1, 24, 2, 58000.00),
(1, 15, 2, 64000.00),
(1, 36, 1, 10000.00),
(2, 30, 3, 81000.00),
(2, 36, 3, 30000.00),
(2, 31, 2, 70000.00),
(3, 32, 1, 48000.00),
(4, 12, 1, 345000.00),
(5, 17, 1, 38000.00),
(5, 9, 3, 84000.00),
(5, 7, 1, 28000.00),
(6, 26, 2, 136000.00),
(7, 7, 3, 84000.00),
(7, 6, 2, 56000.00),
(7, 10, 2, 56000.00),
(8, 38, 3, 75000.00),
(9, 35, 2, 24000.00),
(9, 19, 1, 36000.00),
(10, 31, 3, 105000.00),
(10, 28, 1, 44000.00),
(10, 23, 3, 33000.00),
(11, 35, 1, 12000.00),
(11, 10, 1, 28000.00),
(12, 5, 2, 104000.00),
(12, 24, 1, 29000.00),
(13, 16, 2, 90000.00),
(13, 9, 2, 56000.00),
(13, 7, 1, 28000.00),
(14, 20, 2, 52000.00),
(14, 8, 2, 56000.00),
(14, 23, 1, 11000.00),
(15, 30, 2, 54000.00),
(15, 38, 1, 25000.00),
(16, 17, 1, 38000.00),
(16, 15, 2, 64000.00),
(17, 21, 2, 52000.00),
(17, 32, 2, 96000.00),
(18, 23, 2, 22000.00),
(19, 10, 2, 56000.00),
(20, 22, 2, 26000.00),
(20, 8, 2, 56000.00),
(21, 18, 1, 18000.00),
(21, 15, 3, 96000.00),
(21, 22, 1, 13000.00),
(22, 26, 3, 204000.00),
(22, 2, 3, 135000.00),
(23, 32, 3, 144000.00),
(24, 23, 1, 11000.00),
(24, 22, 2, 26000.00),
(25, 17, 3, 114000.00),
(25, 9, 3, 84000.00),
(25, 22, 3, 39000.00),
(26, 1, 1, 45000.00),
(26, 3, 3, 144000.00),
(27, 14, 2, 760000.00),
(28, 24, 3, 87000.00),
(28, 6, 1, 28000.00),
(28, 36, 3, 30000.00),
(29, 15, 1, 32000.00),
(29, 30, 3, 81000.00),
(30, 27, 3, 108000.00),
(30, 34, 1, 24000.00),
(30, 38, 1, 25000.00),
(31, 12, 3, 1035000.00),
(32, 22, 3, 39000.00),
(32, 23, 2, 22000.00),
(32, 21, 2, 52000.00),
(33, 32, 1, 48000.00),
(34, 30, 2, 54000.00),
(34, 13, 2, 550000.00),
(35, 38, 1, 25000.00),
(36, 35, 3, 36000.00),
(36, 20, 1, 26000.00),
(36, 31, 3, 105000.00),
(37, 21, 2, 52000.00),
(37, 22, 1, 13000.00),
(38, 34, 3, 72000.00),
(38, 25, 1, 24000.00),
(39, 16, 2, 90000.00),
(39, 14, 3, 1140000.00),
(40, 18, 3, 54000.00),
(41, 21, 3, 78000.00),
(41, 24, 3, 87000.00),
(41, 26, 3, 204000.00),
(42, 36, 3, 30000.00),
(42, 10, 3, 84000.00),
(42, 9, 1, 28000.00),
(43, 9, 2, 56000.00),
(44, 11, 3, 960000.00),
(44, 12, 1, 345000.00),
(45, 40, 1, 30000.00),
(45, 32, 2, 96000.00),
(45, 35, 1, 12000.00),
(46, 30, 3, 81000.00),
(47, 33, 1, 30000.00),
(48, 23, 3, 33000.00),
(49, 33, 1, 30000.00),
(50, 11, 1, 320000.00),
(51, 11, 2, 640000.00),
(51, 35, 3, 36000.00),
(52, 14, 3, 1140000.00),
(52, 20, 3, 78000.00),
(52, 29, 2, 66000.00),
(53, 6, 2, 56000.00),
(54, 14, 3, 1140000.00),
(55, 36, 1, 10000.00),
(56, 1, 3, 135000.00),
(56, 31, 2, 70000.00),
(57, 7, 1, 28000.00),
(58, 25, 3, 72000.00),
(58, 39, 3, 75000.00),
(59, 6, 2, 56000.00),
(59, 30, 2, 54000.00),
(60, 18, 2, 36000.00);

SET @app_user = NULL;

-- =====================================================================
-- RNF-S8: USUARIOS DE BASE DE DATOS CON MÍNIMO PRIVILEGIO
-- perfumeria_app: lo usa la aplicación. Puede leer y escribir los datos del
--   negocio, pero sobre la auditoría solo tiene SELECT (los triggers escriben
--   en ella con los permisos de su creador). No puede crear ni borrar tablas.
-- perfumeria_lectura: solo lectura, para reportes o revisiones.
-- =====================================================================
CREATE USER IF NOT EXISTS 'perfumeria_app'@'localhost' IDENTIFIED BY 'PerfumeriaApp2026*';
CREATE USER IF NOT EXISTS 'perfumeria_app'@'127.0.0.1' IDENTIFIED BY 'PerfumeriaApp2026*';
CREATE USER IF NOT EXISTS 'perfumeria_lectura'@'localhost' IDENTIFIED BY 'LecturaYVI2026*';

GRANT SELECT, INSERT, UPDATE         ON perfumeriayvi_v2.users            TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.cliente          TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.proveedor        TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.producto         TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.venta            TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON perfumeriayvi_v2.detalle_venta    TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT                         ON perfumeriayvi_v2.auditoria_ventas TO 'perfumeria_app'@'localhost', 'perfumeria_app'@'127.0.0.1';
GRANT SELECT ON perfumeriayvi_v2.* TO 'perfumeria_lectura'@'localhost';
FLUSH PRIVILEGES;
