package com.jaregu.database.queries.compiling;

import java.util.Optional;
import java.util.function.Supplier;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.ParsedQuery;

final class CompilingContext {

	private static final ThreadLocal<CompilingContext> currentContext = new ThreadLocal<>();

	private ExpressionParser expressionParser;
	private QueriesConfig config = QueriesConfig.getDefault();
	private Optional<ParsedQuery> sourceQuery;

	public static Builder forExpressionParser(ExpressionParser expressionParser) {
		return new Builder(expressionParser);
	}

	private CompilingContext(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public QueriesConfig getConfig() {
		return config;
	}

	public Optional<ParsedQuery> getSourceQuery() {
		return sourceQuery;
	}

	public <T> T withContext(Supplier<T> work) {
		return withContext(this, work);
	}

	public static Optional<CompilingContext> peekCurrent() {
		return Optional.ofNullable(currentContext.get());
	}

	public static CompilingContext getCurrent() {
		return peekCurrent().orElseThrow(() -> new QueryCompileException(
				"Trying to compile query without " + CompilingContext.class.getSimpleName() + "!"));
	}

	public static <T> T withContext(CompilingContext context, Supplier<T> work) {
		CompilingContext oldContext = currentContext.get();
		try {
			currentContext.set(context);
			return work.get();
		} finally {
			currentContext.set(oldContext);
		}
	}

	public static class Builder {

		private CompilingContext context;

		private Builder(ExpressionParser expressionParser) {
			context = new CompilingContext(expressionParser);
		}

		public Builder config(QueriesConfig config) {
			context.config = config;
			return this;
		}

		public Builder source(ParsedQuery sourceQuery) {
			context.sourceQuery = Optional.of(sourceQuery);
			return this;
		}

		public CompilingContext build() {
			return context;
		}
	}
}
