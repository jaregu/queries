package com.jaregu.database.queries.compiling.expr;

public enum ExpressionSeparator implements Separator {

	SEMICOLON(";");

	private String sequence;

	private ExpressionSeparator(String sequence) {
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
