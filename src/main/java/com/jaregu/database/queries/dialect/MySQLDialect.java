package com.jaregu.database.queries.dialect;

public class MySQLDialect extends DefaultDialectImpl {

	@Override
	public String getSuffix() {
		return "mysql";
	}
}
