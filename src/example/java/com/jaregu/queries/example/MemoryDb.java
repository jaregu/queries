package com.jaregu.queries.example;

import javax.sql.DataSource;

import org.dalesbred.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MemoryDb {

	private Database db;

	public MemoryDb() {
		HikariConfig config = new HikariConfig("/com/jaregu/queries/example/hikari.properties");
		DataSource ds = new HikariDataSource(config);
		db = new Database(ds);
	}

	public Database getDb() {
		return db;
	}
}
