package com.jaregu.database.queries.dialect;

public class PostgreSQLDialect extends DefaultDialectImpl {

	@Override
	public String getSuffix() {
		return "postgresql";
	}
}
