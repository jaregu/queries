package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Ignores all SQL comments starting with
 * 
 * <pre>
 * ---
 * </pre>
 * 
 * or
 * 
 * <pre>
 * /**
 * </pre>
 * 
 * and adds them as constant SQLs
 */
public class IgnoredCommentFeature implements QueryCompilerFeature {

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		if (parts.size() == 1) {
			ParsedQueryPart part = parts.get(0);
			return part.isComment() && (part.getContent().startsWith("---") || part.getContent().startsWith("/**"));
		} else {
			return false;
		}
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(PreparedQueryPart.constant(source.getParts().get(0).getContent()));
			}
		};
	}
}
