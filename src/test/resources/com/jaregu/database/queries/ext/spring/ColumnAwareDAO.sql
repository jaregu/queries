-- create_table
create table app_user (
    usr_id int not null primary key,
    usr_first_nm varchar(100) not null,
    usr_last_nm varchar(100),
    usr_age int
);

-- drop_table
drop table if exists app_user;

-- insert
insert into app_user (usr_id, usr_first_nm, usr_last_nm, usr_age)
values (:id, :firstName, :lastName, :age);

-- find_all
select usr_id, usr_first_nm, usr_last_nm, usr_age
from app_user
order by usr_id;

-- find_by_id
select usr_id, usr_first_nm, usr_last_nm, usr_age
from app_user
where usr_id = :id;
