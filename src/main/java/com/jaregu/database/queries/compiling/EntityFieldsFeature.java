package com.jaregu.database.queries.compiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.common.StringSplitter;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.compiling.expr.Expression.ExpressionResult;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.CommentType;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Entity fields builder feature, used like part in SQL:
 * 
 * 
 * 
 * Example SQL:
 * 
 * <pre>
 * [SQL] SELECT -- entityFieldGenerator(template = 'column'; entityClass = 'some.package.SomeTable'; alias = 'st') 
 * FROM some_table st [SQL]
 * </pre>
 * 
 * Important note: When using
 * {@link com.jaregu.database.queries.annotation.Column} on methods, the
 * resulting SQL fields from method annotations will be in alphabetical order.
 * This is because Java doesn't guarantee any specific order when returning the
 * list.
 */
final class EntityFieldsFeature implements QueryCompilerFeature {

	enum ParameterType {
		template(true), entityClass(true), excludeColumns(false), alias(false);

		private final boolean required;

		private ParameterType(boolean required) {
			this.required = required;
		}

		public boolean isRequired() {
			return required;
		}
	}

	enum TemplateType {
		column(cf -> {
			return Collections.singletonList(ParsedQueryPart.create(cf.getColumn()));
		}, 1),

		value((cf) -> {
			return Collections.singletonList(ParsedQueryPart.create(":" + cf.getField()));
		}, 1),

		columnAndValue((cf) -> {
			return Arrays.asList(ParsedQueryPart.create(cf.getColumn() + " = "),
					ParsedQueryPart.create(":" + cf.getField()));
		}, 2);

		private final Function<ColumnField, List<ParsedQueryPart>> template;
		private final int size;

		private TemplateType(Function<ColumnField, List<ParsedQueryPart>> template, int size) {
			this.template = template;
			this.size = size;
		}

		public List<ParsedQueryPart> eval(ColumnField columnField) {
			return template.apply(columnField);
		}

		public int getSeedMultiplicator() {
			return size;
		}
	}

	interface ColumnField {

		String getColumn();

		String getField();
	}

	private static final String FEATURE_NAME = "entityFieldGenerator";

	final private ExpressionParser expressionParser;

	final private Map<String, Class<?>> entities;

