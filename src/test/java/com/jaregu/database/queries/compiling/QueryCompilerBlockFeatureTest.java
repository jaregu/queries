package com.jaregu.database.queries.compiling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.CompiledQueryPart.ResultConsumer;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.SourceQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerBlockFeatureTest {

	private QueryCompilerBlockFeature feature = new QueryCompilerBlockFeature();

	private CompilingContext context;

	@Mock
	private ExpressionParser expressionParser;

	@Before
	public void setUp() {
		context = CompilingContext.forExpressionParser(expressionParser).build();
	}

	@Test
	public void testIsCompilable() throws Exception {
		isNotCompilable();
		isNotCompilable("aaa", "bbb");
		isNotCompilable("aaa", "bbb", "ccc");
		isNotCompilable("aaa", "bbb", "ccc", "ddd");
		isNotCompilable("before", "/* start */", "after");
		isNotCompilable("-- some comment\n");
		isNotCompilable("-- some comment\n", "aaa");
		isNotCompilable("-- #>\n", "-- some comment\n");
		isNotCompilable("-- aaa\n", "-- <# some comment\n");
		isNotCompilable("-- aaa\n", "-- #> aaa\n", "-- <# aaa\n");
		isNotCompilable("-- #> aaa\n", "-- <# aaa\n", "-- aaa\n");
		isNotCompilable("-- #> aaa\n", "-- <# aaa\n", "-- <# aaa\n");
		isNotCompilable("-- #> aaa\n", "-- #> aaa\n", "-- <# aaa\n");

		isCompilable("-- #> aa\n", "-- <# bb\n");
		isCompilable("-- #> aa\n", "aa", "bb", "-- <# bb\n");
		isCompilable("-- #> aa\n", "-- #> aa\n", "-- <# bb\n", "-- <# bb\n");
		isCompilable("-- #> aa\n", "-- #> aa\n", "aaa", "-- <# bb\n", "-- <# bb\n");
		isCompilable("-- #> aa\n", "/* <# bb */");
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
	public void testWithoutExpressionFullOneValue() throws Exception {
		testBlock(null, "-- #>\n", "-- <#\n");
		testBlock(null, "-- #>\n", "-- #>\n", "bb", "-- <#\n", "-- <#\n");
		testBlock(null, "-- #>\n", "aa", "bb", "cc", "-- <#\n");
		testBlock("e1", "-- #> e1   \n", "-- <#\n");
		testBlock("1 + 2", "-- #>1 + 2\n", "-- #>\n", "bb", "-- <#\n", "-- <#\n");
		testBlock("some expr", "-- #>some expr   \n", "aa", "bb", "cc", "-- <#\n");
	}

	private void testBlock(String expr, String... parts) throws Exception {
		testBlock(true, expr, parts);
		testBlock(false, expr, parts);
	}

	private void testBlock(boolean isExecuted, String expr, String... parts) {

		List<SourceQueryPart> sourceParts = toPartList(parts);
		List<String> expectedNames = new LinkedList<>();
		List<Object> expectedParams = new LinkedList<>();
		boolean hasChildren = parts.length > 2;
		boolean hasExpression = expr != null;

		Compiler compiler = mock(Compiler.class);
		ParamsResolver resolver = mock(ParamsResolver.class);
		Expression expression = mock(Expression.class);
		ExpressionParser expressionParser = mock(ExpressionParser.class);
		CompiledQueryPart compiledPart = Mockito.mock(CompiledQueryPart.class);
		Result childrenResult = () -> Collections.singletonList(compiledPart);

		CompilingContext.forExpressionParser(expressionParser).build().withContext(() -> {

			if (hasChildren) {
				when(compiler.compile(any())).thenReturn(childrenResult);
				when(compiledPart.getVariableNames()).thenReturn(Arrays.asList("one", "two", "three"));
				when(resolver.getValue("one")).thenReturn("ONE");
				when(resolver.getValue("two")).thenReturn("TWO");
				when(resolver.getValue("three")).thenReturn(isExecuted ? "THREE" : null);
				expectedNames.add("one");
				expectedNames.add("two");
				expectedNames.add("three");

				expectedParams.addAll(Arrays.asList("ch1", 123));
				doAnswer(i -> {
					((ResultConsumer) i.getArgument(1)).consume("CHILDREN", expectedParams);
					return null;
				}).when(compiledPart).eval(same(resolver), any());
			}

			if (hasExpression) {
				when(expressionParser.parse(expr)).thenReturn(expression);
				when(expression.eval(resolver)).thenReturn(isExecuted);
				when(expression.getVariableNames()).thenReturn(Arrays.asList("1", "2"));
				expectedNames.add("1");
				expectedNames.add("2");
			}

			AtomicBoolean executed = new AtomicBoolean(false);
			AtomicReference<String> sql = new AtomicReference<>();
			AtomicReference<List<Object>> parameters = new AtomicReference<>();

			Result result = feature.compile(() -> {
				return sourceParts;
			}, compiler);
			CompiledQueryPart queryPart = result.getCompiledParts().get(0);

			queryPart.eval(resolver, (s, p) -> {
				executed.set(true);
				sql.set(s);
				parameters.set(p);
			});

			assertEquals(1, result.getCompiledParts().size());
			if (hasChildren) {
				ArgumentCaptor<Source> sourceCaptor = ArgumentCaptor.forClass(Source.class);
				verify(compiler, times(1)).compile(sourceCaptor.capture());
				Source childrenSource = sourceCaptor.getValue();
				assertEquals(sourceParts.size() - 2, childrenSource.getParts().size());
				for (int j = 0; j < childrenSource.getParts().size(); j++) {
					assertSame(sourceParts.get(j + 1), childrenSource.getParts().get(j));
				}
				assertEquals(expectedNames, queryPart.getVariableNames());
			} else {
				verifyZeroInteractions(compiler);
			}

			if (hasExpression) {
				verify(expressionParser, times(1)).parse(expr);
				verifyZeroInteractions(resolver);
			}

			verify(expressionParser, times(0)).isLikeExpression(anyString());

			if (isExecuted || (!hasChildren && !hasExpression)) {
				assertTrue(executed.get());

				String expected = parts[0] + (hasChildren ? "CHILDREN" : "") + parts[parts.length - 1];

				assertEquals(expected, sql.get());
				assertEquals(expectedParams, parameters.get());

			} else {
				assertFalse(executed.get());
			}

			return null;
		});
	}
}
