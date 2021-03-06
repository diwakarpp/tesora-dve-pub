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
CREATE PERSISTENT SITE `dtsite1` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite2` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite3` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT GROUP `dtgroup` ADD dtsite0, dtsite1, dtsite2, dtsite3;
CREATE DATABASE `dtdb` DEFAULT COLLATE = latin1_swedish_ci DEFAULT PERSISTENT GROUP dtgroup;
USE `dtdb`;
CREATE RANGE `r_nnn` ( integer, integer, integer ) PERSISTENT GROUP dtgroup;
CREATE TABLE `t0` ( dvix int, rix int, c0 integer , c1 integer , c2 varchar (22) collate latin1_swedish_ci , c3 smallint , c4 char (19) collate latin1_swedish_ci , c5 char (10) collate latin1_swedish_ci , c6 datetime , c7 datetime , c8 integer ) RANGE DISTRIBUTE ON ( c0, c8, c1 ) USING `r_nnn`;
CREATE TABLE `t1` ( dvix int, rix int, c0 binary (5) , c1 smallint , c2 binary (49) , c3 tinyint , c4 integer , c5 tinyint , c6 int , c7 datetime , c8 tinyint , c9 char (9) collate latin1_swedish_ci , c10 integer , c11 integer , c12 datetime , c13 binary (26) , c14 binary (29) , c15 int , c16 smallint , c17 smallint ) RANGE DISTRIBUTE ON ( c4, c11, c10 ) USING `r_nnn`;
CREATE PERSISTENT SITE `dtsite4` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite5` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite6` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite7` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite4, dtsite5, dtsite6, dtsite7;
CREATE PERSISTENT SITE `dtsite8` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite9` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite8, dtsite9;
CREATE PERSISTENT SITE `dtsite10` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite11` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite10, dtsite11;
CREATE PERSISTENT SITE `dtsite12` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite12;
DROP DATABASE `dtdb`;
DROP RANGE `r_nnn`;
DROP PERSISTENT GROUP `dtgroup`;
DROP PERSISTENT SITE `dtsite12`;
DROP PERSISTENT SITE `dtsite11`;
DROP PERSISTENT SITE `dtsite10`;
DROP PERSISTENT SITE `dtsite9`;
DROP PERSISTENT SITE `dtsite8`;
DROP PERSISTENT SITE `dtsite7`;
DROP PERSISTENT SITE `dtsite6`;
DROP PERSISTENT SITE `dtsite5`;
DROP PERSISTENT SITE `dtsite4`;
DROP PERSISTENT SITE `dtsite3`;
DROP PERSISTENT SITE `dtsite2`;
DROP PERSISTENT SITE `dtsite1`;
DROP PERSISTENT SITE `dtsite0`;
CREATE PERSISTENT SITE `dtsite0` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite1` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite2` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite3` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT GROUP `dtgroup` ADD dtsite0, dtsite1, dtsite2, dtsite3;
CREATE DATABASE `dtdb` DEFAULT COLLATE = latin1_swedish_ci DEFAULT PERSISTENT GROUP dtgroup;
USE `dtdb`;
CREATE RANGE `r_nnn` ( integer, integer, integer ) PERSISTENT GROUP dtgroup;
CREATE TABLE `t0` ( dvix int, rix int, c0 integer , c1 integer , c2 varchar (22) collate latin1_swedish_ci , c3 smallint , c4 char (19) collate latin1_swedish_ci , c5 char (10) collate latin1_swedish_ci , c6 datetime , c7 datetime , c8 integer ) RANGE DISTRIBUTE ON ( c0, c8, c1 ) USING `r_nnn`;
CREATE TABLE `t1` ( dvix int, rix int, c0 binary (5) , c1 smallint , c2 binary (49) , c3 tinyint , c4 integer , c5 tinyint , c6 int , c7 datetime , c8 tinyint , c9 char (9) collate latin1_swedish_ci , c10 integer , c11 integer , c12 datetime , c13 binary (26) , c14 binary (29) , c15 int , c16 smallint , c17 smallint ) RANGE DISTRIBUTE ON ( c4, c11, c10 ) USING `r_nnn`;
CREATE PERSISTENT SITE `dtsite4` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite5` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite6` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite7` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite4, dtsite5, dtsite6, dtsite7;
CREATE PERSISTENT SITE `dtsite8` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite9` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite8, dtsite9;
CREATE PERSISTENT SITE `dtsite10` url= 'jdbc:mysql://localhost:3307';
CREATE PERSISTENT SITE `dtsite11` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite10, dtsite11;
CREATE PERSISTENT SITE `dtsite12` url= 'jdbc:mysql://localhost:3307';
ALTER PERSISTENT GROUP `dtgroup` ADD GENERATION dtsite12;
DROP DATABASE `dtdb`;
DROP RANGE `r_nnn`;
DROP PERSISTENT GROUP `dtgroup`;
DROP PERSISTENT SITE `dtsite12`;
DROP PERSISTENT SITE `dtsite11`;
DROP PERSISTENT SITE `dtsite10`;
DROP PERSISTENT SITE `dtsite9`;
DROP PERSISTENT SITE `dtsite8`;
DROP PERSISTENT SITE `dtsite7`;
DROP PERSISTENT SITE `dtsite6`;
DROP PERSISTENT SITE `dtsite5`;
DROP PERSISTENT SITE `dtsite4`;
DROP PERSISTENT SITE `dtsite3`;
DROP PERSISTENT SITE `dtsite2`;
DROP PERSISTENT SITE `dtsite1`;
DROP PERSISTENT SITE `dtsite0`;
