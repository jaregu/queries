package com.jaregu.database.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.parsing.ParsedQueries;
import com.jaregu.database.queries.parsing.ParsedQuery;
import com.jaregu.database.queries.parsing.QueriesParser;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.proxy.QueriesInvocationHandler;

@RunWith(MockitoJUnitRunner.class)
public class QueriesImplTest {

	@InjectMocks
	private QueriesImpl queries;

	@Mock
	private QueriesParser parser;

	@Mock
	private QueryCompiler compiler;

	@Mock
	private QueriesCache cache;

	@Mock
	private QueriesSources sources;

	@Mock
	private QueriesConfig config;

	private SourceId source1Id = SourceId.ofId("com.jaregu.database.queries.QueriesImplTest");

	@Mock
	private QueriesSource source1;

	private SourceId source2Id = SourceId.ofId("second.source");

	@Mock
	private QueriesSource source2;

	@Mock
	private ParsedQueries parsedQueries1;

	@Mock
	private ParsedQueries parsedQueries2;

	private QueryId queryId11 = source1Id.getQueryId("1-1");

	@Mock
	private ParsedQuery parsedQuery11;

	private QueryId queryId12 = source1Id.getQueryId("1-2");

	@Mock
	private ParsedQuery parsedQuery12;

	private QueryId queryId21 = source2Id.getQueryId("2-1");

	@Mock
	private ParsedQuery parsedQuery21;

	@Mock
	private PreparedQuery preparedQuery11;

	@Mock
	private PreparedQuery preparedQuery12;

	@Mock
	private PreparedQuery preparedQuery21;

	@Before
	public void setUp() {
		when(sources.getSources()).thenReturn(Arrays.asList(source1, source2));
		when(source1.getId()).thenReturn(source1Id);
		when(source2.getId()).thenReturn(source2Id);
		when(parser.parse(source1)).thenReturn(parsedQueries1);
		when(parser.parse(source2)).thenReturn(parsedQueries2);
		// when(parsedQueries1.getQueries()).thenReturn(Arrays.asList(parsedQuery11,
		// parsedQuery12));
		// when(parsedQuery11.getQueryId()).thenReturn(queryId11);
		// when(parsedQuery12.getQueryId()).thenReturn(queryId12);
		when(parsedQueries1.get(queryId11)).thenReturn(parsedQuery11);
		when(parsedQueries1.get(queryId12)).thenReturn(parsedQuery12);

		// when(parsedQueries2.getQueries()).thenReturn(Arrays.asList(parsedQuery21));
		// when(parsedQuery21.getQueryId()).thenReturn(queryId21);
		when(parsedQueries2.get(queryId21)).thenReturn(parsedQuery21);

		when(compiler.compile(parsedQuery11)).thenReturn(preparedQuery11);
		when(compiler.compile(parsedQuery12)).thenReturn(preparedQuery12);
		when(compiler.compile(parsedQuery21)).thenReturn(preparedQuery21);
	}

	@Test
	public void testProxy() {
		TestBar testBar = queries.proxy(TestBar.class);
		assertThat(testBar).isNotNull();

		InvocationHandler handler = Proxy.getInvocationHandler(testBar);
		assertThat(handler).isInstanceOf(QueriesInvocationHandler.class);
	}

	@Test
	public void testStringGet() {
		sourcesCached();
		PreparedQuery query11 = queries.get("com.jaregu.database.queries.QueriesImplTest.1-1");
		assertThat(query11).isSameAs(preparedQuery11);
	}

	@Test
	public void testNonCachedGet() {
		noCache();

		PreparedQuery query11 = queries.get(queryId11);
		PreparedQuery query12 = queries.relativeTo(source1Id).get("1-2");
		PreparedQuery query21 = queries.get(queryId21);

		assertThat(query11).isSameAs(preparedQuery11);
		assertThat(query12).isSameAs(preparedQuery12);
		assertThat(query21).isSameAs(preparedQuery21);

		verify(cache, times(2)).getParsedQueries(eq(source1Id), any());
		verify(parser, times(2)).parse(source1);
		verify(cache, times(1)).getPreparedQuery(eq(queryId11), any());
		verify(cache, times(1)).getPreparedQuery(eq(queryId12), any());
		verify(compiler, times(1)).compile(parsedQuery11);
		verify(compiler, times(1)).compile(parsedQuery12);

		verify(cache, times(1)).getParsedQueries(eq(source2Id), any());
		verify(parser, times(1)).parse(source2);
		verify(cache, times(1)).getPreparedQuery(eq(queryId21), any());
		verify(compiler, times(1)).compile(parsedQuery21);
	}

