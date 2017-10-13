package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Orderable;
import com.jaregu.database.queries.dialect.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class QueryImplTest {

	private QueryImpl query;

	@Mock
	private Dialect dialect;

	@Mock
	private Orderable orderable;

	@Mock
	private Pageable pageable;

	@Mock
	private Query mockQuery;

	private List<?> parameters = Arrays.asList(1, "2", null);

	private Map<String, ?> attributes = Collections.singletonMap("1", "first");

	@Before
	public void setUp() {
		query = new QueryImpl("content", parameters, attributes, dialect);

		when(dialect.toOrderedQuery(same(query), any())).thenReturn(mockQuery);
		when(dialect.toPagedQuery(same(query), any())).thenReturn(mockQuery);
		when(dialect.toCountQuery(same(query))).thenReturn(mockQuery);
	}

	@Test
	public void testGetters() {
		assertThat(query.getSql()).isEqualTo("content");
		assertThat(query.getParameters()).isEqualTo(parameters);
		assertThat(query.getAttributes()).isEqualTo(attributes);
	}

	@Test
	public void testUtilities() {
		assertThat(query.stream()).hasSize(1).first().isSameAs(query);
		assertThat(query.map(Function.identity())).isSameAs(query);
		AtomicReference<Query> ref = new AtomicReference<>();
		query.consume(q -> ref.set(q));
		assertThat(ref.get()).isSameAs(query);
	}

	@Test
	public void testToOrderedQuery() {
		assertThat(query.toOrderedQuery(orderable)).isSameAs(mockQuery);
		verify(dialect, times(1)).toOrderedQuery(query, orderable);
	}

	@Test
	public void testToPagedQuery() {
		assertThat(query.toPagedQuery(pageable)).isSameAs(mockQuery);
		verify(dialect, times(1)).toPagedQuery(query, pageable);
	}

	@Test
	public void testToCountQuery() {
		assertThat(query.toCountQuery()).isSameAs(mockQuery);
		verify(dialect, times(1)).toCountQuery(query);
	}

	@Test
	public void testEquals() {
		assertThat(query).isEqualTo(query);
		assertThat(query).isEqualTo(new QueryImpl("content", parameters, attributes, dialect));
		assertThat(query).isEqualTo(new QueryImpl("content", parameters, attributes, null));
		assertThat(query).isNotEqualTo(new QueryImpl("content1", parameters, attributes, dialect));
		assertThat(query).isNotEqualTo(new QueryImpl("content", Collections.emptyList(), attributes, dialect));
		assertThat(query).isNotEqualTo(new QueryImpl("content", parameters, Collections.emptyMap(), dialect));
	}

	@Test
	public void testHash() {
		assertThat(query.hashCode()).isEqualTo(query.hashCode());
		assertThat(query.hashCode()).isEqualTo(new QueryImpl("content", parameters, attributes, dialect).hashCode());
		assertThat(query.hashCode()).isEqualTo(new QueryImpl("content", parameters, attributes, null).hashCode());
		assertThat(query.hashCode())
				.isNotEqualTo(new QueryImpl("content1", parameters, attributes, dialect).hashCode());
		assertThat(query.hashCode())
				.isNotEqualTo(new QueryImpl("content", Collections.emptyList(), attributes, dialect).hashCode());
		assertThat(query.hashCode())
				.isNotEqualTo(new QueryImpl("content", parameters, Collections.emptyMap(), dialect).hashCode());
	}
}
