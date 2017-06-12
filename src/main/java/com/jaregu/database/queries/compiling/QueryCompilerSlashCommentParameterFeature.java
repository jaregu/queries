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
 *  AND t.SOME_FIELD = 123 /* <i>value_expression[; conditional_expression]</i> *&#x2F; [some additional SQL]
 * ...
 * </pre>
 * 
 * Expression is something for what
 * {@link ExpressionParser#isLikeExpression(String)} is true
 */
public class QueryCompilerSlashCommentParameterFeature extends QueryCompilerCommentParameterBaseFeature {

	@Override
	public boolean isCompilable(Source source) {
		CompilingContext context = CompilingContext.getCurrent();
		List<SourceQueryPart> parts = source.getParts();
		if (parts.size() == 3) {
			SourceQueryPart sql1 = parts.get(0);
			SourceQueryPart comment = parts.get(1);
			SourceQueryPart sql2 = parts.get(2);
			return !sql1.isComment() && comment.isComment()
					&& comment.getCommentType() == CommentType.SLASH_AND_ASTERISK && !sql2.isComment()
					&& isLikeExpression(context.getExpressionParser(), comment.getCommentContent());
		} else {
			return false;
		}
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<SourceQueryPart> sourceParts = source.getParts();

		SourceQueryPart beforeSql = sourceParts.get(0);
		SourceQueryPart comment = sourceParts.get(1);
		SourceQueryPart afterSql = sourceParts.get(2);

		return new PartBuilder().setArgumentSql(beforeSql.getContent())
				.setAfterSql(comment.getContent() + afterSql.getContent()).setComment(comment.getCommentContent())
				.buildResult();
	}
}
