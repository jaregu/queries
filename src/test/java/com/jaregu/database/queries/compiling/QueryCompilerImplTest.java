package com.jaregu.database.queries.compiling;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.parsing.ParsedQuery;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerImplTest {

	private QueryId goodFood = QueryId.of("good.food");

	@Mock(answer = Answers.RETURNS_MOCKS)
	private QueriesConfig interior;

	@Before
	public void setUp() {
	}

	@Test
	public void testDroppings() throws Exception {
		Cafe cafe = open(eatSequences("a", 1), eatSequences("b", 2), eatSequences("c", 3), eatEnds("<", ">"));

		assertEquals("Abc", cafe.serve(unpack("abc")));
		assertEquals("ABBcc", cafe.serve(unpack("abbcc")));
		assertEquals("ABBCCC", cafe.serve(unpack("abbccc")));
		assertEquals("AAABBbCCC", cafe.serve(unpack("aaabbbccc")));

		assertEquals("A", cafe.serve(unpack("<a>")));
		assertEquals("AAA", cafe.serve(unpack("<aaa>")));
		assertEquals("AxA", cafe.serve(unpack("<a<x>a>")));
		assertEquals("AxABBBBdCCCd", cafe.serve(unpack("<a<x>a><bb><bb<dcccd>>")));

		assertEquals("A1A2", cafe.serve("a1", "a2"));
		assertEquals("A1A2c1B1B2c2", cafe.serve("a1", "a2", "<", "c1", "<", "b1", "b2", ">", "c2", ">"));
	}

	private String[] unpack(String food) {
		return food.split("");
	}

	@FunctionalInterface
	private static interface Cafe {

		String serve(String... food);
	}

	private Cafe open(QueryCompilerFeature... eaters) {
		QueryCompilerImpl kitchen = new QueryCompilerImpl(Arrays.asList(eaters));
		return (food) -> serve(cook(kitchen, food));
	}

	private PreparedQuery cook(QueryCompilerImpl kitchen, String... food) {

		ParsedQuery meal = mock(ParsedQuery.class);
		List<ParsedQueryPart> portions = Arrays.asList(food).stream().map(ParsedQueryPart::create)
				.collect(Collectors.toList());
		when(meal.getParts()).thenReturn(portions);
		when(meal.getQueryId()).thenReturn(goodFood);

		PreparedQuery waitress = kitchen.compile(meal);

		assertEquals(goodFood, waitress.getQueryId());
		return waitress;
	}

	private String serve(PreparedQuery meal) {
		Query droppings = meal.build();
		return droppings.getSql().toString();
	}

	private QueryCompilerFeature eatSequences(String eatWhat, int minPortions) {
		QueryCompilerFeature eater = mock(QueryCompilerFeature.class);

		Function<Source, Boolean> isCompilable = source -> source.getParts().size() == minPortions
				&& source.getParts().stream().map(ParsedQueryPart::getContent).allMatch(s -> s.startsWith(eatWhat));

		when(eater.isCompilable(any())).then(i -> {
			Source source = (Source) i.getArgument(0);
			return isCompilable.apply(source);
		});
		when(eater.compile(any(), any())).thenAnswer(i -> {
			Source source = (Source) i.getArgument(0);
			// Compiler chain = (Compiler) i.getArgument(1);
			if (!isCompilable.apply(source)) {
				throw new IllegalStateException("I didn't order this!");
			}
			List<PreparedQueryPart> compiledParts = source.getParts().stream().map(ParsedQueryPart::getContent)
					.map(String::toUpperCase).map(this::compiledPart).collect(Collectors.toList());

			Result result = () -> compiledParts;
			return result;
		});
		return eater;
	}

	private QueryCompilerFeature eatEnds(String starter, String pastry) {
		QueryCompilerFeature eater = mock(QueryCompilerFeature.class);

		Function<Source, Boolean> isCompilable = source -> source.getParts().size() > 2
				&& source.getParts().get(0).getContent().equals(starter)
				&& source.getParts().stream().mapToInt(p -> p.getContent().equals(starter) ? 1 : 0).sum() == source
						.getParts().stream().mapToInt(p -> p.getContent().equals(pastry) ? 1 : 0).sum()
				&& source.getParts().get(source.getParts().size() - 1).getContent().equals(pastry);

		when(eater.isCompilable(any())).then(i -> {
			Source source = (Source) i.getArgument(0);
			return isCompilable.apply(source);
		});
		when(eater.compile(any(), any())).thenAnswer(i -> {
			Source source = (Source) i.getArgument(0);
			Compiler chain = (Compiler) i.getArgument(1);
			if (!isCompilable.apply(source)) {
				throw new IllegalStateException("I didn't order this!");
			}
			List<PreparedQueryPart> compiledParts = chain
					.compile(() -> source.getParts().subList(1, source.getParts().size() - 1)).getParts();

			Result result = () -> compiledParts;
			return result;
		});
		return eater;
	}

	private PreparedQueryPart compiledPart(String evalSql) {
		PreparedQueryPart compiledPart = mock(PreparedQueryPart.class);
		when(compiledPart.build(any())).thenReturn(new PreparedQueryPart.Result() {

			@Override
			public Optional<String> getSql() {
				return Optional.of(evalSql);
			}

			@Override
			public List<Object> getParameters() {
				return Collections.emptyList();
			}

			@Override
			public Map<String, Object> getAttributes() {
				return Collections.emptyMap();
			}
		});

		return compiledPart;
	}
}
