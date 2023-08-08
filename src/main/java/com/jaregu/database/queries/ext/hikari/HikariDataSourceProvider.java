package com.jaregu.database.queries.ext.hikari;

import java.lang.invoke.MethodHandles;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
class HikariDataSourceProvider implements Provider<DataSource> {

	final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private HikariIntegration configuration;

	@Inject
	protected HikariDataSourceProvider(HikariIntegration configuration) {
		this.configuration = configuration;
	}

	@Override
	public DataSource get() {
		LOGGER.debug("Configuring HikariCP");
		HikariDataSource dataSource = new HikariDataSource(configuration.getConfig());
		LOGGER.debug("HikariCP Configured");

		configuration.registerShudownHook(() -> {
			dataSource.close();
		});
		return dataSource;
	}
}
