package com.jaregu.database.queries;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.proxy.QueryConverterFactory;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

@RunWith(MockitoJUnitRunner.class)
public class QueriesConfigImplTest {

	private QueriesConfigImpl config;

	@Mock
	private Dialect dialect;

	@Mock
	private ParameterBinder binder;

	@Mock
	private QueryMapperFactory mapper1;

	@Mock
	private QueryMapperFactory mapper2;

	private Map<Class<? extends Annotation>, QueryMapperFactory> mappers;

	@Mock
	private QueryConverterFactory converter1;

	@Mock
	private QueryConverterFactory converter2;

	private Map<Class<? extends Annotation>, QueryConverterFactory> converters;

	private Class<String> entity1Class = String.class;

	private Class<Number> entity2Class = Number.class;

	private Map<String, Class<?>> entities;

	@Before
	public void setUp() {
		mappers = new HashMap<>();
		mappers.put(Retention.class, mapper1);
		mappers.put(Target.class, mapper2);

		converters = new HashMap<>();
		converters.put(Retention.class, converter1);
		converters.put(Target.class, converter2);

		entities = new HashMap<>();
		entities.put("aaa", entity1Class);
		entities.put("bbb", entity2Class);

		config = new QueriesConfigImpl(dialect, binder, mappers, converters, entities);
	}

	@Test
	public void testGetters() {
		testGetters(config);
	}

	public void testGetters(QueriesConfig config) {
		assertThat(config.getDialect()).isSameAs(dialect);
		assertThat(config.getParameterBinder()).isSameAs(binder);
		assertThat(config.getQueryMapperFactories().values().stream().map(v -> (QueryMapperFactory) v))
				.containsOnly(mapper1, mapper2);
		assertThat(config.getQueryMapperFactories()).containsAllEntriesOf(mappers);
		assertThat(config.getQueryConverterFactories()).containsAllEntriesOf(converters);
		assertThat(config.getEntities()).containsAllEntriesOf(entities);
	}
}
