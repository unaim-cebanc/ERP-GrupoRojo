CREATE DATABASE  IF NOT EXISTS `decasport` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `decasport`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: decasport
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `almacenes`
--

DROP TABLE IF EXISTS `almacenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacenes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `codigo` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `ciudad` varchar(60) NOT NULL,
  `pais` char(2) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_almacenes_codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacenes`
--

LOCK TABLES `almacenes` WRITE;
/*!40000 ALTER TABLE `almacenes` DISABLE KEYS */;
INSERT INTO `almacenes` VALUES (1,'IRN','Almacén Irun',NULL,'Irun','ES',1),(2,'ZGZ','Almacén Zaragoza',NULL,'Zaragoza','ES',1);
/*!40000 ALTER TABLE `almacenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) NOT NULL,
  `email` varchar(120) NOT NULL,
  `ciudad` varchar(60) DEFAULT NULL,
  `pais` char(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_clientes_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES (1,'Iker Etxeberria','iker.etxeberria@correo.example','Donostia','ES'),(2,'Amaia Goikoetxea','amaia.goikoetxea@correo.example','Hernani','ES'),(3,'Maialen Urrutia','maialen.urrutia@correo.example','Bilbao','ES'),(4,'Pierre Dubois','pierre.dubois@courrier.example','Bayonne','FR'),(5,'Lucía Fernández','lucia.fernandez@correo.example','Madrid','ES'),(6,'Club Atletismo Donosti','info@atletismodonosti.example','Donostia','ES');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compras`
--

DROP TABLE IF EXISTS `compras`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compras` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `proveedor_id` int unsigned NOT NULL,
  `almacen_id` int unsigned NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_prevista` date DEFAULT NULL,
  `fecha_confirmacion` datetime DEFAULT NULL,
  `estado` enum('borrador','pendiente','confirmada','cancelada') NOT NULL DEFAULT 'borrador',
  `observaciones` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_compras_proveedor` (`proveedor_id`),
  KEY `fk_compras_almacen` (`almacen_id`),
  CONSTRAINT `fk_compras_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `fk_compras_proveedor` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `ck_compras_confirmacion_estado` CHECK ((((`estado` = _utf8mb4'confirmada') and (`fecha_confirmacion` is not null)) or ((`estado` <> _utf8mb4'confirmada') and (`fecha_confirmacion` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compras`
--

LOCK TABLES `compras` WRITE;
/*!40000 ALTER TABLE `compras` DISABLE KEYS */;
INSERT INTO `compras` VALUES (1,1,1,'2026-09-25 00:00:00','2026-10-05','2026-10-04 10:30:00','confirmada','Reposición de zapatillas y camisetas de running'),(2,2,1,'2026-10-01 00:00:00','2026-10-12','2026-10-11 09:15:00','confirmada','Reposición de botas de senderismo (stock bajo)'),(3,1,1,'2026-10-16 00:00:00','2026-10-26','2026-10-19 13:53:48','confirmada','Reposición de cascos de ciclismo'),(4,3,2,'2026-10-19 00:00:00',NULL,NULL,'borrador','Pendiente de confirmar cantidades de material fitness'),(5,2,2,'2026-10-08 00:00:00','2026-10-20',NULL,'cancelada','Cancelada por cambio de modelo de chaqueta'),(6,2,1,'2026-10-20 10:40:34','2026-10-25','2026-10-20 10:40:34','confirmada','Reposición de botas y tela');
/*!40000 ALTER TABLE `compras` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `entregas`
--

DROP TABLE IF EXISTS `entregas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entregas` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pedido_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad_entregada` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('preparada','enviada','entregada') NOT NULL DEFAULT 'preparada',
  `fecha_entrega` datetime DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_entregas_linea_pedido` (`pedido_id`,`producto_id`),
  CONSTRAINT `fk_entregas_linea_pedido` FOREIGN KEY (`pedido_id`, `producto_id`) REFERENCES `lineas_pedido` (`pedido_id`, `producto_id`),
  CONSTRAINT `ck_entregas_cantidad` CHECK ((`cantidad_entregada` > 0)),
  CONSTRAINT `ck_entregas_fecha_entrega` CHECK ((((`estado` = _utf8mb4'entregada') and (`fecha_entrega` is not null)) or ((`estado` <> _utf8mb4'entregada') and (`fecha_entrega` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entregas`
--

LOCK TABLES `entregas` WRITE;
/*!40000 ALTER TABLE `entregas` DISABLE KEYS */;
INSERT INTO `entregas` VALUES (1,1,1,1,'2026-10-20 10:40:34','entregada','2026-09-23 12:00:00','Recogido en tienda'),(2,1,2,3,'2026-10-20 10:40:34','entregada','2026-09-23 12:00:00','Recogido en tienda'),(3,2,3,1,'2026-10-20 10:40:34','enviada',NULL,'En reparto'),(4,2,5,1,'2026-10-20 10:40:34','enviada',NULL,'En reparto'),(5,3,6,1,'2026-10-20 10:40:34','preparada',NULL,'Entrega parcial: falta 1 unidad');
/*!40000 ALTER TABLE `entregas` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_entregas_bi` BEFORE INSERT ON `entregas` FOR EACH ROW BEGIN
  DECLARE v_pedida INT;
  DECLARE v_ya     INT DEFAULT 0;

  SELECT cantidad INTO v_pedida
    FROM lineas_pedido
   WHERE pedido_id = NEW.pedido_id AND producto_id = NEW.producto_id;
  IF v_pedida IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no esta en el pedido';
  END IF;

  SELECT COALESCE(SUM(cantidad_entregada), 0) INTO v_ya
    FROM entregas
   WHERE pedido_id = NEW.pedido_id AND producto_id = NEW.producto_id;
  IF v_ya + NEW.cantidad_entregada > v_pedida THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se entrega mas cantidad de la pedida';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `existencias`
--

DROP TABLE IF EXISTS `existencias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `existencias` (
  `almacen_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL DEFAULT '0',
  `stock_minimo` int NOT NULL DEFAULT '10',
  PRIMARY KEY (`almacen_id`,`producto_id`),
  KEY `fk_existencias_producto` (`producto_id`),
  CONSTRAINT `fk_existencias_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `fk_existencias_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_existencias_cantidad` CHECK ((`cantidad` >= 0)),
  CONSTRAINT `ck_existencias_stock_minimo` CHECK ((`stock_minimo` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `existencias`
--

LOCK TABLES `existencias` WRITE;
/*!40000 ALTER TABLE `existencias` DISABLE KEYS */;
INSERT INTO `existencias` VALUES (1,1,59,10),(1,2,117,10),(1,3,54,10),(1,4,26,10),(1,5,24,10),(1,6,18,10),(1,7,50,10),(1,8,42,10),(1,9,33,10),(1,10,88,10),(1,11,110,20),(1,12,170,20),(1,13,130,20),(2,1,25,10),(2,3,20,10),(2,6,10,10),(2,9,37,10);
/*!40000 ALTER TABLE `existencias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturas`
--

DROP TABLE IF EXISTS `facturas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturas` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pedido_id` int unsigned NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `base` decimal(12,2) NOT NULL,
  `iva` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_facturas_pedido` (`pedido_id`),
  CONSTRAINT `fk_facturas_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `ck_facturas_total` CHECK ((`total` = (`base` + `iva`)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturas`
--

LOCK TABLES `facturas` WRITE;
/*!40000 ALTER TABLE `facturas` DISABLE KEYS */;
INSERT INTO `facturas` VALUES (1,1,'2026-10-14 12:14:19',149.75,31.45,181.20),(2,2,'2026-10-14 12:14:19',144.80,30.41,175.21),(3,4,'2026-10-15 09:40:20',25.90,5.44,31.34);
/*!40000 ALTER TABLE `facturas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interacciones`
--

DROP TABLE IF EXISTS `interacciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interacciones` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` int unsigned NOT NULL,
  `oportunidad_id` int unsigned DEFAULT NULL,
  `tipo` enum('llamada','email','reunion') NOT NULL,
  `nota` varchar(255) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_interacciones_cliente` (`cliente_id`),
  KEY `fk_interacciones_oportunidad` (`oportunidad_id`),
  CONSTRAINT `fk_interacciones_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `fk_interacciones_oportunidad` FOREIGN KEY (`oportunidad_id`) REFERENCES `oportunidades` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interacciones`
--

LOCK TABLES `interacciones` WRITE;
/*!40000 ALTER TABLE `interacciones` DISABLE KEYS */;
INSERT INTO `interacciones` VALUES (1,1,3,'reunion','Interesado en zapatillas de trail','2026-10-03 17:20:00'),(2,4,2,'email','Pide presupuesto de material de ciclismo','2026-09-27 11:00:00'),(3,6,1,'llamada','Negociando equipación para la temporada','2026-09-14 10:00:00'),(4,6,1,'email','Enviada propuesta económica','2026-09-21 09:30:00'),(5,3,4,'llamada','Primer contacto, quiere catálogo','2026-10-08 12:15:00');
/*!40000 ALTER TABLE `interacciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lineas_compra`
--

DROP TABLE IF EXISTS `lineas_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lineas_compra` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `coste_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lineas_compra_producto` (`compra_id`,`producto_id`),
  KEY `fk_lineas_compra_producto` (`producto_id`),
  CONSTRAINT `fk_lineas_compra_compra` FOREIGN KEY (`compra_id`) REFERENCES `compras` (`id`),
  CONSTRAINT `fk_lineas_compra_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_lineas_compra_cantidad` CHECK ((`cantidad` > 0)),
  CONSTRAINT `ck_lineas_compra_coste` CHECK ((`coste_unitario` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lineas_compra`
--

LOCK TABLES `lineas_compra` WRITE;
/*!40000 ALTER TABLE `lineas_compra` DISABLE KEYS */;
INSERT INTO `lineas_compra` VALUES (1,1,1,30,52.00),(2,1,2,60,9.50),(3,2,4,20,70.00),(4,3,9,25,28.00),(5,4,6,15,22.00),(6,4,7,30,8.50),(7,5,5,10,48.00),(8,6,4,20,51.00),(9,6,11,50,2.00);
/*!40000 ALTER TABLE `lineas_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lineas_pedido`
--

DROP TABLE IF EXISTS `lineas_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lineas_pedido` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pedido_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_linea_producto` (`pedido_id`,`producto_id`),
  KEY `fk_lineas_producto` (`producto_id`),
  CONSTRAINT `fk_lineas_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lineas_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_lineas_cantidad` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lineas_pedido`
--

LOCK TABLES `lineas_pedido` WRITE;
/*!40000 ALTER TABLE `lineas_pedido` DISABLE KEYS */;
INSERT INTO `lineas_pedido` VALUES (1,1,1,1,89.90),(2,1,2,3,19.95),(3,2,3,1,59.90),(4,2,5,1,84.90),(5,3,6,2,39.90),(6,3,7,5,16.95),(7,3,8,6,22.90),(8,4,10,2,12.95);
/*!40000 ALTER TABLE `lineas_pedido` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_lineas_bi` BEFORE INSERT ON `lineas_pedido` FOR EACH ROW BEGIN
  DECLARE v_precio  DECIMAL(10,2);
  DECLARE v_almacen INT UNSIGNED;
  DECLARE v_stock   INT DEFAULT 0;

  SELECT precio INTO v_precio
    FROM productos WHERE id = NEW.producto_id;
  IF v_precio IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe';
  END IF;

  SELECT almacen_id INTO v_almacen
    FROM pedidos WHERE id = NEW.pedido_id;

  SELECT cantidad INTO v_stock
    FROM existencias
   WHERE almacen_id = v_almacen AND producto_id = NEW.producto_id;

  IF v_stock < NEW.cantidad THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente en el almacen del pedido';
  END IF;

  SET NEW.precio_unitario = v_precio;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_lineas_ai` AFTER INSERT ON `lineas_pedido` FOR EACH ROW BEGIN
  UPDATE existencias e
    JOIN pedidos p ON p.id = NEW.pedido_id
     SET e.cantidad = e.cantidad - NEW.cantidad
   WHERE e.almacen_id = p.almacen_id
     AND e.producto_id = NEW.producto_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `lineas_presupuesto`
--

DROP TABLE IF EXISTS `lineas_presupuesto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lineas_presupuesto` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `presupuesto_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `descuento_pct` decimal(5,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lineas_presupuesto_producto` (`presupuesto_id`,`producto_id`),
  KEY `fk_lineas_presupuesto_producto` (`producto_id`),
  CONSTRAINT `fk_lineas_presupuesto_presupuesto` FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuestos` (`id`),
  CONSTRAINT `fk_lineas_presupuesto_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_lineas_presupuesto_cantidad` CHECK ((`cantidad` > 0)),
  CONSTRAINT `ck_lineas_presupuesto_descuento` CHECK ((`descuento_pct` between 0 and 100)),
  CONSTRAINT `ck_lineas_presupuesto_precio` CHECK ((`precio_unitario` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lineas_presupuesto`
--

LOCK TABLES `lineas_presupuesto` WRITE;
/*!40000 ALTER TABLE `lineas_presupuesto` DISABLE KEYS */;
INSERT INTO `lineas_presupuesto` VALUES (1,1,2,20,19.95,10.00),(2,1,8,10,22.90,10.00),(3,1,10,15,12.95,5.00),(4,2,3,1,59.90,0.00),(5,2,5,1,84.90,0.00),(6,3,1,1,89.90,0.00),(7,3,2,2,19.95,0.00);
/*!40000 ALTER TABLE `lineas_presupuesto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lista_materiales`
--

DROP TABLE IF EXISTS `lista_materiales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lista_materiales` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `producto_id` int unsigned NOT NULL,
  `componente_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lista_materiales_componente` (`producto_id`,`componente_id`),
  KEY `fk_lista_materiales_componente` (`componente_id`),
  CONSTRAINT `fk_lista_materiales_componente` FOREIGN KEY (`componente_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `fk_lista_materiales_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_lista_materiales_cantidad` CHECK ((`cantidad` > 0)),
  CONSTRAINT `ck_lista_materiales_distinto` CHECK ((`producto_id` <> `componente_id`))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lista_materiales`
--

LOCK TABLES `lista_materiales` WRITE;
/*!40000 ALTER TABLE `lista_materiales` DISABLE KEYS */;
INSERT INTO `lista_materiales` VALUES (1,3,11,2),(2,3,12,3),(3,3,13,2);
/*!40000 ALTER TABLE `lista_materiales` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_lista_materiales_bi` BEFORE INSERT ON `lista_materiales` FOR EACH ROW BEGIN
  IF NOT EXISTS (SELECT 1 FROM productos WHERE id = NEW.producto_id AND fabricable = TRUE) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Solo un producto fabricable puede tener lista de materiales';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_lista_materiales_bu` BEFORE UPDATE ON `lista_materiales` FOR EACH ROW BEGIN
  IF NOT EXISTS (SELECT 1 FROM productos WHERE id = NEW.producto_id AND fabricable = TRUE) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Solo un producto fabricable puede tener lista de materiales';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `oportunidades`
--

DROP TABLE IF EXISTS `oportunidades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oportunidades` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` int unsigned NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `importe_estimado` decimal(12,2) NOT NULL DEFAULT '0.00',
  `probabilidad` tinyint unsigned NOT NULL DEFAULT '10',
  `etapa` enum('prospecto','propuesta','negociacion','ganada','perdida') NOT NULL DEFAULT 'prospecto',
  `fecha_creacion` date NOT NULL DEFAULT (curdate()),
  `fecha_cierre_prevista` date DEFAULT NULL,
  `fecha_cierre` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_oportunidades_cliente` (`cliente_id`),
  CONSTRAINT `fk_oportunidades_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `ck_oportunidades_cierre_etapa` CHECK ((((`etapa` in (_utf8mb4'ganada',_utf8mb4'perdida')) and (`fecha_cierre` is not null)) or ((`etapa` not in (_utf8mb4'ganada',_utf8mb4'perdida')) and (`fecha_cierre` is null)))),
  CONSTRAINT `ck_oportunidades_importe` CHECK ((`importe_estimado` >= 0)),
  CONSTRAINT `ck_oportunidades_probabilidad` CHECK ((`probabilidad` <= 100))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oportunidades`
--

LOCK TABLES `oportunidades` WRITE;
/*!40000 ALTER TABLE `oportunidades` DISABLE KEYS */;
INSERT INTO `oportunidades` VALUES (1,6,'Equipación del club temporada 26/27',4800.00,60,'negociacion','2026-09-10','2026-11-15',NULL),(2,4,'Pack de material de ciclismo',450.00,100,'ganada','2026-09-01','2026-09-30','2026-09-28'),(3,1,'Zapatillas de trail y ropa técnica',250.00,50,'propuesta','2026-10-03','2026-11-03',NULL),(4,3,'Catálogo de material de running',300.00,20,'prospecto','2026-10-08','2026-12-01',NULL),(5,5,'Equipamiento para maratón',380.00,0,'perdida','2026-09-05','2026-09-25','2026-09-20');
/*!40000 ALTER TABLE `oportunidades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes_fabricacion`
--

DROP TABLE IF EXISTS `ordenes_fabricacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes_fabricacion` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `producto_id` int unsigned NOT NULL,
  `almacen_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_prevista` date DEFAULT NULL,
  `fecha_fin` datetime DEFAULT NULL,
  `estado` enum('borrador','en_proceso','terminada','cancelada') NOT NULL DEFAULT 'borrador',
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ordenes_fabricacion_producto` (`producto_id`),
  KEY `fk_ordenes_fabricacion_almacen` (`almacen_id`),
  CONSTRAINT `fk_ordenes_fabricacion_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `fk_ordenes_fabricacion_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `ck_ordenes_fabricacion_cantidad` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes_fabricacion`
--

LOCK TABLES `ordenes_fabricacion` WRITE;
/*!40000 ALTER TABLE `ordenes_fabricacion` DISABLE KEYS */;
INSERT INTO `ordenes_fabricacion` VALUES (1,3,1,10,'2026-10-20 10:40:34','2026-10-22','2026-10-20 10:40:34','terminada','Lote de mochilas de octubre'),(2,3,1,25,'2026-10-20 10:40:34','2026-11-05',NULL,'borrador','Lote de mochilas de noviembre');
/*!40000 ALTER TABLE `ordenes_fabricacion` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_ordenes_fabricacion_bi` BEFORE INSERT ON `ordenes_fabricacion` FOR EACH ROW BEGIN
  IF NOT EXISTS (SELECT 1 FROM productos WHERE id = NEW.producto_id AND fabricable = TRUE) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Solo se pueden crear ordenes de fabricacion de productos fabricables';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_ordenes_fabricacion_bu` BEFORE UPDATE ON `ordenes_fabricacion` FOR EACH ROW BEGIN
  IF OLD.estado IN ('terminada','cancelada') AND NEW.estado <> OLD.estado THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Una orden terminada o cancelada no puede cambiar de estado';
  END IF;

  IF NEW.estado = 'terminada' AND OLD.estado <> 'terminada' THEN
    IF NOT EXISTS (SELECT 1 FROM lista_materiales WHERE producto_id = NEW.producto_id) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no tiene lista de materiales';
    END IF;

    IF EXISTS (
      SELECT 1
        FROM lista_materiales lm
        LEFT JOIN existencias e
               ON e.producto_id = lm.componente_id AND e.almacen_id = NEW.almacen_id
       WHERE lm.producto_id = NEW.producto_id
         AND COALESCE(e.cantidad, 0) < lm.cantidad * NEW.cantidad
    ) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Faltan componentes en el almacen para terminar la orden';
    END IF;

    SET NEW.fecha_fin = COALESCE(NEW.fecha_fin, NOW());
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_ordenes_fabricacion_au` AFTER UPDATE ON `ordenes_fabricacion` FOR EACH ROW BEGIN
  IF NEW.estado = 'terminada' AND OLD.estado <> 'terminada' THEN
    UPDATE existencias e
      JOIN lista_materiales lm
        ON lm.componente_id = e.producto_id AND lm.producto_id = NEW.producto_id
       SET e.cantidad = e.cantidad - lm.cantidad * NEW.cantidad
     WHERE e.almacen_id = NEW.almacen_id;

    INSERT IGNORE INTO existencias (almacen_id, producto_id, cantidad)
    VALUES (NEW.almacen_id, NEW.producto_id, 0);

    UPDATE existencias
       SET cantidad = cantidad + NEW.cantidad
     WHERE almacen_id = NEW.almacen_id AND producto_id = NEW.producto_id;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` int unsigned NOT NULL,
  `almacen_id` int unsigned NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('pendiente','facturado') NOT NULL DEFAULT 'pendiente',
  PRIMARY KEY (`id`),
  KEY `fk_pedidos_cliente` (`cliente_id`),
  KEY `fk_pedidos_almacen` (`almacen_id`),
  CONSTRAINT `fk_pedidos_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `fk_pedidos_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES (1,1,1,'2026-09-21 11:30:00','facturado'),(2,4,1,'2026-09-28 20:45:00','facturado'),(3,6,1,'2026-10-02 10:00:00','pendiente'),(4,2,1,'2026-10-15 09:40:20','facturado'),(5,2,1,'2026-10-15 09:40:20','pendiente');
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presupuestos`
--

DROP TABLE IF EXISTS `presupuestos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presupuestos` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` int unsigned NOT NULL,
  `oportunidad_id` int unsigned DEFAULT NULL,
  `pedido_id` int unsigned DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_validez` date NOT NULL,
  `estado` enum('borrador','enviado','aceptado','rechazado','caducado') NOT NULL DEFAULT 'borrador',
  `observaciones` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_presupuestos_pedido` (`pedido_id`),
  KEY `fk_presupuestos_cliente` (`cliente_id`),
  KEY `fk_presupuestos_oportunidad` (`oportunidad_id`),
  CONSTRAINT `fk_presupuestos_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `fk_presupuestos_oportunidad` FOREIGN KEY (`oportunidad_id`) REFERENCES `oportunidades` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_presupuestos_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `ck_presupuestos_validez` CHECK ((`fecha_validez` >= cast(`fecha` as date)))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presupuestos`
--

LOCK TABLES `presupuestos` WRITE;
/*!40000 ALTER TABLE `presupuestos` DISABLE KEYS */;
INSERT INTO `presupuestos` VALUES (1,6,1,NULL,'2026-09-21 09:30:00','2026-10-21','aceptado','Propuesta de equipación para la temporada'),(2,4,2,2,'2026-09-20 11:00:00','2026-10-20','aceptado','Aceptado: se convirtió en el pedido 2'),(3,1,3,NULL,'2026-10-03 17:30:00','2026-11-03','aceptado',NULL);
/*!40000 ALTER TABLE `presupuestos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `sku` varchar(20) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `categoria` varchar(40) NOT NULL,
  `fabricable` tinyint(1) NOT NULL DEFAULT '0',
  `precio` decimal(10,2) NOT NULL,
  `proveedor_id` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_productos_sku` (`sku`),
  KEY `fk_productos_proveedor` (`proveedor_id`),
  CONSTRAINT `fk_productos_proveedor` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `ck_productos_precio` CHECK ((`precio` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'RUN-ZAP-001','Zapatillas running Aero 2','Running',0,89.90,1),(2,'RUN-CAM-001','Camiseta técnica Breeze','Running',0,19.95,1),(3,'MON-MOC-001','Mochila de montaña 30L','Montaña',1,59.90,2),(4,'MON-BOT-001','Botas de senderismo Pirineo','Montaña',0,119.00,2),(5,'MON-CHA-001','Chaqueta impermeable','Montaña',0,84.90,2),(6,'FIT-MAN-001','Mancuernas 10 kg (par)','Fitness',0,39.90,3),(7,'FIT-ESB-001','Esterilla de yoga','Fitness',0,16.95,3),(8,'FUT-BAL-001','Balón de fútbol talla 5','Fútbol',0,22.90,3),(9,'CIC-CAS-001','Casco de ciclismo','Ciclismo',0,49.90,1),(10,'NAT-GAF-001','Gafas de natación','Natación',0,12.95,3),(11,'MP-TEL-001','Tela ripstop (metro)','Materia prima',0,3.50,2),(12,'MP-CRE-001','Cremallera reforzada','Materia prima',0,1.20,2),(13,'MP-COR-001','Correa acolchada','Materia prima',0,2.40,2);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_productos_fabricable_bu` BEFORE UPDATE ON `productos` FOR EACH ROW BEGIN
  IF OLD.fabricable = TRUE AND NEW.fabricable = FALSE THEN
    IF EXISTS (SELECT 1 FROM lista_materiales WHERE producto_id = OLD.id)
       OR EXISTS (SELECT 1 FROM ordenes_fabricacion
                   WHERE producto_id = OLD.id
                     AND estado IN ('borrador','en_proceso')) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto tiene lista de materiales u ordenes abiertas: no se puede marcar como no fabricable';
    END IF;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(120) NOT NULL,
  `pais` char(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_proveedores_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
INSERT INTO `proveedores` VALUES (1,'RunTech France','pedidos@runtech.example','FR'),(2,'Montaña Pro S.L.','ventas@montanapro.example','ES'),(3,'FitGear Distribución','info@fitgear.example','ES');
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recepciones`
--

DROP TABLE IF EXISTS `recepciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recepciones` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad_recibida` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('pendiente','en_transito','recibida') NOT NULL DEFAULT 'pendiente',
  `fecha_recepcion` datetime DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_recepciones_linea_compra` (`compra_id`,`producto_id`),
  CONSTRAINT `fk_recepciones_linea_compra` FOREIGN KEY (`compra_id`, `producto_id`) REFERENCES `lineas_compra` (`compra_id`, `producto_id`),
  CONSTRAINT `ck_recepciones_cantidad` CHECK ((`cantidad_recibida` > 0)),
  CONSTRAINT `ck_recepciones_fecha_recepcion` CHECK ((((`estado` = _utf8mb4'recibida') and (`fecha_recepcion` is not null)) or ((`estado` <> _utf8mb4'recibida') and (`fecha_recepcion` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recepciones`
--

LOCK TABLES `recepciones` WRITE;
/*!40000 ALTER TABLE `recepciones` DISABLE KEYS */;
INSERT INTO `recepciones` VALUES (1,6,4,20,'2026-10-20 10:40:34','recibida','2026-10-20 10:40:34','Entrega completa'),(2,6,11,30,'2026-10-20 10:40:34','recibida','2026-10-20 10:40:34','Primera entrega, faltan 20 metros');
/*!40000 ALTER TABLE `recepciones` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_recepciones_bi` BEFORE INSERT ON `recepciones` FOR EACH ROW BEGIN
  DECLARE v_estado VARCHAR(20);
  DECLARE v_pedida INT;
  DECLARE v_ya     INT DEFAULT 0;

  SELECT estado INTO v_estado FROM compras WHERE id = NEW.compra_id;
  IF v_estado <> 'confirmada' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Solo se puede recibir mercancia de una compra confirmada';
  END IF;

  SELECT cantidad INTO v_pedida
    FROM lineas_compra
   WHERE compra_id = NEW.compra_id AND producto_id = NEW.producto_id;
  IF v_pedida IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no esta en la compra';
  END IF;

  SELECT COALESCE(SUM(cantidad_recibida), 0) INTO v_ya
    FROM recepciones
   WHERE compra_id = NEW.compra_id AND producto_id = NEW.producto_id;
  IF v_ya + NEW.cantidad_recibida > v_pedida THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se recibe mas cantidad de la pedida';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_recepciones_ai` AFTER INSERT ON `recepciones` FOR EACH ROW BEGIN
  IF NEW.estado = 'recibida' THEN
    CALL sp_aplicar_recepcion(NEW.compra_id, NEW.producto_id, NEW.cantidad_recibida);
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_recepciones_bu` BEFORE UPDATE ON `recepciones` FOR EACH ROW BEGIN
  DECLARE v_pedida INT;
  DECLARE v_otras  INT DEFAULT 0;

  IF OLD.estado = 'recibida' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Una recepcion ya recibida no se puede modificar';
  END IF;

  IF NEW.compra_id <> OLD.compra_id OR NEW.producto_id <> OLD.producto_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'No se puede cambiar la compra ni el producto de una recepcion';
  END IF;

  IF NEW.cantidad_recibida <> OLD.cantidad_recibida THEN
    SELECT cantidad INTO v_pedida
      FROM lineas_compra
     WHERE compra_id = NEW.compra_id AND producto_id = NEW.producto_id;

    SELECT COALESCE(SUM(cantidad_recibida), 0) INTO v_otras
      FROM recepciones
     WHERE compra_id = NEW.compra_id AND producto_id = NEW.producto_id
       AND id <> OLD.id;

    IF v_otras + NEW.cantidad_recibida > v_pedida THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se recibe mas cantidad de la pedida';
    END IF;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_recepciones_au` AFTER UPDATE ON `recepciones` FOR EACH ROW BEGIN
  IF NEW.estado = 'recibida' AND OLD.estado <> 'recibida' THEN
    CALL sp_aplicar_recepcion(NEW.compra_id, NEW.producto_id, NEW.cantidad_recibida);
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Temporary view structure for view `v_capacidad_fabricacion`
--

DROP TABLE IF EXISTS `v_capacidad_fabricacion`;
/*!50001 DROP VIEW IF EXISTS `v_capacidad_fabricacion`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_capacidad_fabricacion` AS SELECT 
 1 AS `producto_sku`,
 1 AS `producto`,
 1 AS `almacen`,
 1 AS `num_materiales`,
 1 AS `unidades_fabricables`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_catalogo_movil`
--

DROP TABLE IF EXISTS `v_catalogo_movil`;
/*!50001 DROP VIEW IF EXISTS `v_catalogo_movil`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_catalogo_movil` AS SELECT 
 1 AS `sku`,
 1 AS `nombre`,
 1 AS `categoria`,
 1 AS `precio`,
 1 AS `stock`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_compras_recepcion`
--

DROP TABLE IF EXISTS `v_compras_recepcion`;
/*!50001 DROP VIEW IF EXISTS `v_compras_recepcion`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_compras_recepcion` AS SELECT 
 1 AS `compra_id`,
 1 AS `proveedor`,
 1 AS `estado`,
 1 AS `fecha_confirmacion`,
 1 AS `unidades_pedidas`,
 1 AS `unidades_recibidas`,
 1 AS `situacion_recepcion`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_materiales_fabricacion`
--

DROP TABLE IF EXISTS `v_materiales_fabricacion`;
/*!50001 DROP VIEW IF EXISTS `v_materiales_fabricacion`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_materiales_fabricacion` AS SELECT 
 1 AS `producto_sku`,
 1 AS `producto`,
 1 AS `almacen`,
 1 AS `material_sku`,
 1 AS `material`,
 1 AS `cantidad_por_unidad`,
 1 AS `stock_material`,
 1 AS `unidades_posibles_con_este_material`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_pedidos_resumen`
--

DROP TABLE IF EXISTS `v_pedidos_resumen`;
/*!50001 DROP VIEW IF EXISTS `v_pedidos_resumen`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_pedidos_resumen` AS SELECT 
 1 AS `pedido_id`,
 1 AS `fecha`,
 1 AS `cliente`,
 1 AS `estado`,
 1 AS `total_sin_iva`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_pipeline_crm`
--

DROP TABLE IF EXISTS `v_pipeline_crm`;
/*!50001 DROP VIEW IF EXISTS `v_pipeline_crm`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_pipeline_crm` AS SELECT 
 1 AS `etapa`,
 1 AS `num_oportunidades`,
 1 AS `importe_total`,
 1 AS `importe_ponderado`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_presupuestos_resumen`
--

DROP TABLE IF EXISTS `v_presupuestos_resumen`;
/*!50001 DROP VIEW IF EXISTS `v_presupuestos_resumen`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_presupuestos_resumen` AS SELECT 
 1 AS `presupuesto_id`,
 1 AS `cliente`,
 1 AS `estado`,
 1 AS `fecha`,
 1 AS `fecha_validez`,
 1 AS `total_sin_iva`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_stock_bajo`
--

DROP TABLE IF EXISTS `v_stock_bajo`;
/*!50001 DROP VIEW IF EXISTS `v_stock_bajo`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_stock_bajo` AS SELECT 
 1 AS `almacen`,
 1 AS `sku`,
 1 AS `producto`,
 1 AS `cantidad`,
 1 AS `stock_minimo`,
 1 AS `proveedor`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_capacidad_fabricacion`
--

/*!50001 DROP VIEW IF EXISTS `v_capacidad_fabricacion`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_capacidad_fabricacion` AS select `pf`.`sku` AS `producto_sku`,`pf`.`nombre` AS `producto`,`a`.`codigo` AS `almacen`,count(0) AS `num_materiales`,min(floor((coalesce(`e`.`cantidad`,0) / `lm`.`cantidad`))) AS `unidades_fabricables` from (((`lista_materiales` `lm` join `productos` `pf` on(((`pf`.`id` = `lm`.`producto_id`) and (`pf`.`fabricable` = true)))) join `almacenes` `a`) left join `existencias` `e` on(((`e`.`producto_id` = `lm`.`componente_id`) and (`e`.`almacen_id` = `a`.`id`)))) group by `pf`.`sku`,`pf`.`nombre`,`a`.`codigo` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_catalogo_movil`
--

/*!50001 DROP VIEW IF EXISTS `v_catalogo_movil`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_catalogo_movil` AS select `pr`.`sku` AS `sku`,`pr`.`nombre` AS `nombre`,`pr`.`categoria` AS `categoria`,`pr`.`precio` AS `precio`,coalesce(sum(`e`.`cantidad`),0) AS `stock` from (`productos` `pr` left join `existencias` `e` on((`e`.`producto_id` = `pr`.`id`))) group by `pr`.`id`,`pr`.`sku`,`pr`.`nombre`,`pr`.`categoria`,`pr`.`precio` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_compras_recepcion`
--

/*!50001 DROP VIEW IF EXISTS `v_compras_recepcion`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_compras_recepcion` AS select `c`.`id` AS `compra_id`,`pv`.`nombre` AS `proveedor`,`c`.`estado` AS `estado`,`c`.`fecha_confirmacion` AS `fecha_confirmacion`,sum(`lc`.`cantidad`) AS `unidades_pedidas`,coalesce(sum(`r`.`recibida`),0) AS `unidades_recibidas`,(case when (coalesce(sum(`r`.`recibida`),0) = 0) then 'sin recibir' when (coalesce(sum(`r`.`recibida`),0) < sum(`lc`.`cantidad`)) then 'parcial' else 'completa' end) AS `situacion_recepcion` from (((`compras` `c` join `proveedores` `pv` on((`pv`.`id` = `c`.`proveedor_id`))) join `lineas_compra` `lc` on((`lc`.`compra_id` = `c`.`id`))) left join (select `recepciones`.`compra_id` AS `compra_id`,`recepciones`.`producto_id` AS `producto_id`,sum(`recepciones`.`cantidad_recibida`) AS `recibida` from `recepciones` where (`recepciones`.`estado` = 'recibida') group by `recepciones`.`compra_id`,`recepciones`.`producto_id`) `r` on(((`r`.`compra_id` = `lc`.`compra_id`) and (`r`.`producto_id` = `lc`.`producto_id`)))) group by `c`.`id`,`pv`.`nombre`,`c`.`estado`,`c`.`fecha_confirmacion` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_materiales_fabricacion`
--

/*!50001 DROP VIEW IF EXISTS `v_materiales_fabricacion`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_materiales_fabricacion` AS select `pf`.`sku` AS `producto_sku`,`pf`.`nombre` AS `producto`,`a`.`codigo` AS `almacen`,`mat`.`sku` AS `material_sku`,`mat`.`nombre` AS `material`,`lm`.`cantidad` AS `cantidad_por_unidad`,coalesce(`e`.`cantidad`,0) AS `stock_material`,floor((coalesce(`e`.`cantidad`,0) / `lm`.`cantidad`)) AS `unidades_posibles_con_este_material` from ((((`lista_materiales` `lm` join `productos` `pf` on(((`pf`.`id` = `lm`.`producto_id`) and (`pf`.`fabricable` = true)))) join `productos` `mat` on((`mat`.`id` = `lm`.`componente_id`))) join `almacenes` `a`) left join `existencias` `e` on(((`e`.`producto_id` = `lm`.`componente_id`) and (`e`.`almacen_id` = `a`.`id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_pedidos_resumen`
--

/*!50001 DROP VIEW IF EXISTS `v_pedidos_resumen`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_pedidos_resumen` AS select `p`.`id` AS `pedido_id`,`p`.`fecha` AS `fecha`,`c`.`nombre` AS `cliente`,`p`.`estado` AS `estado`,`fn_total_pedido`(`p`.`id`) AS `total_sin_iva` from (`pedidos` `p` join `clientes` `c` on((`c`.`id` = `p`.`cliente_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_pipeline_crm`
--

/*!50001 DROP VIEW IF EXISTS `v_pipeline_crm`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_pipeline_crm` AS select `oportunidades`.`etapa` AS `etapa`,count(0) AS `num_oportunidades`,sum(`oportunidades`.`importe_estimado`) AS `importe_total`,round(sum(((`oportunidades`.`importe_estimado` * `oportunidades`.`probabilidad`) / 100)),2) AS `importe_ponderado` from `oportunidades` group by `oportunidades`.`etapa` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_presupuestos_resumen`
--

/*!50001 DROP VIEW IF EXISTS `v_presupuestos_resumen`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_presupuestos_resumen` AS select `p`.`id` AS `presupuesto_id`,`c`.`nombre` AS `cliente`,`p`.`estado` AS `estado`,`p`.`fecha` AS `fecha`,`p`.`fecha_validez` AS `fecha_validez`,coalesce((select round(sum(((`l`.`cantidad` * `l`.`precio_unitario`) * (1 - (`l`.`descuento_pct` / 100)))),2) from `lineas_presupuesto` `l` where (`l`.`presupuesto_id` = `p`.`id`)),0) AS `total_sin_iva` from (`presupuestos` `p` join `clientes` `c` on((`c`.`id` = `p`.`cliente_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_stock_bajo`
--

/*!50001 DROP VIEW IF EXISTS `v_stock_bajo`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_stock_bajo` AS select `a`.`nombre` AS `almacen`,`pr`.`sku` AS `sku`,`pr`.`nombre` AS `producto`,`e`.`cantidad` AS `cantidad`,`e`.`stock_minimo` AS `stock_minimo`,`pv`.`nombre` AS `proveedor` from (((`existencias` `e` join `almacenes` `a` on((`a`.`id` = `e`.`almacen_id`))) join `productos` `pr` on((`pr`.`id` = `e`.`producto_id`))) join `proveedores` `pv` on((`pv`.`id` = `pr`.`proveedor_id`))) where (`e`.`cantidad` < `e`.`stock_minimo`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-21  9:07:49
