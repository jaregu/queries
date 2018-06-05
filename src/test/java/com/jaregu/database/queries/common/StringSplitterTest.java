package com.jaregu.database.queries.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class StringSplitterTest {

	@Test
	public void testSplit() {
		List<String> parts = StringSplitter.on(',').split("aaa,bbb,ccc");
		assertThat(parts).containsExactly("aaa", "bbb", "ccc");
	}

	@Test
	public void testSplitWithSeperator() {
		List<String> parts = StringSplitter.on(',').includeSeparator(true).split("aaa,bbb,ccc");
		assertThat(parts).containsExactly("aaa,", "bbb,", "ccc");
	}

	@Test
	public void testSplitNewLine() {
		List<String> parts = StringSplitter.on('\n').split("aaa\nbbb\nccc");
		assertThat(parts).containsExactly("aaa", "bbb", "ccc");
	}
}
