-- ============================================================================
-- Base de datos SGFP - Sistema de Gestion de Finanzas Personales
-- Script para crear la base en OTRO equipo (MySQL 8.x, motor InnoDB).
--
-- Reconstruido a partir del diagrama entidad-relacion de la base sgfp.
-- Es seguro ejecutarlo mas de una vez: no borra nada. Solo crea lo que falta
-- e inserta los datos de ejemplo si la tabla esta vacia.
--
-- Uso (consola):  mysql -u root -p < sql/sgfp_completo.sql
-- Uso (DBeaver):  abrir este archivo en el editor SQL y ejecutarlo como script
--                 (Alt + X).
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sgfp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgfp;

-- ---------------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario     INT          NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    correo         VARCHAR(100) NOT NULL,
    fecha_registro DATE         NOT NULL,
    PRIMARY KEY (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cuentas (
    id_cuenta     INT          NOT NULL AUTO_INCREMENT,
    nombre_cuenta VARCHAR(100) NOT NULL,
    tipo_cuenta   VARCHAR(50)  NOT NULL,
    banco         VARCHAR(100) NOT NULL,
    id_usuario    INT          NOT NULL,
    PRIMARY KEY (id_cuenta),
    CONSTRAINT fk_cuentas_usuarios
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS medios_pago (
    id_medio_pago      INT          NOT NULL AUTO_INCREMENT,
    nombre_medio_pago  VARCHAR(100) NOT NULL,
    entidad_financiera VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_medio_pago)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria     INT          NOT NULL AUTO_INCREMENT,
    nombre_categoria VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_categoria)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS movimientos (
    id_movimiento INT           NOT NULL AUTO_INCREMENT,
    fecha         DATE          NOT NULL,
    descripcion   VARCHAR(150)  NOT NULL,
    monto_total   DECIMAL(12,2) NOT NULL,
    id_cuenta     INT           NOT NULL,
    id_medio_pago INT           NOT NULL,
    PRIMARY KEY (id_movimiento),
    CONSTRAINT fk_movimientos_cuentas
        FOREIGN KEY (id_cuenta) REFERENCES cuentas (id_cuenta),
    CONSTRAINT fk_movimientos_medios_pago
        FOREIGN KEY (id_medio_pago) REFERENCES medios_pago (id_medio_pago)
) ENGINE = InnoDB;

-- Relaciona cada movimiento con sus categorias (relacion muchos a muchos).
CREATE TABLE IF NOT EXISTS detalle_categoria (
    id_movimiento  INT           NOT NULL,
    id_categoria   INT           NOT NULL,
    monto_asignado DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id_movimiento, id_categoria),
    CONSTRAINT fk_detalle_movimientos
        FOREIGN KEY (id_movimiento) REFERENCES movimientos (id_movimiento),
    CONSTRAINT fk_detalle_categorias
        FOREIGN KEY (id_categoria) REFERENCES categorias (id_categoria)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Datos de ejemplo (solo se insertan si la tabla esta vacia)
-- ---------------------------------------------------------------------------

INSERT INTO usuarios (nombre, correo, fecha_registro)
SELECT 'Eddy Alexander Altamar Angulo', 'altamar786@gmail.com', '2026-11-01'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuarios);

INSERT INTO cuentas (nombre_cuenta, tipo_cuenta, banco, id_usuario)
SELECT 'Ahorros BBVA', 'Ahorros', 'BBVA', (SELECT MIN(id_usuario) FROM usuarios)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cuentas);

INSERT INTO medios_pago (nombre_medio_pago, entidad_financiera)
SELECT 'Tarjeta Débito', 'BBVA'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM medios_pago);

INSERT INTO categorias (nombre_categoria)
SELECT nombre
FROM (SELECT 'Mercado' AS nombre
      UNION ALL SELECT 'Transporte'
      UNION ALL SELECT 'Salario') AS nuevas
WHERE NOT EXISTS (SELECT 1 FROM categorias);

INSERT INTO movimientos (fecha, descripcion, monto_total, id_cuenta, id_medio_pago)
SELECT fecha, descripcion, monto, (SELECT MIN(id_cuenta) FROM cuentas),
       (SELECT MIN(id_medio_pago) FROM medios_pago)
FROM (SELECT '2026-11-01' AS fecha, 'Pago de salario' AS descripcion, 1800000.00 AS monto
      UNION ALL SELECT '2026-11-03', 'Recarga tarjeta de bus', 50000.00
      UNION ALL SELECT '2026-11-05', 'Mercado de la semana', 120000.00) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM movimientos);

INSERT INTO detalle_categoria (id_movimiento, id_categoria, monto_asignado)
SELECT m.id_movimiento, c.id_categoria, m.monto_total
FROM movimientos m
JOIN categorias c
  ON (m.descripcion = 'Pago de salario'         AND c.nombre_categoria = 'Salario')
  OR (m.descripcion = 'Recarga tarjeta de bus'  AND c.nombre_categoria = 'Transporte')
  OR (m.descripcion = 'Mercado de la semana'    AND c.nombre_categoria = 'Mercado')
WHERE NOT EXISTS (SELECT 1 FROM detalle_categoria);
