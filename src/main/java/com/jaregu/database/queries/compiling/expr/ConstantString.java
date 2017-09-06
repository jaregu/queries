package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public final class ConstantString extends ConstantBaseImpl<String> {

	final private static String SYMBOL = "'";
	final private static String ESCAPE = "''";

	public ConstantString(String value) {
		super(value);
	}

	@Override
	public Object add(Operand operand) {
		Object otherValue = operand.getValue();
		if (otherValue != null) {
			return getValue() + otherValue.toString();
		} else {
			return super.add(operand);
		}
	}

	public static Optional<Constant> parse(String expression) {
		if (expression.startsWith(SYMBOL)) {
			if (!expression.endsWith(SYMBOL)) {
				throw new ExpressionParseException(
						"Can't parse string constant expression, string is not closed: " + expression + "!");
			}
			return Optional
					.of(new ConstantString(expression.substring(1, expression.length() - 1).replace(ESCAPE, SYMBOL)));
		} else {
			return Optional.empty();
		}
	}

	public static Optional<Constant> of(Object value) {
		if (value instanceof String) {
			return Optional.of(new ConstantString((String) value));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public String toString() {
		return "\"" + getValue() + "\"";
	}
}
