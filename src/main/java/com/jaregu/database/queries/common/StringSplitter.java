package com.jaregu.database.queries.common;

import java.util.ArrayList;
import java.util.List;

public class StringSplitter {

	private final char separator;
	private final boolean include;

	public StringSplitter(char separator, boolean include) {
		this.separator = separator;
		this.include = include;
	}

	public static StringSplitter on(char separator) {
		return new StringSplitter(separator, false);
	}

	public StringSplitter includeSeparator(boolean include) {
		return new StringSplitter(separator, include);
	}

	public List<String> split(String text) {
		int offset = 0;
		int next = 0;
		final char ch = '\n';
		List<String> lines = new ArrayList<>();
		while ((next = text.indexOf(ch, offset)) != -1) {
			lines.add(text.substring(offset, include ? next + 1 : next));
			offset = next + 1;
		}
		if (offset < text.length()) {
			lines.add(text.substring(offset, text.length()));
		}
		return lines;
	}
}