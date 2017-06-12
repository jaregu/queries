package com.jaregu.database.queries.compiling;

import java.util.List;

import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.CommentType;
import com.jaregu.database.queries.parsing.SourceQueryPart;

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
public class QueryCompilerHyphenCommentParameterFeature extends QueryCompilerCommentParameterBaseFeature {

	@Override
	public boolean isCompilable(Source source) {
		CompilingContext context = CompilingContext.getCurrent();
		List<SourceQueryPart> parts = source.getParts();
		if (parts.size() == 2) {
			SourceQueryPart sql = parts.get(0);
			SourceQueryPart comment = parts.get(1);

			return !sql.isComment() && comment.isComment() && comment.getCommentType() == CommentType.HYPHENS
					&& isLikeExpression(context.getExpressionParser(), comment.getCommentContent());
		} else {
			return false;
		}
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<SourceQueryPart> sourceParts = source.getParts();

		SourceQueryPart sql = sourceParts.get(0);
		SourceQueryPart comment = sourceParts.get(1);

		return new PartBuilder().setArgumentSql(sql.getContent()).setComment(comment.getCommentContent())
				.setAfterSql(comment.getContent()).buildResult();
	}
}
