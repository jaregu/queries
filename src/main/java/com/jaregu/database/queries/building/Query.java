package com.jaregu.database.queries.building;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public interface Query {

	QueryId getQueryId();

	String getSql();

	List<Object> getParameters();
}
