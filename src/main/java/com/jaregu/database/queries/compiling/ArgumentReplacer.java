package com.jaregu.database.queries.compiling;

import static java.lang.Character.isWhitespace;
import static java.util.Objects.requireNonNull;

public final class ArgumentReplacer {

	private final String sql;
	private final boolean addComment;

	public static ArgumentReplacer forSql(String sql) {
		return new ArgumentReplacer(sql, false);
	}

	private ArgumentReplacer(String sql, boolean addComment) {
		this.sql = sql;
		this.addComment = addComment;
	}

	public ArgumentReplacer addComment(boolean addComment) {
		return new ArgumentReplacer(sql, addComment);
	}

	/**
	 * Returns SQL in which last SQL argument is replaced with ? sign
	 * 
	 * @return
	 * @throws QueryCompileException
	 */
	public String replace() throws QueryCompileException {
		int endIndex;
		for (endIndex = requireNonNull(sql).length(); endIndex > 0; endIndex--) {
			char currentChar = sql.charAt(endIndex - 1);
			if (currentChar != ',' && !isWhitespace(currentChar)) {
				break;
			}
		}
		if (endIndex == 0) {
			throw new QueryCompileException("SQL: [" + sql + "] does not contain value parameters!");
		}

		char lastChar = sql.charAt(endIndex - 1);
		int beginIndex = -1;
		if (lastChar == '\'') {
			int searchIndex = endIndex - 1;
			do {
				beginIndex = sql.substring(0, searchIndex).lastIndexOf('\'');
				if (beginIndex > 0 && sql.charAt(beginIndex - 1) == '\'') {
					searchIndex = beginIndex - 1;
					beginIndex = -1;
				} else {
					searchIndex = beginIndex;
				}
			} while (beginIndex < 0 && searchIndex > 0);

		} else if (Character.isDigit(lastChar)) {
			for (beginIndex = endIndex - 1; beginIndex >= 0; beginIndex--) {
				if (!Character.isDigit(sql.charAt(beginIndex))) {
					break;
				}
			}
			beginIndex++;
		} else if (lastChar == ')') {
			boolean insideString = false;
			int bracketCount = 0;
			for (beginIndex = endIndex - 1; beginIndex >= 0; beginIndex--) {
				char c = sql.charAt(beginIndex);
				if (c == ')' && !insideString) {
					bracketCount++;
				} else if (c == '(' && !insideString) {
					bracketCount--;
					if (bracketCount == 0) {
						break;
					}
				} else if (c == '\'') {
					insideString = !insideString;
				}
			}
		} else if (endIndex - 4 >= 0 && sql.substring(endIndex - 4, endIndex).equalsIgnoreCase("null")) {
			beginIndex = endIndex - 4;
		} else if (lastChar == '?') {
			return sql;
		} else {
			throw new QueryCompileException("SQL: [" + sql + "] doesn't end with constant or binded value parameter!");
		}

		if (beginIndex < 0) {
			throw new QueryCompileException(
					"SQL: [" + sql + "] doesn't contain correct constant or binded value parameter!");
		}

		return sql.substring(0, beginIndex) + "?" + sql.substring(endIndex)
				+ (addComment ? " /* " + sql.substring(beginIndex, endIndex) + " */ " : "");
	}
}
