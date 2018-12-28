-- MySQL dump 10.13  Distrib 5.6.40, for macos10.13 (x86_64)
--
-- Host: 127.0.0.1    Database: prosolo
-- ------------------------------------------------------
-- Server version	5.7.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity1` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `difficulty` int(11) DEFAULT '3',
  `duration` bigint(20) NOT NULL,
  `grading_mode` varchar(255) NOT NULL,
  `max_points` int(11) NOT NULL,
  `result_type` varchar(255) NOT NULL,
  `rubric_visibility` varchar(255) NOT NULL,
  `student_can_edit_response` char(1) DEFAULT 'F',
  `student_can_see_other_responses` char(1) DEFAULT 'F',
  `type` varchar(255) NOT NULL,
  `version` bigint(20) NOT NULL,
  `visible_for_unenrolled_students` char(1) DEFAULT 'F',
  `accept_grades` char(1) DEFAULT 'F',
  `consumer_key` varchar(255) DEFAULT NULL,
  `launch_url` varchar(255) DEFAULT NULL,
  `open_in_new_window` char(1) DEFAULT 'F',
  `score_calculation` varchar(255) DEFAULT NULL,
  `shared_secret` varchar(255) DEFAULT NULL,
  `text` text,
  `link_name` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `url_type` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `rubric` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_51vt5mwt287fcjsoap7vsm5e2` (`created_by`),
  KEY `FK_r28bfc9xvnj41036ky8xb7o98` (`rubric`),
  CONSTRAINT `FK_51vt5mwt287fcjsoap7vsm5e2` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_r28bfc9xvnj41036ky8xb7o98` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity1`
--

