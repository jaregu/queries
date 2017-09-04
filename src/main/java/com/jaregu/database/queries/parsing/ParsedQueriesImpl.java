package com.jaregu.database.queries.parsing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public class ParsedQueriesImpl implements ParsedQueries {

	private SourceId sourceId;
	private List<ParsedQuery> sources;
	private Map<QueryId, ParsedQuery> queries;

	public ParsedQueriesImpl(SourceId sourceId, List<ParsedQuery> sources) {
		this.sourceId = sourceId;
		this.sources = Collections.unmodifiableList(sources);
		this.queries = sources.stream().collect(Collectors.toMap(ParsedQuery::getQueryId, Function.identity()));
	}

	@Override
	public SourceId getSourceId() {
		return sourceId;
	}

	@Override
	public Iterator<ParsedQuery> iterator() {
		return sources.iterator();
	}

	@Override
	public ParsedQuery get(QueryId id) {
		ParsedQuery query;
		if ((query = queries.get(id)) == null) {
			throw new QueriesParseException("Uknown query Id: " + id);
		}
		return query;
	}

	@Override
	public List<ParsedQuery> getQueries() {
		return sources;
	}

	@Override
	public String toString() {
		return "SourceQueries" + sources;
	}
}
