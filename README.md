# jaregu-queries
2
Java based SQL templating project. Store your queries in *.sql files and build queries for executing. Supports simple expressions and conditional clauses.
3

# Quick-start

Create some sql file aaa/bbb/dummy.sql:
``` sql
-- create-dummy
create table dummy (
id int, 
foo int, 
bar varchar(100),
PRIMARY KEY (id));

-- insert-dummy
insert into dummy (id, foo, bar) values (:id, :foo, :bar);

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

```
Create Queries instance
```java
Queries queries = Queries.builder().sourceOfResource("aaa/bbb/dummy.sql").build();
```

And build some queries:
```java
Query query = queries.get(QueryId.of("aaa.bbb.dummy.create-dummy")).build();
execute(query); // execute method is sql executing layer not showed here
```

Create some POJO
```java
public class Dummy {
  public int id;
  public Integer foo;
  public String bar;
}
```

Use POJO for inserts statement
```java
PreparedQuery query = queries.get(QueryId.of("aaa.bbb.dummy.create-dummy"));
Dummy dummy = new Dummy();
dummy.id = 1;
//set some other fields
execute(query.build(dummy)); // execute method is sql executing layer
dummy.id = 2;
execute(query.build(dummy));
```
See example for more in depth features: [sample-queries.sql](https://github.com/jaregu/queries/blob/master/src/example/resources/com/jaregu/queries/example/sample-queries.sql) [SampleQueries.java](https://github.com/jaregu/queries/blob/master/src/example/java/com/jaregu/queries/example/SampleQueries.java)
