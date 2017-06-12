package com.jaregu.database.queries.compiling;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.compiling.CompiledQueryPart.ResultConsumer;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.SourceQuery;
import com.jaregu.database.queries.parsing.SourceQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class QueryCompilerImplEaterTest {

	private QueryId goodFood = QueryId.of("good.food");

	@Mock
	private ExpressionParser interior;

	@Mock
	private QueriesConfig reservations;

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
		QueryCompilerImpl kitchen = new QueryCompilerImpl(reservations, interior, Arrays.asList(eaters));
		return (food) -> serve(cook(kitchen, food));
	}

	private List<CompiledQueryPart> cook(QueryCompilerImpl kitchen, String... food) {

		SourceQuery meal = mock(SourceQuery.class);
		List<SourceQueryPart> portions = Arrays.asList(food).stream().map(SourceQueryPart::create)
				.collect(Collectors.toList());
		when(meal.getParts()).thenReturn(portions);
		when(meal.getQueryId()).thenReturn(goodFood);

		CompiledQuery waitress = kitchen.compile(meal);

		assertEquals(goodFood, waitress.getQueryId());
		return waitress.getParts();
	}

	private String serve(List<CompiledQueryPart> meal) {
		StringBuilder droppings = new StringBuilder();
		meal.forEach(d -> d.eval(null, (s, v) -> {
			droppings.append(s);
		}));
		return droppings.toString();
	}

	private QueryCompilerFeature eatSequences(String eatWhat, int minPortions) {
		QueryCompilerFeature eater = mock(QueryCompilerFeature.class);

		Function<Source, Boolean> isCompilable = source -> source.getParts().size() == minPortions
				&& source.getParts().stream().map(SourceQueryPart::getContent).allMatch(s -> s.startsWith(eatWhat));

		when(eater.isCompilable(any())).then(i -> {
			Source source = (Source) i.getArgument(0);
			return isCompilable.apply(source);
		});
		when(eater.compile(any(), any())).thenAnswer(i -> {
			Source source = (Source) i.getArgument(0);
			//Compiler chain = (Compiler) i.getArgument(1);
			if (!isCompilable.apply(source)) {
				throw new IllegalStateException("I didn't order this!");
			}
			List<CompiledQueryPart> compiledParts = source.getParts().stream().map(SourceQueryPart::getContent)
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
			List<CompiledQueryPart> compiledParts = chain
					.compile(() -> source.getParts().subList(1, source.getParts().size() - 1)).getCompiledParts();

			Result result = () -> compiledParts;
			return result;
		});
		return eater;
	}

	private CompiledQueryPart compiledPart(String evalSql) {
		CompiledQueryPart compiledPart = mock(CompiledQueryPart.class);
		doAnswer(i -> {
			ResultConsumer consumer = i.getArgument(1);
			consumer.consume(evalSql, Collections.emptyList());
			return null;
		}).when(compiledPart).eval(any(), any());
		return compiledPart;
	}
}