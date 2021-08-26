RFID Reader integration for Qapel

This server receives html posts from a properly configured SpeedwayR reader running Speedway Connect

Speedway connect needs to be configured to point at the server to send HTTP: Posts to

http://IP_ADDRESS_OF_THIS_SERVICE:CONFIGURED_PORT/tag/impinj/add_tag

It should be enabled for BASIC authentication username: user, password: qapel

Each reader needs to be given a unique name.

Timestamp format should be ISO 8601

This server can be configured to map each antenna on the reader to a different work station and reader result

All configuration is in the MySql database, and tags are read into the database with the following schema:

CREATE TABLE `repository` (
  `epc` varchar(45) NOT NULL,
  `station_id` int NOT NULL,
  `final_status` varchar(45) DEFAULT NULL,
  `last_read` timestamp NULL DEFAULT NULL,
  `first_read` timestamp NULL DEFAULT NULL,
  `num_reads` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`epc`,`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `station` (
  `id` int NOT NULL AUTO_INCREMENT,
  `station_order` int DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `stations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `station_id` int DEFAULT NULL,
  `reader_name` varchar(45) DEFAULT NULL,
  `antenna` int DEFAULT NULL,
  `order` int DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7w09xcu5neesfusmbxkd6at8b` (`station_id`),
  CONSTRAINT `FK7w09xcu5neesfusmbxkd6at8b` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tags` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `Reader_Name` varchar(45) NOT NULL,
  `EPC` varchar(45) NOT NULL,
  `Antenna` tinyint DEFAULT NULL,
  `Status` varchar(45) DEFAULT NULL,
  `Station_ID` int DEFAULT NULL,
  `First_Read` timestamp NULL DEFAULT NULL,
  `Last_Read` timestamp NULL DEFAULT NULL,
  `Num_Reads` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idtags_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `unknown_stations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reader_name` varchar(45) DEFAULT NULL,
  `antenna` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user` (
  `id` int NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `idtags` int NOT NULL,
  `antenna` tinyint NOT NULL,
  `epc` varchar(255) DEFAULT NULL,
  `first_read` datetime(6) DEFAULT NULL,
  `last_read` datetime(6) DEFAULT NULL,
  `num_reads` int NOT NULL,
  `reader_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


Build the system in the rfid project directory using the command:

./mvnw package

This will package the uber jar in the /target directory

The default server port is 8080 and the default database setup is shown in the sample below.

An application.properties file can be created in a sub directory from target to change these settings using this
format:

spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/reader
spring.datasource.username=qapel
spring.datasource.password=digqap2021
spring.datasource.driver-class-name =com.mysql.cj.jdbc.Driver
server.port=80


Note, if you are going to change the port to 80 you will need to run the application with root privileges

sudo java -jar rfid-0.0.1-SNAPSHOT.jar

Note: It is a good idea to tune java parameters to the system it is running on and provide heap management parameters

Web pages can then be accessed from a browser on the network: HTTP://IP_OF_THIS_SERVICE:CONFIGURED_PORT


