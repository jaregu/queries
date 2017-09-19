package com.jaregu.database.queries;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;
import com.jaregu.database.queries.parsing.ParsedQuery;
import com.jaregu.database.queries.parsing.QueriesParseException;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;

public final class QueriesImpl implements Queries {

	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final QueriesConfig config;
	private final QueriesSources sources;

	private volatile Map<SourceId, QueriesSource> sourcesById;

	private QueriesImpl(QueriesConfig config, QueriesSources sources) {
		this.config = config;
		this.sources = sources;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public PreparedQuery get(QueryId queryId) {
		return QueriesContext.of(config).withContext(() -> {
			return config.getCache().getPreparedQuery(queryId, this::prepareQuery);
		});
	}

	@Override
	public RetativeQueries ofSource(SourceId sourceId) {
		return new RetativeQueries() {

			@Override
			public PreparedQuery get(String id) {
				return QueriesImpl.this.get(sourceId.getQueryId(id));
			}
		};
	}

	private PreparedQuery prepareQuery(QueryId queryId) {
		ParsedQuery sourceQuery = config.getCache().getParsedQueries(queryId.getSourceId(), this::parseQueries)
				.get(queryId);
		log.debug("Starting to compile queryId: {}!", queryId);
		return config.getCompiler().compile(sourceQuery);
	}

	private ParsedQueries parseQueries(SourceId sourceId) {
		QueriesSource queriesSource = ensureSources().get(sourceId);
		if (queriesSource == null) {
			throw new QueryException("Can't find source with id: " + sourceId);
		}
		log.debug("Starting to parse queries source: {}!", sourceId);
		return config.getParser().parse(queriesSource);
	}

	private Map<SourceId, QueriesSource> ensureSources() {
		if (sourcesById == null) {
			synchronized (this) {
				log.debug("Initializing queries sources!");
				if (sourcesById == null) {
					Map<SourceId, QueriesSource> sourcesById = sources.getSources().stream()
							.collect(Collectors.toMap(QueriesSource::getId, Function.identity()));
					this.sourcesById = sourcesById;
				}
			}
		}
		return sourcesById;
	}

	public static class Builder {

		private Optional<QueriesConfig> config = Optional.empty();
		private List<QueriesSource> sources = new LinkedList<>();

		public Builder sources(QueriesSources sources) {
			this.sources = new ArrayList<>(sources.getSources());
			return this;
		}

		public Builder addSource(QueriesSource source) {
			this.sources.add(source);
			return this;
		}

		public Builder addSources(QueriesSource... sources) {
			this.sources.addAll(Arrays.asList(sources));
			return this;
		}

		public Builder addSources(Collection<QueriesSource> sources) {
			this.sources.addAll(sources);
			return this;
		}

		public Builder config(QueriesConfig config) {
			this.config = Optional.of(config);
			return this;
		}

		public Queries build() {
			if (sources.isEmpty())
				throw new QueriesParseException("Can't build Queries, sources is empty");
			return new QueriesImpl(config.orElse(QueriesConfig.builder().build()), QueriesSources.of(sources));
		}
	}
}
