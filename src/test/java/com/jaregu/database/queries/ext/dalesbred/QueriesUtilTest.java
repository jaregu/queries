package com.jaregu.database.queries.ext.dalesbred;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.dalesbred.datatype.SqlArray;
import org.dalesbred.query.SqlQuery;
import org.junit.Test;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;
import com.jaregu.database.queries.dialect.Dialects;

public class QueriesUtilTest {

	@Test
	public void testUUIDArrayMapping() {

		UUID uuid1 = UUID.randomUUID();
		UUID uuid21 = UUID.randomUUID();
		UUID uuid22 = UUID.randomUUID();

		List<Serializable> args = Arrays.asList("some string", uuid1, new UUID[] { uuid21, uuid22 });
		SqlQuery sqlQuery = QueriesUtil.toQuery(createQuery("this is SQL query", args));

		assertThat(sqlQuery.getSql()).isEqualTo("this is SQL query");
		assertThat(sqlQuery.getArguments().get(0)).isEqualTo("some string");
		assertThat(sqlQuery.getArguments().get(1)).isEqualTo(uuid1);
		assertThat(sqlQuery.getArguments().get(2)).isInstanceOf(SqlArray.class);
		@SuppressWarnings("unchecked")
		List<Object> values = (List<Object>) ((SqlArray) sqlQuery.getArguments().get(2)).getValues();
		assertThat(values).containsExactly((Object) uuid21, (Object) uuid22);
	}

	private Query createQuery(String sql, List<?> parameters) {
		return new QueryImpl(sql, parameters, Collections.emptyMap(), Dialects.defaultDialect());
	}
}
