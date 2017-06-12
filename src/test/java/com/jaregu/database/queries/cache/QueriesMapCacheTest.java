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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.parsing.SourceQueries;

@RunWith(MockitoJUnitRunner.class)
public class QueriesMapCacheTest {

	private QueriesMapCache cache = new QueriesMapCache();

	@Mock
	private SourceQueries sourceQueriesA;

	@Mock
	private SourceQueries sourceQueriesB;

	@Mock
	private SourceQueries sourceQueriesC;

	@Mock
	private CompiledQuery compiledQuery1;

	@Mock
	private CompiledQuery compiledQuery2;

	@Mock
	private CompiledQuery compiledQuery3;

	@Before
	public void setUp() {

	}

	@Test
	public void testGetSourceQueries() throws Exception {

		Map<SourceId, SourceQueries> sources = new HashMap<>();
		sources.put(SourceId.of("aaa"), sourceQueriesA);
		sources.put(SourceId.of("bbb"), sourceQueriesB);
		sources.put(SourceId.of("ccc"), sourceQueriesC);

		Map<SourceId, Integer> invokedCounts = new ConcurrentHashMap<>();
		Function<SourceId, SourceQueries> sourceQueriesSupplier = (id) -> {
			invokedCounts.compute(id, (key, curr) -> curr == null ? 1 : curr + 1);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return sources.get(id);
		};

		List<SourceId> keys = new ArrayList<>(sources.keySet());
		List<SourceQueries> results = doRequest(
				(index) -> cache.getSourceQueries(keys.get(index % 3), sourceQueriesSupplier), 30);

		for (int count : invokedCounts.values()) {
			assertEquals(1, count);
		}

		for (int i = 0; i < results.size(); i++) {
			assertSame(sources.get(keys.get(i % 3)), results.get(i));
		}
	}

	@Test
	public void testGetCompiledQuery() throws Exception {

		Map<QueryId, CompiledQuery> sources = new HashMap<>();
		sources.put(SourceId.of("1").queryId("1"), compiledQuery1);
		sources.put(QueryId.of("2.1"), compiledQuery2);
		sources.put(QueryId.of("2.2"), compiledQuery3);

		Map<QueryId, Integer> invokedCounts = new ConcurrentHashMap<>();
		Function<QueryId, CompiledQuery> sourceQueriesSupplier = (id) -> {
			invokedCounts.compute(id, (key, curr) -> curr == null ? 1 : curr + 1);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return sources.get(id);
		};

		List<QueryId> keys = new ArrayList<>(sources.keySet());
		List<CompiledQuery> results = doRequest(
				(index) -> cache.getCompiledQuery(keys.get(index % 3), sourceQueriesSupplier), 30);

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
		//List<T> result = new ArrayList<>(howMuchThreads);
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
