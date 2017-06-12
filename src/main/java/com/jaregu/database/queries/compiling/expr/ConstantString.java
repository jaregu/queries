package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public class ConstantString extends ConstantBaseImpl<String> {

	public ConstantString(String value) {
		super(value);
	}

	@Override
	public Object add(Object object) {
		if (object != null) {
			return getValue() + object.toString();
		} else {
			return super.add(object);
		}
	}

	public static Optional<Constant> parse(String expression) {
		if (expression.startsWith(StringSymbol.SYMBOL.getSequence())) {
			if (!expression.endsWith(StringSymbol.SYMBOL.getSequence())) {
				throw new ExpressionParseException(
						"Can't parse string constant expression, string is not closed: " + expression + "!");
			}
			return Optional.of(new ConstantString(expression.substring(1, expression.length() - 1)
					.replace(StringSymbol.ESCAPE.getSequence(), StringSymbol.SYMBOL.getSequence())));
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
