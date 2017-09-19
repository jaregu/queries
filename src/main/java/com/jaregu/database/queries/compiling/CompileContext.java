package com.jaregu.database.queries.compiling;

import java.util.Optional;
import java.util.function.Supplier;

import com.jaregu.database.queries.parsing.ParsedQuery;

final class CompileContext {

	private static final ThreadLocal<CompileContext> CONTEXT = new ThreadLocal<>();

	private final ParsedQuery sourceQuery;

	private CompileContext(ParsedQuery sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	public static CompileContext of(ParsedQuery sourceQuery) {
		return new CompileContext(sourceQuery);
	}

	public ParsedQuery getSourceQuery() {
		return sourceQuery;
	}

	public <T> T withContext(Supplier<T> work) {
		return withContext(this, work);
	}

	public static Optional<CompileContext> peekCurrent() {
		return Optional.ofNullable(CONTEXT.get());
	}

	public static CompileContext getCurrent() {
		return peekCurrent().orElseThrow(() -> new QueryCompileException(
				"Trying to compile query without " + CompileContext.class.getSimpleName() + "!"));
	}

	public static <T> T withContext(CompileContext context, Supplier<T> work) {
		CompileContext oldContext = CONTEXT.get();
		try {
			CONTEXT.set(context);
			return work.get();
		} finally {
			CONTEXT.set(oldContext);
		}
	}
}
