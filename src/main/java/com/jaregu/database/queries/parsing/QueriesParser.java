package com.jaregu.database.queries.parsing;

@FunctionalInterface
public interface QueriesParser {

	ParsedQueries parse(QueriesSource source);

	public static QueriesParser createDefault() {
		return new QueriesParserImpl();
	}
}
