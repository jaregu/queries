package com.jaregu.queries.example;

import javax.sql.DataSource;

import org.dalesbred.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MariaDbTest {

	private DataSource ds;
	private Database mariaDb;
	private DataSource ds2;
	private Database hsqlDb;

	public static void main(String[] args) {
		MariaDbTest test = new MariaDbTest();
		test.init();
		test.testCreateStatements();
	}

	private void testCreateStatements() {
		String createStatement = "-- QUERY ID: QueryId[com.jaregu.queries.example.create.person]\r\n" + 
				"create table person (id int, \r\n" + 
				"first_name varchar(100), \r\n" + 
				"last_name varchar(100));";
		System.out.println(createStatement);
		mariaDb.update(createStatement);
		hsqlDb.update(createStatement);
	}

	private void init() {
		initDataSource();
		initDatabase();
		initDataSource2();
		initDatabase2();
	}

	private void initDataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(
				"jdbc:mariadb://localhost:3406/test?autoReconnect=true&?useUnicode=yes&characterEncoding=UTF-8");
		config.setUsername("test");
		config.setPassword("test");
		config.setAutoCommit(false);
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		ds = new HikariDataSource(config);
	}

	private void initDatabase() {
		mariaDb = new Database(ds);
	}

	private void initDataSource2() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:hsqldb:mem:customer");
		config.setUsername("sa");
		config.setPassword("");
		ds2 = new HikariDataSource(config);
	}

	private void initDatabase2() {
		hsqlDb = new Database(ds2);
	}
}
