package com.jaregu.database.queries.compiling;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.compiling.EntityFieldsFeature.ColumnField;

public class EntityFieldsBuilder {

	private final Class<?> entityClass;
	private final Set<String> excludedColumns;
	private final Optional<String> alias;

	public EntityFieldsBuilder(Class<?> entityClass, Set<String> excludedColumns, Optional<String> alias) {
		this.entityClass = entityClass;
		this.excludedColumns = excludedColumns;
		this.alias = alias;
	}

	public List<ColumnField> build() {
		String columnAliasPrefix = alias.map(a -> a + ".").orElse("");
		List<ColumnField> namesFromMethods = Stream.of(entityClass.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Column.class))
				.filter(method -> method.getName().startsWith("get")
						|| method.getName().startsWith("is")
						|| method.getName().startsWith("set"))
				.map(method -> {
					Column columnAnnotation = method.getAnnotation(Column.class);
					String field = getFieldNameFromMethodName(method);
					String columnName = getColumnName(columnAnnotation, field);
					String column = columnAliasPrefix + columnName;

					return excludedColumns.contains(columnName) ? null : new ColumnFieldImpl(column, field);
				})
				.filter(cf -> cf != null)
				.collect(Collectors.toList());

		List<ColumnField> namesFromFields = Stream.of(entityClass.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(Column.class))
				.map(f -> {
					Column columnAnnotation = f.getAnnotation(Column.class);
					String columnName = getColumnName(columnAnnotation, f.getName());
					String column = columnAliasPrefix + columnName;
					String field = f.getName();
					return excludedColumns.contains(columnName) ? null : new ColumnFieldImpl(column, field);
				})
				.filter(cf -> cf != null)
				.collect(Collectors.toList());

		return Stream.concat(namesFromFields.stream(), namesFromMethods.stream())
				.sorted(Comparator.comparing(ColumnField::getField))
				.collect(Collectors.toList());
	}

	private String getColumnName(Column columnAnnotation, String field) {
		return columnAnnotation.name().length() == 0 ? field : columnAnnotation.name();
	}

	private String getFieldNameFromMethodName(Method method) {
		String field;
		if (method.getName().startsWith("get")
				|| method.getName().startsWith("set")) {
			field = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
		} else {
			field = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
		}
		return field;
	}

	private static class ColumnFieldImpl implements ColumnField {

		private final String column;
		private final String field;

		public ColumnFieldImpl(String column, String field) {
			this.column = column;
			this.field = field;
		}

		@Override
		public String toString() {
			return "ColumnField[column=" + column + ", field=" + field + "]";
		}

		@Override
		public String getColumn() {
			return column;
		}

		@Override
		public String getField() {
			return field;
		}
	}
}