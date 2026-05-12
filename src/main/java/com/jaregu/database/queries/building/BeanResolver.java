package com.jaregu.database.queries.building;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BeanResolver implements NamedResolver {

	/**
	 * Per-class accessor map. Built lazily on first {@code BeanResolver}
	 * construction for a given class, then reused for the lifetime of the JVM.
	 * Eliminates both repeat reflection lookups and the exception-driven
	 * control flow that the previous implementation paid on every miss.
	 */
	private static final ConcurrentMap<Class<?>, Map<String, Accessor>> ACCESSORS = new ConcurrentHashMap<>();

	private final Object bean;
	private final Map<String, Accessor> accessors;

	public BeanResolver(Object bean) {
		this.bean = Objects.requireNonNull(bean);
		this.accessors = ACCESSORS.computeIfAbsent(bean.getClass(), BeanResolver::buildAccessors);
	}

	protected Object getBean() {
		return bean;
	}

	@Override
	public Object getValue(String variableName) {
		boolean nested = variableName.contains(".");
		String propertyName = nested ? variableName.substring(0, variableName.indexOf(".")) : variableName;
		Accessor accessor = accessors.get(propertyName);
		if (accessor == null) {
			throw new QueryBuildException("Failed to get value for property/getter with name: '" + variableName + "'",
					new QueryBuildException("No field or getter found with name: '" + propertyName + "'"));
		}
		try {
			Object value = accessor.get(bean);
			if (nested && value != null) {
				return new BeanResolver(value).getValue(variableName.substring(variableName.indexOf(".") + 1));
			}
			return value;
		} catch (IllegalAccessException e) {
			throw new QueryBuildException("Failed to access property/getter with name: '" + propertyName + "'", e);
		} catch (InvocationTargetException e) {
			throw new QueryBuildException("Could not access property/getter with name: '" + propertyName + "'", e);
		}
	}

	/**
	 * Looks up the accessor method for {@code propertyName}. Retained for
	 * backwards-compatible subclass access; new code should rely on
	 * {@link #getValue(String)} which routes through the cached accessor map.
	 */
	public Optional<Method> findGetter(String propertyName) {
		Accessor accessor = accessors.get(propertyName);
		if (accessor instanceof MethodAccessor m) {
			return Optional.of(m.method);
		}
		return Optional.empty();
	}

	/**
	 * Builds the accessor map for a class in a single pass over its public
	 * methods + fields. Priority order matches the legacy implementation:
	 * <ol>
	 *   <li>{@code getXxx()} (JavaBean getter)</li>
	 *   <li>{@code isXxx()} (JavaBean boolean getter)</li>
	 *   <li>{@code xxx()} (Java 17 record-style accessor / any no-arg public
	 *       method without a prefix)</li>
	 *   <li>public field</li>
	 * </ol>
	 * The first hit for a given property name wins.
	 */
	private static Map<String, Accessor> buildAccessors(Class<?> cls) {
		Map<String, MethodAccessor> getters = new HashMap<>();
		Map<String, MethodAccessor> isGetters = new HashMap<>();
		Map<String, MethodAccessor> recordStyle = new HashMap<>();

		for (Method m : cls.getMethods()) {
			if (m.getParameterCount() != 0) {
				continue;
			}
			if (m.getReturnType() == void.class) {
				continue;
			}
			String name = m.getName();
			if (name.indexOf('$') >= 0) {
				// Synthetic / lambda methods
				continue;
			}
			if (name.length() > 3 && name.startsWith("get") && isUpperOrDigit(name.charAt(3))) {
				getters.putIfAbsent(decapitalize(name.substring(3)), new MethodAccessor(m));
			} else if (name.length() > 2 && name.startsWith("is") && isUpperOrDigit(name.charAt(2))) {
				isGetters.putIfAbsent(decapitalize(name.substring(2)), new MethodAccessor(m));
			} else {
				// Record components (id(), label()) and any other no-arg
				// public methods. Bridges, hashCode(), toString() etc. land
				// here too — matching the previous implementation.
				recordStyle.putIfAbsent(name, new MethodAccessor(m));
			}
		}

		Map<String, Accessor> map = new HashMap<>(getters.size() + isGetters.size() + recordStyle.size() + 8);
		map.putAll(getters);
		isGetters.forEach(map::putIfAbsent);
		recordStyle.forEach(map::putIfAbsent);

		for (Field f : cls.getFields()) {
			map.putIfAbsent(f.getName(), new FieldAccessor(f));
		}

		return Map.copyOf(map);
	}

	private static boolean isUpperOrDigit(char c) {
		return Character.isUpperCase(c) || Character.isDigit(c);
	}

	private static String decapitalize(String s) {
		if (s.isEmpty()) {
			return s;
		}
		// Mirror java.beans.Introspector.decapitalize: leave fully-uppercase
		// prefixes alone (URL stays URL, not uRL).
		if (s.length() > 1 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
			return s;
		}
		return toLowerCase(s.charAt(0)) + s.substring(1);
	}

	@SuppressWarnings("unused")
	private static String capitalize(String s) {
		// Retained for any external callers via reflection; new code goes
		// through the prebuilt accessor map.
		return s.isEmpty() ? s : (toUpperCase(s.charAt(0)) + s.substring(1));
	}

	sealed interface Accessor permits MethodAccessor, FieldAccessor {
		Object get(Object bean) throws IllegalAccessException, InvocationTargetException;
	}

	static final class MethodAccessor implements Accessor {
		private final Method method;
		private volatile boolean accessibleSet;

		MethodAccessor(Method method) {
			this.method = method;
		}

		@Override
		public Object get(Object bean) throws IllegalAccessException, InvocationTargetException {
			if (!accessibleSet) {
				try {
					return method.invoke(bean);
				} catch (IllegalAccessException retry) {
					// Public method on a non-public class — flip the
					// accessibility once, then cache the result.
					method.setAccessible(true);
					accessibleSet = true;
					return method.invoke(bean);
				}
			}
			return method.invoke(bean);
		}
	}

	static final class FieldAccessor implements Accessor {
		private final Field field;

		FieldAccessor(Field field) {
			this.field = field;
		}

		@Override
		public Object get(Object bean) throws IllegalAccessException {
			return field.get(bean);
		}
	}
}
