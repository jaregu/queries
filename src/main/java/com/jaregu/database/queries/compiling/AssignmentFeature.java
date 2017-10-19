package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.Expression.ExpressionResult;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Assignment feature
 * 
 * Example SQL:
 * 
 * <pre>
 * -- attr_name_1 = value_expression_1[; attr_name_2 = value_expression_2 ...]
 * </pre>
 */
final class AssignmentFeature implements QueryCompilerFeature {

	private final ExpressionParser expressionParser;

	AssignmentFeature(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		ParsedQueryPart part;
		return parts.size() == 1 && (part = parts.get(0)).isComment()
				&& expressionParser.isLikeExpression(part.getCommentContent());
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<Expression> expressions = expressionParser.parse(source.getParts().get(0).getCommentContent());

		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new AssignmentVariablePart(expressions));
			}
		};
	}

	private static final class AssignmentVariablePart implements PreparedQueryPart {

		private List<Expression> expressions;

		private AssignmentVariablePart(List<Expression> expressions) {
			this.expressions = expressions;
		}

		@Override
		public Result build(ParametersResolver resolver) {
			Map<String, Object> attributes = new LinkedHashMap<>();
			for (Expression expression : expressions) {
				ExpressionResult expressionResult = expression.eval(resolver);
				attributes.putAll(expressionResult.getOutputVariables());
			}
			return new PreparedQueryPartResultImpl(Optional.empty(), Collections.emptyList(), attributes);
		}

		@Override
		public String toString() {
			return "AssignmentVariablePart" + expressions;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(expressions);
		}
	}
}