LOCK TABLES `activity1` WRITE;
/*!40000 ALTER TABLE `activity1` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity1` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity1_captions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity1_captions` (
  `activity1` bigint(20) NOT NULL,
  `captions` bigint(20) NOT NULL,
  PRIMARY KEY (`activity1`,`captions`),
  UNIQUE KEY `UK_b1n7gy4nb3oplj5v8jhali4rs` (`captions`),
  CONSTRAINT `FK_8gchebrsymmudflj24yo48n79` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`),
  CONSTRAINT `FK_b1n7gy4nb3oplj5v8jhali4rs` FOREIGN KEY (`captions`) REFERENCES `resource_link` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity1_captions`
--

LOCK TABLES `activity1_captions` WRITE;
/*!40000 ALTER TABLE `activity1_captions` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity1_captions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity1_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity1_files` (
  `activity1` bigint(20) NOT NULL,
  `files` bigint(20) NOT NULL,
  PRIMARY KEY (`activity1`,`files`),
  UNIQUE KEY `UK_h9l61ttwr60t42cj53buu3rm0` (`files`),
  CONSTRAINT `FK_h9l61ttwr60t42cj53buu3rm0` FOREIGN KEY (`files`) REFERENCES `resource_link` (`id`),
  CONSTRAINT `FK_m20elbr34b3oe5p86dcd773nl` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity1_files`
--

LOCK TABLES `activity1_files` WRITE;
/*!40000 ALTER TABLE `activity1_files` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity1_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity1_links`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity1_links` (
  `activity1` bigint(20) NOT NULL,
  `links` bigint(20) NOT NULL,
  PRIMARY KEY (`activity1`,`links`),
  UNIQUE KEY `UK_mpsbwvmwi7kc3a6sbcm8s9e5f` (`links`),
  CONSTRAINT `FK_mpsbwvmwi7kc3a6sbcm8s9e5f` FOREIGN KEY (`links`) REFERENCES `resource_link` (`id`),
  CONSTRAINT `FK_rhmeeesk0sdkg3qagjmf9lroj` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity1_links`
--

LOCK TABLES `activity1_links` WRITE;
/*!40000 ALTER TABLE `activity1_links` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity1_links` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity1_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity1_tags` (
  `activity1` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL,
  PRIMARY KEY (`activity1`,`tags`),
  KEY `FK_4lcw676ek48pd37v9kr0mymim` (`tags`),
  CONSTRAINT `FK_4lcw676ek48pd37v9kr0mymim` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_hynwy4o71nraccfi6lu06ggsc` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity1_tags`
--

LOCK TABLES `activity1_tags` WRITE;
/*!40000 ALTER TABLE `activity1_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity1_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `points` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `activity` bigint(20) NOT NULL,
  `competence_assessment` bigint(20) NOT NULL,
  `grade` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ir3b4ua2jau1q2mlw3xhwr4s7` (`competence_assessment`,`activity`),
  KEY `FK_o3uai8md6l5ilpc98ynjnlks0` (`activity`),
  KEY `FK_pqe8ab74gg1e45r1muu3l2f48` (`grade`),
  CONSTRAINT `FK_8h8fnyxbc74ksc3qdhy48ic3t` FOREIGN KEY (`competence_assessment`) REFERENCES `competence_assessment` (`id`),
  CONSTRAINT `FK_o3uai8md6l5ilpc98ynjnlks0` FOREIGN KEY (`activity`) REFERENCES `activity1` (`id`),
  CONSTRAINT `FK_pqe8ab74gg1e45r1muu3l2f48` FOREIGN KEY (`grade`) REFERENCES `activity_grade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_assessment`
--

LOCK TABLES `activity_assessment` WRITE;
/*!40000 ALTER TABLE `activity_assessment` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_assessment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_criterion_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_criterion_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `criterion` bigint(20) DEFAULT NULL,
  `level` bigint(20) DEFAULT NULL,
  `assessment` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_b2gf4fq74omli2r1ftnq3ggob` (`assessment`,`criterion`),
  KEY `FK_l9389a0coyr4791boeeb8reuj` (`criterion`),
  KEY `FK_j41uvkbj5h50bv2okbqakkolf` (`level`),
  CONSTRAINT `FK_j41uvkbj5h50bv2okbqakkolf` FOREIGN KEY (`level`) REFERENCES `level` (`id`),
  CONSTRAINT `FK_l9389a0coyr4791boeeb8reuj` FOREIGN KEY (`criterion`) REFERENCES `criterion` (`id`),
  CONSTRAINT `FK_py8sdhd47tm0swq51k6gtm8pt` FOREIGN KEY (`assessment`) REFERENCES `activity_assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_criterion_assessment`
--

LOCK TABLES `activity_criterion_assessment` WRITE;
/*!40000 ALTER TABLE `activity_criterion_assessment` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_criterion_assessment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_discussion_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_discussion_message` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(9000) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `discussion` bigint(20) DEFAULT NULL,
  `sender` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_pc9oblcdq0wtaqumrpoghmym5` (`discussion`),
  KEY `FK_gp4rjlpt100i22gadcrqsuyea` (`sender`),
  CONSTRAINT `FK_gp4rjlpt100i22gadcrqsuyea` FOREIGN KEY (`sender`) REFERENCES `activity_discussion_participant` (`id`),
  CONSTRAINT `FK_pc9oblcdq0wtaqumrpoghmym5` FOREIGN KEY (`discussion`) REFERENCES `activity_assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_discussion_message`
--

LOCK TABLES `activity_discussion_message` WRITE;
/*!40000 ALTER TABLE `activity_discussion_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_discussion_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_discussion_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_discussion_participant` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `is_read` char(1) DEFAULT 'T',
  `activity_discussion` bigint(20) DEFAULT NULL,
  `participant` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_1aja12ermpd5n492qpm0wv51p` (`activity_discussion`),
  KEY `FK_klimyuls8rsoqt7v9ce5gqts9` (`participant`),
  CONSTRAINT `FK_1aja12ermpd5n492qpm0wv51p` FOREIGN KEY (`activity_discussion`) REFERENCES `activity_assessment` (`id`),
  CONSTRAINT `FK_klimyuls8rsoqt7v9ce5gqts9` FOREIGN KEY (`participant`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_discussion_participant`
--

LOCK TABLES `activity_discussion_participant` WRITE;
/*!40000 ALTER TABLE `activity_discussion_participant` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_discussion_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_grade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_grade` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_grade`
--

LOCK TABLES `activity_grade` WRITE;
/*!40000 ALTER TABLE `activity_grade` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_wall_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_wall_settings` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `chosen_filter` varchar(255) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_wall_settings`
--

LOCK TABLES `activity_wall_settings` WRITE;
/*!40000 ALTER TABLE `activity_wall_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `activity_wall_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `annotation1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annotation1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `annotated_resource` varchar(255) NOT NULL,
  `annotated_resource_id` bigint(20) NOT NULL,
  `annotation_type` varchar(255) NOT NULL,
  `maker` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qg56tf64cpvwkc5p2q9bjoape` (`maker`),
  CONSTRAINT `FK_qg56tf64cpvwkc5p2q9bjoape` FOREIGN KEY (`maker`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `annotation1`
--

LOCK TABLES `annotation1` WRITE;
/*!40000 ALTER TABLE `annotation1` DISABLE KEYS */;
/*!40000 ALTER TABLE `annotation1` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `announcement` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `announcement_text` varchar(5000) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `credential` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_i5ssis4ksi4j8jsbb9xga1odi` (`created_by`),
  KEY `FK_5n0gf0pgl54yxksqbrpg49jwu` (`credential`),
  CONSTRAINT `FK_5n0gf0pgl54yxksqbrpg49jwu` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_i5ssis4ksi4j8jsbb9xga1odi` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `capability`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capability` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `capability_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capability_role` (
  `capabilities` bigint(20) NOT NULL,
  `roles` bigint(20) NOT NULL,
  PRIMARY KEY (`capabilities`,`roles`),
  KEY `FK_bk8wqohsuock5hix7lnb84b0r` (`roles`),
  CONSTRAINT `FK_1j72vl0nwsv55q41b6ydg0hhd` FOREIGN KEY (`capabilities`) REFERENCES `capability` (`id`),
  CONSTRAINT `FK_bk8wqohsuock5hix7lnb84b0r` FOREIGN KEY (`roles`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `commented_resource_id` bigint(20) DEFAULT NULL,
  `instructor` bit(1) NOT NULL,
  `like_count` int(11) NOT NULL,
  `manager_comment` char(1) DEFAULT 'F',
  `post_date` datetime DEFAULT NULL,
  `resource_type` varchar(255) NOT NULL,
  `parent_comment` bigint(20) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_comment1_commented_resource_id_resource_type` (`commented_resource_id`,`resource_type`),
  KEY `FK_q7hjkgwtsss4hrk6dfs022823` (`parent_comment`),
  KEY `FK_cnb1hy7io12vb2fbi1jdmyalw` (`user`),
  CONSTRAINT `FK_cnb1hy7io12vb2fbi1jdmyalw` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_q7hjkgwtsss4hrk6dfs022823` FOREIGN KEY (`parent_comment`) REFERENCES `comment1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `archived` char(1) DEFAULT 'F',
  `date_published` datetime DEFAULT NULL,
  `duration` bigint(20) NOT NULL,
  `grading_mode` varchar(255) NOT NULL,
  `learning_path_type` varchar(255) NOT NULL,
  `max_points` int(11) NOT NULL,
  `published` bit(1) NOT NULL,
  `student_allowed_to_add_activities` bit(1) NOT NULL,
  `type` varchar(255) NOT NULL,
  `version` bigint(20) NOT NULL,
  `visible_to_all` char(1) DEFAULT 'F',
  `created_by` bigint(20) DEFAULT NULL,
  `first_learning_stage_competence` bigint(20) DEFAULT NULL,
  `learning_stage` bigint(20) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  `original_version` bigint(20) DEFAULT NULL,
  `rubric` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_3oyo6ynive1sdpf54j7lx3ggb` (`first_learning_stage_competence`,`learning_stage`),
  KEY `FK_j0jn9c30xitfotmamc9181go5` (`created_by`),
  KEY `FK_lb3u5p37p6e33a9caoi64r1cj` (`learning_stage`),
  KEY `FK_91ik0ggqkcwdde3bjco1da0gf` (`organization`),
  KEY `FK_btejhdyv6se49077y8v2sw5sq` (`original_version`),
  KEY `FK_11ldaw0qdiu7cbcxb74l4xxcy` (`rubric`),
  CONSTRAINT `FK_11ldaw0qdiu7cbcxb74l4xxcy` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`),
  CONSTRAINT `FK_91ik0ggqkcwdde3bjco1da0gf` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`),
  CONSTRAINT `FK_btejhdyv6se49077y8v2sw5sq` FOREIGN KEY (`original_version`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_j0jn9c30xitfotmamc9181go5` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_lb3u5p37p6e33a9caoi64r1cj` FOREIGN KEY (`learning_stage`) REFERENCES `learning_stage` (`id`),
  CONSTRAINT `FK_rovwcltntpusa8fv9fk2ox4c5` FOREIGN KEY (`first_learning_stage_competence`) REFERENCES `competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence1_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence1_tags` (
  `competence1` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL,
  PRIMARY KEY (`competence1`,`tags`),
  KEY `FK_9lsylf57uy3m2se4ve4ameihu` (`tags`),
  CONSTRAINT `FK_9lsylf57uy3m2se4ve4ameihu` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_njpyjqs5thuuyw5xwm1paihxf` FOREIGN KEY (`competence1`) REFERENCES `competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_activity1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_activity1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `act_order` int(11) DEFAULT NULL,
  `activity` bigint(20) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_px25x3t4d4wxgujuocgi5mc51` (`activity`),
  KEY `FK_ka2ffpd609s5ug445vou0anhi` (`competence`),
  CONSTRAINT `FK_ka2ffpd609s5ug445vou0anhi` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_px25x3t4d4wxgujuocgi5mc51` FOREIGN KEY (`activity`) REFERENCES `activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `approved` bit(1) DEFAULT NULL,
  `assessor_notified` bit(1) NOT NULL,
  `last_asked_for_assessment` datetime DEFAULT NULL,
  `last_assessment` datetime DEFAULT NULL,
  `points` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `assessor` bigint(20) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `student` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lbo2bjanrdpkwedf9p6q6o248` (`assessor`),
  KEY `FK_k9y34nq3hj6fdbdaftoligfkc` (`competence`),
  KEY `FK_tiy256v79b1c8vfiest7m8piw` (`student`),
  CONSTRAINT `FK_k9y34nq3hj6fdbdaftoligfkc` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_lbo2bjanrdpkwedf9p6q6o248` FOREIGN KEY (`assessor`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_tiy256v79b1c8vfiest7m8piw` FOREIGN KEY (`student`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_assessment_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_assessment_config` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `assessment_type` varchar(255) NOT NULL,
  `enabled` char(1) DEFAULT 'F',
  `competence` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7u112t9m0qt8cdf1pwd2t6nam` (`competence`,`assessment_type`),
  CONSTRAINT `FK_16qjgpixu90actkwbekfav4ui` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_assessment_discussion_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_assessment_discussion_participant` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `is_read` char(1) DEFAULT 'T',
  `assessment` bigint(20) DEFAULT NULL,
  `participant` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_136fh13xo3iys1yq0hti8xlnq` (`assessment`),
  KEY `FK_tk9bucqcwbukxgjkkckysawf5` (`participant`),
  CONSTRAINT `FK_136fh13xo3iys1yq0hti8xlnq` FOREIGN KEY (`assessment`) REFERENCES `competence_assessment` (`id`),
  CONSTRAINT `FK_tk9bucqcwbukxgjkkckysawf5` FOREIGN KEY (`participant`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_assessment_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_assessment_message` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(9000) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `assessment` bigint(20) DEFAULT NULL,
  `sender` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_3cpxtlvcviq1sov7gb7fqgcfu` (`assessment`),
  KEY `FK_ioqggfbo6kiw9yr5ghv25rjgb` (`sender`),
  CONSTRAINT `FK_3cpxtlvcviq1sov7gb7fqgcfu` FOREIGN KEY (`assessment`) REFERENCES `competence_assessment` (`id`),
  CONSTRAINT `FK_ioqggfbo6kiw9yr5ghv25rjgb` FOREIGN KEY (`sender`) REFERENCES `competence_assessment_discussion_participant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_bookmark`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_bookmark` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_k4uyv9uljtw36cxchatxxhi7d` (`competence`,`user`),
  KEY `FK_7fwnajpqa9j4dfaciibethkll` (`user`),
  CONSTRAINT `FK_7fwnajpqa9j4dfaciibethkll` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_pu790xeulgg9f166ehutb0p0v` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_criterion_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_criterion_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `criterion` bigint(20) DEFAULT NULL,
  `level` bigint(20) DEFAULT NULL,
  `assessment` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cfbmdanr0gb6gaciqhgiee406` (`assessment`,`criterion`),
  KEY `FK_lkk5e6em0p1ccndm11xybktoy` (`criterion`),
  KEY `FK_smjx7i6jja0xf458dwpfbuaos` (`level`),
  CONSTRAINT `FK_cyefgjjblo1bchk4vjw8fmwxi` FOREIGN KEY (`assessment`) REFERENCES `competence_assessment` (`id`),
  CONSTRAINT `FK_lkk5e6em0p1ccndm11xybktoy` FOREIGN KEY (`criterion`) REFERENCES `criterion` (`id`),
  CONSTRAINT `FK_smjx7i6jja0xf458dwpfbuaos` FOREIGN KEY (`level`) REFERENCES `level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_evidence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_evidence` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `evidence` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_8ih6dh7gn5uo6jwx9r3pt82sw` (`competence`),
  KEY `FK_nctgqbkwam6fn84i27cyaav2p` (`evidence`),
  CONSTRAINT `FK_8ih6dh7gn5uo6jwx9r3pt82sw` FOREIGN KEY (`competence`) REFERENCES `target_competence1` (`id`),
  CONSTRAINT `FK_nctgqbkwam6fn84i27cyaav2p` FOREIGN KEY (`evidence`) REFERENCES `learning_evidence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7mbm0rkejlh524nk5ieyh24ml` (`competence`,`unit`),
  KEY `FK_16il9gr3688yooyw91vsiis91` (`unit`),
  CONSTRAINT `FK_16il9gr3688yooyw91vsiis91` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_d9i2062693wmimcdyvdr1ynhh` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `competence_user_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `competence_user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `inherited` char(1) DEFAULT 'F',
  `privilege` varchar(255) NOT NULL,
  `competence` bigint(20) NOT NULL,
  `inherited_from` bigint(20) DEFAULT NULL,
  `user_group` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_60mk2r1qk5llr3p901h5705y6` (`competence`),
  KEY `FK_e9iw7p463kmjjsyuj9r5wmxpe` (`inherited_from`),
  KEY `FK_4b5s1r5gcdytwdbp2pr4rn4v5` (`user_group`),
  CONSTRAINT `FK_4b5s1r5gcdytwdbp2pr4rn4v5` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`),
  CONSTRAINT `FK_60mk2r1qk5llr3p901h5705y6` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_e9iw7p463kmjjsyuj9r5wmxpe` FOREIGN KEY (`inherited_from`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `archived` char(1) DEFAULT 'F',
  `assessor_assignment_method` varchar(255) DEFAULT NULL,
  `competence_order_mandatory` bit(1) NOT NULL,
  `default_number_of_students_per_instructor` int(11) NOT NULL,
  `delivery_end` datetime DEFAULT NULL,
  `delivery_order` int(11) DEFAULT '0',
  `delivery_start` datetime DEFAULT NULL,
  `duration` bigint(20) NOT NULL,
  `grading_mode` varchar(255) NOT NULL,
  `max_points` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `version` bigint(20) NOT NULL,
  `visible_to_all` char(1) DEFAULT 'F',
  `category` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `delivery_of` bigint(20) DEFAULT NULL,
  `first_learning_stage_credential` bigint(20) DEFAULT NULL,
  `learning_stage` bigint(20) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  `rubric` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6vclpmt9xfxxxybg8tdc4axwt` (`first_learning_stage_credential`,`learning_stage`),
  KEY `FK_6nv6h30ry8lvl7g9lxj83oo2r` (`category`),
  KEY `FK_gwgwt3bm30pwx6lxiukyx1o03` (`created_by`),
  KEY `FK_ox2gmlf29yl2o60nwf28avccl` (`delivery_of`),
  KEY `FK_n2vyrbyyls32y7s3aq1gyyh8o` (`learning_stage`),
  KEY `FK_p1g20qojr5ahymmq2c2hkdqh3` (`organization`),
  KEY `FK_4s00xobf7567n67dwachjpsvm` (`rubric`),
  CONSTRAINT `FK_4s00xobf7567n67dwachjpsvm` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`),
  CONSTRAINT `FK_5ve9kkyxam4skfb85dy0n55ot` FOREIGN KEY (`first_learning_stage_credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_6nv6h30ry8lvl7g9lxj83oo2r` FOREIGN KEY (`category`) REFERENCES `credential_category` (`id`),
  CONSTRAINT `FK_gwgwt3bm30pwx6lxiukyx1o03` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_n2vyrbyyls32y7s3aq1gyyh8o` FOREIGN KEY (`learning_stage`) REFERENCES `learning_stage` (`id`),
  CONSTRAINT `FK_ox2gmlf29yl2o60nwf28avccl` FOREIGN KEY (`delivery_of`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_p1g20qojr5ahymmq2c2hkdqh3` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential1_blogs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential1_blogs` (
  `credential1` bigint(20) NOT NULL,
  `blogs` bigint(20) NOT NULL,
  KEY `FK_dgns5f815wa0piods87ak1unv` (`blogs`),
  KEY `FK_p9mlitfs7rd27q1h6x6xqrtat` (`credential1`),
  CONSTRAINT `FK_dgns5f815wa0piods87ak1unv` FOREIGN KEY (`blogs`) REFERENCES `feed_source` (`id`),
  CONSTRAINT `FK_p9mlitfs7rd27q1h6x6xqrtat` FOREIGN KEY (`credential1`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential1_excluded_feed_sources`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential1_excluded_feed_sources` (
  `credential1` bigint(20) NOT NULL,
  `excluded_feed_sources` bigint(20) NOT NULL,
  KEY `FK_id94tkw373r7i4wpptlstjkt2` (`excluded_feed_sources`),
  KEY `FK_116l6dplwn565owmmjwq9nchy` (`credential1`),
  CONSTRAINT `FK_116l6dplwn565owmmjwq9nchy` FOREIGN KEY (`credential1`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_id94tkw373r7i4wpptlstjkt2` FOREIGN KEY (`excluded_feed_sources`) REFERENCES `feed_source` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential1_hashtags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential1_hashtags` (
  `credential1` bigint(20) NOT NULL,
  `hashtags` bigint(20) NOT NULL,
  PRIMARY KEY (`credential1`,`hashtags`),
  KEY `FK_2s3cange8x6y5lvxsgpulwmro` (`hashtags`),
  CONSTRAINT `FK_2s3cange8x6y5lvxsgpulwmro` FOREIGN KEY (`hashtags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_5k9e4ud4n7uutdcfcp7x8xn2h` FOREIGN KEY (`credential1`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential1_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential1_tags` (
  `credential1` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL,
  PRIMARY KEY (`credential1`,`tags`),
  KEY `FK_e8b1q4xep66kcwhcxfag5a0cl` (`tags`),
  CONSTRAINT `FK_e8b1q4xep66kcwhcxfag5a0cl` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_o10dydyl21smpds2rwh7v8j4s` FOREIGN KEY (`credential1`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `approved` bit(1) DEFAULT NULL,
  `assessed` bit(1) NOT NULL,
  `assessor_notified` bit(1) NOT NULL,
  `last_asked_for_assessment` datetime DEFAULT NULL,
  `last_assessment` datetime DEFAULT NULL,
  `points` int(11) NOT NULL,
  `review` longtext,
  `type` varchar(255) NOT NULL,
  `assessor` bigint(20) DEFAULT NULL,
  `student` bigint(20) DEFAULT NULL,
  `target_credential` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_64x1nbncerttcr6ukwn1i10yy` (`assessor`),
  KEY `FK_o0soi8jus7un1g5yqyfrpo3rs` (`student`),
  KEY `FK_gvcyenwhr1wjurx8srk0l65m` (`target_credential`),
  CONSTRAINT `FK_64x1nbncerttcr6ukwn1i10yy` FOREIGN KEY (`assessor`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_gvcyenwhr1wjurx8srk0l65m` FOREIGN KEY (`target_credential`) REFERENCES `target_credential1` (`id`),
  CONSTRAINT `FK_o0soi8jus7un1g5yqyfrpo3rs` FOREIGN KEY (`student`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_assessment_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_assessment_config` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `assessment_type` varchar(255) NOT NULL,
  `blind_assessment_mode` varchar(255) NOT NULL,
  `enabled` char(1) DEFAULT 'F',
  `credential` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_e7upesftge7cjrbgqwqlbhypi` (`credential`,`assessment_type`),
  CONSTRAINT `FK_amujhji7o6s62pk0sj65q290y` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_assessment_discussion_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_assessment_discussion_participant` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `is_read` char(1) DEFAULT 'T',
  `assessment` bigint(20) DEFAULT NULL,
  `participant` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lq0fgfb3gmn18bita5oybkxft` (`assessment`),
  KEY `FK_safwo8xgq1h61ttmdd9kob4hp` (`participant`),
  CONSTRAINT `FK_lq0fgfb3gmn18bita5oybkxft` FOREIGN KEY (`assessment`) REFERENCES `credential_assessment` (`id`),
  CONSTRAINT `FK_safwo8xgq1h61ttmdd9kob4hp` FOREIGN KEY (`participant`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_assessment_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_assessment_message` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(9000) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `assessment` bigint(20) DEFAULT NULL,
  `sender` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_dlo6gfx7ifgbh5dg06tvyai67` (`assessment`),
  KEY `FK_e1kd4wxe27u2mh91ql011uj1q` (`sender`),
  CONSTRAINT `FK_dlo6gfx7ifgbh5dg06tvyai67` FOREIGN KEY (`assessment`) REFERENCES `credential_assessment` (`id`),
  CONSTRAINT `FK_e1kd4wxe27u2mh91ql011uj1q` FOREIGN KEY (`sender`) REFERENCES `credential_assessment_discussion_participant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_bookmark`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_bookmark` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `credential` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_huxm5oy8mxf4gcckxuy6m5n9a` (`credential`,`user`),
  KEY `FK_n5fkjwxicertabyarbvju43c3` (`user`),
  CONSTRAINT `FK_d125vpplcb2nwcw100ck2ch7e` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_n5fkjwxicertabyarbvju43c3` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_category` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6mnq89yqpnfilldvrsyblvavb` (`organization`,`title`),
  CONSTRAINT `FK_r8ao03qj30gfud7h29sk1a4hr` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_competence1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_competence1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `comp_order` int(11) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `credential` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_4j36i3b2r10ewvv85c9st8c34` (`competence`),
  KEY `FK_nw0ci3p9g1hiy4yan5w664to9` (`credential`),
  CONSTRAINT `FK_4j36i3b2r10ewvv85c9st8c34` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_nw0ci3p9g1hiy4yan5w664to9` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_competence_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_competence_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `competence_assessment` bigint(20) NOT NULL,
  `credential_assessment` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cq14nxxgq3qb98h7v9dd9ubsj` (`credential_assessment`,`competence_assessment`),
  KEY `FK_hs8gjt68hymusnmsuuar20med` (`competence_assessment`),
  CONSTRAINT `FK_hs8gjt68hymusnmsuuar20med` FOREIGN KEY (`competence_assessment`) REFERENCES `competence_assessment` (`id`),
  CONSTRAINT `FK_lvmp3fd0dxsxestak8yervbrq` FOREIGN KEY (`credential_assessment`) REFERENCES `credential_assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_criterion_assessment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_criterion_assessment` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `criterion` bigint(20) DEFAULT NULL,
  `level` bigint(20) DEFAULT NULL,
  `assessment` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bma5i20uxkurqqatu2k4aetlf` (`assessment`,`criterion`),
  KEY `FK_cgpk6q28vp6usjal5rlce9s9i` (`criterion`),
  KEY `FK_p24wh1b1sc1m8m15ler4xbuvv` (`level`),
  CONSTRAINT `FK_cgpk6q28vp6usjal5rlce9s9i` FOREIGN KEY (`criterion`) REFERENCES `criterion` (`id`),
  CONSTRAINT `FK_nn5t74iqwu7v2isk9xj3dtg7u` FOREIGN KEY (`assessment`) REFERENCES `credential_assessment` (`id`),
  CONSTRAINT `FK_p24wh1b1sc1m8m15ler4xbuvv` FOREIGN KEY (`level`) REFERENCES `level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_instructor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_instructor` (
  `id` bigint(20) NOT NULL,
  `date_assigned` datetime DEFAULT NULL,
  `max_number_of_students` int(11) NOT NULL,
  `credential` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8o1ljk3ajomrhnh8d5qfmcnls` (`user`,`credential`),
  KEY `FK_4c65jghtljkorcy98mnko26lt` (`credential`),
  CONSTRAINT `FK_4c65jghtljkorcy98mnko26lt` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_5he8qwqjrj5oitt390xv1c363` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `credential` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_piaytj54rr4klingo78qx9ylt` (`credential`,`unit`),
  KEY `FK_bcknt360dtw8rh53cphe97q45` (`unit`),
  CONSTRAINT `FK_10qyaft78ol2toenfodqdwc8g` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_bcknt360dtw8rh53cphe97q45` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential_user_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential_user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `privilege` varchar(255) NOT NULL,
  `credential` bigint(20) NOT NULL,
  `user_group` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_j07j1pph3s8y22el92gth2yy5` (`credential`,`user_group`,`privilege`),
  KEY `FK_m5cvxlf67t55is1xw2gxfar0i` (`user_group`),
  CONSTRAINT `FK_30jl08j0war3rn3u23m10uyq8` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_m5cvxlf67t55is1xw2gxfar0i` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `criterion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `criterion` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `criterion_order` int(11) NOT NULL,
  `points` double NOT NULL DEFAULT '0',
  `rubric` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_e1f4a4t2ns27fm40ulwry89u8` (`title`,`rubric`),
  KEY `FK_o7ovdmcl7pmxtdnuk3iyn2sep` (`rubric`),
  CONSTRAINT `FK_o7ovdmcl7pmxtdnuk3iyn2sep` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `criterion_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `criterion_level` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `criterion` bigint(20) NOT NULL,
  `level` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pquo8f8sy5d20h8f5hmivseq8` (`criterion`,`level`),
  KEY `FK_t0ynrcrm7kt8i1luj4onvhjv2` (`level`),
  CONSTRAINT `FK_9l1253t7v8atu5lytujimj09n` FOREIGN KEY (`criterion`) REFERENCES `criterion` (`id`),
  CONSTRAINT `FK_t0ynrcrm7kt8i1luj4onvhjv2` FOREIGN KEY (`level`) REFERENCES `level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feed_entry`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feed_entry` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `relevance` double NOT NULL,
  `feed_source` bigint(20) DEFAULT NULL,
  `maker` bigint(20) DEFAULT NULL,
  `subscribed_user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ffdgn3747cqpgdiwhxlarhp8m` (`feed_source`),
  KEY `FK_cgxvubmhf09musstvw2u00y67` (`maker`),
  KEY `FK_5mympaondc7gotjymqgd05l4i` (`subscribed_user`),
  CONSTRAINT `FK_5mympaondc7gotjymqgd05l4i` FOREIGN KEY (`subscribed_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_cgxvubmhf09musstvw2u00y67` FOREIGN KEY (`maker`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_ffdgn3747cqpgdiwhxlarhp8m` FOREIGN KEY (`feed_source`) REFERENCES `feed_source` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feed_entry_hashtags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feed_entry_hashtags` (
  `feed_entry` bigint(20) NOT NULL,
  `hashtags` varchar(255) DEFAULT NULL,
  KEY `FK_es7wwjpedyq7gxlsk0lexcj5x` (`feed_entry`),
  CONSTRAINT `FK_es7wwjpedyq7gxlsk0lexcj5x` FOREIGN KEY (`feed_entry`) REFERENCES `feed_entry` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feed_source`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feed_source` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `last_check` datetime DEFAULT NULL,
  `link` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feeds_digest`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feeds_digest` (
  `dtype` varchar(50) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `date_from` datetime DEFAULT NULL,
  `number_of_users_that_got_email` bigint(20) NOT NULL,
  `time_frame` varchar(255) NOT NULL,
  `date_to` datetime DEFAULT NULL,
  `credential` bigint(20) DEFAULT NULL,
  `feeds_subscriber` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_hcx2rl68idmgg2sfpnbqmv1tv` (`credential`),
  KEY `FK_j4nanu4eoo6lobqy4v64tegmf` (`feeds_subscriber`),
  CONSTRAINT `FK_hcx2rl68idmgg2sfpnbqmv1tv` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  CONSTRAINT `FK_j4nanu4eoo6lobqy4v64tegmf` FOREIGN KEY (`feeds_subscriber`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feeds_digest_entries`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feeds_digest_entries` (
  `feeds_digest` bigint(20) NOT NULL,
  `entries` bigint(20) NOT NULL,
  KEY `FK_sj34xafykjukgs471aa2w9anj` (`entries`),
  KEY `FK_bhda4lck3rkx4tduxm7fhq8wo` (`feeds_digest`),
  CONSTRAINT `FK_bhda4lck3rkx4tduxm7fhq8wo` FOREIGN KEY (`feeds_digest`) REFERENCES `feeds_digest` (`id`),
  CONSTRAINT `FK_sj34xafykjukgs471aa2w9anj` FOREIGN KEY (`entries`) REFERENCES `feed_entry` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `followed_entity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `followed_entity` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `required_to_follow` char(1) DEFAULT 'F',
  `started_following` datetime DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  `followed_user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6w0pe4f2eii8eov3epmwk849e` (`user`,`followed_user`),
  KEY `FK_4eglof45iulhoj8j2qudvbuxm` (`followed_user`),
  CONSTRAINT `FK_4eglof45iulhoj8j2qudvbuxm` FOREIGN KEY (`followed_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_9i2cqb9qu9aomuu5ju2yrm6f9` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequences`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequences` (
  `sequence_name` varchar(255) DEFAULT NULL,
  `sequence_next_hi_value` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `innodb_lock_monitor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `innodb_lock_monitor` (
  `a` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_evidence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `learning_evidence` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(500) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `url` varchar(1200) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qb3lm6liqglp03b789fl0sdvb` (`organization`),
  KEY `FK_te80g9rqpmjnlpmff23h0we5x` (`user`),
  CONSTRAINT `FK_qb3lm6liqglp03b789fl0sdvb` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`),
  CONSTRAINT `FK_te80g9rqpmjnlpmff23h0we5x` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_evidence_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `learning_evidence_tags` (
  `learning_evidence` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL,
  PRIMARY KEY (`learning_evidence`,`tags`),
  KEY `FK_2i6sole2k1kwkjbgsm2u6aoff` (`tags`),
  CONSTRAINT `FK_2i6sole2k1kwkjbgsm2u6aoff` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_ge8rmsf18x8fwwntcnof1w2c3` FOREIGN KEY (`learning_evidence`) REFERENCES `learning_evidence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_stage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `learning_stage` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `learning_stage_order` int(11) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_atmtmfxbwira99uums40pt8q` (`organization`,`title`),
  CONSTRAINT `FK_kmqfvn3rumkbm0b8dsgfbcnm6` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `level` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `level_order` int(11) NOT NULL,
  `points` double NOT NULL DEFAULT '0',
  `points_from` double NOT NULL DEFAULT '0',
  `points_to` double NOT NULL DEFAULT '0',
  `rubric` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_eo5k8jw0yq5kypsaddagxuoo8` (`title`,`rubric`),
  KEY `FK_rtffghkdqv9g81wd3ecvjemdv` (`rubric`),
  CONSTRAINT `FK_rtffghkdqv9g81wd3ecvjemdv` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `locale_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `locale_settings` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lti_consumer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lti_consumer` (
  `id` bigint(20) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `capabilities` varchar(2000) DEFAULT NULL,
  `key_lti_one` varchar(255) NOT NULL,
  `key_lti_two` varchar(255) DEFAULT NULL,
  `secret_lti_one` varchar(255) NOT NULL,
  `secret_lti_two` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lti_service`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lti_service` (
  `id` bigint(20) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `actions` varchar(255) DEFAULT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `formats` varchar(1000) DEFAULT NULL,
  `consumer` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_j04buydudvuvpfslrit8lht1s` (`consumer`),
  CONSTRAINT `FK_j04buydudvuvpfslrit8lht1s` FOREIGN KEY (`consumer`) REFERENCES `lti_consumer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lti_tool`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lti_tool` (
  `id` bigint(20) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `activity_id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `competence_id` bigint(20) NOT NULL,
  `credential_id` bigint(20) NOT NULL,
  `custom_css` varchar(10000) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `launch_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `resource_name` varchar(255) DEFAULT NULL,
  `tool_key` varchar(255) DEFAULT NULL,
  `tool_type` varchar(255) NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `organization` bigint(20) NOT NULL,
  `tool_set` bigint(20) NOT NULL,
  `unit` bigint(20) DEFAULT NULL,
  `user_group` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_n8iw9ueffp6ceo31pnuensikq` (`created_by`),
  KEY `FK_6eimoaon171t8shrxhqs0dils` (`organization`),
  KEY `FK_f8glreyaxbnn1wq3ox0b76igm` (`tool_set`),
  KEY `FK_75s355fn31ogkxv4fwj6ve1hm` (`unit`),
  KEY `FK_64dtvy9vcghm1b7cv0ymsgg4p` (`user_group`),
  CONSTRAINT `FK_64dtvy9vcghm1b7cv0ymsgg4p` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`),
  CONSTRAINT `FK_6eimoaon171t8shrxhqs0dils` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`),
  CONSTRAINT `FK_75s355fn31ogkxv4fwj6ve1hm` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_f8glreyaxbnn1wq3ox0b76igm` FOREIGN KEY (`tool_set`) REFERENCES `lti_tool_set` (`id`),
  CONSTRAINT `FK_n8iw9ueffp6ceo31pnuensikq` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lti_tool_set`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lti_tool_set` (
  `id` bigint(20) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `product_code` varchar(255) DEFAULT NULL,
  `registration_url` varchar(255) DEFAULT NULL,
  `consumer` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_rxmmq9va47gyeome10lkj5ktw` (`consumer`),
  CONSTRAINT `FK_rxmmq9va47gyeome10lkj5ktw` FOREIGN KEY (`consumer`) REFERENCES `lti_consumer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lti_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lti_user` (
  `id` bigint(20) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `consumer` bigint(20) NOT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_406o6qhyoo4xcc0ybhrs47xge` (`consumer`),
  KEY `FK_hxb07pfijibeqr1mm8qnnam0i` (`user`),
  CONSTRAINT `FK_406o6qhyoo4xcc0ybhrs47xge` FOREIGN KEY (`consumer`) REFERENCES `lti_consumer` (`id`),
  CONSTRAINT `FK_hxb07pfijibeqr1mm8qnnam0i` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(9000) DEFAULT NULL,
  `created_timestamp` datetime DEFAULT NULL,
  `message_thread` bigint(20) DEFAULT NULL,
  `sender` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_jgrx8hvls0cxtwdet9tu5tl3o` (`message_thread`),
  KEY `FK_a3km2kv42i1xu571ta911f9dc` (`sender`),
  CONSTRAINT `FK_a3km2kv42i1xu571ta911f9dc` FOREIGN KEY (`sender`) REFERENCES `thread_participant` (`id`),
  CONSTRAINT `FK_jgrx8hvls0cxtwdet9tu5tl3o` FOREIGN KEY (`message_thread`) REFERENCES `message_thread` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_participant` (
  `id` bigint(20) NOT NULL,
  `is_read` char(1) DEFAULT 'F',
  `sender` char(1) DEFAULT 'F',
  `participant` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_nt68vh666kew9wsx344d2tej8` (`participant`),
  CONSTRAINT `FK_nt68vh666kew9wsx344d2tej8` FOREIGN KEY (`participant`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_thread`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_thread` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `started` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `creator` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_owblue8e7depfe80u0tut6vnp` (`creator`),
  CONSTRAINT `FK_owblue8e7depfe80u0tut6vnp` FOREIGN KEY (`creator`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `anonymized_actor` bit(1) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `notification_actor_role` varchar(255) NOT NULL,
  `notify_by_email` char(1) DEFAULT 'T',
  `object_id` bigint(20) NOT NULL,
  `object_owner` char(1) DEFAULT 'F',
  `object_type` varchar(255) DEFAULT NULL,
  `is_read` char(1) DEFAULT 'F',
  `section` varchar(255) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `actor` bigint(20) DEFAULT NULL,
  `receiver` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_8ffsbfqrwx0l4fcwtir4eogs8` (`actor`),
  KEY `FK_eu8xhjr832wpkchfxn2a4ne8f` (`receiver`),
  CONSTRAINT `FK_8ffsbfqrwx0l4fcwtir4eogs8` FOREIGN KEY (`actor`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_eu8xhjr832wpkchfxn2a4ne8f` FOREIGN KEY (`receiver`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_settings` (
  `id` bigint(20) NOT NULL,
  `subscribed_email` char(1) DEFAULT 'F',
  `type` varchar(255) NOT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fhuhqotesvv46234bv4r36w19` (`type`,`user`),
  KEY `FK_ajh6l15bdar6g8o7d01b52j3t` (`user`),
  CONSTRAINT `FK_ajh6l15bdar6g8o7d01b52j3t` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_access_token`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_access_token` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `profile_link` varchar(255) DEFAULT NULL,
  `profile_name` varchar(255) DEFAULT NULL,
  `service` varchar(255) NOT NULL,
  `token` varchar(255) DEFAULT NULL,
  `token_secret` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qwnyxe99obn5c7jamrh80tojn` (`user`),
  CONSTRAINT `FK_qwnyxe99obn5c7jamrh80tojn` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `observation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `observation` (
  `id` bigint(20) NOT NULL,
  `creation_date` datetime DEFAULT NULL,
  `edited` bit(1) NOT NULL,
  `message` longtext,
  `note` longtext,
  `created_by` bigint(20) DEFAULT NULL,
  `created_for` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_f238497jjp2hx63imjj1ndnbv` (`created_by`),
  KEY `FK_owstmit5g7fnsggixn5tmfmd7` (`created_for`),
  CONSTRAINT `FK_f238497jjp2hx63imjj1ndnbv` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_owstmit5g7fnsggixn5tmfmd7` FOREIGN KEY (`created_for`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `observation_suggestion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `observation_suggestion` (
  `observation` bigint(20) NOT NULL,
  `suggestions` bigint(20) NOT NULL,
  PRIMARY KEY (`observation`,`suggestions`),
  KEY `FK_rxdpfym9lrnrum8yak52i6ya5` (`suggestions`),
  CONSTRAINT `FK_q5ec7t3ca2ihn36eifg4u7qw4` FOREIGN KEY (`observation`) REFERENCES `observation` (`id`),
  CONSTRAINT `FK_rxdpfym9lrnrum8yak52i6ya5` FOREIGN KEY (`suggestions`) REFERENCES `suggestion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `observation_symptom`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `observation_symptom` (
  `observation` bigint(20) NOT NULL,
  `symptoms` bigint(20) NOT NULL,
  PRIMARY KEY (`observation`,`symptoms`),
  KEY `FK_axke49cj36g768x69cpf9t57l` (`symptoms`),
  CONSTRAINT `FK_2r1fs2ynpa1istpwqlmpc52yl` FOREIGN KEY (`observation`) REFERENCES `observation` (`id`),
  CONSTRAINT `FK_axke49cj36g768x69cpf9t57l` FOREIGN KEY (`symptoms`) REFERENCES `symptom` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `openidaccount`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `openidaccount` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `openidprovider` varchar(255) NOT NULL,
  `validated_id` varchar(255) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mrch25e7jcip15grmcp54039i` (`user`),
  CONSTRAINT `FK_mrch25e7jcip15grmcp54039i` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `learning_in_stages_enabled` char(1) DEFAULT 'F',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_98elant9gwyioac8t0br67o5p` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `outcome`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outcome` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `result` int(11) DEFAULT NULL,
  `activity` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_c6sy38ram71uel2jdeotexor3` (`activity`),
  CONSTRAINT `FK_c6sy38ram71uel2jdeotexor3` FOREIGN KEY (`activity`) REFERENCES `target_activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registration_key`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registration_key` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `registration_type` varchar(255) NOT NULL,
  `uid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ran40qjbh70o6m8q25xu4qxc9` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reset_key`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reset_key` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `invalid` char(1) NOT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4os64y5nsc6cbbd73qj4lujb1` (`uid`),
  KEY `FK_fxv54xs4m5y1tddmi2errcesu` (`user`),
  CONSTRAINT `FK_fxv54xs4m5y1tddmi2errcesu` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_link`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_link` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `id_parameter_name` varchar(255) DEFAULT NULL,
  `link_name` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_settings` (
  `id` bigint(20) NOT NULL,
  `goal_acceptance_dependend_on_competence` char(1) DEFAULT 'F',
  `individual_competences_can_not_be_evaluated` char(1) DEFAULT 'F',
  `selected_users_can_do_evaluation` char(1) DEFAULT 'F',
  `user_can_create_competence` char(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `system` char(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rubric`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rubric` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `ready_to_use` char(1) NOT NULL DEFAULT 'F',
  `rubric_type` varchar(255) NOT NULL,
  `creator` bigint(20) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4fkcsmege20c5mr184i7sw8a8` (`title`,`organization`),
  KEY `FK_6peou3uwnq3v5xoirhfg4buwh` (`creator`),
  KEY `FK_lf8lryt4i4c66o0lot08j3cu7` (`organization`),
  CONSTRAINT `FK_6peou3uwnq3v5xoirhfg4buwh` FOREIGN KEY (`creator`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_lf8lryt4i4c66o0lot08j3cu7` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rubric_unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rubric_unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `rubric` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9ir0vsnl7lrvdnm4nw907xc0h` (`rubric`,`unit`),
  KEY `FK_2rbgvha4m64j067s9p7f52361` (`unit`),
  CONSTRAINT `FK_2rbgvha4m64j067s9p7f52361` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_85vusepoi725ebye515abgc2` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seen_announcement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `seen_announcement` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `announcement` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_63g4k687qg4rdy0tw55bek8ph` (`announcement`),
  KEY `FK_8xib2pv1vo1dkrj7mwkc7stt0` (`user`),
  CONSTRAINT `FK_63g4k687qg4rdy0tw55bek8ph` FOREIGN KEY (`announcement`) REFERENCES `announcement` (`id`),
  CONSTRAINT `FK_8xib2pv1vo1dkrj7mwkc7stt0` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `social_activity1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_activity1` (
  `dtype` varchar(100) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `comments_disabled` char(1) DEFAULT 'F',
  `last_action` datetime DEFAULT NULL,
  `like_count` int(11) NOT NULL,
  `text` longtext,
  `rich_content_content_type` varchar(255) DEFAULT NULL,
  `rich_content_description` varchar(9000) DEFAULT NULL,
  `rich_content_embed_id` varchar(255) DEFAULT NULL,
  `rich_content_image_size` varchar(255) DEFAULT NULL,
  `rich_content_image_url` varchar(255) DEFAULT NULL,
  `rich_content_last_indexing_update` datetime DEFAULT NULL,
  `rich_content_link` varchar(255) DEFAULT NULL,
  `rich_content_title` varchar(255) DEFAULT NULL,
  `twitter_poster_avatar_url` varchar(255) DEFAULT NULL,
  `twitter_comment` varchar(255) DEFAULT NULL,
  `twitter_poster_name` varchar(255) DEFAULT NULL,
  `twitter_poster_nickname` varchar(255) DEFAULT NULL,
  `twitter_post_url` varchar(255) DEFAULT NULL,
  `twitter_poster_profile_url` varchar(255) DEFAULT NULL,
  `retweet` char(1) DEFAULT 'F',
  `twitter_user_type` int(11) DEFAULT NULL,
  `actor` bigint(20) DEFAULT NULL,
  `comment_object` bigint(20) DEFAULT NULL,
  `activity_target` bigint(20) DEFAULT NULL,
  `target_activity_object` bigint(20) DEFAULT NULL,
  `competence_target` bigint(20) DEFAULT NULL,
  `target_competence_object` bigint(20) DEFAULT NULL,
  `credential_object` bigint(20) DEFAULT NULL,
  `post_object` bigint(20) DEFAULT NULL,
  `unit` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_social_activity1_last_action_id` (`last_action`,`id`),
  KEY `index_social_activity1_actor_last_action_id` (`actor`,`last_action`,`id`),
  KEY `FK_n6q7e5h3tjgxn0p2m0wtdf05y` (`comment_object`),
  KEY `FK_fg2k09y8coydd8lt8mxmsyv8d` (`activity_target`),
  KEY `FK_cl6hufyquct3r8e3kj5foof4p` (`target_activity_object`),
  KEY `FK_dvy0dowvaju4eq844k2qbfkm4` (`competence_target`),
  KEY `FK_ic66jfo7hmtbua887739e9hv6` (`target_competence_object`),
  KEY `FK_r6x1eu9v3w7v4yrq67inwon9i` (`credential_object`),
  KEY `FK_3se0xx7kf9aw5vgmraucntlbv` (`post_object`),
  KEY `FK_9aicxka2n6jvmd38yw2rokm0c` (`unit`),
  CONSTRAINT `FK_369a166cd064b6ju3laifxqh6` FOREIGN KEY (`actor`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_3se0xx7kf9aw5vgmraucntlbv` FOREIGN KEY (`post_object`) REFERENCES `social_activity1` (`id`),
  CONSTRAINT `FK_9aicxka2n6jvmd38yw2rokm0c` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_cl6hufyquct3r8e3kj5foof4p` FOREIGN KEY (`target_activity_object`) REFERENCES `target_activity1` (`id`),
  CONSTRAINT `FK_dvy0dowvaju4eq844k2qbfkm4` FOREIGN KEY (`competence_target`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_fg2k09y8coydd8lt8mxmsyv8d` FOREIGN KEY (`activity_target`) REFERENCES `activity1` (`id`),
  CONSTRAINT `FK_ic66jfo7hmtbua887739e9hv6` FOREIGN KEY (`target_competence_object`) REFERENCES `target_competence1` (`id`),
  CONSTRAINT `FK_n6q7e5h3tjgxn0p2m0wtdf05y` FOREIGN KEY (`comment_object`) REFERENCES `comment1` (`id`),
  CONSTRAINT `FK_r6x1eu9v3w7v4yrq67inwon9i` FOREIGN KEY (`credential_object`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `social_activity1_comments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_activity1_comments` (
  `social_activity1` bigint(20) NOT NULL,
  `comments` bigint(20) NOT NULL,
  UNIQUE KEY `UK_5lk34om0v81djsqof46lemooh` (`comments`),
  KEY `FK_icouxfgkx9j575thujmbiifyw` (`social_activity1`),
  CONSTRAINT `FK_5lk34om0v81djsqof46lemooh` FOREIGN KEY (`comments`) REFERENCES `comment1` (`id`),
  CONSTRAINT `FK_icouxfgkx9j575thujmbiifyw` FOREIGN KEY (`social_activity1`) REFERENCES `social_activity1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `social_activity1_hashtags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_activity1_hashtags` (
  `social_activity1` bigint(20) NOT NULL,
  `hashtags` bigint(20) NOT NULL,
  PRIMARY KEY (`social_activity1`,`hashtags`),
  KEY `FK_dmmihaliyqi3d04t7vr50bept` (`hashtags`),
  CONSTRAINT `FK_7puhxui6hgij23pw2s0jpwu8s` FOREIGN KEY (`social_activity1`) REFERENCES `social_activity1` (`id`),
  CONSTRAINT `FK_dmmihaliyqi3d04t7vr50bept` FOREIGN KEY (`hashtags`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `social_activity_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_activity_config` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `hidden` char(1) DEFAULT 'F',
  `social_activity` bigint(20) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_iji9gdpcntyigmeky9rc8w9e2` (`social_activity`),
  KEY `FK_p4ouuukgvnn6j8to9pc5xnlq4` (`user`),
  CONSTRAINT `FK_iji9gdpcntyigmeky9rc8w9e2` FOREIGN KEY (`social_activity`) REFERENCES `social_activity1` (`id`),
  CONSTRAINT `FK_p4ouuukgvnn6j8to9pc5xnlq4` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `social_network_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_network_account` (
  `id` bigint(20) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `social_network` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suggestion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `suggestion` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `symptom`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `symptom` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `target_activity1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_activity1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `added` bit(1) NOT NULL,
  `common_score` int(11) NOT NULL,
  `completed` bit(1) NOT NULL,
  `date_completed` datetime DEFAULT NULL,
  `number_of_attempts` int(11) NOT NULL,
  `act_order` int(11) DEFAULT NULL,
  `result` longtext,
  `result_post_date` datetime DEFAULT NULL,
  `time_spent` bigint(20) NOT NULL,
  `activity` bigint(20) NOT NULL,
  `target_competence` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_5y8xtibcjsl3q1g2pyjp5v478` (`activity`),
  KEY `FK_fn87y61fx7126byyp4gx2i38v` (`target_competence`),
  CONSTRAINT `FK_5y8xtibcjsl3q1g2pyjp5v478` FOREIGN KEY (`activity`) REFERENCES `activity1` (`id`),
  CONSTRAINT `FK_fn87y61fx7126byyp4gx2i38v` FOREIGN KEY (`target_competence`) REFERENCES `target_competence1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `target_competence1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_competence1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `date_completed` datetime DEFAULT NULL,
  `evidence_summary` varchar(9000) DEFAULT NULL,
  `hidden_from_profile` bit(1) NOT NULL,
  `next_activity_to_learn_id` bigint(20) NOT NULL,
  `progress` int(11) NOT NULL,
  `competence` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_hyygpgbqj161vj4fkdgxupoog` (`competence`,`user`),
  KEY `FK_62p0o7vw29vwnqc58g1hu2vai` (`user`),
  CONSTRAINT `FK_2jik1pn632ups2li1pmvuahdd` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  CONSTRAINT `FK_62p0o7vw29vwnqc58g1hu2vai` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `target_credential1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_credential1` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `assigned_to_instructor` bit(1) NOT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `cluster_name` varchar(255) DEFAULT NULL,
  `competence_assessments_displayed` bit(1) DEFAULT b'1',
  `credential_assessments_displayed` bit(1) DEFAULT b'1',
  `date_finished` datetime DEFAULT NULL,
  `date_started` datetime DEFAULT NULL,
  `evidence_displayed` bit(1) DEFAULT b'1',
  `final_review` varchar(255) DEFAULT NULL,
  `hidden_from_profile` bit(1) NOT NULL,
  `last_action` datetime DEFAULT NULL,
  `next_competence_to_learn_id` bigint(20) NOT NULL,
  `progress` int(11) NOT NULL,
  `credential` bigint(20) NOT NULL,
  `instructor` bigint(20) DEFAULT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_hxf01pvgri60h660un28e1w2q` (`credential`,`user`),
  KEY `FK_7usnugjbauedaxofmlt5mlhau` (`instructor`),
  KEY `FK_76pg8xnfhsda0bvvq7wvwl5xp` (`user`),
  CONSTRAINT `FK_76pg8xnfhsda0bvvq7wvwl5xp` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_7usnugjbauedaxofmlt5mlhau` FOREIGN KEY (`instructor`) REFERENCES `credential_instructor` (`id`),
  CONSTRAINT `FK_s3fmqrd1ct10kj04au40cuqhr` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `terms_of_use`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `terms_of_use` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `accepted` char(1) NOT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `thread_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thread_participant` (
  `id` bigint(20) NOT NULL,
  `archived` char(1) DEFAULT 'F',
  `deleted` char(1) DEFAULT 'F',
  `is_read` char(1) DEFAULT 'F',
  `show_messages_from` datetime DEFAULT NULL,
  `last_read_message` bigint(20) DEFAULT NULL,
  `message_thread` bigint(20) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_2o8pq3r6bxabp5b73ok8qgrcx` (`last_read_message`),
  KEY `FK_ilywn1b6wdhl4ymswbb75ayal` (`message_thread`),
  KEY `FK_ks01qd3o7158kirgyfpkw6cbb` (`user`),
  CONSTRAINT `FK_2o8pq3r6bxabp5b73ok8qgrcx` FOREIGN KEY (`last_read_message`) REFERENCES `message` (`id`),
  CONSTRAINT `FK_ilywn1b6wdhl4ymswbb75ayal` FOREIGN KEY (`message_thread`) REFERENCES `message_thread` (`id`),
  CONSTRAINT `FK_ks01qd3o7158kirgyfpkw6cbb` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `welcome_message` text,
  `organization` bigint(20) NOT NULL,
  `parent_unit` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_hotnqwr5osir4ryygavjto7ac` (`title`,`organization`),
  KEY `FK_oxsulo29jty26qck7xmhhf9f1` (`organization`),
  KEY `FK_8e9s0ln9wmq4ydgbnhhyfjgs5` (`parent_unit`),
  CONSTRAINT `FK_8e9s0ln9wmq4ydgbnhhyfjgs5` FOREIGN KEY (`parent_unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_oxsulo29jty26qck7xmhhf9f1` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unit_role_membership`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit_role_membership` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `role` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cvpfera4fi0vhmc4jd8c6a1j8` (`user`,`unit`,`role`),
  KEY `FK_d5bj6kukr793taljwratj3l05` (`role`),
  KEY `FK_gn7knglhp5coc24iqd4yeevb0` (`unit`),
  CONSTRAINT `FK_d5bj6kukr793taljwratj3l05` FOREIGN KEY (`role`) REFERENCES `role` (`id`),
  CONSTRAINT `FK_gn7knglhp5coc24iqd4yeevb0` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`),
  CONSTRAINT `FK_oiefemc5sijcc7vop0b97lpt6` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `location_name` varchar(255) DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `password_length` int(11) DEFAULT NULL,
  `position` varchar(255) DEFAULT NULL,
  `profile_url` varchar(255) DEFAULT NULL,
  `system` char(1) DEFAULT 'F',
  `user_type` varchar(255) NOT NULL,
  `verification_key` varchar(255) DEFAULT NULL,
  `verified` char(1) NOT NULL,
  `organization` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_a7krvkolmrchxwj22txuhlhj` (`organization`),
  CONSTRAINT `FK_a7krvkolmrchxwj22txuhlhj` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `default_group` char(1) DEFAULT 'F',
  `join_url_active` char(1) DEFAULT 'F',
  `join_url_password` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `unit` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ddfpocbhgcpv8uhvcuf52dx5a` (`unit`),
  CONSTRAINT `FK_ddfpocbhgcpv8uhvcuf52dx5a` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_group_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_user` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `user_group` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_c63cjs9t3jl6nrf3asi7iej52` (`user`,`user_group`),
  KEY `FK_iifontadv5293tvuvf7r9e9u8` (`user_group`),
  CONSTRAINT `FK_iifontadv5293tvuvf7r9e9u8` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`),
  CONSTRAINT `FK_kqb30gm40pn64yo6sgh0jgpcf` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_preference`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_preference` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `update_period` varchar(255) NOT NULL,
  `user` bigint(20) DEFAULT NULL,
  `personal_blog_source` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lubkkfg4oouxyab4bblgr0j2e` (`user`),
  KEY `FK_oskk4hx06767511e5ar5wpdo1` (`personal_blog_source`),
  CONSTRAINT `FK_lubkkfg4oouxyab4bblgr0j2e` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_oskk4hx06767511e5ar5wpdo1` FOREIGN KEY (`personal_blog_source`) REFERENCES `feed_source` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_preference_subscribed_rss_sources`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_preference_subscribed_rss_sources` (
  `user_preference` bigint(20) NOT NULL,
  `subscribed_rss_sources` bigint(20) NOT NULL,
  UNIQUE KEY `UK_k33l3f2g6mo8kpnc0laxm8dxf` (`subscribed_rss_sources`),
  KEY `FK_k1w7aarxe3y1g47f1k9qjh5qb` (`user_preference`),
  CONSTRAINT `FK_k1w7aarxe3y1g47f1k9qjh5qb` FOREIGN KEY (`user_preference`) REFERENCES `user_preference` (`id`),
  CONSTRAINT `FK_k33l3f2g6mo8kpnc0laxm8dxf` FOREIGN KEY (`subscribed_rss_sources`) REFERENCES `feed_source` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_settings` (
  `id` bigint(20) NOT NULL,
  `activity_wall_settings` bigint(20) DEFAULT NULL,
  `locale_settings` bigint(20) DEFAULT NULL,
  `terms_of_use` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_phjwu7o2219iuuh2v53ombw6w` (`activity_wall_settings`),
  KEY `FK_o3slgonymb8ch3md0m5anhlb8` (`locale_settings`),
  KEY `FK_fdwr1xr3v58sye7ukapj4k3pa` (`terms_of_use`),
  CONSTRAINT `FK_e0ji1xk8xkrg1ee92jyuhl5s4` FOREIGN KEY (`id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_fdwr1xr3v58sye7ukapj4k3pa` FOREIGN KEY (`terms_of_use`) REFERENCES `terms_of_use` (`id`),
  CONSTRAINT `FK_o3slgonymb8ch3md0m5anhlb8` FOREIGN KEY (`locale_settings`) REFERENCES `locale_settings` (`id`),
  CONSTRAINT `FK_phjwu7o2219iuuh2v53ombw6w` FOREIGN KEY (`activity_wall_settings`) REFERENCES `activity_wall_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_settings_pages_tutorial_played`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_settings_pages_tutorial_played` (
  `user_settings` bigint(20) NOT NULL,
  `pages_tutorial_played` varchar(255) DEFAULT NULL,
  KEY `FK_rdbdifanhafj621wt7cd8iurc` (`user_settings`),
  CONSTRAINT `FK_rdbdifanhafj621wt7cd8iurc` FOREIGN KEY (`user_settings`) REFERENCES `user_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_social_networks`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_social_networks` (
  `id` bigint(20) NOT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_eriyguekd0ayq7n6njjy86qp` (`user`),
  CONSTRAINT `FK_eriyguekd0ayq7n6njjy86qp` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_social_networks_social_network_accounts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_social_networks_social_network_accounts` (
  `user_social_networks` bigint(20) NOT NULL,
  `social_network_accounts` bigint(20) NOT NULL,
  PRIMARY KEY (`user_social_networks`,`social_network_accounts`),
  UNIQUE KEY `UK_cm6y96m10clu35b3ow94gi5ln` (`social_network_accounts`),
  CONSTRAINT `FK_8qwchc8e5iamlaaysbpg3p7su` FOREIGN KEY (`user_social_networks`) REFERENCES `user_social_networks` (`id`),
  CONSTRAINT `FK_cm6y96m10clu35b3ow94gi5ln` FOREIGN KEY (`social_network_accounts`) REFERENCES `social_network_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_topic_preference_preferred_hashtags_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_topic_preference_preferred_hashtags_tag` (
  `user_preference` bigint(20) NOT NULL,
  `preferred_hashtags` bigint(20) NOT NULL,
  PRIMARY KEY (`user_preference`,`preferred_hashtags`),
  KEY `FK_c2yhsp4xled49t7brdhsapk24` (`preferred_hashtags`),
  CONSTRAINT `FK_c2yhsp4xled49t7brdhsapk24` FOREIGN KEY (`preferred_hashtags`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_ftoj3377h5tkd5url4ajuqdkt` FOREIGN KEY (`user_preference`) REFERENCES `user_preference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_topic_preference_preferred_keywords_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_topic_preference_preferred_keywords_tag` (
  `user_preference` bigint(20) NOT NULL,
  `preferred_keywords` bigint(20) NOT NULL,
  PRIMARY KEY (`user_preference`,`preferred_keywords`),
  KEY `FK_486ac11t2tsvkm3cj1qjn61pu` (`preferred_keywords`),
  CONSTRAINT `FK_486ac11t2tsvkm3cj1qjn61pu` FOREIGN KEY (`preferred_keywords`) REFERENCES `tag` (`id`),
  CONSTRAINT `FK_deretkpd06iv6p27971yv48vt` FOREIGN KEY (`user_preference`) REFERENCES `user_preference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_user_role` (
  `user` bigint(20) NOT NULL,
  `roles` bigint(20) NOT NULL,
  PRIMARY KEY (`user`,`roles`),
  KEY `FK_9uoa85no4a82ukddaycpt17f` (`roles`),
  CONSTRAINT `FK_1e97vv9xu9fx2kaeivgbh1jdx` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_9uoa85no4a82ukddaycpt17f` FOREIGN KEY (`roles`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-19 11:26:25
