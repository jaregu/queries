package com.jaregu.database.queries.compiling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
				.extracting(ColumnField::getField, ColumnField::getColumn)
				.containsExactly(
						tuple("first", "first"),
						tuple("secondSecond", "second_col"),
						tuple("thirdField", "third_col"),
						tuple("fiveIsString", "fifth_col"),
						tuple("fourth", "fourth_col"),
						tuple("sixt", "six_col"));
	}

	@Test
	public void testExcludeWithAlias() {

		EntityFieldsBuilder test = new EntityFieldsBuilder(SomeTable.class,
				new HashSet<>(Arrays.asList("second_col", "third_col", "six_col", "fifth_col")), Optional.of("x"));
		List<ColumnField> columnList = test.build();

		assertThat(columnList)
				.hasSize(2)
				.extracting(ColumnField::getField, ColumnField::getColumn)
				.containsExactly(tuple("first", "x.first"),
						tuple("fourth", "x.fourth_col"));

	}

	@Table(name = "some_table")
	public static class SomeTable {

		Long ignored;

		@Column
		private Long first;

		@Column(name = "second_col")
		private Long secondSecond;

		@Column(name = "third_col")
		private Long thirdField;

		private boolean sixAsBool;

		private String fiveIsString;

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

		public Long getSecond() {
			return secondSecond;
		}

		public void setSixt(boolean third) {
			this.sixAsBool = third;
		}

		@Column(name = "six_col")
		public boolean isSixt() {
			return sixAsBool;
		}

		public String getFourthIsString() {
			return fourthIsString;
		}

		@Column(name = "fourth_col")
		public void setFourth(String fourth) {
			this.fourthIsString = fourth;
		}

		public void setFiveIsString(String fiveIsString) {
			this.fiveIsString = fiveIsString;
		}

		@Column(name = "fifth_col")
		public String getFiveIsString() {
			return fiveIsString;
		}
	}
}
