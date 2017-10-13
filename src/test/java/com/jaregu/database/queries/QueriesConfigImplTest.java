package com.jaregu.database.queries;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

import groovy.lang.DelegatesTo.Target;

@RunWith(MockitoJUnitRunner.class)
public class QueriesConfigImplTest {

	private QueriesConfigImpl config;

	@Mock
	private Dialect dialect;

	@Mock
	private ParameterBinder binder;

	@Mock
	private QueryMapperFactory factory1;

	@Mock
	private QueryMapperFactory factory2;

	private Map<Class<? extends Annotation>, QueryMapperFactory> factories;

	@Before
	public void setUp() {
		factories = new HashMap<>();
		factories.put(Retention.class, factory1);
		factories.put(Target.class, factory2);
		config = new QueriesConfigImpl(dialect, binder, factories);
	}

	@Test
	public void testGetters() {
		testGetters(config);
	}

	public void testGetters(QueriesConfig config) {
		assertThat(config.getDialect()).isSameAs(dialect);
		assertThat(config.getParameterBinder()).isSameAs(binder);
		assertThat(config.getQueryMapperFactories().values().stream().map(v -> (QueryMapperFactory) v))
				.containsOnly(factory1, factory2);
		assertThat(config.getQueryMapperFactories()).containsAllEntriesOf(factories);
	}
}
