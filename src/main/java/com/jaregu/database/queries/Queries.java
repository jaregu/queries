package com.jaregu.database.queries;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.building.ParameterBinders;
import com.jaregu.database.queries.cache.Caches;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Dialects;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.parsing.QueryParseException;
import com.jaregu.database.queries.parsing.Sources;

public interface Queries extends QueriesBase<QueryId> {

	RetativeQueries ofSource(SourceId sourceId);

	/**
	 * Shorthand for <code>builder.sources(...).build()</code>. Use
	 * {@link #builder()} for more configuration options
	 * 
	 * @param sources
	 * @return
	 */
	static Queries of(QueriesSources sources) {
		return builder().sources(sources).build();
	}

	static Queries of(Collection<QueriesSource> sources) {
		return of(QueriesSources.of(sources));
	}

	static Queries of(QueriesSource... sources) {
		return of(QueriesSources.of(sources));
	}

	static Queries.Builder builder() {
		return new Builder();
	}

	/**
	 * Queries builder class. Use builder to configure Queries instance.
	 *
	 */
	public static class Builder
			implements Sources<Builder>, ParameterBinders<Builder>, Dialects<Builder>, Caches<Builder> {

		private List<QueriesSource> sources = new LinkedList<>();
		private Optional<QueriesCache> cache = Optional.empty();
		private Optional<Dialect> dialect = Optional.empty();
		private Optional<ParameterBinder> parameterBinder = Optional.empty();

		@Override
		public Builder sources(QueriesSources sources) {
			this.sources.addAll(sources.getSources());
			return this;
		}

		@Override
		public Builder binder(ParameterBinder binder) {
			this.parameterBinder = Optional.of(binder);
			return this;
		}

		@Override
		public Builder dialect(Dialect dialect) {
			this.dialect = Optional.of(dialect);
			return this;
		}

		@Override
		public Builder cache(QueriesCache cache) {
			this.cache = Optional.of(cache);
			return this;
		}

		public Queries build() {
			if (sources.isEmpty())
				throw new QueryParseException("Can't build Queries, sources is empty");
			return new QueriesImpl(QueriesSources.of(sources),
					QueriesConfig.of(dialect.orElse(Dialects.defaultDialect()), cache.orElse(Caches.noCache()),
							parameterBinder.orElse(ParameterBinders.defaultBinder())));
		}
	}
}