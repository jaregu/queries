package com.jaregu.database.queries.dialect;

public class HSQLDBDialect extends DefaultDialectImpl {

	@Override
	public String getSuffix() {
		return "hsql";
	}
}
