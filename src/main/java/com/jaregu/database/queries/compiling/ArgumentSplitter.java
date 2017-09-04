package com.jaregu.database.queries.compiling;

import static java.lang.Character.isWhitespace;
import static java.util.Objects.requireNonNull;

public final class ArgumentSplitter {

	private final String sql;

	public static ArgumentSplitter of(String sql) {
		return new ArgumentSplitter(sql);
	}

	private ArgumentSplitter(String sql) {
		this.sql = sql;
	}

	/**
	 * Returns SQL which is split by last SQL argument/constant, which is
	 * removed
	 * 
	 * @return
	 * @throws QueryCompileException
	 */
	public Result split() throws QueryCompileException {
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
			int decimalSeparatorCount = 0;
			for (beginIndex = endIndex - 1; beginIndex >= 0; beginIndex--) {
				if (sql.charAt(beginIndex) == '.' && decimalSeparatorCount == 0) {
					decimalSeparatorCount++;
				} else if (!Character.isDigit(sql.charAt(beginIndex))) {
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
		} else {
			throw new QueryCompileException("SQL: [" + sql + "] doesn't end with constant!");
		}

		if (beginIndex < 0) {
			throw new QueryCompileException("SQL: [" + sql + "] doesn't contain correct constant!");
		}

		return new ResultImpl(sql.substring(0, beginIndex), sql.substring(beginIndex, endIndex),
				sql.substring(endIndex));
	}

	public static interface Result {

		String getBeforeSql();

		String getArgumentSql();

		String getAfterSql();
	}

	private static class ResultImpl implements Result {

		private String before;
		private String argument;
		private String after;

		public ResultImpl(String before, String argument, String after) {
			this.before = before;
			this.argument = argument;
			this.after = after;
		}

		@Override
		public String getBeforeSql() {
			return before;
		}

		@Override
		public String getArgumentSql() {
			return argument;
		}

		@Override
		public String getAfterSql() {
			return after;
		}
	}
}
