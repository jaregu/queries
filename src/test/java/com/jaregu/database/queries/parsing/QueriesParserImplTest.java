package com.jaregu.database.queries.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

@RunWith(MockitoJUnitRunner.class)
public class QueriesParserImplTest {

	final private static SourceId SOURCE_ID = SourceId.ofId("some.source.id");

	private QueriesParserImpl parser = new QueriesParserImpl();

	@Test(expected = QueryParseException.class)
	public void testNoCommentParse() throws Exception {
		QueriesSource source = mock(QueriesSource.class);
		when(source.getId()).thenReturn(SOURCE_ID);
		when(source.getContent()).thenReturn("some simple content");
		parser.parse(source);
	}

	@Test
	public void testNewLinesAfterQuery() throws Exception {
		QueriesSource source = mock(QueriesSource.class);
		when(source.getId()).thenReturn(SOURCE_ID);
		when(source.getContent()).thenReturn("some simple content --query 1\n;\n\n\n\n\n");
		parser.parse(source);
	}

	@Test
	public void testSimpleQueries() throws Exception {
		testQueries(queries(query("query 0", queryId("--query 0"))));

		testQueries(queries(query("query 1", queryId("--query 1\n"), "some ", ":simple", " content")));

		testQueries(queries(query("query 2", queryId("-- query 2\n"), "some ", ":really.simple", " content\n",
				"some more content")));

		testQueries(queries(
				query("query 2-1", queryId("-- query 2-1 \n"), "some simple content\n", "some more content\n")));

		testQueries(queries(
				query("query 3", queryId("--  query 3\n"), "some simple content\n", "--  ending with comment\n")));

		testQueries(queries(
				query("query 3-1", queryId("--  query 3-1 \n"), "some simple content\n", "--  ending with comment\n")));

		testQueries(queries(
				query("q4", queryId("/*q4 */"), "some simple content  ", "/* additional comment in the same line */")));

		testQueries(queries(query("q 5", queryId("/*  q 5  */"), "some simple content")));

		testQueries(queries(query("query 6", queryId("/*  query 6  */"), "\n", "some simple content line\n", ";")));

		testQueries(queries(query("query 7", queryId("/*  query 7  */"), "\n", "line 1\n", "line 2\n", "line 3\n",
				"line 4\n", "line 5    ", "/* line 5 comment with \n line\n breaks inside */",
				"  some after comment sql  ")));

		testQueries(queries(query("query 8", "This is not a comment '--query or something'\n",
				"and this is '/*not yet*/' a comment\n", "this is ", queryId("-- query 8\n"), "some content")));

		testQueries(queries(query("query 9", "query is is not on first line \n", "but is on second ",
				queryId("--query 9\n"), "some content")));

		testQueries(queries(query("query 10", queryId("/* query 10 */"), "/* multiple comments*/",
				"/* multiple comments*/", "-- last one\n")));

		testQueries(queries(
				query("query 11", queryId("/* query 11 */"), "something ", "?", " ", "/* Anonymous binding */")));
	}

	@Test
	public void testName() throws Exception {
		testQueries(queries(query("query 8", "This is not a comment '--query or something'\n",
				"and this is '/*not yet*/' a comment\n", "this is ", queryId("-- query 8\n"), "some content")));
	}

	@Test
	public void testMultipleQueries() throws Exception {
		testQueries(queries(query("query 1-1", queryId("-- query 1-1 \n"), "some query 1-1 content;"),
				query("query 1-2", "\n", "\n", "\n", queryId("/* query 1-2 */"), "\n", "some query 1-2 content;")));
		testQueries(queries(query("query 2-1", queryId("-- query 2-1 \n"), "some query 2-1 content;"),
				query("query 2-2", queryId("/* query 2-2 */"), "\n", "some query 2-2 content;")));
		testQueries(queries(query("query 2-1", queryId("-- query 2-1 \n"), "some query 2-1 content;"),
				query("query 2-2", queryId("/* query 2-2 */"), "\n", "some query 2-2 content;")));

		testQueries(queries(

				query("query 3-1", queryId("-- query 3-1 \n"), "AAA;"),

				query("query 3-2", "\n", queryId("/* query 3-2 */"), "\n", "BBB1\n", "BBB2\n", "BBB3\n", "BBB4;"),

				query("query 3-3", "\n", queryId("-- query 3-3 \n"), "\n", "CCC1\n", "  CCC2", "/* some CCC comment */",
						" CCC2 after  \n", "CCC_end;"),

				query("query 3-4", "\n", queryId("/* query 3-4 */"), "\n", "DDD_start\n", "DDD1 ",
						"-- some line comment\n", "DDD2 ", "--- some line comment\n", "DDD;")));

	}

