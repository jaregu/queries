package com.jaregu.database.queries.compiling;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public interface CompiledQuery {

	QueryId getQueryId();

	List<CompiledQueryPart> getParts();
}
