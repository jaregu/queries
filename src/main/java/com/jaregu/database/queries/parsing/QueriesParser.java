package com.jaregu.database.queries.parsing;

@FunctionalInterface
public interface QueriesParser {

	SourceQueries parse(QueriesSource source);

	public static QueriesParser createDefault() {
		return new QueriesParserImpl();
	}
}
