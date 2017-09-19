package com.jaregu.database.queries;

import java.util.Optional;
import java.util.function.Supplier;

final public class QueriesContext {

	private static final ThreadLocal<QueriesContext> CONTEXT = new ThreadLocal<>();

	private final QueriesConfig config;

	private QueriesContext(QueriesConfig config) {
		this.config = config;
	}

	public static QueriesContext of(QueriesConfig config) {
		return new QueriesContext(config);
	}

	public QueriesConfig getConfig() {
		return config;
	}

	public <T> T withContext(Supplier<T> work) {
		return withContext(this, work);
	}

	public static Optional<QueriesContext> peekCurrent() {
		return Optional.ofNullable(CONTEXT.get());
	}

	public static QueriesContext getCurrent() {
		return peekCurrent().orElseThrow(() -> new QueryException("There is no query context set!"));
	}

	public static <T> T withContext(QueriesContext context, Supplier<T> work) {
		QueriesContext oldContext = CONTEXT.get();
		try {
			CONTEXT.set(context);
			return work.get();
		} finally {
			CONTEXT.set(oldContext);
		}
	}
}
