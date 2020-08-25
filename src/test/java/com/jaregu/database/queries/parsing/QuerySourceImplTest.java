package com.jaregu.database.queries.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.dialect.Dialects;

@RunWith(MockitoJUnitRunner.class)
public class QuerySourceImplTest {

	@Mock
	private QueriesConfig config;

	@Test
	public void testSimpleLoad() throws Exception {
		when(config.getDialect()).thenReturn(Dialects.defaultDialect());
		QueriesSource source = QueriesSource.ofClass(AAA.class);
		assertThat(source.readContent(config)).isEqualTo("aaa");
	}

	@Test
	public void testDialectWithoutFile() throws Exception {
		when(config.getDialect()).thenReturn(Dialects.mariaDB());
		QueriesSource source = QueriesSource.ofClass(AAA.class);
		assertThat(source.readContent(config)).isEqualTo("aaa");
	}

	@Test
	public void testDefaultWithDialectFile() throws Exception {
		when(config.getDialect()).thenReturn(Dialects.defaultDialect());
		QueriesSource source = QueriesSource.ofClass(BBB.class);
		assertThat(source.readContent(config)).isEqualTo("bbb");
	}

	@Test
	public void testDialectWithDialectFile() throws Exception {
		when(config.getDialect()).thenReturn(Dialects.mariaDB());
		QueriesSource source = QueriesSource.ofClass(BBB.class);
		assertThat(source.readContent(config)).isEqualTo("bbb.mariadb");
	}

	public static class AAA {
	}

	public static class BBB {
	}
}
