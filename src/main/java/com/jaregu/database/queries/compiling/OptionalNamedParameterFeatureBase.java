package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.Expression.ExpressionResult;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.CommentType;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

public abstract class OptionalNamedParameterFeatureBase implements QueryCompilerFeature {

	private final ExpressionParser expressionParser;
	private final ParameterBinder parameterBinder;

	protected final Function<ParsedQueryPart, Boolean> isSqlWithoutNewLine = (p) -> p.isSimplePart()
			&& !p.getContent().contains("\n");

	protected final Function<ParsedQueryPart, Boolean> isSQL = (p) -> p.isSimplePart();

	protected final Function<ParsedQueryPart, Boolean> isAnonymousVariable = (p) -> p.isAnonymousVariable();

	protected final Function<ParsedQueryPart, Boolean> isHyphenCommentExpression = isHyphenCommentExpression();

	protected final Function<ParsedQueryPart, Boolean> isSlashCommentExpression = isSlashCommentExpression();

	OptionalNamedParameterFeatureBase(ExpressionParser expressionParser, ParameterBinder parameterBinder) {
		this.expressionParser = expressionParser;
		this.parameterBinder = parameterBinder;
	}

	private Function<ParsedQueryPart, Boolean> isHyphenCommentExpression() {
		return (p) -> {
			return p.isComment() && p.getCommentType() == CommentType.HYPHENS
					&& expressionParser.isLikeExpression(p.getCommentContent());

		};
	}

	private Function<ParsedQueryPart, Boolean> isSlashCommentExpression() {
		return (p) -> {
			return p.isComment() && p.getCommentType() == CommentType.SLASH_AND_ASTERISK
					&& expressionParser.isLikeExpression(p.getCommentContent());

		};
	}

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

			return new Result() {
				@Override
				public List<PreparedQueryPart> getParts() {
					return Collections.singletonList(new CompiledQueryOneParameterPart(beforeSql.toString(),
							afterSql.toString(), expressions, parameterBinder));
				}
			};
		}

		private List<Expression> getExpressions() {
			List<Expression> expressions = expressionParser
					.parse(source.getParts().get(commentPartIndex).getCommentContent());
			return expressions;
		}

	}

	private static final class CompiledQueryOneParameterPart implements PreparedQueryPart {

		private final String beforeSql;
		private final String afterSql;
		private final Expression valueExpression;
		private final Optional<Expression> conditionalExpression;
		private final ParameterBinder parameterBinder;

		public CompiledQueryOneParameterPart(String beforeSql, String afterSql, List<Expression> expressions,
				ParameterBinder parameterBinder) {
			this.beforeSql = beforeSql;
			this.afterSql = afterSql;
			this.valueExpression = expressions.get(0);
			this.conditionalExpression = expressions.size() == 1 ? Optional.empty() : Optional.of(expressions.get(1));
			this.parameterBinder = parameterBinder;
		}

		@Override
		public Result build(ParametersResolver resolver) {
			if (addCriterionLine(resolver)) {
				ExpressionResult expressionResult = valueExpression.eval(resolver);
				Object value = expressionResult.getReturnValue();
				ParameterBinder.Result variableBindingResult = parameterBinder.process(value);
				String sql = beforeSql + variableBindingResult.getSql() + afterSql;
				List<Object> parameters = variableBindingResult.getParameters();
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
				NamedResolver namedResolver = resolver.toNamed();
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

		@Override
		public String toString() {
			return beforeSql + " ? " + afterSql;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(beforeSql, afterSql);
		}
	}
}
