package com.jaregu.database.queries.compiling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.SourceQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerBindedVariableFeatureTest {

	private QueryCompilerBindedVariableFeature feature = new QueryCompilerBindedVariableFeature();

	private CompilingContext context;

	@Mock
	private ExpressionParser expressionParser;

	@Mock
	private Compiler compiler;

	@Mock
	private ParamsResolver resolver;

	@Before
	public void setUp() {
		context = CompilingContext.forExpressionParser(expressionParser).build();
	}

	@Test
	public void testIsCompilable() throws Exception {
		isNotCompilable();
		isNotCompilable("aaa");
		isNotCompilable("aaa", "bbb");
		isNotCompilable("aaa", ":bbb");
		isNotCompilable(":aaa", "bbb");
		isNotCompilable("--aaa", ":bbb");
		isNotCompilable(":aaa", "--bbb");

		isCompilable(":aaa");
		isCompilable(":aaaBbb.ccc");
	}

	private void isNotCompilable(String... parts) {
		isCompilable(false, parts);
	}

	private void isCompilable(String... parts) {
		isCompilable(true, parts);
	}

	private void isCompilable(boolean equal, String... parts) {
		context.withContext(() -> {
			assertEquals(equal, feature.isCompilable(() -> toPartList(parts)));
			return null;
		});

		verify(expressionParser, times(0)).parse(anyString());
	}

	private List<SourceQueryPart> toPartList(String... parts) {
		return Arrays.asList(parts).stream().map(SourceQueryPart::create).collect(Collectors.toList());
	}

	@Test
	public void testCompiling() throws Exception {

		AtomicBoolean executed = new AtomicBoolean(false);
		AtomicReference<String> sql = new AtomicReference<>();
		AtomicReference<List<Object>> parameters = new AtomicReference<>();
		when(resolver.getValue("aaaBbb.ccc")).thenReturn("XXX");

		Result result = feature.compile(() -> toPartList(":aaaBbb.ccc"), compiler);
		CompiledQueryPart queryPart = result.getCompiledParts().get(0);

		queryPart.eval(resolver, (s, p) -> {
			executed.set(true);
			sql.set(s);
			parameters.set(p);
		});

		assertEquals(1, result.getCompiledParts().size());

		assertTrue(executed.get());
		assertEquals("?", sql.get());
		assertEquals(1, parameters.get().size());
		assertEquals("XXX", parameters.get().get(0));

		verifyZeroInteractions(compiler);
	}
}
