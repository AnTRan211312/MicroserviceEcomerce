CREATE DATABASE  IF NOT EXISTS `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `auth_db`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: auth_db
-- ------------------------------------------------------
-- Server version	8.4.7

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
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `api_path` varchar(255) NOT NULL,
  `method` varchar(255) NOT NULL,
  `module` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/users','POST','USER','Tạo User'),(2,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/users','GET','USER','Lấy danh sách User'),(3,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/users/{id}','GET','USER','Tìm User theo id'),(4,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/users','PUT','USER','Cập nhật User'),(5,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/users/{id}','DELETE','USER','Xóa User theo id'),(6,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/permissions','POST','PERMISSION','Tạo quyền hạn'),(7,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/permissions','GET','PERMISSION','Lấy danh sách quyền hạn'),(8,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/permissions/all','GET','PERMISSION','Lấy toàn bộ quyền hạn (không phân trang)'),(9,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/permissions/{id}','PUT','PERMISSION','Cập nhật quyền hạn'),(10,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/permissions/{id}','DELETE','PERMISSION','Xóa quyền hạn'),(11,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/roles','POST','ROLE','Tạo role'),(12,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/roles','GET','ROLE','Lấy danh sách role'),(13,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/roles/{id}','PUT','ROLE','Cập nhật Role'),(14,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/roles/{id}','DELETE','ROLE','Xóa Role theo id'),(15,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/products','POST','PRODUCT','Tạo sản phẩm mới'),(16,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/products','GET','PRODUCT','Lấy danh sách tất cả sản phẩm'),(17,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/products/{id}','GET','PRODUCT','Lấy chi tiết một sản phẩm'),(18,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/products/{id}','PUT','PRODUCT','Cập nhật thông tin sản phẩm'),(19,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/products/{id}','DELETE','PRODUCT','Xóa một sản phẩm'),(20,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/categories','POST','CATEGORY','Tạo danh mục mới'),(21,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/categories','GET','CATEGORY','Lấy danh sách danh mục'),(22,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/categories/{id}','PUT','CATEGORY','Cập nhật danh mục'),(23,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/categories/{id}','DELETE','CATEGORY','Xóa danh mục'),(24,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/orders','GET','ORDER','Lấy danh sách tất cả đơn hàng'),(25,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/orders/{id}','GET','ORDER','Lấy chi tiết một đơn hàng'),(26,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/orders/{id}','PUT','ORDER','Cập nhật trạng thái đơn hàng'),(27,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/orders/user/{userId}','GET','ORDER','Lấy tất cả đơn hàng của một người dùng'),(28,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/orders/{id}','DELETE','ORDER','Xóa một đơn hàng'),(29,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/inventory','GET','INVENTORY','Lấy danh sách tồn kho'),(30,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/inventory/{productId}','GET','INVENTORY','Kiểm tra tồn kho của sản phẩm'),(31,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/inventory/add','POST','INVENTORY','Thêm số lượng tồn kho'),(32,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/inventory/update','PUT','INVENTORY','Cập nhật số lượng tồn kho'),(33,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/payments','GET','PAYMENT','Lấy lịch sử tất cả giao dịch'),(34,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/payments/{transactionId}','GET','PAYMENT','Lấy chi tiết một giao dịch'),(35,'2025-11-03 02:46:14.000000','admin@gmail.com','admin@gmail.com','2025-11-03 02:46:14.000000','/api/payments/refund','POST','PAYMENT','Thực hiện hoàn tiền cho đơn hàng');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `role_description` varchar(255) DEFAULT NULL,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK716hgxp60ym1lifrdgp67xt5k` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'2025-11-02 20:57:19.162213','admin','admin','2025-11-02 20:57:19.162213',_binary '','vai trò quản trị viên','ADMIN'),(2,'2025-11-02 20:57:19.229464','admin','admin','2025-11-02 20:57:19.229464',_binary '','vai trò người dùng','USER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles_permissions`
--

DROP TABLE IF EXISTS `roles_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles_permissions` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FKbx9r9uw77p58gsq4mus0mec0o` (`permission_id`),
  CONSTRAINT `FKbx9r9uw77p58gsq4mus0mec0o` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKqi9odri6c1o81vjox54eedwyh` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles_permissions`
--

LOCK TABLES `roles_permissions` WRITE;
/*!40000 ALTER TABLE `roles_permissions` DISABLE KEYS */;
INSERT INTO `roles_permissions` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),(1,31),(1,32),(1,33),(1,34),(1,35);
/*!40000 ALTER TABLE `roles_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `role_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `FKp56c1712k691lhsyewcssf40f` (`role_id`),
  CONSTRAINT `FKp56c1712k691lhsyewcssf40f` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-11-02 22:30:18.857270','anonymousUser','anonymousUser','2025-11-02 22:30:18.857270',NULL,NULL,'nguyenvana@example.com',NULL,NULL,'$2a$10$SS0RhOJ/vmEVDX.3Ezfg1Ol/4XMDQForPiAdwsbv1lWmzLOgV5UI.','Nguyễn Văn A',2),(2,'2025-11-02 23:15:45.401992','anonymousUser','anonymousUser','2025-11-02 23:15:45.401992',NULL,NULL,'nguyenvanan@example.com',NULL,NULL,'$2a$10$GARmMMq5Nm.Bs.5AsSUr.OrFsB12JqWpcGZQEjaDL8TIUxgnGFG4m','Nguyễn Văn A',2),(3,'2025-11-03 00:50:18.000000','admin','admin','2025-11-03 00:50:18.000000','Admin Address','1990-01-01','admin@gmail.com','MALE',NULL,'$2a$10$o.zI3wDc/HSJqWd4fcZ7Ve.qdPfdB08kBbtYZnwskZ35FECkyhxVm','admin',1),(4,'2025-11-03 01:05:53.936826','anonymousUser','anonymousUser','2025-11-03 01:05:53.936826',NULL,NULL,'test681907488@example.com',NULL,NULL,'$2a$10$o.zI3wDc/HSJqWd4fcZ7Ve.qdPfdB08kBbtYZnwskZ35FECkyhxVm','Test User',2),(5,'2025-11-03 01:15:38.627976','anonymousUser','anonymousUser','2025-11-03 02:07:09.621424',NULL,NULL,'an01639419125@gmail.com',NULL,NULL,'$2a$10$QLzzD6ZBhvkIvY1rO3shVuiCnoqrvF2M5nsT.Lm09ClOxwawOvW1W','Tran An',2),(6,'2025-11-03 04:02:12.111698','anonymousUser','anonymousUser','2025-11-03 04:02:12.111698',NULL,NULL,'an0163941915@gmail.com',NULL,NULL,'$2a$10$jSwcBDLwG.eHdp41T/VKKeDsbE3tg.QX.psIyg0Hu9zppLPbNNluu','Tran An',2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-03 11:19:18
