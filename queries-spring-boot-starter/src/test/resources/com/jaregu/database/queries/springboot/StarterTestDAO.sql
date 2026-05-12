-- create_table
create table starter_item (
    id int not null primary key,
    label varchar(100) not null
);

-- insert
insert into starter_item (id, label) values (:id, :label);

-- find_all
select id, label from starter_item order by id;

-- count
select count(*) from starter_item;

-- search_paged
select id, label from starter_item;
