package com.jaregu.database.queries.compiling.expr;

public enum BlockSymbol implements Separator {

	START("("), END(")");

	private String sequence;

	private BlockSymbol(String sequence) {
		this.sequence = sequence;
	}

	@Override
	public String getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		return sequence;
	}
}