package com.jaregu.database.queries.building;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.compiling.CompiledQueryPart;
import com.jaregu.database.queries.compiling.CompiledQueryPart.ResultConsumer;

@RunWith(MockitoJUnitRunner.class)
public class QueryBuilderImplTest {

	private QueryBuilderImpl builder = new QueryBuilderImpl();

	private QueryId queryId = QueryId.of("aaa.bbb");

	@Mock
	private CompiledQuery compiledQuery;

	@Mock
	private CompiledQueryPart part1;

	@Mock
	private CompiledQueryPart part2;

	@Mock
	private CompiledQueryPart part3;

	@Mock
	private ParamsResolver resolver;

	@Test
	public void testBuild() throws Exception {
		when(compiledQuery.getQueryId()).thenReturn(queryId);
		when(compiledQuery.getParts()).thenReturn(Arrays.asList(part1, part2, part3));

		doAnswer(i -> {
			ResultConsumer consumer = (ResultConsumer) i.getArgument(1);
			consumer.consume("AAA", Arrays.asList("1", "2"));
			return null;
		}).when(part1).eval(same(resolver), any());

		doAnswer(i -> {
			return null;
		}).when(part2).eval(same(resolver), any());

		doAnswer(i -> {
			ResultConsumer consumer = (ResultConsumer) i.getArgument(1);
			consumer.consume("BBB", Arrays.asList(1, 2));
			return null;
		}).when(part3).eval(same(resolver), any());

		Query query = builder.build(compiledQuery, resolver);

		assertEquals(queryId, query.getQueryId());
		assertEquals("AAABBB", query.getSql());
		assertEquals(Arrays.asList("1", "2", 1, 2), query.getParameters());
	}
}
