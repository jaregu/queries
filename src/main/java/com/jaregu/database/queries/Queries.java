package com.jaregu.database.queries;

import java.util.Collection;
import java.util.function.Supplier;

import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;

public interface Queries extends QueriesBase<QueryId> {

	RetativeQueries ofSource(SourceId sourceId);

	static QueriesImpl.Builder builder() {
		return QueriesImpl.builder();
	}

	static QueriesConfigImpl.Builder configBuilder() {
		return QueriesConfig.builder();
	}

	static Queries of(QueriesSources sources) {
		return of(QueriesConfig.createDefault(), sources);
	}

	static Queries of(Collection<QueriesSource> sources) {
		return of(QueriesConfig.createDefault(), QueriesSources.of(sources));
	}

	static Queries of(QueriesSource... sources) {
		return of(QueriesConfig.createDefault(), QueriesSources.of(sources));
	}

	static Queries of(QueriesConfig config, Collection<QueriesSource> sources) {
		return of(config, QueriesSources.of(sources));
	}

	static Queries of(QueriesConfig config, QueriesSource... sources) {
		return of(config, QueriesSources.of(sources));
	}

	static Queries of(QueriesConfig config, QueriesSources sources) {
		return QueriesImpl.builder().config(config).sources(sources).build();
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