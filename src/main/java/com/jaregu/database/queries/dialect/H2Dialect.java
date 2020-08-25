package com.jaregu.database.queries.dialect;

public class H2Dialect extends DefaultDialectImpl {

	@Override
	public String getSuffix() {
		return "h2";
	}
}
