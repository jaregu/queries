package com.jaregu.database.queries.compiling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.annotation.Table;
import com.jaregu.database.queries.compiling.EntityFieldsFeature.ColumnField;

public class EntityFieldsBuilderTest {

	@Test
	public void testBuild() {

		EntityFieldsBuilder test = new EntityFieldsBuilder(SomeTable.class,
				Collections.emptySet(), Optional.empty());
		List<ColumnField> columnList = test.build();

		assertThat(columnList)
				.hasSize(4)
				.extracting(ColumnField::getField, ColumnField::getColumn)
				.containsOnly(
						tuple("first", "first"),
						tuple("second", "second_col"),
						tuple("third", "third_col"),
						tuple("fourth", "fourth_col"));
	}

	@Test
	public void testExcludeWithAlias() {

		EntityFieldsBuilder test = new EntityFieldsBuilder(SomeTable.class,
				new HashSet<>(Arrays.asList("second_col", "third_col")), Optional.of("x"));
		List<ColumnField> columnList = test.build();

		assertThat(columnList)
				.hasSize(2)
				.extracting(ColumnField::getField, ColumnField::getColumn)
				.containsOnly(tuple("first", "x.first"),
						tuple("fourth", "x.fourth_col"));

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
