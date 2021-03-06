---
-- #%L
-- Tesora Inc.
-- Database Virtualization Engine
-- %%
-- Copyright (C) 2011 - 2014 Tesora Inc.
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License, version 3,
-- as published by the Free Software Foundation.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Affero General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with this program. If not, see <http://www.gnu.org/licenses/>.
-- #L%
---
CREATE PERSISTENT SITE `dtsite0` url= 'jdbc:mysql://localhost:3307'; 
CREATE PERSISTENT GROUP `dtgroup` ADD dtsite0; 
CREATE DATABASE `dtdb` DEFAULT COLLATE = latin1_swedish_ci DEFAULT PERSISTENT GROUP dtgroup; 
USE `dtdb`; 
CREATE RANGE `r_vbi` ( varchar(17), binary(48), int ) PERSISTENT GROUP dtgroup; 
CREATE TABLE `t0` ( dvix int, rix int, c0 smallint , c1 datetime , c2 int , c3 tinyint , c4 int , c5 int , c6 binary (48) , c7 int , c8 varchar (17) collate latin1_swedish_ci , c9 char (24) collate latin1_swedish_ci ) RANGE DISTRIBUTE ON ( c8, c6, c2 ) USING `r_vbi`; 
CREATE PERSISTENT SITE `dtsite1` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite1; 
CREATE PERSISTENT SITE `dtsite2` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite2; 
CREATE PERSISTENT SITE `dtsite3` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite3; 
CREATE PERSISTENT SITE `dtsite4` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite4; 

DROP DATABASE `dtdb`; 
DROP RANGE `r_vbi`; 
DROP PERSISTENT GROUP `dtgroup`; 
DROP PERSISTENT SITE `dtsite4`; 
DROP PERSISTENT SITE `dtsite3`; 
DROP PERSISTENT SITE `dtsite2`; 
DROP PERSISTENT SITE `dtsite1`; 
DROP PERSISTENT SITE `dtsite0`; 

CREATE PERSISTENT SITE `dtsite5` url= 'jdbc:mysql://localhost:3307'; 
CREATE PERSISTENT GROUP `dtgroup` ADD dtsite5; 
CREATE DATABASE `dtdb` DEFAULT COLLATE = latin1_swedish_ci DEFAULT PERSISTENT GROUP dtgroup; 
USE `dtdb`; 
CREATE RANGE `r_ccviv` ( char(24), char(24), varchar(29), int, varchar(15) ) PERSISTENT GROUP dtgroup;
CREATE TABLE `t0` ( dvix int, rix int, c0 int , c1 char (24) collate latin1_swedish_ci , c2 varchar (29) collate latin1_swedish_ci , c3 integer , c4 smallint , c5 datetime , c6 int , c7 smallint , c8 binary (44) , c9 varchar (15) collate latin1_swedish_ci , c10 binary (28) , c11 tinyint , c12 datetime , c13 char (23) collate latin1_swedish_ci, c14 char (24) collate latin1_swedish_ci , c15 tinyint ) RANGE DISTRIBUTE ON ( c1, c14, c2, c6, c9 ) USING `r_ccviv`; 
CREATE PERSISTENT SITE `dtsite6` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite6; 
CREATE PERSISTENT SITE `dtsite7` url= 'jdbc:mysql://localhost:3307'; 
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite7; 

DROP TABLE `t0`;
DROP RANGE `r_ccviv`;
DROP DATABASE `dtdb`;
DROP PERSISTENT GROUP `dtgroup`;
DROP PERSISTENT SITE `dtsite5`; 
DROP PERSISTENT SITE `dtsite6`; 
DROP PERSISTENT SITE `dtsite7`; 
