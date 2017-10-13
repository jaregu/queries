package com.jaregu.database.queries.parsing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation represent collection of {@link QueriesSource}
 *
 */
@FunctionalInterface
public interface QueriesSources {

	Collection<QueriesSource> getSources();

	static QueriesSources empty() {
		return () -> Collections.emptyList();
	}

	static QueriesSources of(Collection<QueriesSource> sources) {
		return () -> sources;
	}

	static QueriesSources of(QueriesSource... sources) {
		return () -> Arrays.asList(sources);
	}
}
