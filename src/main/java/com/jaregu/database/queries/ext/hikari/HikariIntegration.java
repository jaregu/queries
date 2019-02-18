package com.jaregu.database.queries.ext.hikari;

import com.zaxxer.hikari.HikariConfig;

public interface HikariIntegration {

	public HikariConfig getConfig();

	void registerShudownHook(ShutdownHook hook);

	@FunctionalInterface
	static interface ShutdownHook {

		void shutDown();
	}
}
