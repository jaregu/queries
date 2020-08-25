# Jaregu-queries

[![Build Status](https://travis-ci.org/jaregu/queries.svg?branch=master)](https://travis-ci.org/jaregu/queries)

Java based SQL templating project. Store your queries in sql files and build queries for executing.
Main features:
- SQL templating with conditional blocks
- Built-in expression language for simple parameter conditions and tuning (parameter + '%')
- Mandatory and optional parameter support
- Anonymous, constant or named parameter naming support (use field = ? or field = 'AAA' or field = :aaa syntax)
- Optional SQL IN clause support
- SQL Dialects for built-in SQL query conversion to COUNT, ORDER BY and LIMIT queries
- SQL Dialects for overridable query source SQL files (aaa/bbb.sql and aaa/bbb.mariadb.sql for same query source)
- Proxiable interfaces support (use interface with annotations to create bridge between sql file and java code, unleash easy binding with DI)
- DI supported (Optional [Guice](https://github.com/google/guice) support included)
- Example executing layer included (Optional [Dalesbred](https://dalesbred.org/) executing layer + [HikariCP](https://github.com/brettwooldridge/HikariCP) sql datasource wrapper included)
- Conversion support for queries (Proxied interface can return data from database using some defined coversion)
- Optional queries caching possibility (Optional [Caffeine](https://github.com/ben-manes/caffeine) cache wrapper included)
- Query attributes support for executing, caching or some other layer (additional info about query)
- Only one required dependence [slf4j](http://www.slf4j.org/)

# Quick-start (With DI - Guice)

Add project dependencies (in build.gradle):
``` groovy
  dependencies {
    
    // Jaregu Queries
    implementation 'com.jaregu:queries:1.+'
    // DI Guice
    implementation 'com.google.inject:guice:4.+'
    //  HSQLDB in memory database for testing
    implementation 'org.hsqldb:hsqldb:2.+'
    // SQL datasource pool
    implementation 'com.zaxxer:HikariCP:3.+'
    // Execute layer 
    implementation 'org.dalesbred:dalesbred:+'
    
    // Lombok for POJOs on steroids
    annotationProcessor 'org.projectlombok:lombok:1.+'
    compileOnly 'org.projectlombok:lombok:1.+'
    
  }
```

We create Job.java file:
```java
package jaregu.queries.di.example;

import java.time.Instant;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Table(name = "job")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@With
public class Job {

	@Column(name = "id")
	private Integer id;
	@Column(name = "name")
	private String name;
	@Column(name = "short_description")
	private String shortDescription;
	@Column(name = "version")
	private Integer version;
	@Column(name = "created")
	private Instant created;
	@Column(name = "modified")
	private Instant modified;
}
```
And simple IdName.java just to demonstrate some other select
```java
package jaregu.queries.di.example;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class IdName {

	private Integer id;
	private String name;
}
```

Search support class looks like:
```java
package jaregu.queries.di.example;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.ext.OrderableSearch;
import com.jaregu.database.queries.ext.PageableSearch;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.Builder.Default;

@Value
@Builder
@With
public class JobsSearch implements OrderableSearch<JobsSearch>, PageableSearch<JobsSearch> {

	private Integer limit;
	private Integer offset;
	@Default
	private List<String> orderBy = Collections.emptyList();

	private Integer id;
	private String name;

	public static JobsSearch byId(Integer id) {
		return builder().id(id).build();
	}

	public static JobsSearch byName(String name) {
		return builder().name(name).build();
	}

	public static JobsSearch all() {
		return builder().build();
	}
}
```

Main job SQL file src/main/java/jaregu/queries/di/example/JobDAO.sql:
```sql
-- create
CREATE TABLE job
(
    id int NOT NULL,
    name varchar(50) NOT NULL,
    short_description varchar(500),
    version integer NOT NULL,
    created timestamp NOT NULL,
    modified timestamp NOT NULL,
    
    CONSTRAINT job_pkey PRIMARY KEY (id)
);

-- insert
INSERT INTO job	(/* entityFieldGenerator(template = 'column' entityClass = 'Job' excludeColumns = 'version, created, modified') */
	 ,version, created, modified)
VALUES
	(/* entityFieldGenerator(template = 'value' entityClass = 'Job' excludeColumns = 'version, created, modified') */
	, 1, NOW(), NOW())
;

-- update
UPDATE job
SET
	-- entityFieldGenerator(template = 'columnAndValue' entityClass = 'Job' excludeColumns = 'id, version, created, modified')
	,version = version + 1
	,modified = NOW()
WHERE id = :id
	AND version = :version
;

-- delete
DELETE FROM job
WHERE id = :id;

-- search_entities
SELECT -- entityFieldGenerator(template = 'column' entityClass = 'Job' alias = 'j')
FROM job j
WHERE 1 = 1
	AND j.id = 1 -- :id
	AND j.name LIKE '%' /* '%' + :name + '%'; :name != null && :name != '' */
;

-- search_special
SELECT j.id 
	,j.name
FROM job j	
WHERE 1 = 1
	AND j.id = 1 -- :id
	AND j.name LIKE '%' /* '%' + :name + '%'; :name != null && :name != '' */
;
```

We create interface for bridging this SQL file to java JobDAO.java:
```java
package jaregu.queries.di.example;

import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.ext.dalesbred.ExecuteUpdate;
import com.jaregu.database.queries.ext.dalesbred.FindAll;
import com.jaregu.database.queries.ext.dalesbred.FindOptional;
import com.jaregu.database.queries.ext.dalesbred.FindUnique;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueryParam;
import com.jaregu.database.queries.proxy.QueryRef;

@QueriesSourceClass
public interface JobDAO {

	@QueryRef("create")
	@ExecuteUpdate
	void create();

	@QueryRef("insert")
	@ExecuteUpdate(unique = true)
	void insert(Job job);

	@QueryRef("update")
	@ExecuteUpdate(unique = true)
	void update(Job job);

	@QueryRef("delete")
	@ExecuteUpdate(unique = true)
	void delete(@QueryParam("id") Integer id);

	@QueryRef(value = "search_entities")
	@FindOptional(Job.class)
	Optional<Job> findEntity(JobsSearch search);

	@QueryRef(value = "search_entities")
	@FindUnique(Job.class)
	Job getEntity(JobsSearch search);

	@QueryRef(value = "search_entities", toSorted = true, toPaged = true)
	@FindAll(Job.class)
	List<Job> searchEntities(JobsSearch search);

	@QueryRef(value = "search_special", toSorted = true, toPaged = true)
	@FindAll(IdName.class)
	List<IdName> searchSpecial(JobsSearch search);

	@QueryRef(value = "search_special", toCount = true)
	@FindUnique(Integer.class)
	Integer getRowCount(JobsSearch search);
}
```
Our app startup looks like:
```java
package jaregu.queries.di.example;

import org.hsqldb.server.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jaregu.database.queries.ext.dalesbred.DalesbredModule;
import com.jaregu.database.queries.ext.guice.QueriesModule;
import com.jaregu.database.queries.ext.hikari.HikariIntegration;
import com.jaregu.database.queries.ext.hikari.HikariModule;
import com.zaxxer.hikari.HikariConfig;

public class App {

	public static void main(String[] args) {

		Server server = createHSQLServer();
		ConnectionPool connectionPool = new ConnectionPool();

		Injector injector = Guice.createInjector(
				QueriesModule.queriesModule(),
				DalesbredModule.create(),
				HikariModule.create(connectionPool),
				QueriesModule.proxyModule(JobDAO.class),
				QueriesModule.entityModule(Job.class));

		// This is the place where we call our main code
		injector.getInstance(Jobs.class).test();

		connectionPool.shutDown();
		server.stop();
	}

	private static Server createHSQLServer() {
		Server server = new Server();
		server.setSilent(true);
		server.setDatabaseName(0, "mainDb");
		server.setDatabasePath(0, "mem:mainDb");
		server.setPort(9001);
		server.start();
		return server;
	}

	private static class ConnectionPool implements HikariIntegration {

		private ShutdownHook hook;

		@Override
		public HikariConfig getConfig() {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:hsqldb:hsql://localhost:9001/mainDb");
			config.setUsername("SA");
			config.setPassword("");
			config.setAutoCommit(false);
			return config;
		}

		@Override
		public void registerShudownHook(ShutdownHook hook) {
			this.hook = hook;
		}

		public void shutDown() {
			this.hook.shutDown();
		}
	}
}
```

This is our main code place for this example Jobs.java :
```java
package jaregu.queries.di.example;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Jobs {

	private JobDAO dao;

	@Inject
	Jobs(JobDAO dao) {
		this.dao = dao;
	}

	void test() {
		// table creation
		dao.create();

		// inserting first record
		dao.insert(Job.builder()
				.id(1)
				.name("first")
				.shortDescription("some description")
				.build());

		// inserting second record
		dao.insert(Job.builder()
				.id(2)
				.name("second")
				.build());

		// output: [Job(id=1, name=first, shortDescription=some description, version=1, created=2020-08-25T21:04:40.047Z, modified=2020-08-25T21:04:40.047Z)]
		System.out.println(dao.searchEntities(JobsSearch.byId(1)));

		// output: [Job(id=1, name=first, shortDescription=some description, version=1, created=2020-08-25T21:04:40.047Z, modified=2020-08-25T21:04:40.047Z), Job(id=2, name=second, shortDescription=null, version=1, created=2020-08-25T21:04:40.054Z, modified=2020-08-25T21:04:40.054Z)]
		System.out.println(dao.searchEntities(JobsSearch.all()));

		// output: 2
		System.out.println(dao.getRowCount(JobsSearch.all()));

		// output: [IdName(id=1, name=first), IdName(id=2, name=second)]
		System.out.println(dao.searchSpecial(JobsSearch.all()));

		// output: [IdName(id=2, name=second)]
		System.out.println(dao.searchSpecial(JobsSearch.byName("se")));

		// deleting first record
		dao.delete(1);

		// output: [IdName(id=2, name=second)]
		System.out.println(dao.searchSpecial(JobsSearch.all()));

	}
}
```

# Quick-start (Without DI)

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
