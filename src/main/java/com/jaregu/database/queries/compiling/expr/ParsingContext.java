package com.jaregu.database.queries.compiling.expr;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

final class ParsingContext {

	private static final ThreadLocal<ParsingContext> CONTEXT = new ThreadLocal<>();

	private String expression;

	public static Builder forExpression(String expression) {
		return new Builder(expression);
	}

	private ParsingContext(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	public <T> T withContext(Supplier<T> work) {
		return withContext(this, work);
	}

	public static Optional<ParsingContext> peekCurrent() {
		return Optional.ofNullable(CONTEXT.get());
	}

	public static ParsingContext getCurrent() {
		return peekCurrent().orElseThrow(() -> new ExpressionParseException(
				"Trying to parse expression without " + ParsingContext.class.getSimpleName() + "!"));
	}

	public static <T> T withContext(ParsingContext context, Supplier<T> work) {
		ParsingContext oldContext = CONTEXT.get();
		try {
			CONTEXT.set(Objects.requireNonNull(context));
			return work.get();
		} finally {
			CONTEXT.set(oldContext);
		}
	}

	public static class Builder {

		private ParsingContext context;

		private Builder(String expression) {
			context = new ParsingContext(expression);
		}

		public ParsingContext build() {
			return context;
		}
	}
}
