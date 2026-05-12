-- create_table
create table starter_entity (
    id int not null primary key,
    name varchar(100) not null
);

-- drop_table
drop table if exists starter_entity;

-- insert
insert into starter_entity (/* entityFieldGenerator(template = 'column' entityClass = 'StarterEntity') */)
values (/* entityFieldGenerator(template = 'value' entityClass = 'StarterEntity') */);

-- update_by_id
update starter_entity set
    -- entityFieldGenerator(template = 'columnAndValue' entityClass = 'StarterEntity' excludeColumns = 'id')
where id = :id;

-- find_all
select -- entityFieldGenerator(template = 'column' entityClass = 'StarterEntity' alias = 'e')
from starter_entity e
order by e.id;
