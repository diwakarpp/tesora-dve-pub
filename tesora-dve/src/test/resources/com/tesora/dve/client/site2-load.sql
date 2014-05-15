drop database if exists site2_d7test;
drop database if exists site2_TestDB;
drop database if exists site3_d7test;
drop database if exists site3_TestDB;
drop database if exists site4_d7test;
drop database if exists site4_TestDB;
drop database if exists site5_d7test;
drop database if exists site5_TestDB;
drop database if exists site6_d7test;
drop database if exists site6_TestDB;
create database site2_TestDB;
use site2_TestDB;

create table alltypes ( bit_column bit,
						tinyint_column tinyint,
						bigint_column bigint,
						longvarb_column long varbinary,
						varb_column varbinary(200),
						binary_column binary(10),
						text_column text,
						char_column char(10),
						num_column numeric(10,2),
						dec_column decimal(5,4),
						int_column integer,
						smallint_column smallint,
						float_column float,
						double_column double,
						varchar_column varchar(10),
						date_column date,
						time_column time,
						datetime_column datetime,
						intu_column integer unsigned,
						bigintu_column bigint unsigned );


insert into alltypes values (0,2,22222222222222,"bin2","bin2","bin2","text2","char2",2.23,2.2345,2,2,2.222222,2.222222,"varchar2","2022-02-22","22:22:22","2022-02-22 22:22:22",22222,2222222222);

