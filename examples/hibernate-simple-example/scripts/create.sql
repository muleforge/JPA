drop database testdb1;
drop database testdb2;


create database testdb1 CHARACTER SET utf8;
create database testdb2 CHARACTER SET utf8;

 grant all on testdb1.* to 'jpa'@'localhost'identified by 'jpa' ;
 grant all on testdb2.* to 'jpa'@'localhost'identified by 'jpa' ;

use testdb1;

create table ITEMTABLE (
  id INTEGER not NULL,
  name VARCHAR(50) not NULL,
  price INTEGER not NULL);

use testdb2;
create table ITEMTABLE (
  id INTEGER not NULL,
  name VARCHAR(50) not NULL,
  price INTEGER not NULL);



