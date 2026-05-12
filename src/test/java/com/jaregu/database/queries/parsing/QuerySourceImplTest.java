package com.jaregu.database.queries.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.dialect.Dialects;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