	private void noCache() {
		when(cache.getParsedQueries(any(), any())).then(a -> {
			Supplier<ParsedQueries> supplier = a.getArgument(1);
			return supplier.get();
		});
		when(cache.getPreparedQuery(any(), any())).then(a -> {
			Supplier<PreparedQuery> supplier = a.getArgument(1);
			return supplier.get();
		});
	}

	@Test
	public void testCachedParsedGet() throws Exception {
		sourcesCached();

		PreparedQuery query11 = queries.get(queryId11);
		PreparedQuery query12 = queries.relativeTo(source1Id).get("1-2");
		PreparedQuery query21 = queries.get(queryId21);

		assertThat(query11).isSameAs(preparedQuery11);
		assertThat(query12).isSameAs(preparedQuery12);
		assertThat(query21).isSameAs(preparedQuery21);

		verifyNoInteractions(parser);
		verify(cache, times(2)).getParsedQueries(eq(source1Id), any());
		verify(compiler, times(1)).compile(parsedQuery11);
		verify(compiler, times(1)).compile(parsedQuery12);

		verify(cache, times(1)).getParsedQueries(eq(source2Id), any());
		verify(compiler, times(1)).compile(parsedQuery21);

		verify(cache, times(1)).getPreparedQuery(eq(queryId11), any());
		verify(cache, times(1)).getPreparedQuery(eq(queryId12), any());
		verify(cache, times(1)).getPreparedQuery(eq(queryId21), any());
	}

	private void sourcesCached() {
		when(cache.getParsedQueries(any(), any())).then(a -> {
			SourceId sourceId = a.getArgument(0);
			if (sourceId.equals(source1Id)) {
				return parsedQueries1;
			} else if (sourceId.equals(source2Id)) {
				return parsedQueries2;
			}
			throw new IllegalStateException("There must be only two sources!");
		});
		when(cache.getPreparedQuery(any(), any())).then(a -> {
			Supplier<PreparedQuery> supplier = a.getArgument(1);
			return supplier.get();
		});
	}

	@Test
	public void testCachedPreparedGet() throws Exception {
		preparedCached();

		PreparedQuery query11 = queries.get(queryId11);
		PreparedQuery query12 = queries.relativeTo(source1Id).get("1-2");
		PreparedQuery query21 = queries.get(queryId21);

		assertThat(query11).isSameAs(preparedQuery11);
		assertThat(query12).isSameAs(preparedQuery12);
		assertThat(query21).isSameAs(preparedQuery21);

		verifyNoInteractions(parser);
		verifyNoInteractions(compiler);

		verify(cache, never()).getParsedQueries(eq(source1Id), any());
		verify(cache, never()).getParsedQueries(eq(source2Id), any());

		verify(cache, times(1)).getPreparedQuery(eq(queryId11), any());
		verify(cache, times(1)).getPreparedQuery(eq(queryId12), any());
		verify(cache, times(1)).getPreparedQuery(eq(queryId21), any());
	}

	private void preparedCached() {
		when(cache.getPreparedQuery(any(), any())).then(a -> {
			QueryId queryId = a.getArgument(0);
			if (queryId.equals(queryId11)) {
				return preparedQuery11;
			} else if (queryId.equals(queryId12)) {
				return preparedQuery12;
			} else if (queryId.equals(queryId21)) {
				return preparedQuery21;
			}
			throw new IllegalStateException("There is only three parsed queries!");
		});
	}

	public interface TestBar {

	}

	public class Params {

		public String aaa;
		private final String bbb;

		public Params(String bbb) {
			this.bbb = bbb;
		}

		public String getBbb() {
			return bbb;
		}
	}

}
