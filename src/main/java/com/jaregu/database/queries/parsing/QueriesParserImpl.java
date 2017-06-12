package com.jaregu.database.queries.parsing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.common.Lexer;
import com.jaregu.database.queries.common.StringSplitter;
import com.jaregu.database.queries.common.Lexer.LexerPattern;

public class QueriesParserImpl implements QueriesParser {

	private static final LexerPattern HYPEN_COMMENT = Lexer.newPattern().stopAfter("\n").stopAtEof();
	private static final LexerPattern BLOCK_COMMENT = Lexer.newPattern().stopAfter("*/");
	private static final LexerPattern NAMED_VARIABLE = Lexer.newPattern()
			.stopAfter(Lexer.regexp(Pattern.compile(":(\\w+\\.)*\\w+")));
	private static final LexerPattern SQL_PART = Lexer.newPattern().skipAllBetween("'", "'").skipAllBetween("\"", "\"")
			.skipSequence("::").stopAfterAnyOf(";").stopBeforeAnyOf("--", "/*", ":").stopAtEof();
	private static final StringSplitter BREAK_TO_LINES = StringSplitter.on('\n').includeSeparator(true);

	@Override
	public SourceQueries parse(QueriesSource source) {
		List<String> parts = splitSource(source.getContent());
		List<List<SourceQueryPart>> queries = groupQueries(parts);
		List<SourceQuery> sourceQueries = queries.stream().map(q -> createSourceQuery(source.getSourceId(), q))
				.collect(Collectors.toList());
		return new SourceQueriesImpl(source.getSourceId(), sourceQueries);
	}

	private List<String> splitSource(String source) {
		List<String> parts = new ArrayList<>();
		Lexer lx = new Lexer(source);
		while (lx.hasMore()) {

			if (lx.lookingAt("--")) {
				parts.add(lx.read(HYPEN_COMMENT));
			} else if (lx.lookingAt("/*")) {
				parts.add(lx.read(BLOCK_COMMENT));
			} else if (lx.lookingAt(":")) {
				parts.add(lx.read(NAMED_VARIABLE));
			} else {
				//MME it is close to 10 times faster to split outside lexer
				String chunk = lx.read(SQL_PART);
				parts.addAll(BREAK_TO_LINES.split(chunk));
			}
		}
		return parts;
	}

	private List<List<SourceQueryPart>> groupQueries(List<String> parts) {
		List<List<SourceQueryPart>> queries = new LinkedList<>();
		List<SourceQueryPart> currentQuery = new LinkedList<>();
		for (String part : parts) {
			SourceQueryPart queryPart = SourceQueryPart.create(part);
			currentQuery.add(queryPart);
			if (!queryPart.isComment() && queryPart.getContent().contains(";")) {
				queries.add(currentQuery);

				currentQuery = new LinkedList<>();
			}
		}
		if (!currentQuery.isEmpty() && currentQuery.stream().anyMatch(s -> s.getContent().trim().length() > 0)) {
			queries.add(currentQuery);
		}
		return queries;
	}

	private SourceQueryImpl createSourceQuery(SourceId sourceId, List<SourceQueryPart> parts) {
		List<SourceQueryPart> queryParts = new ArrayList<>(parts.size() + 1);
		QueryId queryId = null;
		for (SourceQueryPart part : parts) {
			if (queryId == null && part.isComment()) {
				queryId = QueryId.of(sourceId, part.getCommentContent());
				queryParts.add(SourceQueryPart.create(part.getCommentType().wrap(" QUERY ID: " + queryId)));
			} else {
				queryParts.add(part);
			}
		}

		if (queryId == null) {
			throw new QueriesParseException(
					"Can't get query ID, there is no comments in this SQL query, query source id (" + sourceId + ")!\n"
							+ parts);
		}

		return new SourceQueryImpl(queryId, queryParts);
	}
}
