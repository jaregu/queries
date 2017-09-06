package com.jaregu.database.queries.compiling.expr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.jaregu.database.queries.building.ParametersResolver;

final class EvaluationContext {

	private static final ThreadLocal<EvaluationContext> currentContext = new ThreadLocal<>();

	private ParametersResolver variableResolver;
	private Optional<Expression> baseExpression;
	private Map<String, Object> outputVariables;

	public static Builder forVariableResolver(ParametersResolver variableResolver) {
		return new Builder(variableResolver);
	}

	private EvaluationContext(ParametersResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	public ParametersResolver getVariableResolver() {
		return variableResolver;
	}

	public Optional<Expression> getBaseExpression() {
		return baseExpression;
	}

	public Map<String, Object> getOutputVariables() {
		return outputVariables != null ? Collections.unmodifiableMap(outputVariables) : Collections.emptyMap();
	}

	public void setOutputVariable(String name, Object value) {
		if (outputVariables == null) {
			outputVariables = new LinkedHashMap<>();
		}
		outputVariables.put(name, value);
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

		private Builder(ParametersResolver variableResolver) {
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
