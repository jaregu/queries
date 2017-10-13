package com.jaregu.database.queries.parsing;

import java.util.Collection;
import java.util.function.Supplier;

import com.jaregu.database.queries.SourceId;

/**
 * Builder methods for adding SQL sources. See {@link #sources(QueriesSources)},
 * {@link QueriesSource}
 *
 * @param <T>
 */
public interface Sources<T extends Sources<?>> {

	/**
	 * Adds SQL statements sources
	 * 
	 * @param sources
	 * @return
	 */
	public T sources(QueriesSources sources);

	/**
	 * Adds SQL statements source
	 * 
	 * @param sources
	 * @return
	 */
	default T source(QueriesSource source) {
		return sources(QueriesSources.of(source));
	}

	/**
	 * Adds SQL statements sources
	 * 
	 * @param sources
	 * @return
	 */
	default T sources(QueriesSource... sources) {
		return sources(QueriesSources.of(sources));
	}

	/**
	 * Adds SQL statements sources
	 * 
	 * @param sources
	 * @return
	 */
	default T sources(Collection<QueriesSource> sources) {
		return sources(QueriesSources.of(sources));
	}

	/**
	 * Adds SQL statements source, shorthand for adding SQL statements source
	 * based on class, see {@link QueriesSource#ofClass(Class)}
	 * 
	 * @param sources
	 * @return
	 */
	default T sourceOfClass(Class<?> clazz) {
		return sources(QueriesSource.ofClass(clazz));
	}

	/**
	 * Adds SQL statements source, shorthand for adding SQL statements source
	 * based on resource, see {@link QueriesSource#ofResource(String)}
	 * 
	 * @param sources
	 * @return
	 */
	default T sourceOfResource(String resourcePath) {
		return sources(QueriesSource.ofResource(resourcePath));
	}

	/**
	 * Adds SQL statements source, shorthand for adding SQL statements source
	 * based on content supplier, see
	 * {@link QueriesSource#ofContent(SourceId, Supplier)}
	 * 
	 * @param sourceId
	 * @param contentSupplier
	 * @return
	 */
	default T sourceOfContent(SourceId sourceId, Supplier<String> contentSupplier) {
		return sources(QueriesSource.ofContent(sourceId, contentSupplier));
	}
}