	EntityFieldsFeature(ExpressionParser expressionParser, Map<String, Class<?>> entities) {
		this.expressionParser = expressionParser;
		this.entities = entities;
	}

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return parts.size() == 1 && isJpaFunctionPart(parts.get(0));
	}

	private boolean isJpaFunctionPart(ParsedQueryPart testPart) {
		return testPart.isComment() && testPart.getCommentContent().startsWith(FEATURE_NAME)
				&& expressionParser.isLikeExpression(getParametersExpression(testPart));
	}

	private String getParametersExpression(ParsedQueryPart testPart) {
		return testPart.getCommentContent().substring(FEATURE_NAME.length());
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<ParsedQueryPart> sourceParts = source.getParts();
		ParsedQueryPart generatorCommentPart = sourceParts.get(0);

		Map<ParameterType, String> parameterByType = extractParameters(
				expressionParser.parse(getParametersExpression(generatorCommentPart)));
		checkRequiredParameters(parameterByType);

		Class<?> entityClass = extractEntity(parameterByType);

		TemplateType templateType = parseTemplateType(parameterByType.get(ParameterType.template));
		Optional<String> alias = Optional.ofNullable(parameterByType.get(ParameterType.alias));
		Set<String> excludedColumns = getExcludedColumns(parameterByType);
		List<ColumnField> fields = new EntityFieldsBuilder(entityClass, excludedColumns, alias)
				.build();
		checkHasAtLeastOneField(fields);

		List<ParsedQueryPart> fieldParts = new ArrayList<>(
				fields.size() * templateType.getSeedMultiplicator() + fields.size() + 1);
		Iterator<ColumnField> fieldsIterator = fields.iterator();
		fieldParts.addAll(templateType.eval(fieldsIterator.next()));
		while (fieldsIterator.hasNext()) {
			fieldParts.add(ParsedQueryPart.create(", "));
			fieldParts.addAll(templateType.eval(fieldsIterator.next()));
		}
		if (generatorCommentPart.getCommentType() == CommentType.HYPHENS) {
			fieldParts.add(ParsedQueryPart.create("\n"));
		}

		List<PreparedQueryPart> children = compiler.compile(Source.of(fieldParts)).getParts();

		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new EnityFieldsPart(children));
			}
		};
	}

	private Map<ParameterType, String> extractParameters(List<Expression> parameters) {
		Map<ParameterType, String> parameterByType = new HashMap<>(ParameterType.values().length);
		for (Expression expression : parameters) {
			// first implementations is static, params must be constants
			ExpressionResult result = expression.eval(ParametersResolver.empty());
			for (Entry<String, ?> paramEntry : result.getOutputVariables().entrySet()) {
				if (paramEntry.getValue() == null || !(paramEntry.getValue() instanceof String)) {
					throw new QueryCompileException(
							"Entity fields generation feature expects all parameters to be String constants!");
				}
				String value = (String) paramEntry.getValue();
				ParameterType parameterType = parseParameterType(paramEntry.getKey());
				if (parameterByType.containsKey(parameterType)) {
					throw new QueryCompileException(
							"Entity fields generation feature syntax error: Multiple '" + parameterType
									+ "' parameters!");
				}
				parameterByType.put(parameterType, value);
			}
		}
		return parameterByType;
	}

	private ParameterType parseParameterType(String value) {
		try {
			return ParameterType.valueOf(value);
		} catch (RuntimeException e) {
			throw new QueryCompileException(
					"Entity fields generation feature template expects only parameters named: "
							+ Arrays.asList(ParameterType.values()) + "!");
		}
	}

	private void checkRequiredParameters(Map<ParameterType, String> parameterByType) {
		for (ParameterType parameterType : ParameterType.values()) {
			if (parameterType.isRequired() && !parameterByType.containsKey(parameterType)) {
				throw new QueryCompileException(
						"Entity fields generation feature syntax error: Rquired parameter '" + parameterType
								+ "' is not supplied!");
			}
		}
	}

	private Class<?> extractEntity(Map<ParameterType, String> parameterByType) {
		try {
			String entityAliasOrClass = parameterByType.get(ParameterType.entityClass);
			if (this.entities.containsKey(entityAliasOrClass)) {
				return entities.get(entityAliasOrClass);
			} else {
				return Class.forName(entityAliasOrClass);
			}
		} catch (ClassNotFoundException e) {
			throw new QueryCompileException(
					"Entity fields generation feature syntax error: Entity class '"
							+ parameterByType.get(ParameterType.entityClass)
							+ "' not found!");
		}
	}

	private TemplateType parseTemplateType(String value) {
		try {
			return TemplateType.valueOf(value);
		} catch (RuntimeException e) {
			throw new QueryCompileException(
					"Entity fields generation feature template parameter expected values are one of: "
							+ Arrays.asList(TemplateType.values()) + "!");
		}
	}

	private Set<String> getExcludedColumns(Map<ParameterType, String> parameterByType) {
		List<String> excluded = Optional
				.ofNullable(parameterByType.get(ParameterType.excludeColumns))
				.map(cols -> StringSplitter.on(',').split(cols))
				.orElse(Collections.emptyList());

		Set<String> excludedColumns = excluded.stream()
				.map(String::trim)
				.collect(Collectors.toSet());
		return excludedColumns;
	}

	private void checkHasAtLeastOneField(List<ColumnField> fields) {
		if (fields.isEmpty())
			throw new QueryCompileException(
					"Entity fields generation feature - entity doesn't contain any fields marked with @Column annotation!");
	}

	private static final class EnityFieldsPart implements PreparedQueryPart {

		final private List<PreparedQueryPart> children;

		public EnityFieldsPart(List<PreparedQueryPart> children) {
			this.children = children;
		}

		@Override
		public Result build(ParametersResolver resolver) {
			StringBuilder sql = new StringBuilder();
			List<Object> allParams = new LinkedList<>();
			Map<String, Object> allAttrs = new HashMap<>();
			for (PreparedQueryPart part : children) {
				Result result = part.build(resolver);
				result.getSql().ifPresent(sql::append);
				allParams.addAll(result.getParameters());
				allAttrs.putAll(result.getAttributes());
			}
			return new PreparedQueryPartResultImpl(Optional.of(sql.toString()), allParams, allAttrs);
		}

		@Override
		public String toString() {
			return children.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(children);
		}
	}
}