	private void testQueries(TestQueries queries) {
		String sql = queries.toString();
		ParsedQueries sourceQueries = null;
		TestQuery query = null;
		ParsedQuery sourceQuery = null;
		CharSequence part = null;
		ParsedQueryPart queryPart = null;
		try {
			QueriesSource source = mock(QueriesSource.class);
			when(source.getContent()).thenReturn(sql);
			when(source.getId()).thenReturn(SOURCE_ID);

			sourceQueries = parser.parse(source);

			verify(source, times(1)).getContent();

			assertEquals(queries.queries.size(), sourceQueries.getQueries().size());
			for (int i = 0; i < queries.queries.size(); i++) {
				query = queries.queries.get(i);
				QueryId queryId = QueryId.of(SOURCE_ID, query.queryId);

				sourceQuery = sourceQueries.getQueries().get(i);
				assertSame(sourceQuery, sourceQueries.get(queryId));
				assertEquals(queryId, sourceQuery.getQueryId());
				assertEquals(query.parts.size(), sourceQuery.getParts().size());

				for (int j = 0; j < query.parts.size(); j++) {
					part = query.parts.get(j);
					queryPart = sourceQuery.getParts().get(j);

					if (part instanceof QueryIdPart) {
						CommentType commentType = CommentType.parseCommentType(part.toString()).get();
						assertEquals(commentType.wrap(" QUERY ID: " + queryId), queryPart.getContent());
					} else {
						assertEquals(part.toString(), queryPart.getContent());
					}
				}
			}
		} catch (AssertionError e) {
			System.out.println(sql);
			System.out.println("--------------------------------------\n");
			System.out.println("---->>>>> PROBLEMATIC QUERY: " + query);
			System.out.println("--------------------------------------\n");
			System.out.println("---->>>>> QUERY PARSED: " + sourceQuery);
			System.out.println("--------------------------------------\n");
			System.out.println("---->>>>> PROBLEMATIC PART: " + part);
			System.out.println("--------------------------------------\n");
			System.out.println("---->>>>> PART PARSED: " + queryPart);

			throw e;
		}
	}

	private TestQueries queries(TestQuery... queries) {
		return new TestQueries(Arrays.asList(queries));

	}

	private static TestQuery query(String queryId, CharSequence... sequences) {
		return new TestQuery(queryId, Arrays.asList(sequences));
	}

	private static QueryIdPart queryId(String comment) {
		return new QueryIdPart(comment);
	}

	/*
	 * private static CommentPart comment(String comment) { return new
	 * CommentPart(comment); }
	 */

	private static class TestQueries {

		private List<TestQuery> queries;

		public TestQueries(List<TestQuery> queries) {
			this.queries = queries;
		}

		@Override
		public String toString() {
			return queries.stream().map(TestQuery::toString).reduce("", (a, b) -> a + b);
		}
	}

	private static class TestQuery {

		private String queryId;
		private List<CharSequence> parts;

		public TestQuery(String queryId, List<CharSequence> parts) {
			this.queryId = queryId;
			this.parts = parts;
		}

		@Override
		public String toString() {

			return parts.stream().map(Object::toString).reduce("", (a, b) -> a + b);
		}
	}

	/*
	 * private static class CommentPart extends CustomCharSequence {
	 * 
	 * public CommentPart(String comment) { super(comment); } }
	 */

	private static class QueryIdPart extends CustomCharSequence {

		public QueryIdPart(String comment) {
			super(comment);
		}
	}

	private static class CustomCharSequence implements CharSequence {

		private String comment;

		public CustomCharSequence(String comment) {
			this.comment = comment;
		}

		@Override
		public int length() {
			return comment.length();
		}

		@Override
		public char charAt(int index) {
			return comment.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return comment.subSequence(start, end);
		}

		@Override
		public String toString() {
			return comment;
		}
	}
}
