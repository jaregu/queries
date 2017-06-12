package com.jaregu.database.queries.compiling;

import static com.jaregu.database.queries.parsing.SourceQueryPart.create;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueriesConfigImpl;
import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerBlockCommentParameterFeatureTest {

	private QueryCompilerSlashCommentParameterFeature feature = new QueryCompilerSlashCommentParameterFeature();

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

	@Test(expected = QueryCompileException.class)
	public void testEvaluationWithoutContext() {
		feature.isCompilable(Collections::emptyList);
	}

	@Test
	public void testIsCompilable() throws Exception {
		when(expressionParser.isLikeExpression(anyString())).thenReturn(true);

		context.withContext(() -> {

			assertEquals(false, feature.isCompilable(Collections::emptyList));
			assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("bbb"))));
			assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("bbb"), create("ccc"))));
			assertEquals(false,
					feature.isCompilable(() -> asList(create("aaa"), create("bbb"), create("ccc"), create("ddd"))));

			assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("-- some comment"))));
			assertEquals(false,
					feature.isCompilable(() -> asList(create("aaa"), create("-- some comment"), create("bbb"))));

			assertEquals(false, feature.isCompilable(() -> asList(create("/* start */"))));
			assertEquals(false, feature.isCompilable(() -> asList(create("/* start */"), create("aaa"))));
			assertEquals(false, feature.isCompilable(() -> asList(create("aaa"), create("/* start */"))));
			assertEquals(false,
					feature.isCompilable(() -> asList(create("aaa"), create("bbb"), create("/* start */"))));

			assertEquals(true,
					feature.isCompilable(() -> asList(create("before"), create("/* start */"), create("after"))));

			return null;
		});

		verify(expressionParser, times(0)).parse(anyString());
	}

	@Test
	public void testIsCompilableWhenParserSaysNo() throws Exception {
		when(expressionParser.isLikeExpression(anyString())).thenReturn(false);

		context.withContext(() -> {

			assertEquals(false,
					feature.isCompilable(() -> asList(create("before"), create("/* start */"), create("after"))));

			return null;
		});

		verify(expressionParser, times(0)).parse(anyString());
	}

	@Test
	public void testOneExpressionFullValues() throws Exception {
		withDifferentContext(() -> testOneExpressionTest(true));
	}

	@Test
	public void testOneExpressionEmptyValues() throws Exception {
		withDifferentContext(() -> testOneExpressionTest(true));
	}

	private void testOneExpressionTest(boolean fullValues) {
		Expression expression = mock(Expression.class);

		when(expressionParser.parse("start")).thenReturn(expression);
		when(expression.getVariableNames()).thenReturn(Arrays.asList("one", "t w o"));
		when(expression.eval(resolver)).thenReturn("expression VALUE");
		when(resolver.getValue("one")).thenReturn("full1");
		when(resolver.getValue("t w o")).thenReturn(fullValues ? "full2" : null);

		AtomicBoolean executed = new AtomicBoolean(false);
		AtomicReference<String> sqlHoler = new AtomicReference<>();
		AtomicReference<List<Object>> parametersHolder = new AtomicReference<>();

		Result result = feature.compile(
				() -> asList(create("before (replace me)"), create("/* start */"), create(" after")), compiler);
		CompiledQueryPart queryPart = result.getCompiledParts().get(0);
		queryPart.eval(resolver, (sql, parameters) -> {
			executed.set(true);
			sqlHoler.set(sql);
			parametersHolder.set(parameters);
		});

		assertEquals(1, result.getCompiledParts().size());

		if (fullValues) {
			assertTrue(executed.get());
			assertEquals("before ?" + getArgumentCommentIfEnabled("(replace me)") + "/* start */ after",
					sqlHoler.get());
			assertEquals(1, parametersHolder.get().size());
			assertEquals("expression VALUE", parametersHolder.get().get(0));
		} else {
			assertFalse(executed.get());
		}

		verify(expressionParser, times(0)).isLikeExpression(anyString());
		verify(compiler, times(0)).compile(any());
	}

	@Test
	public void testTwoExpressionFullValues() throws Exception {
		withDifferentContext(() -> testConditionalExpressionTest(true));
	}

	@Test
	public void testTwoExpressionEmptyValues() throws Exception {
		withDifferentContext(() -> testConditionalExpressionTest(true));
	}

	private void testConditionalExpressionTest(boolean condition) {
		Expression valueExpression = mock(Expression.class);
		Expression conditionExpression = mock(Expression.class);

		when(expressionParser.parse("start ")).thenReturn(valueExpression);
		when(expressionParser.parse(" condition")).thenReturn(conditionExpression);

		when(valueExpression.getVariableNames()).thenReturn(Arrays.asList("one", "two"));
		when(valueExpression.eval(resolver)).thenReturn("VALUE");

		when(conditionExpression.getVariableNames()).thenReturn(Arrays.asList("three", "four"));
		when(conditionExpression.eval(resolver)).thenReturn(condition);

		AtomicBoolean executed = new AtomicBoolean(false);
		AtomicReference<String> sqlHoler = new AtomicReference<>();
		AtomicReference<List<Object>> parametersHolder = new AtomicReference<>();
		Result result = feature.compile(
				() -> asList(create("before (replace me)"), create("/* start ; condition */"), create(" after")),
				compiler);
		CompiledQueryPart queryPart = result.getCompiledParts().get(0);
		queryPart.eval(resolver, (sql, parameters) -> {
			executed.set(true);
			sqlHoler.set(sql);
			parametersHolder.set(parameters);
		});

		assertEquals(1, result.getCompiledParts().size());

		if (condition) {
			assertTrue(executed.get());
			assertEquals("before ?" + getArgumentCommentIfEnabled("(replace me)") + "/* start ; condition */ after",
					sqlHoler.get());
			assertEquals(1, parametersHolder.get().size());
			assertEquals("VALUE", parametersHolder.get().get(0));
		} else {
			assertFalse(executed.get());
		}

		verify(expressionParser, times(0)).isLikeExpression(anyString());
		verify(resolver, times(0)).getValue(any());
		verify(compiler, times(0)).compile(any());
	}

	private String getArgumentCommentIfEnabled(String commentContent) {
		if (CompilingContext.getCurrent().getConfig().isOriginalArgumentCommented()) {
			return " /* " + commentContent + " */ ";
		}
		return "";
	}

	private void withDifferentContext(Runnable run) {
		context.withContext(() -> {
			run.run();
			CompilingContext.forExpressionParser(expressionParser)
					.withConfig(QueriesConfigImpl.getDefault().setOriginalArgumentCommented(true)).build()
					.withContext(() -> {
						run.run();
						return null;
					});
			return null;
		});
	}
}
