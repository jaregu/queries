package com.jaregu.database.queries.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.jaregu.database.queries.QueryId;

public class SourceQueryImplTest {

	@Test
	public void testConstruction() throws Exception {
		QueryId someId = Mockito.mock(QueryId.class);
		SourceQueryPart part1 = Mockito.mock(SourceQueryPart.class);
		SourceQueryPart part2 = Mockito.mock(SourceQueryPart.class);
		SourceQueryPart part3 = Mockito.mock(SourceQueryPart.class);
		List<SourceQueryPart> parts = Arrays.asList(part1, part2, part3);

		SourceQueryImpl query = new SourceQueryImpl(someId, parts);

		assertSame(someId, query.getQueryId());
		assertEquals(parts.size(), query.getParts().size());
		assertEquals(parts, query.getParts());
		assertSame(parts.get(0), query.getParts().get(0));
		assertSame(parts.get(1), query.getParts().get(1));
		assertSame(parts.get(2), query.getParts().get(2));
	}
}
