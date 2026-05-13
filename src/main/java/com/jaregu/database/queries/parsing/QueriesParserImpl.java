package com.jaregu.database.queries.parsing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jaregu.database.queries.QueriesConfig;
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

	/**
	 * Matches a name-comment content with a trailing {@code (batch)} marker —
	 * e.g. {@code insert (batch)} or {@code do something else (BATCH)}. The
	 * first group captures the actual query name.
	 */
	private static final Pattern BATCH_MARKER = Pattern.compile("(.*?)\\s*\\(batch\\)\\s*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private final QueriesConfig config;

	QueriesParserImpl(QueriesConfig config) {
		this.config = config;
	}

	@Override
	public ParsedQueries parse(QueriesSource source) {
		List<String> parts = splitSource(source.readContent(config));
		List<GroupedQuery> queries = groupQueries(parts);
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

	private List<GroupedQuery> groupQueries(List<String> parts) {
		List<RawGroup> rawGroups = rawGroup(parts);
		return mergeBatchGroups(rawGroups);
	}

	private List<RawGroup> rawGroup(List<String> parts) {
		List<RawGroup> queries = new LinkedList<>();
		List<ParsedQueryPart> currentQuery = new LinkedList<>();
		for (String part : parts) {
			ParsedQueryPart queryPart = ParsedQueryPart.create(part);
			if (!queryPart.isComment() && queryPart.getContent().endsWith(";")) {
				currentQuery.add(ParsedQueryPart.create(part.substring(0, part.indexOf(";"))));
				queries.add(new RawGroup(currentQuery, true));
				currentQuery = new LinkedList<>();
			} else {
				currentQuery.add(queryPart);
			}
		}
		if (!currentQuery.isEmpty() && currentQuery.stream().anyMatch(s -> s.getContent().trim().length() > 0)) {
			queries.add(new RawGroup(currentQuery, false));
		}
		return queries;
	}

	private List<GroupedQuery> mergeBatchGroups(List<RawGroup> rawGroups) {
		List<GroupedQuery> result = new LinkedList<>();
		int i = 0;
		while (i < rawGroups.size()) {
			RawGroup group = rawGroups.get(i);
			if (!hasBatchMarker(group.parts)) {
				result.add(new GroupedQuery(group.parts, false));
				i++;
				continue;
			}
			List<ParsedQueryPart> merged = new LinkedList<>(group.parts);
			int j = i + 1;
			// Absorb following groups (re-inserting the ; that was stripped between
			// them) until we encounter a group whose first non-whitespace part is
			// a comment — that's the next query's name-comment.
			while (group.terminated && j < rawGroups.size() && !startsWithComment(rawGroups.get(j).parts)) {
				merged.add(ParsedQueryPart.create(";"));
				merged.addAll(rawGroups.get(j).parts);
				group = rawGroups.get(j);
				j++;
			}
			// If the batch group's final absorbed sub-group was ;-terminated,
			// preserve that terminator too.
			if (group.terminated) {
				merged.add(ParsedQueryPart.create(";"));
			}
			result.add(new GroupedQuery(merged, true));
			i = j;
		}
		return result;
	}

	private boolean hasBatchMarker(List<ParsedQueryPart> parts) {
		for (ParsedQueryPart p : parts) {
			if (p.isComment()) {
				return BATCH_MARKER.matcher(p.getCommentContent()).matches();
			}
		}
		return false;
	}

	private boolean startsWithComment(List<ParsedQueryPart> parts) {
		for (ParsedQueryPart p : parts) {
			if (p.isComment()) {
				return true;
			}
			if (p.getContent().trim().length() > 0) {
				return false;
			}
		}
		return false;
	}

	private ParsedQueryImpl createSourceQuery(SourceId sourceId, GroupedQuery group) {
		List<ParsedQueryPart> parts = group.parts;
		List<ParsedQueryPart> queryParts = new ArrayList<>(parts.size() + 1);
		QueryId queryId = null;
		for (ParsedQueryPart part : parts) {
			if (queryId == null && part.isComment()) {
				String commentContent = part.getCommentContent();
				String queryName = commentContent;
				Matcher matcher = BATCH_MARKER.matcher(commentContent);
				if (matcher.matches()) {
					queryName = matcher.group(1).trim();
				}
				queryId = QueryId.of(sourceId, queryName);
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

		return new ParsedQueryImpl(queryId, queryParts, group.isBatch);
	}

	/**
	 * Raw query group from the first parsing pass — same shape as the original
	 * {@code groupQueries} output, plus a flag tracking whether the group ended
	 * at a {@code ;} terminator (vs. running to EOF).
	 */
	private static final class RawGroup {
		final List<ParsedQueryPart> parts;
		final boolean terminated;

		RawGroup(List<ParsedQueryPart> parts, boolean terminated) {
			this.parts = parts;
			this.terminated = terminated;
		}
	}

	/**
	 * Post-batch-merge group; carries whether the resulting {@link ParsedQuery}
	 * should be flagged as a batch query.
	 */
	private static final class GroupedQuery {
		final List<ParsedQueryPart> parts;
		final boolean isBatch;

		GroupedQuery(List<ParsedQueryPart> parts, boolean isBatch) {
			this.parts = parts;
			this.isBatch = isBatch;
		}
	}
}
