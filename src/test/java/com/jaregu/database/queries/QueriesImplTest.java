package com.jaregu.database.queries;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;

public class QueriesImplTest {

	private static final MetricRegistry metrics = new MetricRegistry();
	private static final Timer responses = metrics.timer(name(QueriesImplTest.class, "all"));
	private static final Timer query = metrics.timer(name(QueriesImplTest.class, "one-query"));

	private QueriesImpl queries; // = new QueriesImpl();

	@Test
	public void testName() throws Exception {

	}

	public static void main(String[] args) throws InterruptedException {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MICROSECONDS).build();
		// reporter.start(1, TimeUnit.SECONDS);

		Queries queries = Queries.ofSources(QueriesSources
				.of(Arrays.asList(QueriesSource.ofResource("com/jaregu/database/queries/AppUsersDAO.sql"))));
		final Timer.Context context = responses.time();
		for (int i = 0; i < 100000; i++) {
			try (Timer.Context oneq = query.time()) {
				Map<String, Object> params = new HashMap<>();
				params.put("userid", 1234l);
				queries.get(QueryId.of("AppUsersDAO.some")).build(params);
			}
		}
		context.stop();
		reporter.report();
	}
}
