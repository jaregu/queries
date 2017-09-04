package com.jaregu.database.queries;

import java.util.Collection;
import java.util.function.Supplier;

import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;

public interface Queries extends QueriesBase<QueryId> {

	RetativeQueries ofSource(SourceId sourceId);

	static QueriesImpl.Builder newBuilder() {
		return new QueriesImpl.Builder();
	}

	static Queries ofSources(Collection<QueriesSource> sources) {
		return ofSources(QueriesSources.of(sources));
	}

	static Queries ofSources(QueriesSource... sources) {
		return ofSources(QueriesSources.of(sources));
	}

	static Queries ofSources(QueriesSources sources) {
		return new QueriesImpl.Builder().setSources(sources).build();
	}

	static QueriesSource sourceOfResource(Class<?> clazz) {
		return QueriesSource.ofResource(clazz);
	}

	static QueriesSource sourceOfResource(String resourcePath) {
		return QueriesSource.ofResource(resourcePath);
	}

	static QueriesSource sourceOf(SourceId sourceId, Supplier<String> contentSupplier) {
		return QueriesSource.of(sourceId, contentSupplier);
	}
}