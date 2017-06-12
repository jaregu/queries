package com.jaregu.database.queries.compiling.expr;

import java.util.List;

import com.jaregu.database.queries.building.ParamsResolver;

public class ExpressionImpl implements Expression {

	private ExpressionBlock block;

	public ExpressionImpl(ExpressionBlock block) {
		this.block = block;
	}

	@Override
	public Object eval(ParamsResolver variableResolver) {
		EvaluationContext context = EvaluationContext.forVariableResolver(variableResolver).withBaseExpression(this)
				.build();
		return context.withContext(() -> {
			return block.getValue();
		});
	}

	@Override
	public List<String> getVariableNames() {
		return block.getVariableNames();
	}
}
