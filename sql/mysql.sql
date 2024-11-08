DROP TABLE IF EXISTS `player_equipments`;
DROP TABLE IF EXISTS `player_items`;
DROP TABLE IF EXISTS `players`;
DROP TABLE IF EXISTS `accounts`;
DROP TABLE IF EXISTS `market`;
DROP TABLE IF EXISTS `mission_storage`;
DROP TABLE IF EXISTS `sensors`;
DROP TABLE IF EXISTS `sensors_input`;

CREATE TABLE `accounts`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(32) NOT NULL DEFAULT '',
	`password` VARCHAR(255) NOT NULL/* VARCHAR(32) NOT NULL COMMENT 'MD5'*//* VARCHAR(40) NOT NULL COMMENT 'SHA1'*/,
	PRIMARY KEY (`id`), UNIQUE (`name`)
) ENGINE = InnoDB;

INSERT INTO `accounts` VALUES (1, '1', '1');

CREATE TABLE `players`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(255) NOT NULL,
	`world_map` INT NOT NULL DEFAULT 0,
	`account_id` INT NOT NULL DEFAULT 0,
	`level` INT NOT NULL DEFAULT 1,
	`experience` BIGINT NOT NULL DEFAULT 0,
	`posx` INT NOT NULL DEFAULT 0,
	`posy` INT NOT NULL DEFAULT 0,
	`gold` BIGINT NOT NULL DEFAULT 0,
	`first_login` BOOLEAN NOT NULL DEFAULT TRUE,
	`online` BOOLEAN NOT NULL DEFAULT FALSE,
	PRIMARY KEY (`id`), UNIQUE (`name`),
	KEY (`account_id`),
	FOREIGN KEY (`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO `players` VALUES (1, 'Jotun', 0, 1, 1, 0, 0, 0, 0, true, false);

CREATE TABLE `player_items`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`uid` INT NOT NULL,
	`player_id` INT NOT NULL DEFAULT 0,
	`level` INT NOT NULL DEFAULT 0,
	`page` INT NOT NULL DEFAULT 0,
	`idxi` INT NOT NULL DEFAULT 0,
	`idxj` INT NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`),
	KEY (`player_id`),
	FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO `player_items` VALUES (1, 3000, 1, 1, 1, 0, 0);

CREATE TABLE `player_equipments`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`uid` INT NOT NULL,
	`player_id` INT NOT NULL DEFAULT 0,
	`level` INT NOT NULL DEFAULT 0,
	`slot` INT NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`),
	KEY (`player_id`),
	FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO `player_equipments` VALUES (1, 3000, 1, 1, 1);

CREATE TABLE `market`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`uid` INT NOT NULL,
	`player_id` INT NOT NULL DEFAULT 0,
	`level` INT NOT NULL DEFAULT 0,
	`price` BIGINT NOT NULL DEFAULT 0,
	`quality` INT NOT NULL DEFAULT 0,
	`sold` BOOLEAN NOT NULL DEFAULT FALSE,
	PRIMARY KEY (`id`),
	KEY (`player_id`),
	FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO `market` VALUES (1, 7000, 1, 10, 520, 3, false);

CREATE TABLE `mission_storage`
(
	`mission_id` INT NOT NULL DEFAULT 0,
	`player_id` INT NOT NULL DEFAULT 0,
	`timestamp` timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (`mission_id`, `player_id`),
	KEY (`player_id`),
	FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO `mission_storage` VALUES (0, 1, NULL);

CREATE TABLE `sensors`
(
	`sensor_id` VARCHAR(255) NOT NULL,
	`sensor_type` VARCHAR(255) NOT NULL DEFAULT 'mission',
	`mission_id` INT,
	`thumbnail_id` INT,
	`code` VARCHAR(255),
	`n_inputs` INT NOT NULL DEFAULT 0,
	`input_type` VARCHAR(255),
	PRIMARY KEY (`sensor_id`)
) ENGINE = InnoDB;

INSERT INTO `sensors` VALUES ('1-F45EAB2755CC', 'mission', 0, 0, 'RGBR', 0, NULL);
INSERT INTO `sensors` VALUES ('1-34B1F7D508B2', 'mission', 1, 1, NULL, 2, 'integer') ;

CREATE TABLE `sensors_input`
(
	`id` INT NOT NULL AUTO_INCREMENT,
	`sensor_id` VARCHAR(255) NOT NULL,
	`input_type` VARCHAR(255) NOT NULL,
	`input_name` VARCHAR(255) NOT NULL,
	`input_data` VARCHAR(255) NOT NULL,
	`timestamp` timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (`id`),
	KEY (`sensor_id`),
	FOREIGN KEY (`sensor_id`) REFERENCES `sensors`(`sensor_id`) ON DELETE CASCADE
) ENGINE = InnoDB;