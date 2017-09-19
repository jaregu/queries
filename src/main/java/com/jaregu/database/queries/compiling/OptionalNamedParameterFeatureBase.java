package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.jaregu.database.queries.QueriesContext;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParameterBindingBuilder;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.Expression.ExpressionResult;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.CommentType;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

public abstract class OptionalNamedParameterFeatureBase implements QueryCompilerFeature {

	protected static final Function<ParsedQueryPart, Boolean> IS_SQL_WITHOUT_NEWLINE = (p) -> p.isSimplePart()
			&& !p.getContent().contains("\n");

	protected static final Function<ParsedQueryPart, Boolean> IS_SQL = (p) -> p.isSimplePart();

	protected static final Function<ParsedQueryPart, Boolean> IS_ANONYMOUS_VARIABLE = (p) -> p.isAnonymousVariable();

	protected static final Function<ParsedQueryPart, Boolean> IS_HYPHEN_COMMENT_EXPRESSION = (p) -> {
		ExpressionParser parser = QueriesContext.getCurrent().getConfig().getExpressionParser();
		return p.isComment() && p.getCommentType() == CommentType.HYPHENS
				&& parser.isLikeExpression(p.getCommentContent());

	};

	protected static final Function<ParsedQueryPart, Boolean> IS_SLASH_COMMENT_EXPRESSION = (p) -> {
		ExpressionParser parser = QueriesContext.getCurrent().getConfig().getExpressionParser();
		return p.isComment() && p.getCommentType() == CommentType.SLASH_AND_ASTERISK
				&& parser.isLikeExpression(p.getCommentContent());

	};

	protected final boolean isPartsLike(Source source, List<Function<ParsedQueryPart, Boolean>> matchers) {
		List<ParsedQueryPart> parts = source.getParts();
		boolean matching = true;
		if (parts.size() == matchers.size()) {
			for (int i = 0; i < matchers.size(); i++) {
				Function<ParsedQueryPart, Boolean> function = matchers.get(i);
				ParsedQueryPart part = parts.get(i);
				matching = matching && function.apply(part);
				if (!matching) {
					break;
				}
			}
		} else {
			matching = false;
		}
		return matching;
	}

	protected class Builder {

		private Source source;
		private Optional<Integer> beforePartIndex;
		private boolean replaceLastConstant;
		private List<Integer> afterPartIndex = Collections.emptyList();
		private int commentPartIndex;

		public Builder(Source source) {
			this.source = source;

		}

		public Builder before(int beforePartIndex) {
			return before(beforePartIndex, false);
		}

		public Builder before(int beforePartIndex, boolean replaceLastConstant) {
			this.beforePartIndex = Optional.of(beforePartIndex);
			this.replaceLastConstant = replaceLastConstant;
			return this;
		}

		public Builder after(Integer... afterPartIndexes) {
			this.afterPartIndex = Arrays.asList(afterPartIndexes);
			return this;
		}

		public Builder comment(int commentPartIndex) {
			this.commentPartIndex = commentPartIndex;
			return this;
		}

		public Result build() {
			List<ParsedQueryPart> parts = source.getParts();

			StringBuilder beforeSql = new StringBuilder();
			StringBuilder afterSql = new StringBuilder();
			if (replaceLastConstant) {
				ArgumentSplitter.Result splitResult = ArgumentSplitter.of(parts.get(beforePartIndex.get()).getContent())
						.split();
				beforeSql.append(splitResult.getBeforeSql());
				afterSql.append(CommentType.SLASH_AND_ASTERISK.wrap(splitResult.getArgumentSql()))
						.append(splitResult.getAfterSql());
			} else {
				beforePartIndex.map(parts::get).map(ParsedQueryPart::getContent).ifPresent(beforeSql::append);
			}
			afterPartIndex.stream().map(parts::get).map(ParsedQueryPart::getContent).forEach(afterSql::append);
			List<Expression> expressions = getExpressions();

			ParameterBindingBuilder bindingBuilder = QueriesContext.getCurrent().getConfig()
					.getParameterBindingBuilder();

			return new Result() {
				@Override
				public List<PreparedQueryPart> getParts() {
					return Collections.singletonList(new CompiledQueryOneParameterPart(beforeSql.toString(),
							afterSql.toString(), expressions, bindingBuilder));
				}
			};
		}

		private List<Expression> getExpressions() {
			QueriesContext context = QueriesContext.getCurrent();
			List<Expression> expressions = context.getConfig().getExpressionParser()
					.parse(source.getParts().get(commentPartIndex).getCommentContent());
			return expressions;
		}

	}

	protected static class CompiledQueryOneParameterPart implements PreparedQueryPart {

		private String beforeSql;
		private String afterSql;
		private Expression valueExpression;
		private Optional<Expression> conditionalExpression;
		private ParameterBindingBuilder bindingBuilder;

		public CompiledQueryOneParameterPart(String beforeSql, String afterSql, List<Expression> expressions,
				ParameterBindingBuilder bindingBuilder) {
			this.beforeSql = beforeSql;
			this.afterSql = afterSql;
			this.valueExpression = expressions.get(0);
			this.conditionalExpression = expressions.size() == 1 ? Optional.empty() : Optional.of(expressions.get(1));
			this.bindingBuilder = bindingBuilder;
		}

		@Override
		public Result build(ParametersResolver resolver) {
			if (addCriterionLine(resolver)) {
				ExpressionResult expressionResult = valueExpression.eval(resolver);
				Object value = expressionResult.getReturnValue();
				ParameterBindingBuilder.Result variableBindingResult = bindingBuilder.process(value);
				String sql = beforeSql + variableBindingResult.getSql() + afterSql;
				List<Object> parameters = variableBindingResult.getParemeters();
				return new PreparedQueryPartResultImpl(Optional.of(sql), parameters,
						expressionResult.getOutputVariables());
			} else {
				return PreparedQueryPart.EMPTY;
			}
		}

		private boolean addCriterionLine(ParametersResolver resolver) {
			if (conditionalExpression.isPresent()) {
				Object conditionResult = conditionalExpression.get().eval(resolver).getReturnValue();
				if (conditionResult == null || !(conditionResult instanceof Boolean)) {
					throw new QueryBuildException("Can't build SQL optional criterion (" + beforeSql + "?" + afterSql
							+ "), condition expression result is not boolean: " + conditionResult
							+ " evaluated expression: " + conditionalExpression);
				}
				return (Boolean) conditionResult;
			} else {
				NamedResolver namedResolver = resolver.getNamedResolver();
				List<String> usedVariables = valueExpression.getVariableNames();
				if (usedVariables.size() == 1) {
					String variableName = usedVariables.get(0);
					Object value = namedResolver.getValue(variableName);
					if (value == null || ((value instanceof Collection<?>) && ((Collection<?>) value).isEmpty())) {
						return false;
					}
				} else {
					for (String variableName : usedVariables) {
						if (namedResolver.getValue(variableName) == null) {
							return false;
						}
					}
				}
				return true;
			}
		}
	}
}
