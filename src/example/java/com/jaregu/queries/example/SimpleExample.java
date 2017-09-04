package com.jaregu.queries.example;

import static com.jaregu.database.queries.Queries.sourceOf;
import static com.jaregu.database.queries.Queries.sourceOfResource;
import static org.dalesbred.query.SqlQuery.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dalesbred.Database;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.RetativeQueries;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.building.BuildtQuery;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SimpleExample {

	public static void main(String[] args) {
		SimpleExample simple = new SimpleExample();
		// SQL pool and database initialization
		simple.initDataSource();
		simple.initDatabase();

		// Queries initialization
		simple.initQueries();

		// Working with database
		simple.createTables();
		simple.insertPersons();
		simple.insertDummy();

		simple.findAllPersons().forEach(System.out::println);
		System.out.println(simple.getPersonCount());

		PersonSearch search = new PersonSearch();
		System.out.println("-- PAGE 1 --");
		System.out.println(simple.findPersons(search));
		System.out.println("-- PAGE 2 --");
		search.nextPage();
		System.out.println(simple.findPersons(search));
		System.out.println("-- PAGE 3 --");
		search.nextPage();
		System.out.println(simple.findPersons(search));

		/*
		 * search = new PersonSearch(); search.firstName = 'Will'
		 * System.out.println();
		 */

		/*
		 * String string =
		 * "-- some: [comment] \n create table person (id int, \n first_name varchar(100), \n last_name varchar(100));"
		 * ; System.out.println(string); db.update(string);
		 */

		/*
		 * QueryId insertPersonArray =
		 * populateSource.getQueryId("insert person arr"); Query queryUsingList
		 * = queries.get(insertPersonArray, Arrays.asList(3, "David", "Calco"));
		 * db.update(queryUsingList.getSql(),
		 * queryUsingList.getParameters().toArray());
		 * 
		 * Query queryUsingArray = queries.get(insertPersonArray, 3, "David",
		 * "Calco"); db.update(queryUsingArray.getSql(),
		 * queryUsingArray.getParameters().toArray());
		 */
	}

	private HikariDataSource ds;
	private Database db;
	private Queries queries;
	private QueriesSource createSource;
	private QueriesSource populateSource;
	private QueriesSource otherSource;

	private void initDataSource() {
		HikariConfig config = new HikariConfig("/com/jaregu/queries/example/hikari.properties");
		ds = new HikariDataSource(config);
	}

	private void initDatabase() {
		db = new Database(ds);
	}

	private void initQueries() {
		createSource = sourceOfResource("com/jaregu/queries/example/create.sql");
		populateSource = sourceOfResource("com/jaregu/queries/example/populate.sql");
		otherSource = sourceOf(SourceId.ofId("some.source"), () -> {
			return "-- query1\nselect 1;";
		});

		queries = Queries.ofSources(createSource, populateSource, sourceOfResource(SimpleExample.class), otherSource);
	}

	private void createTables() {
		RetativeQueries createQueries = queries.ofSource(createSource.getId());
		db.update(createQueries.get("person").build().getSql());
		db.update(createQueries.get("team").build().getSql());
		db.update(createQueries.get("team_person").build().getSql());
		db.update(createQueries.get("dummy").build().getSql());
	}

	private void insertPersons() {
		PreparedQuery personInsert = queries.get(populateSource.getQueryId("insert person"));

		for (int i = 1; i <= 100; i++) {
			personInsert.build(new Person(i, "Jhon" + i, "Fovro" + i))
					.map(q -> db.update(query(q.getSql(), q.getParameters())));
		}

		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("id", 200);
		paramsMap.put("firstName", "Will");
		paramsMap.put("lastName", "Notcho");
		BuildtQuery queryUsingMap = personInsert.build(paramsMap);
		db.update(queryUsingMap.getSql(), queryUsingMap.getParameters().toArray());
	}

	private void insertDummy() {
		PreparedQuery dummyInsert = queries.get(populateSource.getQueryId("insert dummy"));

		for (int i = 1; i <= 100; i++) {
			dummyInsert.build(new Dummy(i, i + 1000, "foobar" + i))
					.map(q -> db.update(query(q.getSql(), q.getParameters())));
		}
	}

	private Integer getPersonCount() {
		RetativeQueries selectQueries = queries.ofSource(SourceId.ofClass(SimpleExample.class));
		return selectQueries.get("count persons").build().map(q -> db.findUniqueInt(q.getSql()));
	}

	private List<Person> findAllPersons() {
		RetativeQueries selectQueries = queries.ofSource(SourceId.ofClass(SimpleExample.class));
		return db.findAll(Person.class, selectQueries.get("find all").build().getSql());
	}

	private List<Person> findPersons(PersonSearch search) {
		RetativeQueries selectQueries = queries.ofSource(SourceId.ofClass(SimpleExample.class));
		return selectQueries.get("persons search").build(search)
				.map(q -> db.findAll(Person.class, query(q.getSql(), q.getParameters())));
	}

	public static class Person {

		public Integer id;
		public String firstName;
		public String lastName;

		public Person() {
		}

		public Person(Integer id, String firstName, String lastName) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Person [id=").append(id).append(", firstName=").append(firstName).append(", lastName=")
					.append(lastName).append("]");
			return builder.toString();
		}
	}

	public static class PersonSearch {

		public String firstName;
		public String firstName2;

		public String text;

		public int offset = 0;
		public int pageSize = 10;

		public void nextPage() {
			offset = offset + pageSize;
		}
	}

	public static class Dummy {

		private int id;
		private Integer foo;
		private String bar;

		public Dummy(int id, Integer foo, String bar) {
			this.id = id;
			this.foo = foo;
			this.bar = bar;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Integer getFoo() {
			return foo;
		}

		public void setFoo(Integer foo) {
			this.foo = foo;
		}

		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}
	}

	private Database createLocalMariaDb() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(
				"jdbc:mariadb://localhost:3406/test?autoReconnect=true&?useUnicode=yes&characterEncoding=UTF-8");
		config.setUsername("test");
		config.setPassword("test");
		config.setAutoCommit(false);
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		Database mariaDb = new Database(new HikariDataSource(config));
		return mariaDb;
	}
}
