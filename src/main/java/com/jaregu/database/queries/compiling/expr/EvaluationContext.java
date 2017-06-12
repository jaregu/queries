package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;
import java.util.function.Supplier;

import com.jaregu.database.queries.building.ParamsResolver;

final class EvaluationContext {

	private static final ThreadLocal<EvaluationContext> currentContext = new ThreadLocal<>();

	private ParamsResolver variableResolver;
	private Optional<Expression> baseExpression;

	public static Builder forVariableResolver(ParamsResolver variableResolver) {
		return new Builder(variableResolver);
	}

	private EvaluationContext(ParamsResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	public ParamsResolver getVariableResolver() {
		return variableResolver;
	}

	public Optional<Expression> getBaseExpression() {
		return baseExpression;
	}

	public <T> T withContext(Supplier<T> work) {
		return withContext(this, work);
	}

	public static Optional<EvaluationContext> peekCurrent() {
		return Optional.ofNullable(currentContext.get());
	}

	public static EvaluationContext getCurrent() {
		return peekCurrent().orElseThrow(() -> new ExpressionEvalException(
				"Trying to evaluate expression without " + EvaluationContext.class.getSimpleName() + "!"));
	}

	public static <T> T withContext(EvaluationContext context, Supplier<T> work) {
		EvaluationContext oldContext = currentContext.get();
		try {
			currentContext.set(context);
			return work.get();
		} finally {
			currentContext.set(oldContext);
		}
	}

	public static class Builder {

		private EvaluationContext context;

		private Builder(ParamsResolver variableResolver) {
			context = new EvaluationContext(variableResolver);
		}

		public Builder withBaseExpression(Expression expression) {
			context.baseExpression = Optional.of(expression);
			return this;
		}

		public EvaluationContext build() {
			return context;
		}
	}
}
