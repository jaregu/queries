package com.jaregu.database.queries;

import com.jaregu.database.queries.compiling.PreparedQuery;

public interface QueriesFinder<T> {

	PreparedQuery get(T id);
}