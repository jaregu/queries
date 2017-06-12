package com.jaregu.database.queries.compiling.expr;

public enum StringSymbol implements Separator {

	SYMBOL("'"), ESCAPE("''");

	private String sequence;

	private StringSymbol(String sequence) {
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
