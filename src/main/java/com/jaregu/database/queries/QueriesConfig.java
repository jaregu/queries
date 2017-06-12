package com.jaregu.database.queries;

public interface QueriesConfig {

	boolean isOriginalArgumentCommented();

	static QueriesConfig getDefault() {
		return QueriesConfigImpl.getDefault();
	}
}
