package com.jaregu.database.queries.compiling;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.annotation.Table;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

@RunWith(MockitoJUnitRunner.class)
public class EntityFieldsFeatureTest {

	@Spy
	private ExpressionParser expressionParser = ExpressionParser.defaultParser();

	@Mock
	private Compiler compiler;

	@Mock
	private ParametersResolver resolver;

	private Map<String, Class<?>> entities;

	private EntityFieldsFeature feature;

	@Before
	public void setUp() {
		when(compiler.compile(any())).thenAnswer((i) -> {
			Source source = i.getArgument(0);
			QueryCompilerFeature.Result result = () -> source.getParts()
					.stream()
					.map(pqp -> PreparedQueryPart.constant(pqp.getContent()))
					.collect(Collectors.toList());
			return result;
		});

		entities = new HashMap<>();

		feature = new EntityFieldsFeature(expressionParser, entities);
	}

	@Test(expected = QueryCompileException.class)
	public void testCompileUknownTemplate() {
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'xxx' "
				+ "entityClass = '" + SomeTable.class.getName() + "') */");
		compilePart(part);
	}

	@Test
	public void testCompileColumns() {
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'column' "
				+ "entityClass = '" + SomeTable.class.getName() + "') */");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("first, second_col, third_col, fourth_col");
	}

	@Test
	public void testCompileColumnsWithAlias() {
		ParsedQueryPart part = ParsedQueryPart.create("-- entityFieldGenerator(template = 'column' "
				+ "entityClass = '" + SomeTable.class.getName() + "' alias = 'x')");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get())
				.isEqualToNormalizingWhitespace("x.first, x.second_col, x.third_col, x.fourth_col");
	}

	@Test
	public void testCompileColumnsWithExclude() {
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'column' "
				+ "entityClass = '" + SomeTable.class.getName() + "' excludeColumns = 'first, fourth_col') */");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("second_col, third_col");
	}

	@Test
	public void testCompileColumnsWithExcludeAndAlias() {
		ParsedQueryPart part = ParsedQueryPart.create("-- entityFieldGenerator(template = 'column' "
				+ "entityClass = '" + SomeTable.class.getName() + "' "
				+ "excludeColumns = 'first, fourth_col' "
				+ "alias = 'x')");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("x.second_col, x.third_col");
	}

	@Test
	public void testCompileValueWithExcludeAndAlias() {
		ParsedQueryPart part = ParsedQueryPart.create("-- entityFieldGenerator(template = 'value' "
				+ "entityClass = '" + SomeTable.class.getName() + "' "
				+ "excludeColumns = 'first, fourth_col' "
				+ "alias = 'notUsed')");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace(":second, :third");
	}

	@Test
	public void testCompileColumnAndValueWithExcludeAndAlias() {
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'columnAndValue' "
				+ "entityClass = '" + SomeTable.class.getName() + "' "
				+ "excludeColumns = 'first, fourth_col' "
				+ "alias = 'x') */");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("x.second_col = :second, x.third_col = :third");
	}

	@Test
	public void testEntityAliasSimpleClassName() {
		entities.put(SomeTable.class.getSimpleName(), SomeTable.class);
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'column' "
				+ "entityClass = 'SomeTable' excludeColumns = 'first, fourth_col') */");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("second_col, third_col");
	}

	@Test
	public void testEntitysAliasSomeString() {
		entities.put("AaA", SomeTable.class);
		ParsedQueryPart part = ParsedQueryPart.create("/* entityFieldGenerator(template = 'column' "
				+ "entityClass = 'AaA' excludeColumns = 'first, fourth_col') */");

		PreparedQueryPart.Result build = compilePart(part);

		assertThat(build.getSql().get()).isEqualToNormalizingWhitespace("second_col, third_col");
	}

	private PreparedQueryPart.Result compilePart(ParsedQueryPart part) {
		QueryCompilerFeature.Result result = feature.compile(Source.of(singletonList(part)), compiler);
		assertThat(result.getParts()).hasSize(1);
		PreparedQueryPart.Result build = result.getParts().get(0).build(resolver);
		return build;
	}

	@Table(name = "some_table")
	public static class SomeTable {

		Long ignored;

		@Column
		private Long first;

		private Long secondSecond;

		private boolean thirdAsBool;

		private String fourthIsString;

		public void setFirst(Long first) {
			this.first = first;
		}

		public Long getFirst() {
			return first;
		}

		public void setSecond(Long secondSecond) {
			this.secondSecond = secondSecond;
		}

		@Column(name = "second_col")
		public Long getSecond() {
			return secondSecond;
		}

		public void setThird(boolean third) {
			this.thirdAsBool = third;
		}

		@Column(name = "third_col")
		public boolean isThird() {
			return thirdAsBool;
		}

		public String getFourth() {
			return fourthIsString;
		}

		@Column(name = "fourth_col")
		public void setFourth(String fourth) {
			this.fourthIsString = fourth;
		}
	}
}
