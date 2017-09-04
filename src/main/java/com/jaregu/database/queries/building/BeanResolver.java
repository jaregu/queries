package com.jaregu.database.queries.building;

import static java.lang.Character.toUpperCase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class BeanResolver implements NamedResolver {

	private Object bean;

	public BeanResolver(Object bean) {
		this.bean = Objects.requireNonNull(bean);

	}

	public Object getValue(String variableName) {
		boolean nested = variableName.contains(".");
		String propertyName = nested ? variableName.substring(0, variableName.indexOf(".")) : variableName;
		try {
			Object value;
			Optional<Method> getter = findGetter(propertyName);
			if (getter.isPresent()) {
				try {
					value = getter.get().invoke(bean);
				} catch (IllegalAccessException e) {
					// MME finder can find only public methods, yes it is
					// possible to access private implementation public methods
					// but other way it wouldn't be possible to access public
					// interface methods on private implementations too
					getter.get().setAccessible(true);
					value = getter.get().invoke(bean);
				}
			} else {
				Optional<Field> field = findField(propertyName);
				value = field.orElseThrow(
						() -> new QueryBuildException("No field or getter found with name: '" + propertyName + "'"))
						.get(bean);
			}

			if (nested && value != null) {
				// TODO MME use some caching
				return new BeanResolver(value).getValue(variableName.substring(variableName.indexOf(".") + 1));
			} else {
				return value;
			}

		} catch (QueryBuildException e) {
			throw new QueryBuildException("Failed to get value for property/getter with name: '" + variableName + "'",
					e.getCause() == null ? e : e.getCause());
		} catch (IllegalAccessException e) {
			throw new QueryBuildException("Failed to access property/getter with name: '" + propertyName + "'", e);
		} catch (InvocationTargetException e) {
			throw new QueryBuildException("Could not access property/getter with name: '" + propertyName + "'", e);
		}
	}

	private Optional<Field> findField(String name) {
		try {
			return Optional.of(bean.getClass().getField(name));
		} catch (NoSuchFieldException e) {
			return Optional.empty();
		}
	}

	public Optional<Method> findGetter(String propertyName) {
		String capitalizedName = capitalize(propertyName);
		try {
			return Optional.of(bean.getClass().getMethod("get" + capitalizedName));
		} catch (NoSuchMethodException e1) {
			try {
				return Optional.of(bean.getClass().getMethod("is" + capitalizedName));
			} catch (NoSuchMethodException e2) {
				return Optional.empty();
			}
		}
	}

	private static String capitalize(String s) {
		return s.isEmpty() ? s : (toUpperCase(s.charAt(0)) + s.substring(1));
	}
}
