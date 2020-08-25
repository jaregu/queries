package com.jaregu.database.queries.dialect;

public class MariaDBDialect extends DefaultDialectImpl {

	@Override
	public String getSuffix() {
		return "mariadb";
	}
}
