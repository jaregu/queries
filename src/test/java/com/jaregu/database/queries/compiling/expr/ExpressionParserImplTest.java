package com.jaregu.database.queries.compiling.expr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionParserImplTest {

	private ExpressionParserImpl impl = new ExpressionParserImpl();

	@Mock
	private NamedResolver resolver;

	@Before
	public void setUp() {
		when(resolver.getValue("aaa")).thenReturn("AAA");
		when(resolver.getValue("N0")).thenReturn(0);
		when(resolver.getValue("N123")).thenReturn(123);
		when(resolver.getValue("N123456")).thenReturn(123.456);
		when(resolver.getValue("bT")).thenReturn(true);
		when(resolver.getValue("bF")).thenReturn(false);
		when(resolver.getValue("nil")).thenReturn(null);
	}

	@Test
	public void testIsLikeExpression() {
		isNotLike("");
		isNotLike(";");
		isNotLike("aaa");
		isLike(" aaa = 12");
		isNotLike(".aaa");
		isLike(":aaa");
		isNotLike("+");
		isNotLike("<=");
		isLike("<=   aa < bbb");
		isLike("1+2+3");
		isLike("'1 '+'2 3 4'+' 3'");
		isLike("1+'aaa '' bbb'+3");
		isLike("(aaa)");
		isLike(" (1)  + (2) *  (3)   ");
		isLike(" (1)  + (2 + (1 + 2 + 3) + (1 +4 + (6 + 7))) *  (3)   ");
		isLike("()()(())((()())())");
		isNotLike("1 + b c + 2");

		isNotLike(" aaa(cc = 'aaa') ");
		isNotLike(" { ");
		isNotLike(" :aa { ");
		isNotLike(" :a + 1 { ");
		isNotLike(" } ");
		isLike(" :aa");
		isNotLike("this is just some text");
		isNotLike("you can :a + :b if needed");
	}

	private void isLike(String expression) {
		assertTrue(impl.isLikeExpression(expression));
	}

	private void isNotLike(String expression) {
		assertFalse(impl.isLikeExpression(expression));
	}

	@Test
	public void testConstants() {
		eval(123, "123");
		eval(null, " null");
		eval(true, " true ");
		eval(false, "FALSE  ");
		eval(new BigDecimal("123.456"), " 123.456 ");
		eval("abc", "'abc'");
		eval("abc abc", "'abc abc'");
		eval("abc ' abc ", " 'abc '' abc '  ");
	}

	@Test
	public void testVariables() {
		eval("AAA", ":aaa");
		eval(0, ":N0");
		eval(123, ":N123");
		eval(123.456, ":N123456");
		eval(true, ":bT");
		eval(false, ":bF");
		eval(null, ":nil");
		eval(false, "! :bT");
		eval(true, " ! :bF ");
	}

	@Test
	public void testEquality() {
		eval(true, " 1  == 1");
		eval(false, " 1  != 1");
		eval(true, "  1  != 2 ");
		eval(true, " 'aAb' ==  'aAb' ");
		eval(true, " :aaa ==  'AAA' ");
		eval(true, "null == :nil");
		eval(false, "null != :nil");
		eval(true, "true == true");
		eval(false, "true != true");
		eval(false, "true == false");
		eval(true, "true != false");
	}

	@Test
	public void testMath() {
		eval(3, "1+2");
		eval(6, " 1+2+ 3   ");
		eval(7, "1 + 2 *   3");
		eval(7, " (1)  + (2) * \n (3)  \n \n");
		eval(9, "(1+ 2  )* 3");
		eval(79, " (1)  + (2 + (1 + 1 * 1 * 2 + 3) + (1 +4 + (6 + 7))) *  (3)   ");
	}

	@Test
	public void testConcat() {
		eval("1 2 3 4 5", "'1 '+'2 3 4'+' 5'");
		eval("%AAA%", "'%'+ :aaa +'%'");
	}

	@Test
	public void testLogic() {
		eval(false, "1 >2");
		eval(true, "1 <2");
		eval(true, "1 <=2");
		eval(true, "2 <=2");
		eval(false, "2< 2");
		eval(true, "true && true");
		eval(false, "true && false");
		eval(true, "true || true");
		eval(true, "true || false");
		eval(false, "false || false");
		eval(true, "2< 2 || 4 >2");
		eval(false, "2< 2 || 4 <=2");
		eval(false, "true && true && true && false");
		eval(true, "true && true || true && false");
		eval(false, "true && (true || true) && false");
		eval(false, "(1 > 2 || 2 <=2) && (true && :bF)");
		eval(true, " :bT != null && :bT");
		eval(false, ":nil != null");
		eval(true, " :nil == null");
		eval(true, "(1 > 2 || 2 <=2) && (true && !:bF)");
	}

	@Test
	public void testEvaluationPrecedance() {
		eval(false, ":bT && :bT && :bT && :bF && :nil");
		eval(false, ":nil != null && :nil > 3");
		eval(true, ":bT && :bT && :bT && !:bF");
		eval(true, "false ? true : false ? false: true");
		eval("aa", "!false ? 'aa' : ('aa' + :nil)");
	}

	@Test
	public void testTernary() {
		eval(1, "true? 1: 2");
		eval("bb", "false ? 'aa' : 'bb'");
	}

	@Test
	public void testMultiple() {
		evalMultiple("'1 '+'2 3 4'+' 5'; :aaa + 'a'", "1 2 3 4 5", "AAAa");
		evalMultiple("'1'; '2' ; '3' ;'4';'5'", "1", "2", "3", "4", "5");
	}

	@Test
	public void testAssignment() {
		evalVariables("aaa = 'bbb'", Collections.singletonMap("aaa", "bbb"));
		isNotLike("aaa");

		Map<String, Object> values = new HashMap<>();
		values.put("aaa", "A");
		values.put("bbb", "C");
		values.put("ccc", 1);
		values.put("ddd", true);
		values.put("f123", 123);
		evalVariables("aaa = 'A';bbb='B';bbb='C'  ; ccc  = 1; ddd= !false; f123 = :bF ? 'T' : 123", values);
	}

	@Test
	public void testParseException() {
		isNotLike("+");
		parseException("'a'+");
		parseException("<=   aa < bbb");
		parseException("(1+2 + (3+4) + 5");
		parseException("5 ; * 4");
	}

	@Test
	public void testConstantIntegerOperations() {
		eval(10, "5 * 2");
		eval(5, "10 / 2");
		eval(7, "5 + 2");
		eval(3, "5 - 2");

		eval(false, "5 > 5");
		eval(false, "5 > 6");
		eval(true, "5 > 4");
		eval(true, "5 > 4.8");
		evalException("5 > true");
		evalException("5 > null");
		evalException("5 > '123'");

		eval(true, "5 >= 5");
		eval(false, "5 >= 6");
		eval(true, "5 >= 4");
		evalException("5 >= true");
		evalException("5 >= null");
		evalException("5 >= '123'");

		eval(false, "5 < 5");
		eval(true, "5 < 6");
		eval(false, "5 < 4");
		evalException("5 < true");
		evalException("5 < null");
		evalException("5 < '123'");

		eval(true, "5 <= 5");
		eval(true, "5 <= 6");
		eval(false, "5 <= 4");
		evalException("5 <= true");
		evalException("5 <= null");
		evalException("5 <= '123'");

		eval(true, "5 == 5");
		eval(false, "5 == 6");
		eval(false, "5 == 4");
		eval(false, "5 == true");
		eval(false, "5 == null");
		eval(false, "5 == '123'");

		eval(false, "5 != 5");
		eval(true, "5 != 6");
		eval(true, "5 != 4");
		eval(true, "5 != true");
		eval(true, "5 != null");
		eval(true, "5 != '123'");

		evalException("5 && 2");
		evalException("5 || 2");
		evalException("!5");
	}

	@Test
	public void testConstantLongOperations() {
		eval(1012312221233l, "1012312221232 + 1");
	}

	@Test
	public void testConstantDecimalOperations() {
		eval(new BigDecimal("13.75"), "5.5 * 2.5");
		eval(new BigDecimal("5.4"), "10.8 / 2");
		eval(new BigDecimal("8.7"), "5.5 + 3.2");
		eval(new BigDecimal("3.2"), "5.5 - 2.3");

		eval(false, "5.5 > 5.5");
		eval(false, "5.5 > 6.5");
		eval(true, "5.5 > 4.5");

		eval(true, "5.5 >= 5.5");
		eval(false, "5.5 >= 6.5");
		eval(true, "5.5 >= 4.5");

		eval(false, "5.5 < 5.5");
		eval(true, "5.5 < 6.5");
		eval(false, "5.5 < 4.5");

		eval(true, "5.5 <= 5.5");
		eval(true, "5.5 <= 6.5");
		eval(false, "5.5 <= 4.5");

		eval(true, "5.5 == 5.5");
		eval(false, "5.5 == 6.5");
		eval(false, "5.5 == 4.5");

		eval(false, "5.5 != 5.5");
		eval(true, "5.5 != 6.5");
		eval(true, "5.5 != 4.5");

		evalException("5.5 && 2.5");
		evalException("5.5 || 2.5");
		evalException("!5.5");
	}

	@Test
	public void testConstantBoolean() {
		evalException("true * 2");
		evalException("true / 2");
		evalException("true + 2");
		evalException("true - 1");
		evalException("true < true");
		evalException("true <= false");
		evalException("true > false");
		evalException("true >= false");

		eval(true, "true == true");
		eval(false, "true == false");
		eval(false, "true == 1");
		eval(false, "true == 5.5");
		eval(false, "true == 'true'");

		eval(true, "true && true");
		eval(false, "true && false");
		eval(true, "true || true");
		eval(true, "true || false");
		eval(true, "false || true");
		eval(false, "false || false");

		evalException("true && 2.5");
		evalException("true && 2");
		evalException("true && '111'");
		evalException("true && null");
		eval(true, "true || 2.5");
		eval(true, "true || 2");
		eval(true, "true || '111'");
		eval(true, "true || null");
		evalException("false || 2.5");
		evalException("false || 2");
		evalException("false || '111'");
		evalException("false || null");

		eval(true, "!false");
		eval(false, "!true");
	}

	@Test
	public void testStringConstant() {
		evalException("'aaa' * 2");
		evalException("'aaa' / 2");
		evalException("'aaa' - 1");
		evalException("'aaa' < 'bbb'");
		evalException("'aaa' <= 'bbb'");
		evalException("'aaa' > 'bbb'");
		evalException("'aaa' >= 'bbb'");
		evalException("'aaa' && 2.5");
		evalException("'aaa' && 2");
		evalException("'aaa' && '111'");
		evalException("'aaa' && null");
		evalException("'aaa' || 2.5");
		evalException("'aaa' || 2");
		evalException("'aaa' || '111'");
		evalException("'aaa' || null");

		eval(true, "'aaa' == 'aaa'");
		eval(false, "'aaa' == 'bbb'");
		eval(false, "'aaa' == 1");
		eval(false, "'aaa' == 5.5");
		eval(false, "'aaa' == 'true'");

		eval(false, "'aaa' != 'aaa'");
		eval(true, "'aaa' != 'bbb'");
		eval(true, "'aaa' != 1");
		eval(true, "'aaa' != 5.5");
		eval(true, "'aaa' != 'true'");

		eval("aaabbb", "'aaa' + 'bbb'");
		eval("aaa2", "'aaa' + 2");
		eval("aaa2.58", "'aaa' + 2.58");
		eval("aaatrue", "'aaa' + true");
		eval("aaafalse", "'aaa' + false");
		evalException("'aaa' + null");
		evalException("!'aaa'");
	}

	private void parseException(String expression) {
		try {
			assertTrue(impl.isLikeExpression(expression));
			impl.parse(expression);
			fail();
		} catch (ExpressionParseException e) {
		}
	}

	private void eval(Object expected, String expression) {
		assertTrue(impl.isLikeExpression(expression));
		assertEquals(expected,
				impl.parse(expression).get(0).eval(ParametersResolver.ofNamedParameters(resolver)).getReturnValue());
	}

	private void evalException(String expression) {
		try {
			assertTrue(impl.isLikeExpression(expression));
			impl.parse(expression).get(0).eval(ParametersResolver.ofNamedParameters(resolver));
			fail();
		} catch (ExpressionEvalException e) {
		}
	}

	private void evalMultiple(String expression, Object... expected) {
		assertTrue(impl.isLikeExpression(expression));
		assertThat(impl.parse(expression).stream().map(e -> e.eval(ParametersResolver.ofNamedParameters(resolver)))
				.map(r -> r.getReturnValue()).collect(Collectors.toList())).containsExactly(expected);
	}

	private void evalVariables(String expression, Map<String, Object> expected) {
		assertTrue(impl.isLikeExpression(expression));
		Map<String, Object> result = impl.parse(expression).stream()
				.map(e -> e.eval(ParametersResolver.ofNamedParameters(resolver))).map(r -> r.getOutputVariables())
				.map(Map::entrySet).flatMap(Collection::stream)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
		assertThat(result).containsAllEntriesOf(expected);
	}

	@Test
	public void testCollections() {

		NamedResolver resolver = NamedResolver.ofBean(new CollectionsHolder());
		evalCol(true, ":emptySet.empty", resolver);
		evalCol(true, ":emptyList.empty", resolver);
		evalCol(true, ":emptyMap.empty", resolver);
		evalCol(false, ":col1.empty", resolver);
		evalCol(false, ":numbers.empty", resolver);
		evalCol(false, ":strings.empty", resolver);
	}

	private void evalCol(Object expected, String expression, NamedResolver resolver) {
		assertTrue(impl.isLikeExpression(expression));
		assertEquals(expected,
				impl.parse(expression).get(0).eval(ParametersResolver.ofNamedParameters(resolver)).getReturnValue());
	}

	public static class CollectionsHolder {
		public Set<String> emptySet = new HashSet<>();
		public List<String> emptyList = new ArrayList<>();
		public Map<String, String> emptyMap = new HashMap<>();
		public List<String> col1 = Collections.singletonList("1");
		public List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
		public List<String> strings = Arrays.asList("a", "b", "c");
	}
}
