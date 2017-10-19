-- create-dummy
-- first comment in query is query identification and it is mandatory
-- each query has uniqe ID (Query ID) which consist of source id and query first comment content
create table dummy (
id int, 
foo int, 
bar varchar(100),
PRIMARY KEY (id));

-- create-dummy-oracle
-- first comment in query is query identification and it is mandatory
-- each query has uniqe ID (Query ID) which consist of source id and query first comment content
CREATE TABLE dummy
	(id INT NOT NULL, foo INT, bar VARCHAR(100));

-- all-dummies
/* simplest select query there is, no parameters, just select */
select * from dummy;

-- insert-dummy
-- query expects mandatory parameters which are passed as bean or map
-- named mandatory parameters are good for insert, update and complicated select stamements
-- if :foo value is null, it is ok, but there must be a property(field or getter method) or map key associated with that name 
insert into dummy (id, foo, bar) values (:id, :foo, :bar);

-- count-dummies
-- query expects one no-name mandatory parameter
-- it is easier for simple queries to pass no-name parameters
select count(1) from dummy where id > ?;

-- multiple-iterable-params
-- query expects multiple no name (iterable source) mandatory parameters
select * from dummy where foo = ? or bar = ?;

-- named-list-parameters
/* query expects mandatory parameters which are passed as list or array */
select * from dummy where foo = :0 or bar = :1;

-- error-variable-resolvers-mixed
-- this will generate error, mixed iterable and named style variables
-- choose one style for each select
select * from dummy where foo = ? and bar = :name;

-- named-optional-criterion-parameters
-- query expects optional parameters which are passed as bean or map or list if used with list index names
select * from dummy where 1 = 1
-- optional criterion line will be added if parameter is not null (default behavior)
-- below question mark is not considered as anonymous parameter, but only as placeholder for passed named 'foo' parameter
and foo = ? -- :foo
-- use ? or constant - string, number, NULL, or some expression inside brackets '([some sql])' as placeholder 
-- for real binding place, using constant allows to execute query without changes to test its syntax or execute plan
and (bar = 'whatever' /* :bar */);

-- in-clause-support-anonymous
/* By default there is no IN clause support for some collection parameters, but it is easy to add some */
select * from dummy
where id IN (?);

-- in-clause-support-named
/* By default there is no IN clause support for some collection parameters, but it is easy to add some */
select * from dummy
where id IN (:collectionOfIds);

-- in-clause-support-optional-named
/* By default there is no IN clause support for some collection parameters, but it is easy to add some */
select * from dummy
where 1 = 1
and id IN (? /* :collectionOfIds */);


-- named-conditional-criterion-parameters
-- query expects optional parameters which are passed as bean or map
select id from dummy where 1 = 1
/* optional criterion line will be added if condition block is true,
extended comment syntax is like: value_expression;condition_expression */
-- if foo property is not null and is greater than 4, criterion line will be added,
-- and value for binded parameter will be foo + 1
and foo > ? -- :foo + 1; :foo != null && :foo > 3
;

-- search-example
select *
from dummy
where 1 = 1
-- all criterions will be added if value will be non empty string
and (LOWER(bar) = LOWER('THIS will be replaced' /* :barEq ; :barEq != null && :barEq != '' */) 
and (bar like null /* :barStarts + '%'; :barStarts != null && :barStarts != '' */)
and (bar like '%foo%' /* '%' + :barContains + '%'; :barContains != null && :barContains != '' */)
order by id desc
limit :offset, :limit -- both offset and limit parameters are mandatory, so they have to be supplied
;

-- optional-blocks
-- select can contain multiple conditional parts, which will be added if condition is true
-- conditional part consists of two comments, first one is like: condition_expression {
-- second one contains only closing bracket }
select d.*
/* :addBlock != null { */, dd.* /* } */ 
from dummy d
/* (:addBlock != null) { */ join dummy dd on d.id = dd.id -- }
where 1 = 1
-- (:addBlock != null) {
	/* inside optional block */
	and dd.bar = 'X' /* :bar */
	-- and there can be blocks inside block
	-- :addNested != null && :addNested {
		-- there can be mandatory parameters whithin block, thay will be required if block is enabled	
		and dd.foo = :foo /* inside nested block*/
	-- }
-- }
;

-- select-attrs
/* maybe there is need for some additional parameters for query, like information for query executing layer
 for example how long can we cache result of this query, for parameter(s) use syntax 
 like: param_1 = value_expression_1 [; param_2 = value_expression_2 ]...[;param_n = value_expression_n] 
 it is possible to even use passed in parameters, example below */
-- caching = true; removeCachedAfterMin = (12 + 3) * 2; passThrough = :passThrough
select *
from dummy
where id = :id

