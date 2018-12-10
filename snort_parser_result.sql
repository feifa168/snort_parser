/*
SQLyog 企业版 - MySQL GUI v8.14 
MySQL - 5.1.61-community-log : Database - ids
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`ids` /*!40100 DEFAULT CHARACTER SET gb2312 */;

USE `ids`;

/*Table structure for table `alert` */

DROP TABLE IF EXISTS `alert`;

CREATE TABLE `alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增变量',
  `time` datetime DEFAULT NULL COMMENT '告警时间',
  `pri` int(11) DEFAULT NULL COMMENT 'syslog的pri，后3比特是告警级别severity,其余高bit为告警模块facility',
  `host` varchar(128) DEFAULT NULL COMMENT '日志发送端主机',
  `tag` varchar(128) DEFAULT NULL COMMENT '日志标签，比如snort',
  `gid` int(11) DEFAULT NULL,
  `sid` int(11) DEFAULT NULL,
  `rid` int(11) DEFAULT NULL,
  `msg` varchar(512) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `proto` varchar(32) DEFAULT NULL COMMENT '协议类型',
  `sip` varchar(128) DEFAULT NULL,
  `sport` int(11) DEFAULT NULL COMMENT '端口号',
  `isleft2right` tinyint(1) DEFAULT NULL COMMENT 'true为sip->dip,false为sip<-dip',
  `dip` varbinary(128) DEFAULT NULL,
  `dport` int(11) DEFAULT NULL COMMENT '端口号',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=467522 DEFAULT CHARSET=gb2312;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
