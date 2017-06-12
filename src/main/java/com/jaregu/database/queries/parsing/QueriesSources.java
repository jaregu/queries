package com.jaregu.database.queries.parsing;

import java.util.Arrays;
import java.util.Collection;

@FunctionalInterface
public interface QueriesSources {

	Collection<QueriesSource> getSources();

	static QueriesSources empty() {
		return QueriesSourcesEmpty.getInstance();
	}

	static QueriesSources of(Collection<QueriesSource> sources) {
		return () -> sources;
	}

	static QueriesSources of(QueriesSource... sources) {
		return () -> Arrays.asList(sources);
	}
}
