package com.jaregu.database.queries.compiling;

import static com.jaregu.database.queries.parsing.SourceQueryPart.create;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerIgnoredCommentsFeatureTest {

	private QueryCompilerIgnoredCommentsFeature feature = new QueryCompilerIgnoredCommentsFeature();

	@Mock
	private Compiler compiler;

	@Mock
	private ParamsResolver resolver;

	@Before
	public void setUp() {
	}

	@Test
	public void testIsCompilable() throws Exception {

		assertEquals(false, feature.isCompilable(Collections::emptyList));
		assertEquals(false, feature.isCompilable(() -> asList(create("aaa"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("bbb"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("bbb"), create("ccc"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("/* block comment */"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("-- some comment\n"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("/** block comment */"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("--- some comment\n"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("/** block comment */"), create("aaa"))));
		assertEquals(false, feature.isCompilable(() -> asList(create("--- some comment\n"), create("aaa"))));

		assertEquals(true, feature.isCompilable(() -> asList(create("/** block comment */"))));
		assertEquals(true, feature.isCompilable(() -> asList(create("--- some comment\n"))));

	}

	@Test
	public void testCommentCompiling() throws Exception {

		AtomicBoolean executed = new AtomicBoolean(false);
		AtomicReference<String> sql = new AtomicReference<>();
		AtomicReference<List<Object>> parameters = new AtomicReference<>();

		Result result = feature.compile(() -> asList(create("--- SOME comment \n")), compiler);
		CompiledQueryPart queryPart = result.getCompiledParts().get(0);
		queryPart.eval(resolver, (s, p) -> {
			executed.set(true);
			sql.set(s);
			parameters.set(p);
		});

		assertEquals(1, result.getCompiledParts().size());

		assertTrue(executed.get());
		assertEquals("--- SOME comment \n", sql.get());
		assertEquals(0, parameters.get().size());

		verifyZeroInteractions(compiler);
		verifyZeroInteractions(resolver);
	}
}
