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

class QueriesParserImpl implements QueriesParser {

	private static final LexerPattern HYPEN_COMMENT = Lexer.newPattern().stopAfter("\n").stopAtEof();
	private static final LexerPattern BLOCK_COMMENT = Lexer.newPattern().stopAfter("*/");
	private static final LexerPattern NAMED_VARIABLE = Lexer.newPattern()
			.stopAfter(Lexer.regexp(Pattern.compile(":(\\w+\\.)*\\w+")));
	private static final LexerPattern ANONYMOUS_VARIABLE = Lexer.newPattern().stopAfter("?");
	private static final LexerPattern SQL_PART = Lexer.newPattern().skipAllBetween("'", "'").skipAllBetween("`", "`")
			.skipAllBetween("\"", "\"").skipSequence("::").stopAfterAnyOf(";").stopBeforeAnyOf("--", "/*", ":", "?")
			.stopAtEof();
	private static final StringSplitter BREAK_TO_LINES = StringSplitter.on('\n').includeSeparator(true);

	QueriesParserImpl() {
	}

	@Override
	public ParsedQueries parse(QueriesSource source) {
		List<String> parts = splitSource(source.getContent());
		List<List<ParsedQueryPart>> queries = groupQueries(parts);
		List<ParsedQuery> sourceQueries = queries.stream().map(q -> createSourceQuery(source.getId(), q))
				.collect(Collectors.toList());
		return new ParsedQueriesImpl(source.getId(), sourceQueries);
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
			} else if (lx.lookingAt("?")) {
				parts.add(lx.read(ANONYMOUS_VARIABLE));
			} else {
				// MME it is close to 10 times faster to split outside lexer
				String chunk = lx.read(SQL_PART);
				parts.addAll(BREAK_TO_LINES.split(chunk));
			}
		}
		return parts;
	}

	private List<List<ParsedQueryPart>> groupQueries(List<String> parts) {
		List<List<ParsedQueryPart>> queries = new LinkedList<>();
		List<ParsedQueryPart> currentQuery = new LinkedList<>();
		for (String part : parts) {
			// boolean endingPart = && part.endsWith(";");
			// endingPart ? part.substring(0, part.length() - 1) :
			ParsedQueryPart queryPart = ParsedQueryPart.create(part);
			if (!queryPart.isComment() && queryPart.getContent().endsWith(";")) {
				currentQuery.add(ParsedQueryPart.create(part.substring(0, part.indexOf(";"))));
				queries.add(currentQuery);
				currentQuery = new LinkedList<>();
			} else {
				currentQuery.add(queryPart);
			}
		}
		if (!currentQuery.isEmpty() && currentQuery.stream().anyMatch(s -> s.getContent().trim().length() > 0)) {
			queries.add(currentQuery);
		}
		return queries;
	}

	private ParsedQueryImpl createSourceQuery(SourceId sourceId, List<ParsedQueryPart> parts) {
		List<ParsedQueryPart> queryParts = new ArrayList<>(parts.size() + 1);
		QueryId queryId = null;
		for (ParsedQueryPart part : parts) {
			if (queryId == null && part.isComment()) {
				queryId = QueryId.of(sourceId, part.getCommentContent());
				queryParts.add(ParsedQueryPart.create(part.getCommentType().wrap(" QUERY ID: " + queryId)));
			} else {
				queryParts.add(part);
			}
		}

		if (queryId == null) {
			throw new QueryParseException(
					"Can't get query ID, there is no comments in this SQL query, query source id (" + sourceId + ")!\n"
							+ parts);
		}

		return new ParsedQueryImpl(queryId, queryParts);
	}
}
