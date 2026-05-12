package com.jaregu.database.queries.ext.spring;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import com.jaregu.database.queries.annotation.Column;

/**
 * {@link RowMapper} that honours {@link Column} annotations on the target
 * class, with a fall-back to Spring's standard snake_case → camelCase
 * convention for unannotated properties.
 *
 * <p>Supports both:
 * <ul>
 *   <li><b>JavaBean targets</b> — no-arg constructor + setters. Properties
 *       are matched first by {@code @Column(name = ...)} on the field or
 *       getter/setter, then by name.</li>
 *   <li><b>Java 17 record targets</b> — bound via the canonical constructor.
 *       Record components are matched by {@code @Column(name = ...)} on the
 *       field/accessor (annotations on record components propagate to both),
 *       then by component name.</li>
 * </ul>
 *
 * <p>One mapper instance can map any number of result rows; reflection
 * metadata is computed once at construction.
 */
public final class SpringColumnAwareRowMapper<T> implements RowMapper<T> {

	private final Class<T> mappedClass;
	private final boolean isRecord;

	/** Lower-cased SQL column name → java property/component name. */
	private final Map<String, String> columnToProperty;

	/** Record-only: canonical constructor and parameter types (positional). */
	private final Constructor<T> recordCtor;
	private final Class<?>[] recordParamTypes;
	/** Record-only: component name → index in the canonical constructor. */
	private final Map<String, Integer> recordComponentIndex;

	/** Bean-only: property name → setter/field descriptor for fast lookup. */
	private final Map<String, PropertyDescriptor> beanProperties;

	/**
	 * Returns the appropriate {@link RowMapper} for the given row class:
	 * <ul>
	 *   <li>{@link SingleColumnRowMapper} for simple value types
	 *       ({@code Integer}, {@code String}, {@code UUID}, etc.) — typical
	 *       for {@code SELECT COUNT(*)} or single-column aggregates.</li>
	 *   <li>{@link SpringColumnAwareRowMapper} for everything else (records,
	 *       JavaBeans).</li>
	 * </ul>
	 */
	public static <T> RowMapper<T> forClass(Class<T> rowClass) {
		if (BeanUtils.isSimpleValueType(rowClass)) {
			return new SingleColumnRowMapper<>(rowClass);
		}
		return new SpringColumnAwareRowMapper<>(rowClass);
	}

	@SuppressWarnings("unchecked")
	public SpringColumnAwareRowMapper(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.isRecord = mappedClass.isRecord();
		this.columnToProperty = readColumnAnnotations(mappedClass);

		if (isRecord) {
			RecordComponent[] comps = mappedClass.getRecordComponents();
			this.recordParamTypes = new Class<?>[comps.length];
			this.recordComponentIndex = new LinkedHashMap<>(comps.length);
			for (int i = 0; i < comps.length; i++) {
				recordParamTypes[i] = comps[i].getType();
				recordComponentIndex.put(comps[i].getName().toLowerCase(Locale.ROOT), i);
			}
			try {
				this.recordCtor = mappedClass.getDeclaredConstructor(recordParamTypes);
				this.recordCtor.setAccessible(true);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(
						"Record " + mappedClass.getName() + " has no canonical constructor", e);
			}
			this.beanProperties = Collections.emptyMap();
		} else {
			this.recordCtor = null;
			this.recordParamTypes = null;
			this.recordComponentIndex = Collections.emptyMap();

			Map<String, PropertyDescriptor> props = new HashMap<>();
			for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
				if (pd.getWriteMethod() != null) {
					props.put(pd.getName().toLowerCase(Locale.ROOT), pd);
				}
			}
			this.beanProperties = props;
		}
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return isRecord ? mapRecord(rs) : mapBean(rs);
	}

	private T mapBean(ResultSet rs) throws SQLException {
		T instance = BeanUtils.instantiateClass(mappedClass);
		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			String columnName = JdbcUtils.lookupColumnName(rsmd, i);
			String propertyName = resolveProperty(columnName);
			PropertyDescriptor pd = beanProperties.get(propertyName.toLowerCase(Locale.ROOT));
			if (pd != null) {
				Object value = JdbcUtils.getResultSetValue(rs, i, pd.getPropertyType());
				wrapper.setPropertyValue(pd.getName(), value);
			}
		}
		return instance;
	}

	private T mapRecord(ResultSet rs) throws SQLException {
		Object[] args = new Object[recordParamTypes.length];
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			String columnName = JdbcUtils.lookupColumnName(rsmd, i);
			String propertyName = resolveProperty(columnName);
			Integer idx = recordComponentIndex.get(propertyName.toLowerCase(Locale.ROOT));
			if (idx != null) {
				args[idx] = JdbcUtils.getResultSetValue(rs, i, recordParamTypes[idx]);
			}
		}
		try {
			return recordCtor.newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw new SQLException(
					"Failed to instantiate record " + mappedClass.getName(), e);
		}
	}

	/**
	 * Resolves an SQL column name to a Java property/component name. First
	 * checks {@code @Column}-derived mappings; otherwise falls back to the
	 * standard snake_case → camelCase derivation used by Spring's default
	 * row mappers.
	 */
	private String resolveProperty(String columnName) {
		String mapped = columnToProperty.get(columnName.toLowerCase(Locale.ROOT));
		return mapped != null ? mapped : underscoreToCamelCase(columnName);
	}

	/**
	 * Walks declared fields and methods of {@code cls} and returns a map of
	 * lower-cased {@code @Column(name)} → java member name.
	 */
	private static Map<String, String> readColumnAnnotations(Class<?> cls) {
		Map<String, String> map = new HashMap<>();
		for (Field f : cls.getDeclaredFields()) {
			Column c = f.getAnnotation(Column.class);
			if (c != null && !c.name().isEmpty()) {
				map.put(c.name().toLowerCase(Locale.ROOT), f.getName());
			}
		}
		for (Method m : cls.getDeclaredMethods()) {
			Column c = m.getAnnotation(Column.class);
			if (c != null && !c.name().isEmpty()) {
				String property = methodToPropertyName(m.getName());
				if (property != null) {
					map.putIfAbsent(c.name().toLowerCase(Locale.ROOT), property);
				}
			}
		}
		return map;
	}

	private static String methodToPropertyName(String methodName) {
		if (methodName.startsWith("get") && methodName.length() > 3) {
			return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
		}
		if (methodName.startsWith("is") && methodName.length() > 2) {
			return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
		}
		if (methodName.startsWith("set") && methodName.length() > 3) {
			return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
		}
		// Bare accessor (record-style): "name()" → "name"
		return methodName;
	}

	/** snake_case → camelCase (e.g. "short_description" → "shortDescription"). */
	private static String underscoreToCamelCase(String name) {
		if (name.indexOf('_') < 0) {
			return name;
		}
		StringBuilder sb = new StringBuilder(name.length());
		boolean upperNext = false;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '_') {
				upperNext = true;
			} else if (upperNext) {
				sb.append(Character.toUpperCase(c));
				upperNext = false;
			} else {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.toString();
	}

}
