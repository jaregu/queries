package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Matches parameter line
 * <p>
 * Example:
 * 
 * <pre>
 * ...
 *  AND t.SOME_FIELD = 123 -- <i>value_expression[; conditional_expression]</i>
 * ...
 * </pre>
 * 
 * Expression is something for what
 * {@link ExpressionParser#isLikeExpression(String)} is true
 * 
 * <p>
 * <i>value_expression</i> will be value for binded SQL parameter
 * <p>
 * <i>conditional_expression</i> is testable condition when this row will be
 * added to SQL, default condition is <i>conditional_expression != null</i>
 */
public class OptionalHyphenNamedParameterFeature extends OptionalNamedParameterFeatureBase {

	private final static List<Function<ParsedQueryPart, Boolean>> _const_comment_ = Arrays
			.asList(IS_SQL_WITHOUT_NEWLINE, IS_HYPHEN_COMMENT_EXPRESSION);
	private final static List<Function<ParsedQueryPart, Boolean>> _question_comment_ = Arrays
			.asList(IS_ANONYMOUS_VARIABLE, IS_HYPHEN_COMMENT_EXPRESSION);
	private final static List<Function<ParsedQueryPart, Boolean>> _question_sql_comment_ = Arrays
			.asList(IS_ANONYMOUS_VARIABLE, IS_SQL_WITHOUT_NEWLINE, IS_HYPHEN_COMMENT_EXPRESSION);
	private final static List<Function<ParsedQueryPart, Boolean>> _sql_question_comment_ = Arrays
			.asList(IS_SQL_WITHOUT_NEWLINE, IS_ANONYMOUS_VARIABLE, IS_HYPHEN_COMMENT_EXPRESSION);
	private final static List<Function<ParsedQueryPart, Boolean>> _sql_question_sql_comment_ = Arrays.asList(
			IS_SQL_WITHOUT_NEWLINE, IS_ANONYMOUS_VARIABLE, IS_SQL_WITHOUT_NEWLINE, IS_HYPHEN_COMMENT_EXPRESSION);

	@Override
	public boolean isCompilable(Source source) {
		return isPartsLike(source, _const_comment_) || isPartsLike(source, _question_comment_)
				|| isPartsLike(source, _question_sql_comment_) || isPartsLike(source, _sql_question_comment_)
				|| isPartsLike(source, _sql_question_sql_comment_);
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		Builder builder;
		if (isPartsLike(source, _const_comment_)) {
			builder = new Builder(source).before(0, true).after(1).comment(1);
		} else if (isPartsLike(source, _question_comment_)) {
			builder = new Builder(source).after(1).comment(1);
		} else if (isPartsLike(source, _question_sql_comment_)) {
			builder = new Builder(source).after(1, 2).comment(2);
		} else if (isPartsLike(source, _sql_question_comment_)) {
			builder = new Builder(source).before(0).after(2).comment(2);
		} else if (isPartsLike(source, _sql_question_sql_comment_)) {
			builder = new Builder(source).before(0).after(2, 3).comment(3);
		} else {
			throw new QueryCompileException("Wrong parts count!");
		}
		return builder.build();
	}
}
