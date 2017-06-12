package com.jaregu.database.queries.compiling;

import static com.jaregu.database.queries.compiling.CompiledQueryPart.constant;
import static com.jaregu.database.queries.parsing.SourceQueryPart.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.SourceQuery;
import com.jaregu.database.queries.parsing.SourceQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerImplTest {

	private QueryCompilerImpl noOpCompiler;

	private QueryCompilerImpl allOpCompiler;

	@Mock
	private ExpressionParser parser;

	@Mock
	private QueriesConfig config;

	@Mock
	private QueryCompilerFeature noOpFeature;

	@Mock
	private QueryCompilerFeature allAcceptFeature;

	@Mock
	private CompiledQueryPart compiledPart1;

	@Mock
	private CompiledQueryPart compiledPart2;

	@Mock
	private CompiledQueryPart compiledPart3;

	@Mock
	private SourceQuery sourceQuery;
	private List<SourceQueryPart> simpleSourceParts;

	@Mock
	private ParamsResolver variableResolver;

	@Before
	public void setUp() {
		noOpCompiler = new QueryCompilerImpl(config, parser, Collections.singletonList(noOpFeature));
		allOpCompiler = new QueryCompilerImpl(config, parser, Arrays.asList(noOpFeature, allAcceptFeature));
		simpleSourceParts = Arrays.asList(create("1"), create("2"), create("3"));
		when(noOpFeature.isCompilable(any())).thenReturn(false);

		when(allAcceptFeature.isCompilable(any())).thenReturn(true);
		when(allAcceptFeature.compile(any(), any())).thenReturn(() -> Collections.singletonList(compiledPart1),
				() -> Collections.singletonList(compiledPart2), () -> Collections.singletonList(compiledPart3));
	}

	@Test
	public void testPartIterationWithNoOp() {

		when(sourceQuery.getParts()).thenReturn(simpleSourceParts);

		ArgumentCaptor<Source> sourceCaptor = ArgumentCaptor.forClass(Source.class);
		CompiledQuery compiledQuery = noOpCompiler.compile(sourceQuery);
		verify(noOpFeature, times(6)).isCompilable(sourceCaptor.capture());

		List<Source> allSources = sourceCaptor.getAllValues();

		assertEquals("1", sourceToString(allSources.get(0)));
		assertEquals("12", sourceToString(allSources.get(1)));
		assertEquals("123", sourceToString(allSources.get(2)));
		assertEquals("2", sourceToString(allSources.get(3)));
		assertEquals("23", sourceToString(allSources.get(4)));
		assertEquals("3", sourceToString(allSources.get(5)));

		assertEquals(3, compiledQuery.getParts().size());
		assertEquals(constant("1"), compiledQuery.getParts().get(0));
		assertEquals(constant("2"), compiledQuery.getParts().get(1));
		assertEquals(constant("3"), compiledQuery.getParts().get(2));
	}

	@Test
	public void testPartIterationWithAllAccept() {

		when(sourceQuery.getParts()).thenReturn(simpleSourceParts);

		CompiledQuery compiledQuery = allOpCompiler.compile(sourceQuery);

		ArgumentCaptor<Source> noOpSourceCaptor = ArgumentCaptor.forClass(Source.class);
		verify(noOpFeature, times(3)).isCompilable(noOpSourceCaptor.capture());

		ArgumentCaptor<Source> allOpSourceCaptor = ArgumentCaptor.forClass(Source.class);
		verify(allAcceptFeature, times(3)).isCompilable(allOpSourceCaptor.capture());

		List<Source> noOpSources = noOpSourceCaptor.getAllValues();
		assertEquals("1", sourceToString(noOpSources.get(0)));
		assertEquals("2", sourceToString(noOpSources.get(1)));
		assertEquals("3", sourceToString(noOpSources.get(2)));

		List<Source> allOpSources = allOpSourceCaptor.getAllValues();
		assertEquals("1", sourceToString(allOpSources.get(0)));
		assertEquals("2", sourceToString(allOpSources.get(1)));
		assertEquals("3", sourceToString(allOpSources.get(2)));

		verify(noOpFeature, times(0)).compile(any(), any());

		allOpSourceCaptor = ArgumentCaptor.forClass(Source.class);
		verify(allAcceptFeature, times(3)).compile(allOpSourceCaptor.capture(), any());
		allOpSources = allOpSourceCaptor.getAllValues();
		assertEquals("1", sourceToString(allOpSources.get(0)));
		assertEquals("2", sourceToString(allOpSources.get(1)));
		assertEquals("3", sourceToString(allOpSources.get(2)));

		assertEquals(3, compiledQuery.getParts().size());
		assertSame(compiledPart1, compiledQuery.getParts().get(0));
		assertSame(compiledPart2, compiledQuery.getParts().get(1));
		assertSame(compiledPart3, compiledQuery.getParts().get(2));
	}

	private String sourceToString(Source source) {
		return source.getParts().stream().map(SourceQueryPart::getContent).reduce((s1, s2) -> s1 + s2).get();
	}
}
