package com.jaregu.database.queries.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

@RunWith(MockitoJUnitRunner.class)
public class QueriesMapCacheTest {

	private QueriesMapCache cache = new QueriesMapCache();

	@Mock
	private ParsedQueries sourceQueriesA;

	@Mock
	private ParsedQueries sourceQueriesB;

	@Mock
	private ParsedQueries sourceQueriesC;

	@Mock
	private PreparedQuery compiledQuery1;

	@Mock
	private PreparedQuery compiledQuery2;

	@Mock
	private PreparedQuery compiledQuery3;

	@Before
	public void setUp() {

	}

	@Test
	public void testGetSourceQueries() throws Exception {

		Map<SourceId, ParsedQueries> sources = new HashMap<>();
		sources.put(SourceId.ofId("aaa"), sourceQueriesA);
		sources.put(SourceId.ofId("bbb"), sourceQueriesB);
		sources.put(SourceId.ofId("ccc"), sourceQueriesC);

		Map<SourceId, Integer> invokedCounts = new ConcurrentHashMap<>();

		List<Supplier<ParsedQueries>> suppliers = sources.entrySet().stream().map(e -> {
			Supplier<ParsedQueries> sourceQueriesSupplier = () -> {
				invokedCounts.compute(e.getKey(), (key, curr) -> curr == null ? 1 : curr + 1);
				try {
					Thread.sleep(100);
				} catch (InterruptedException exc) {
					throw new RuntimeException(exc);
				}
				return sources.get(e.getKey());
			};
			return sourceQueriesSupplier;
		}).collect(Collectors.toList());

		List<SourceId> keys = new ArrayList<>(sources.keySet());
		List<ParsedQueries> results = doRequest(
				(index) -> cache.getParsedQueries(keys.get(index % 3), suppliers.get(index % 3)), 30);

		for (int count : invokedCounts.values()) {
			assertEquals(1, count);
		}

		for (int i = 0; i < results.size(); i++) {
			assertSame(sources.get(keys.get(i % 3)), results.get(i));
		}
	}

	@Test
	public void testGetCompiledQuery() throws Exception {

		Map<QueryId, PreparedQuery> sources = new HashMap<>();
		sources.put(SourceId.ofId("1").getQueryId("1"), compiledQuery1);
		sources.put(QueryId.of("2.1"), compiledQuery2);
		sources.put(QueryId.of("2.2"), compiledQuery3);

		Map<QueryId, Integer> invokedCounts = new ConcurrentHashMap<>();

		List<Supplier<PreparedQuery>> suppliers = sources.entrySet().stream().map(e -> {
			Supplier<PreparedQuery> sourceQueriesSupplier = () -> {
				invokedCounts.compute(e.getKey(), (key, curr) -> curr == null ? 1 : curr + 1);
				try {
					Thread.sleep(100);
				} catch (InterruptedException exc) {
					throw new RuntimeException(exc);
				}
				return sources.get(e.getKey());
			};
			return sourceQueriesSupplier;
		}).collect(Collectors.toList());

		List<QueryId> keys = new ArrayList<>(sources.keySet());
		List<PreparedQuery> results = doRequest(
				(index) -> cache.getPreparedQuery(keys.get(index % 3), suppliers.get(index % 3)), 30);

		for (int count : invokedCounts.values()) {
			assertEquals(1, count);
		}

		for (int i = 0; i < results.size(); i++) {
			assertSame(sources.get(keys.get(i % 3)), results.get(i));
		}
	}

	private <T> List<T> doRequest(Function<Integer, T> runWhat, int howMuchThreads) {
		List<T> result = new ArrayList<>(howMuchThreads);
		List<Thread> runners = new LinkedList<>();
		// List<T> result = new ArrayList<>(howMuchThreads);
		for (int i = 0; i < howMuchThreads; i++) {
			int j = i;
			result.add(null);
			Thread runner = new Thread(() -> {
				result.set(j, runWhat.apply(j));
			});
			runner.start();
			runners.add(runner);
		}
		for (Thread runner : runners) {
			try {
				runner.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
}
