package com.jaregu.database.queries.compiling.expr;

import static com.jaregu.database.queries.compiling.expr.AssociativityType.LEFT_TO_RIGHT;
import static com.jaregu.database.queries.compiling.expr.AssociativityType.NOT_ASSOCIATIVE;
import static com.jaregu.database.queries.compiling.expr.AssociativityType.RIGHT_TO_LEFT;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OperatorType implements Operator {

	// Precedence http://introcs.cs.princeton.edu/java/11precedence/

	EXPRESSION_SEPARATOR(20, LEFT_TO_RIGHT, ";"),

	BLOCK(16, LEFT_TO_RIGHT, "(", ")"),

	NOT(14, RIGHT_TO_LEFT, Operand::not, "!"),

	MULTIPLY(12, LEFT_TO_RIGHT, Operand::multiply, "*"),

	DIVIDE(12, LEFT_TO_RIGHT, Operand::divide, "/"),

	PLUS(11, LEFT_TO_RIGHT, Operand::add, "+"),

	MINUS(11, LEFT_TO_RIGHT, Operand::subtract, "-"),

	GREATER(9, NOT_ASSOCIATIVE, Operand::greater, ">"),

	GREATER_OR_EQUAL(9, NOT_ASSOCIATIVE, Operand::greaterOrEqual, ">="),

	LESSER(9, NOT_ASSOCIATIVE, Operand::lesser, "<"),

	LESSER_OR_EQUAL(9, NOT_ASSOCIATIVE, Operand::lesserOrEqual, "<="),

	EQUAL(8, LEFT_TO_RIGHT, Operand::equal, "=="),

	NOT_EQUAL(8, LEFT_TO_RIGHT, Operand::notEqual, "!="),

	AND(4, LEFT_TO_RIGHT, Operand::and, "&&"),

	OR(3, LEFT_TO_RIGHT, Operand::or, "||"),

	TERNARY(2, RIGHT_TO_LEFT, Operand::ternary, "?", ": "),

	ASSIGN(1, RIGHT_TO_LEFT, Operand::assign, "=");

	private static final List<Operator> operators = Collections
			.unmodifiableList(Stream.of(values()).sorted((o1, o2) -> {
				return Integer.compare(o2.getPrecedence(), o1.getPrecedence());
			}).collect(Collectors.toList()));

	private static final Map<Integer, List<Operator>> operatorsByPrecedence = Collections
			.unmodifiableMap(Stream.of(values())
					.map(ot -> (Operator) ot)
					.sorted((o1, o2) -> {
						return Integer.compare(o2.getPrecedence(), o1.getPrecedence());
					}).collect(
							groupingBy(Operator::getPrecedence, LinkedHashMap::new,
									mapping(Function.identity(), toList()))));

	private static final Map<String, Operator> orderedSequenceToOperator = Collections
			.unmodifiableMap(Stream.of(values()).flatMap(o -> {
				return o.getSequences().stream().map(s -> new Entry(s, o));
			}).sorted((e1, e2) -> {
				return Integer.compare(e2.sequence.length(), e1.sequence.length());
			}).collect(
					Collectors.toMap(Entry::getSequence, Entry::getOperatorType, (e1, e2) -> e1, LinkedHashMap::new)));

	private Function<Operand, Object> unary;
	private BiFunction<Operand, Operand, Object> binary;
	private TernaryFunction<Operand, Operand, Operand, Object> ternary;
	private int precedence;
	private AssociativityType associativity;
	private List<String> sequences;

	private OperatorType(int precedence, AssociativityType associativity, String... sequences) {
		this.precedence = precedence;
		this.associativity = associativity;
		this.sequences = Collections.unmodifiableList(Arrays.asList(sequences));
	}

	private OperatorType(int precedence, AssociativityType associativity, Function<Operand, Object> unary,
			String... sequences) {
		this(precedence, associativity, sequences);
		this.unary = unary;
	}

	private OperatorType(int precedence, AssociativityType associativity, BiFunction<Operand, Operand, Object> binary,
			String... sequences) {
		this(precedence, associativity, sequences);
		this.binary = binary;
	}

	private OperatorType(int precedence, AssociativityType associativity,
			TernaryFunction<Operand, Operand, Operand, Object> ternary, String... sequences) {
		this(precedence, associativity, sequences);
		this.ternary = ternary;
	}

	/*
	 * public Object invoke(Operand operand, Operand value) { return
	 * binary.apply(operand, value); }
	 */

	@Override
	public int getPrecedence() {
		return precedence;
	}

	@Override
	public AssociativityType getAssociativity() {
		return associativity;
	}

	@Override
	public List<String> getSequences() {
		return sequences;
	}

	@Override
	public boolean isNullary() {
		return unary == null && binary == null && ternary == null;
	}

	@Override
	public boolean isUnary() {
		return unary != null;
	}

	@Override
	public boolean isBinary() {
		return binary != null;
	}

	@Override
	public boolean isTernary() {
		return ternary != null;
	}

	@Override
	public String toString() {
		return sequences.toString();
	}

	@Override
	public Function<Operand, Object> getUnary() {
		return unary;
	}

	@Override
	public BiFunction<Operand, Operand, Object> getBinary() {
		return binary;
	}

	@Override
	public TernaryFunction<Operand, Operand, Operand, Object> getTernary() {
		return ternary;
	}

	public static List<Operator> getOperators() {
		return operators;
	}

	public static Map<Integer, List<Operator>> getOperatorsByPrecedence() {
		return operatorsByPrecedence;
	}

	/**
	 * Sort operators using their sequence lengths in reverse order, to match
	 * longest separators first, because longest can contain shortest ones
	 * 
	 * @return
	 */
	public static Map<String, Operator> getOperatorsBySequence() {
		return orderedSequenceToOperator;
	}

	private static class Entry {

		private String sequence;
		private OperatorType operatorType;

		public Entry(String sequence, OperatorType operatorType) {
			this.sequence = sequence;
			this.operatorType = operatorType;
		}

		public String getSequence() {
			return sequence;
		}

		public OperatorType getOperatorType() {
			return operatorType;
		}
	}
}