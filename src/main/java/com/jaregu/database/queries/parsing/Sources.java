package com.jaregu.database.queries.parsing;

import java.util.Collection;
import java.util.function.Supplier;

import com.jaregu.database.queries.SourceId;

public interface Sources<T extends Sources<?>> {

	/**
	 * Adds SQL queries sources
	 * 
	 * @param sources
	 * @return
	 */
	public T sources(QueriesSources sources);

	default T source(QueriesSource source) {
		return sources(QueriesSources.of(source));
	}

	default T sources(QueriesSource... sources) {
		return sources(QueriesSources.of(sources));
	}

	default T sources(Collection<QueriesSource> sources) {
		return sources(QueriesSources.of(sources));
	}

	default T sourceOfClass(Class<?> clazz) {
		return sources(QueriesSource.ofClass(clazz));
	}

	default T sourceOfResource(String resourcePath) {
		return sources(QueriesSource.ofResource(resourcePath));
	}

	default T sourceOfContent(SourceId sourceId, Supplier<String> contentSupplier) {
		return sources(QueriesSource.ofContent(sourceId, contentSupplier));
	}
}
