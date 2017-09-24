package com.jaregu.database.queries.compiling;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Optional;

import com.jaregu.database.queries.building.ParametersResolver;

final class ConstantPart implements PreparedQueryPart {

	private final String constant;
	private final Result result;

	ConstantPart(String constant) {
		this.constant = requireNonNull(constant);
		this.result = new PreparedQueryPartResultImpl(Optional.of(constant), Collections.emptyList(),
				Collections.emptyMap());
	}

	@Override
	public String toString() {
		return constant;
	}

	@Override
	public Result build(ParametersResolver resolver) {
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof ConstantPart) {
			return constant.equals(((ConstantPart) obj).constant);
		}
		return false;
	}
}
