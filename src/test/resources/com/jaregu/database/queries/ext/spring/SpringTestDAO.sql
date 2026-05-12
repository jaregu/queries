-- create_table
create table spring_test_item (
    id int not null primary key,
    name varchar(100) not null,
    description varchar(500)
);

-- drop_table
drop table if exists spring_test_item;

-- insert
insert into spring_test_item (id, name, description)
values (:id, :name, :description);

-- update_name
update spring_test_item set name = :name where id = :id;

-- delete
delete from spring_test_item where id = :id;

-- find_all
select id, name, description from spring_test_item order by id;

-- find_by_id
select id, name, description from spring_test_item where id = :id;

-- count
select count(*) from spring_test_item;
