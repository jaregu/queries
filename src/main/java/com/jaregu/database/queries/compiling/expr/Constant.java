package com.jaregu.database.queries.compiling.expr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Constant extends Operand {

	static final List<Function<String, Optional<Constant>>> STRING_PARSERS = Arrays.asList(ConstantNull::parse,
			ConstantBoolean::parse, ConstantString::parse, ConstantInteger::parse, ConstantLong::parse,
			ConstantDecimal::parse);
	static final List<Function<Object, Optional<Constant>>> VALUE_PARSERS = Arrays.asList(ConstantNull::of,
			ConstantBoolean::of, ConstantString::of, ConstantInteger::of, ConstantLong::of, ConstantDecimal::of);

	public static Optional<Constant> parse(String value) {
		for (Function<String, Optional<Constant>> parser : STRING_PARSERS) {
			Optional<Constant> result = parser.apply(value);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	public static Constant of(Object value) {
		for (Function<Object, Optional<Constant>> parser : VALUE_PARSERS) {
			Optional<Constant> result = parser.apply(value);
			if (result.isPresent()) {
				return result.get();
			}
		}
		return new ConstantObject(value);
	}
}
