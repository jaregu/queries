package com.jaregu.database.queries.ext.guice;

import com.jaregu.database.queries.Queries;

@FunctionalInterface
public interface QueriesConfigurator {

	public void configure(Queries.Builder builder);
}
