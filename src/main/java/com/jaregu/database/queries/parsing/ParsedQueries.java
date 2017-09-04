package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public interface ParsedQueries extends Iterable<ParsedQuery> {

	SourceId getSourceId();

	ParsedQuery get(QueryId id);

	List<ParsedQuery> getQueries();
}
