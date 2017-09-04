package com.jaregu.database.queries.compiling.expr;

import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.building.ParametersResolver;

public class ExpressionImpl implements Expression {

	private ExpressionBlock block;

	public ExpressionImpl(ExpressionBlock block) {
		this.block = block;
	}

	@Override
	public List<String> getVariableNames() {
		return block.getVariableNames();
	}

	@Override
	public ExpressionResult eval(ParametersResolver variableResolver) {
		EvaluationContext context = EvaluationContext.forVariableResolver(variableResolver).withBaseExpression(this)
				.build();
		return new ResultImpl(context.withContext(() -> {
			return block.getValue();
		}), context.getOutputVariables());
	}

	@Override
	public String toString() {
		return "Expression[" + block.toString() + "]";
	}

	private static class ResultImpl implements ExpressionResult {

		private Object value;
		private Map<String, Object> output;

		public ResultImpl(Object value, Map<String, Object> output) {
			this.value = value;
			this.output = output;
		}

		@Override
		public Object getReturnValue() {
			return value;
		}

		@Override
		public Map<String, Object> getOutputVariables() {
			return output;
		}
	}
}
