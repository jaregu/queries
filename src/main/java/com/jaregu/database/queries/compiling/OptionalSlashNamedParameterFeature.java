package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Matches parameter line
 * <p>
 * Example:
 * 
 * <pre>
 * ...
 *  AND t.SOME_FIELD = 123 /* <i>value_expression[; conditional_expression]</i> *&#x2F; [some additional SQL]
 * ...
 * </pre>
 * 
 * Expression is something for what
 * {@link ExpressionParser#isLikeExpression(String)} is true
 */
final class OptionalSlashNamedParameterFeature extends OptionalNamedParameterFeatureBase {

	private final List<Function<ParsedQueryPart, Boolean>> _const_comment_sql_ = Arrays.asList(isSqlWithoutNewLine,
			isSlashCommentExpression, isSQL);
	private final List<Function<ParsedQueryPart, Boolean>> _question_comment_sql_ = Arrays.asList(isAnonymousVariable,
			isSlashCommentExpression, isSQL);
	private final List<Function<ParsedQueryPart, Boolean>> _question_sql_comment_sql_ = Arrays
			.asList(isAnonymousVariable, isSqlWithoutNewLine, isSlashCommentExpression, isSQL);
	private final List<Function<ParsedQueryPart, Boolean>> _sql_question_comment_sql_ = Arrays
			.asList(isSqlWithoutNewLine, isAnonymousVariable, isSlashCommentExpression, isSQL);
	private final List<Function<ParsedQueryPart, Boolean>> _sql_question_sql_comment_sql_ = Arrays
			.asList(isSqlWithoutNewLine, isAnonymousVariable, isSqlWithoutNewLine, isSlashCommentExpression, isSQL);

	OptionalSlashNamedParameterFeature(ExpressionParser expressionParser, ParameterBinder parameterBinder) {
		super(expressionParser, parameterBinder);
	}

	@Override
	public boolean isCompilable(Source source) {
		return isPartsLike(source, _const_comment_sql_) || isPartsLike(source, _question_comment_sql_)
				|| isPartsLike(source, _question_sql_comment_sql_) || isPartsLike(source, _sql_question_comment_sql_)
				|| isPartsLike(source, _sql_question_sql_comment_sql_);
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		Builder builder;
		if (isPartsLike(source, _const_comment_sql_)) {
			builder = new Builder(source).before(0, true).after(1, 2).comment(1);
		} else if (isPartsLike(source, _question_comment_sql_)) {
			builder = new Builder(source).after(1, 2).comment(1);
		} else if (isPartsLike(source, _question_sql_comment_sql_)) {
			builder = new Builder(source).after(1, 2, 3).comment(2);
		} else if (isPartsLike(source, _sql_question_comment_sql_)) {
			builder = new Builder(source).before(0).after(2, 3).comment(2);
		} else if (isPartsLike(source, _sql_question_sql_comment_sql_)) {
			builder = new Builder(source).before(0).after(2, 3, 4).comment(3);
		} else {
			throw new QueryCompileException("Wrong parts count!");
		}
		return builder.build();
	}
}
