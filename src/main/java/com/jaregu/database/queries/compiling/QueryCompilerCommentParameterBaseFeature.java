package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;

public abstract class QueryCompilerCommentParameterBaseFeature implements QueryCompilerFeature {

	protected boolean isLikeExpression(ExpressionParser parser, String comment) {
		int splitIndex = getSemicolonIndex(comment);
		if (splitIndex > 0) {
			return parser.isLikeExpression(comment.substring(0, splitIndex))
					&& parser.isLikeExpression(comment.substring(splitIndex + 1));
		} else {
			return parser.isLikeExpression(comment);
		}
	}

	private int getSemicolonIndex(String comment) {
		boolean insideString = false;
		for (int i = 0; i < comment.length(); i++) {
			char c = comment.charAt(i);
			if (c == ';' && !insideString) {
				return i;
			} else if (c == '\'') {
				insideString = !insideString;
			}
		}
		return -1;
	}

	protected class PartBuilder {

		private String argumentSql;
		private String afterSql;
		private String comment;

		public PartBuilder() {
		}

		public PartBuilder setArgumentSql(String sql) {
			this.argumentSql = sql;
			return this;
		}

		public PartBuilder setAfterSql(String sql) {
			this.afterSql = sql;
			return this;
		}

		public PartBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		public Result buildResult() {
			CompilingContext context = CompilingContext.getCurrent();
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append(ArgumentReplacer.forSql(argumentSql)
					.addComment(context.getConfig().isOriginalArgumentCommented()).replace());
			if (afterSql != null) {
				sqlBuilder.append(afterSql);
			}
			String sql = sqlBuilder.toString();

			List<String> variableNames = new LinkedList<>();
			ExpressionParser parser = context.getExpressionParser();
			Function<ParamsResolver, Boolean> conditionFunction;
			Expression valueExpression;
			int splitIndex = getSemicolonIndex(comment);
			if (splitIndex > 0) {
				valueExpression = parser.parse(comment.substring(0, splitIndex));
				Expression conditionExpression = parser.parse(comment.substring(splitIndex + 1));
				variableNames.addAll(conditionExpression.getVariableNames());
				conditionFunction = (v) -> {
					Object result = conditionExpression.eval(v);
					return result != null && result instanceof Boolean && (Boolean) result;
				};
			} else {
				valueExpression = parser.parse(comment);
				conditionFunction = (v) -> {
					boolean isAllSupplied = true;
					for (String variableName : valueExpression.getVariableNames()) {
						if (v.getValue(variableName) == null) {
							isAllSupplied = false;
							break;
						}
					}
					return isAllSupplied;
				};
			}

			variableNames.addAll(valueExpression.getVariableNames());

			return new Result() {
				@Override
				public List<CompiledQueryPart> getCompiledParts() {
					return Collections.singletonList(
							new CompiledQueryOneParameterPart(sql, conditionFunction, valueExpression, variableNames));
				}
			};
		}

	}

	protected static class CompiledQueryOneParameterPart implements CompiledQueryPart {

		private String sql;
		private Function<ParamsResolver, Boolean> conditionFunction;
		private Expression valueExpression;
		private List<String> variableNames;

		public CompiledQueryOneParameterPart(String sql, Function<ParamsResolver, Boolean> conditionFunction,
				Expression valueExpression, List<String> variableNames) {
			this.sql = sql;
			this.conditionFunction = conditionFunction;
			this.valueExpression = valueExpression;
			this.variableNames = Collections.unmodifiableList(variableNames);
		}

		@Override
		public void eval(ParamsResolver variableResolver, ResultConsumer resultConsumer) {

			if (conditionFunction.apply(variableResolver)) {
				Object value = valueExpression.eval(variableResolver);
				resultConsumer.consume(sql, Collections.singletonList(value));
			}
		}

		@Override
		public List<String> getVariableNames() {
			return variableNames;
		}
	}
}
