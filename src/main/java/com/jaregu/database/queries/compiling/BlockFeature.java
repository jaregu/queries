package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.jaregu.database.queries.QueriesContext;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Nest-able conditional SQL block feature. Will add conditionally everything
 * between two comments containing { }.
 * 
 * Example SQL:
 * 
 * <pre>
 * ...
 * /* <i>condition_expression</i> { *&#x2F;
 * [SQL, could contain other block and/or conditional parameters]
 * -- } Block ends here
 * 
 * </pre>
 */
public class BlockFeature implements QueryCompilerFeature {

	private static final String BLOCK_OPEN_SYMBOLS = "{";
	private static final String BLOCK_CLOSE_SYMBOLS = "}";

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return isAtLeastTwoParts(parts) && startsWithBlock(parts) && endsWithBlock(parts)
				&& isOpenedAndClosedBlockCountMatching(parts);
	}

	private boolean isAtLeastTwoParts(List<ParsedQueryPart> parts) {
		return parts.size() >= 2;
	}

	private boolean startsWithBlock(List<ParsedQueryPart> parts) {
		ParsedQueryPart testPart = parts.get(0);
		return isBlockStartPart(testPart);
	}

	private boolean isBlockStartPart(ParsedQueryPart testPart) {
		return testPart.isComment() && testPart.getCommentContent().endsWith(BLOCK_OPEN_SYMBOLS);
	}

	private boolean endsWithBlock(List<ParsedQueryPart> parts) {
		ParsedQueryPart testPart = parts.get(parts.size() - 1);
		return isBlockEndPart(testPart);
	}

	private boolean isBlockEndPart(ParsedQueryPart testPart) {
		return testPart.isComment() && testPart.getCommentContent().startsWith(BLOCK_CLOSE_SYMBOLS);
	}

	private boolean isOpenedAndClosedBlockCountMatching(List<ParsedQueryPart> parts) {
		int hierarhy = 0;
		for (ParsedQueryPart part : parts) {
			if (isBlockStartPart(part)) {
				hierarhy++;
			} else if (isBlockEndPart(part)) {
				hierarhy--;
			}
		}
		return hierarhy == 0;
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		QueriesContext context = QueriesContext.getCurrent();
		List<ParsedQueryPart> sourceParts = source.getParts();

		ParsedQueryPart openComment = sourceParts.get(0);
		String conditionExpression = openComment.getCommentContent()
				.substring(0, openComment.getCommentContent().length() - BLOCK_OPEN_SYMBOLS.length()).trim();

		List<PreparedQueryPart> children;
		if (sourceParts.size() > 2) {
			children = compiler.compile(Source.of(sourceParts.subList(1, sourceParts.size() - 1))).getParts();
		} else {
			children = Collections.emptyList();
		}

		Function<ParametersResolver, Boolean> conditionFunction;
		if (conditionExpression.length() > 0) {
			Expression expression = context.getConfig().getExpressionParser().parse(conditionExpression).get(0);
			conditionFunction = (v) -> {
				Object result = expression.eval(v).getReturnValue();
				if (result == null || !(result instanceof Boolean)) {
					throw new QueryBuildException(
							"Can't build SQL block feature, condition expression result is not boolean: " + expression);
				}
				return result != null && result instanceof Boolean && (Boolean) result;
			};
		} else {
			throw new QueryCompileException("Can't compile block feature, there is no condition!");
		}

		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new BlockPart(sourceParts.get(0).getContent(),
						sourceParts.get(sourceParts.size() - 1).getContent(), children, conditionFunction));
			}
		};
	}

	protected static class BlockPart implements PreparedQueryPart {

		final private String prefix;
		final private String suffix;
		final private List<PreparedQueryPart> children;
		final private Function<ParametersResolver, Boolean> conditionFunction;

		public BlockPart(String prefix, String suffix, List<PreparedQueryPart> children,
				Function<ParametersResolver, Boolean> conditionFunction) {
			this.prefix = prefix;
			this.suffix = suffix;
			this.children = children;
			this.conditionFunction = conditionFunction;
		}

		@Override
		public Result build(ParametersResolver resolver) {
			if (conditionFunction.apply(resolver)) {
				StringBuilder sql = new StringBuilder(prefix);
				List<Object> allParams = new LinkedList<>();
				Map<String, Object> allAttrs = new HashMap<>();
				for (PreparedQueryPart part : children) {
					Result result = part.build(resolver);
					result.getSql().ifPresent(sql::append);
					allParams.addAll(result.getParameters());
					allAttrs.putAll(result.getAttributes());
				}
				sql.append(suffix);
				return new PreparedQueryPartResultImpl(Optional.of(sql.toString()), allParams, allAttrs);
			} else {
				return PreparedQueryPart.EMPTY;
			}
		}
	}
}
