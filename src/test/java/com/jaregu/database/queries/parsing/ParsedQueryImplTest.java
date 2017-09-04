package com.jaregu.database.queries.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.jaregu.database.queries.QueryId;

public class ParsedQueryImplTest {

	@Test
	public void testConstruction() throws Exception {
		QueryId someId = Mockito.mock(QueryId.class);
		ParsedQueryPart part1 = Mockito.mock(ParsedQueryPart.class);
		ParsedQueryPart part2 = Mockito.mock(ParsedQueryPart.class);
		ParsedQueryPart part3 = Mockito.mock(ParsedQueryPart.class);
		List<ParsedQueryPart> parts = Arrays.asList(part1, part2, part3);

		ParsedQueryImpl query = new ParsedQueryImpl(someId, parts);

		assertSame(someId, query.getQueryId());
		assertEquals(parts.size(), query.getParts().size());
		assertEquals(parts, query.getParts());
		assertSame(parts.get(0), query.getParts().get(0));
		assertSame(parts.get(1), query.getParts().get(1));
		assertSame(parts.get(2), query.getParts().get(2));
	}
}
