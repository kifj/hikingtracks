--
-- Table structure for table `geolocation`
--

CREATE TABLE `geolocation` (
  `id` int(11) NOT NULL,
  `area` varchar(100) DEFAULT NULL,
  `elev` float DEFAULT NULL,
  `lat` float DEFAULT NULL,
  `lng` float DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `source` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `track_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_geolocation_location` (`location`),
  KEY `idx_geolocation_area` (`area`),
  KEY `idx_geolocation_country` (`country`),
  KEY `FK_48ics04idh3dxk0l8gu30vmpv` (`track_id`),
  CONSTRAINT `FK3ee8i2r5wmiftjda3yectxmj5` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`),
  CONSTRAINT `FK_48ics04idh3dxk0l8gu30vmpv` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `hibernate_sequence`
--

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `image`
--

CREATE TABLE `image` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `url` varchar(200) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `track_id` int(11) NOT NULL,
  `lat` double DEFAULT NULL,
  `lon` double DEFAULT NULL,
  `nr` int(11) NOT NULL,
  `image_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5FAA95BD419FF34` (`track_id`),
  KEY `idx_image_nr` (`nr`),
  CONSTRAINT `FK709l66t5kafxctemhb2t0gydd` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`),
  CONSTRAINT `fk_track_image` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `image_data`
--

CREATE TABLE `image_data` (
  `id` int(11) NOT NULL,
  `image_data` longblob DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `image_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kup9iljg3c2usew0appcesqi3` (`image_id`),
  KEY `fk_image_data_image` (`image_id`),
  CONSTRAINT `fk_image_data_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `thumbnail`
--

CREATE TABLE `thumbnail` (
  `id` int(11) NOT NULL,
  `image_data` longblob NOT NULL,
  `type` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `image_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_image_image_id_type` (`image_id`,`type`),
  UNIQUE KEY `idx_thumbnail_image_type` (`type`,`image_id`),
  KEY `FK4F4E50EC54864534` (`image_id`),
  CONSTRAINT `FKejypvunne3uney30546lvdgw4` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`),
  CONSTRAINT `fk_thumbnail_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `track`
--

CREATE TABLE `track` (
  `id` int(11) NOT NULL,
  `track_date` date DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `published` tinyint(1) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `last_change` datetime DEFAULT NULL,
  `publish_date` date DEFAULT NULL,
  `lat` double DEFAULT NULL,
  `lon` double DEFAULT NULL,
  `activity` varchar(32) DEFAULT NULL,
  `geolocation_available` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_track_name_unq` (`name`,`user_id`),
  KEY `FK697F14B8804B140` (`user_id`),
  KEY `idx_track_location` (`location`),
  KEY `idx_track_published` (`published`),
  KEY `idx_track_name` (`name`),
  CONSTRAINT `FKqwwkfhaejpmfjgud9ghs8fe4g` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`),
  CONSTRAINT `fk_user_track` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `track_data`
--

CREATE TABLE `track_data` (
  `id` int(11) NOT NULL,
  `track_data` longblob DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `url` varchar(200) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `track_id` int(11) NOT NULL,
  `end_elev` float DEFAULT NULL,
  `end_lat` float DEFAULT NULL,
  `end_lng` float DEFAULT NULL,
  `high_elev` float DEFAULT NULL,
  `high_lat` float DEFAULT NULL,
  `high_lng` float DEFAULT NULL,
  `low_elev` float DEFAULT NULL,
  `low_lat` float DEFAULT NULL,
  `low_lng` float DEFAULT NULL,
  `start_elev` float DEFAULT NULL,
  `start_lat` float DEFAULT NULL,
  `start_lng` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK451BCE1ED419FF34` (`track_id`),
  KEY `idx_start_lat` (`start_lat`),
  KEY `idx_start_lng` (`start_lng`),
  KEY `idx_end_lat` (`end_lat`),
  KEY `idx_end_lng` (`end_lng`),
  CONSTRAINT `FKlnhy6yrleo9tb5n94sulapytn` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`),
  CONSTRAINT `fk_track_track_data` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `user_account`
--

CREATE TABLE `user_account` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `expires` datetime DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `published` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `idx_user_email` (`email`),
  UNIQUE KEY `idx_user_token` (`token`),
  KEY `idx_user_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
