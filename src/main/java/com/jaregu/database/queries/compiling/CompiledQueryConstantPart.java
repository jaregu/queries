package com.jaregu.database.queries.compiling;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.building.ParamsResolver;

public class CompiledQueryConstantPart implements CompiledQueryPart {

	private String constant;

	public CompiledQueryConstantPart(String constant) {
		this.constant = requireNonNull(constant);
	}

	@Override
	public void eval(ParamsResolver variableResolver, ResultConsumer resultConsumer) {
		resultConsumer.consume(constant, Collections.emptyList());
	}

	@Override
	public List<String> getVariableNames() {
		return Collections.emptyList();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof CompiledQueryConstantPart) {
			return constant.equals(((CompiledQueryConstantPart) obj).constant);
		}
		return false;
	}
}
