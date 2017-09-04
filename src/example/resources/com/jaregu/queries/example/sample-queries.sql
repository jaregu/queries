-- create-dummy
-- first comment in query is query identification and it is mandatory
-- each query has uniqe ID (Query ID) which consist of source id and query first comment content
create table dummy (
id int, 
foo int, 
bar varchar(100),
PRIMARY KEY (id));

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
-- for real binding place
and (bar = 'whatever' /* :bar */);

-- in-clause-support
/* in clause support in optional named parameters, it is possible to pass collection which will be
 * translated to multiple parameters */
select * from dummy where id IN (1 /* :collectionOfIds */);

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
-- criterion will be added if barEq property/key is non null non empty string value
select * from dummy where 1 = 1
and (LOWER(bar) = LOWER('THIS will be replaced' /* :barEq ; :barEq != null && :barEq != '' */) 
and (bar like null /* :barStarts + '%'; :barStarts != null && :barStarts != '' */)
and (bar like '%foo%' /* '%' + :barLike + '%'; :barLike != null && :barLike != '' */)
order by id desc
limit :offset, :pageSize -- both offset and pageSize parameters are mandatory, so they have to be non null
;

-- optional-block
-- select can contain multiple conditional parts, which will be added if condition is true
-- conditional part consists of two comments, first one is like: condition_expression {
-- second one contains only closing bracket }, it is possible to use square brackets too [ ] 
select d.*
/* if :dd != null { */, dd.* /* } */ 
from dummy d
/* if (:dd != null) { */ join dummy dd on d.id = dd.id -- }
where 1 = 1
-- if (:dd != null) {
	and dd.foo = 1 -- :foo
	and dd.bar = 'X' /* :bar */
	-- there can be mandatory parameters whithin block, thay will be required if block is enabled
	and dd.id = :id
	-- and there can be blocks inside block
	-- if (ddd != null) {
		and dd.id = :id
	-- }
-- }
;

-- select-attrs
/* maybe there is need for some additional parameters for query, like information for some query executing layer
 for example how long can we cache result of this query, for parameter(s) use syntax like: param_1 = value_expression_1 [; param_2 = value_expression_2 ]...[;param_n = value_expression_n] 
 it is possible to even use passed parameters, example below */

-- caching = true; caching_removeAfterMin = 12; internalId = :internalId
select *
from dummy
where id = :id;


-- count-query
-- referencing to some other query and using parameter with include? or
-- additionakl block for each select or
-- for this purpose use outside helper
SELECT count(1) FROM ( select * from dummy /* some other query include syntax? */ ) x;

-- count-query-with-limit
-- every comment which looks like expression is considered as optional named variable clause
-- if there is need to disable some working expression, it is possible to start comment
--- with three hyphens signs, like this comment 
/** or two asterisks after slash will switch off any parsing for comment */ 

SELECT count(1) FROM ( SELECT 1 FROM dummy LIMIT 1001 ) a;

-- sort-order
-- sort orders repeat and in list clause
SELECT * FROM dummy
WHERE 1 = 1
/* if (:orderBy.empty == false) {*/ -- replaceable must be replaced with value not with binding
ORDER BY 1 /* for (prop = :orderBy ) {*/ , 'replaceable' /* print :prop.name */ /* } */ 
/* } */
;


