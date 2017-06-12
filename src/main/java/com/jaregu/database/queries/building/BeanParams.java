package com.jaregu.database.queries.building;

import static java.lang.Character.toUpperCase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class BeanParams implements ParamsResolver {

	private Object bean;

	public static void main(String[] args) {

		BeanParams params = new BeanParams(new SomeBase());

		System.out.println(params.getValue("aaa"));
		System.out.println(params.getValue("bbb"));
		System.out.println(params.getValue("ccc"));
		System.out.println(params.getValue("ddd"));
		System.out.println(params.getValue("eee"));

		System.out.println(params.getValue("ddd.aaa"));
		System.out.println(params.getValue("ddd.bbb"));
		System.out.println(params.getValue("ddd.ccc"));
		System.out.println(params.getValue("ddd.ddd"));

		System.out.println(params.getValue("eee.aaa"));

	}

	public BeanParams(Object bean) {
		this.bean = Objects.requireNonNull(bean);

	}

	public Object getValue(String variableName) {
		boolean nested = variableName.contains(".");
		String propertyName = nested ? variableName.substring(0, variableName.indexOf(".")) : variableName;
		try {
			if (variableName.contains(".")) {

			}
			Object value; //.map(method -> )
			Optional<Method> getter = findGetter(propertyName);
			if (getter.isPresent()) {
				value = getter.get().invoke(bean);
			} else {
				Optional<Field> field = findField(propertyName);
				value = field.orElseThrow(
						() -> new QueryBuildException("No field or getter found with name: '" + propertyName + "'"))
						.get(bean);
			}

			if (nested && value != null) {
				return new BeanParams(value).getValue(variableName.substring(variableName.indexOf(".") + 1));
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
		} catch (NoSuchMethodException e) {
			try {
				return Optional.of(bean.getClass().getMethod("is" + capitalizedName));
			} catch (NoSuchMethodException e1) {
				return Optional.empty();
			}
		}
	}

	private static String capitalize(String s) {
		return s.isEmpty() ? s : (toUpperCase(s.charAt(0)) + s.substring(1));
	}

	public static class SomeBase {

		public int aaa = 123;
		private String bbb = "string value";
		private Integer ccc = 1234;
		private SomeChild ddd = new SomeChild();
		public SomeChild eee = null;

		public String getBbb() {
			return bbb;
		}

		public Integer getCcc() {
			return ccc;
		}

		public SomeChild getDdd() {
			return ddd;
		}
	}

	public static class SomeChild {

		private byte aaa = 11;
		private String bbb = "child string";
		protected String ccc = "some protected field";
		public Number ddd = 123d;

		public byte getAaa() {
			return aaa;
		}

		public String getBbb() {
			return bbb;
		}

		public String getCcc() {
			return ccc;
		}

		public Number getDdd() {
			return ddd;
		}
	}

}
