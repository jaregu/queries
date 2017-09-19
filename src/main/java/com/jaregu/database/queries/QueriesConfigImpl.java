package com.jaregu.database.queries;

import java.util.Optional;

import com.jaregu.database.queries.building.ParameterBindingBuilder;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.QueriesParser;

public class QueriesConfigImpl implements QueriesConfig {

	private final QueriesCache cache;

	private final QueriesParser parser;

	private final QueryCompiler compiler;

	private final ExpressionParser expressionParser;

	private final ParameterBindingBuilder parameterBindingBuilder;

	private QueriesConfigImpl(QueriesCache cache, QueriesParser parser, QueryCompiler compiler,
			ExpressionParser expressionParser, ParameterBindingBuilder parameterBindingBuilder) {
		this.cache = cache;
		this.parser = parser;
		this.compiler = compiler;
		this.expressionParser = expressionParser;
		this.parameterBindingBuilder = parameterBindingBuilder;
	}

	@Override
	public QueriesCache getCache() {
		return cache;
	}

	@Override
	public QueriesParser getParser() {
		return parser;
	}

	@Override
	public QueryCompiler getCompiler() {
		return compiler;
	}

	@Override
	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	@Override
	public ParameterBindingBuilder getParameterBindingBuilder() {
		return parameterBindingBuilder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Optional<QueriesCache> cache = Optional.empty();
		private Optional<QueriesParser> parser = Optional.empty();
		private Optional<QueryCompiler> compiler = Optional.empty();
		private Optional<ExpressionParser> expressionParser = Optional.empty();
		private Optional<ParameterBindingBuilder> parameterBindingBuilder = Optional.empty();

		public Builder cache(QueriesCache cache) {
			this.cache = Optional.of(cache);
			return this;
		}

		public Builder parser(QueriesParser parser) {
			this.parser = Optional.of(parser);
			return this;
		}

		public Builder compiler(QueryCompiler compiler) {
			this.compiler = Optional.of(compiler);
			return this;
		}

		public Builder expressionParser(ExpressionParser expressionParser) {
			this.expressionParser = Optional.of(expressionParser);
			return this;
		}

		public Builder parameterBindingBuilder(ParameterBindingBuilder parameterBindingBuilder) {
			this.parameterBindingBuilder = Optional.of(parameterBindingBuilder);
			return this;
		}

		public QueriesConfig build() {
			return new QueriesConfigImpl(cache.orElse(QueriesCache.noCache()),
					parser.orElse(QueriesParser.createDefault()), compiler.orElse(QueryCompiler.createDefault()),
					expressionParser.orElse(ExpressionParser.createDefault()),
					parameterBindingBuilder.orElse(ParameterBindingBuilder.createDefault()));
		}
	}
}
