package com.jaregu.database.queries.compiling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.jaregu.database.queries.compiling.ArgumentSplitter.Result;

public class ArgumentSplitterTest {

	@Test
	public void testErrors() {
		error(() -> getSplitted(""));
		error(() -> getSplitted(" "));
		error(() -> getSplitted("    "));
		error(() -> getSplitted("aaaaa'"));
		error(() -> getSplitted(" ? "));
		error(() -> getSplitted("aa''aaa'"));
		error(() -> getSplitted("'''"));
		error(() -> getSplitted(null));
	}

	private Result getSplitted(String part) {
		return ArgumentSplitter.of(part).split();
	}

	@Test
	public void testStringConstant() {
		test("'aaa'", "", "'aaa'", "");
		test("''", "", "''", "");
		test("''''", "", "''''", "");

		test("'aaa tim''s money'", "", "'aaa tim''s money'", "");
		test("'aaa' ", "", "'aaa'", " ");
		test("  'aaa' ", "  ", "'aaa'", " ");
		test("something here 'aaa', ", "something here ", "'aaa'", ", ");
	}

	@Test
	public void testNumberConstant() {
		test("123", "", "123", "");
		test("1", "", "1", "");
		test("11.12", "", "11.12", "");
		test("9998787", "", "9998787", "");

		test("123,", "", "123", ",");
		test("123.11  , ", "", "123.11", "  , ");
		test("   123.2", "   ", "123.2", "");
		test(" some sql  123.321 ", " some sql  ", "123.321", " ");

	}

	@Test
	public void testSomeFunctions() {
		test("(xxx)", "", "(xxx)", "");
		test("(aaa(xxx, 'something', orthis))", "", "(aaa(xxx, 'something', orthis))", "");
		test("(some) ((aaaa), (1211, (12))) ", "(some) ", "((aaaa), (1211, (12)))", " ");
		test("(aaa(bbb))(aaa  (xxx, 'something', orthis))  ", "(aaa(bbb))", "(aaa  (xxx, 'something', orthis))", "  ");
		test("'some other ( string with' 'and )' (aaa(xxx, '(', orthis))   ", "'some other ( string with' 'and )' ",
				"(aaa(xxx, '(', orthis))", "   ");
		test(", ((xxx ',') bbb), ", ", ", "((xxx ',') bbb)", ", ");
	}

	@Test
	public void testNull() {
		test("NULL", "", "NULL", "");
		test("null", "", "null", "");
		test("NuLl", "", "NuLl", "");
		test("bumbum null,", "bumbum ", "null", ",");
	}

	private void test(String sql, String expectedBefore, String expectedArgument, String expectedAfter) {
		Result result = ArgumentSplitter.of(sql).split();
		assertThat(result.getBeforeSql()).isEqualTo(expectedBefore);
		assertThat(result.getArgumentSql()).isEqualTo(expectedArgument);
		assertThat(result.getAfterSql()).isEqualTo(expectedAfter);
	}

	private void error(Callable<Result> callable) {
		try {
			callable.call();
			fail();
		} catch (Exception e) {
		}
	}
}
