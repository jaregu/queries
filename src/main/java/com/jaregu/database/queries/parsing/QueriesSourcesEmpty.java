package com.jaregu.database.queries.parsing;

import java.util.Collections;
import java.util.List;

public final class QueriesSourcesEmpty implements QueriesSources {

	private static final QueriesSources INSTANCE = new QueriesSourcesEmpty();

	private QueriesSourcesEmpty() {
	}

	public static QueriesSources getInstance() {
		return INSTANCE;
	}

	@Override
	public List<QueriesSource> getSources() {
		return Collections.emptyList();
	}
}
