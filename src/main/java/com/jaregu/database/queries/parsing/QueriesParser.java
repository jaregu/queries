package com.jaregu.database.queries.parsing;

import com.jaregu.database.queries.QueriesConfig;

@FunctionalInterface
public interface QueriesParser {

	ParsedQueries parse(QueriesSource source);

	public static QueriesParser of(QueriesConfig config) {
		return new QueriesParserImpl(config);
	}
}
