package com.jaregu.database.queries.compiling.expr;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.common.Lexer;
import com.jaregu.database.queries.common.Lexer.LexerPattern;

public class ExpressionParserImpl implements ExpressionParser {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final LexerPattern OPERAND = Lexer
			.newPattern().skipAllBetween("'", "'").stopBefore(Lexer.whitespace()).stopBeforeAnyOf(OperatorType
					.getOperators().stream().map(Operator::getSequences).flatMap(List::stream).toArray(String[]::new))
			.stopAtEof();
	private static final LexerPattern WHITESPACE = Lexer.newPattern().stopAfter(Lexer.whitespace()).stopAtEof();

	@Override
	public boolean isLikeExpression(String expression) {
		SplitParts parts = split(expression);
		if (parts.size() == 0) {
			return false;
		} else if (parts.size() == 1) {
			Optional<Operand> operand = parseOperand(parts.get(0));
			return operand.isPresent() && !(operand.get() instanceof OutputVariable);
		} else {
			Optional<Operand> operand;
			if ((operand = parseOperand(parts.get(0))).isPresent() && operand.get() instanceof OutputVariable &&
					!(parts.get(1).isOperator())) {
				return false;
			}

			Optional<Operand> lastOperand = Optional.empty();
			for (SplitPart part : parts) {

				if (part.isSequence()) {
					operand = parseOperand(part);
					if (!operand.isPresent()) {
						return false;
					} else if (operand.get() instanceof OutputVariable && lastOperand.isPresent()
							&& lastOperand.get() instanceof OutputVariable) {
						return false;
					}
					lastOperand = operand;
				} else {
					lastOperand = Optional.empty();
				}
			}
			return true;
		}
	}

	@Override
	public List<Expression> parse(String expression) throws ExpressionParseException {
		return ParsingContext.forExpression(expression).build().withContext(() -> {
			logger.trace("Parsing expression (0): {}", expression);
			SplitParts parts = split(expression);
			logger.trace("Parsing expression (1) after splitting: {}", parts);
			List<Operand> operands = parse(parts);
			logger.trace("Parsing expression (2) after parsing: {}", operands);
			return operands.stream().map(ExpressionImpl::new).collect(Collectors.toList());
		});
	}

	private SplitParts split(String expression) {
		Set<Entry<String, Operator>> operators = OperatorType.getOperatorsBySequence().entrySet();
		List<SplitPart> parts = new ArrayList<>();
		Lexer lx = new Lexer(expression);
		boolean found;
		while (lx.hasMore()) {
			found = false;
			for (Entry<String, Operator> operatorEntry : operators) {
				if (lx.lookingAt(operatorEntry.getKey())) {
					lx.expect(operatorEntry.getKey());
					parts.add(new SplitPart(operatorEntry.getKey(), operatorEntry.getValue()));
					found = true;
					break;
				}
			}
			if (!found) {
				String chunk = lx.read(OPERAND);
				if (chunk != null) {
					parts.add(new SplitPart(chunk));
				} else if (lx.lookingAt(Lexer.whitespace())) {
					lx.read(WHITESPACE);
				} else {
					throw new ExpressionParseException("Can't read next token : " + lx);
				}
			}

		}
		return new SplitParts(parts);
	}

	private List<Operand> parse(SplitParts parts) {
		return parseParts(parts).stream().map(p -> toOperand(p)).collect(Collectors.toList());
	}

	private SplitParts parseParts(SplitParts parts) {
		Map<Integer, List<Operator>> operatorsByPrecedance = OperatorType.getOperatorsByPrecedence();
		for (List<Operator> operators : operatorsByPrecedance.values()) {

			int delta = 0;
			do {
				SplitParts updatedParts = parseOperators(parts, operators);
				delta = parts.size() - updatedParts.size();
				parts = updatedParts;
			} while (delta > 0);
		}
		return parts;
	}

	private SplitParts parseOperators(SplitParts parts, List<Operator> operators) {
		boolean rightToLeft = operators.get(0).getAssociativity() == AssociativityType.RIGHT_TO_LEFT;
		int delta = rightToLeft ? -1 : 1;
		for (int i = rightToLeft ? parts.size() - 1 : 0; rightToLeft ? i >= 0 : i < parts.size(); i = i + delta) {
			SplitPart part = parts.get(i);
			if (part.isOperator()) {

				for (Operator operator : operators) {
					if (operator == part.getOperator()) {

						// special case - processing is doing operator
						// first of all if operator has more than one sequence,
						// we have to match first one from right end
						if (!operator.getSequences().get(rightToLeft ? operator.getSequences().size() - 1 : 0)
								.equals(part.getSequence())) {
							throw new ExpressionParseException(
									"Can't parse expression, there is wrong character sequence starting " + operator
											+ " operator! In expression part: " + parts + "!");

						}

						if (operator.isNullary()) {
							if (operator == OperatorType.EXPRESSION_SEPARATOR) {
								return parseExpressionParts(parts, i);
							} else if (operator == OperatorType.BLOCK) {
								return parseBlockParts(parts, i);
							} else {
								throw new ExpressionParseException("Can't parse expression, unknown nullary " + operator
										+ " operator! In expression part: " + parts + "!");
							}

						} else if (operator.isUnary()) {
							int operandIndex = i - delta;
							if (!parts.isInBounds(operandIndex)) {
								throw new ExpressionParseException("Can't parse expression, unary " + operator
										+ " operator has no operand! Expression part: " + parts + "!");
							} else {
								int from = rightToLeft ? i : i - 1;
								int to = rightToLeft ? i + 2 : i + 1;
								return parts.substitute(from, to,
										new SplitPart(new UnaryOperand(operator, toOperand(parts.get(operandIndex)))));
							}

						} else if (operator.isBinary()) {
							int leftOperandIndex = i - 1;
							int rightOperandIndex = i + 1;
							if (!parts.isInBounds(leftOperandIndex) || !parts.isInBounds(rightOperandIndex)) {
								throw new ExpressionParseException("Can't parse expression, binary " + operator
										+ " operator has one or none operands! Expression part: " + parts + "!");
							} else {
								return parts.substitute(i - 1, i + 2,
										new SplitPart(
												new BinaryOperand(operator, toOperand(parts.get(leftOperandIndex)),
														toOperand(parts.get(rightOperandIndex)))));
							}
						} else if (operator.isTernary()) {
							int from = rightToLeft ? i - 3 : i - 1;
							int to = from + 5;
							if (!parts.isInBounds(from) || !parts.isInBounds(to - 1)) {
								throw new ExpressionParseException("Can't parse expression, ternary (" + operator
										+ ") operator have not all operands! Expression part: " + parts + "!");
							} else if (!parts.get(from + 1).isOperator()
									|| parts.get(from + 1).getOperator() != operator
									|| !parts.get(from + 3).isOperator()
									|| parts.get(from + 3).getOperator() != operator) {
								throw new ExpressionParseException("Can't parse expression, ternary (" + operator
										+ ") operator have not all operators correctly! Expression part: " + parts
										+ "!");
							} else if (operator.getSequences().size() > 1 && (!operator.getSequences().get(0)
									.equals(parts.get(from + 1).getSequence())
									|| !operator.getSequences().get(1).equals(parts.get(from + 3).getSequence()))) {
								throw new ExpressionParseException("Can't parse expression, ternary (" + operator
										+ ") operator have not all operator symbols in order! Expression part: " + parts
										+ "!");
							} else {
								return parts.substitute(from, to,
										new SplitPart(new TernaryOperand(operator, toOperand(parts.get(from)),
												toOperand(parts.get(from + 2)), toOperand(parts.get(from + 4)))));
							}
						}
					}
				}
			}
		}
		return parts;
	}

	private SplitParts parseExpressionParts(SplitParts parts, int index) {
		SplitParts firstPart = parseParts(parts.subParts(0, index));
		SplitParts secondParts = null;
		if (index + 1 < parts.size()) {
			secondParts = parseParts(parts.subParts(index + 1, parts.size()));
		}
		return new SplitParts(firstPart, secondParts);
	}

	private SplitParts parseBlockParts(SplitParts parts, int index) {
		String startSymbol = OperatorType.BLOCK.getSequences().get(0);
		String endSymbol = OperatorType.BLOCK.getSequences().get(1);

		if (index + 1 < parts.size()) {
			int bracketCount = 1;
			int endIndex;
			for (endIndex = index + 1; endIndex < parts.size(); endIndex++) {
				SplitPart part = parts.get(endIndex);
				if (part.isOperator() && part.getOperator() == OperatorType.BLOCK) {
					if (startSymbol.equals(part.getSequence())) {
						bracketCount++;
					} else if (endSymbol.equals(part.getSequence())) {
						if (endIndex == index + 1) {
							throw new ExpressionParseException(
									"Can't parse expression, there is one opening bracket followed with closing one in block: "
											+ parts + "!");
						}
						bracketCount--;
						if (bracketCount == 0) {
							break;
						}
					}
				}
			}
			if (bracketCount > 0) {
				throw new ExpressionParseException(
						"Can't parse expression, there is no closing bracket in expression " + parts + "!");
			}

			SplitParts beforePart = parts.subParts(0, index);
			SplitParts parsedParts = parseParts(parts.subParts(index + 1, endIndex));
			SplitParts afterParts = null;
			if (endIndex + 1 < parts.size()) {
				afterParts = parts.subParts(endIndex + 1, parts.size());
			}
			return new SplitParts(beforePart, parsedParts, afterParts);
		} else {
			throw new ExpressionParseException(
					"Can't parse expression, there is no closing bracket in expression " + parts + "!");
		}
	}

	public Operand toOperand(SplitPart part) {
		return parseOperand(part).orElseThrow(
				() -> new ExpressionParseException("Can't parse expression, operand expected, but got: " + part));
	}

	public Optional<Operand> parseOperand(SplitPart part) {
		if (part.isOperand()) {
			return Optional.of(part.getOperand());
		} else if (part.isSequence()) {
			Optional<Constant> constant = Constant.parse(part.getSequence());
			if (constant.isPresent()) {
				return Optional.of(constant.get());
			} else {
				Optional<Variable> variable = Variable.parse(part.getSequence());
				if (variable.isPresent()) {
					return Optional.of(variable.get());
				} else {
					Optional<OutputVariable> output = OutputVariable.parse(part.getSequence());
					if (output.isPresent()) {
						return Optional.of(output.get());
					}
				}
			}
		}
		return Optional.empty();
	}

	private static class SplitParts implements Iterable<SplitPart> {

		private List<SplitPart> parts;

		public SplitParts(List<SplitPart> parts) {
			this.parts = parts;
		}

		public SplitParts(SplitParts... parts) {
			this.parts = Arrays.asList(parts).stream().filter(p -> p != null).map(p -> p.parts).flatMap(List::stream)
					.collect(Collectors.toList());
		}

		public SplitParts substitute(int from, int to, SplitPart substitute) {
			return substitute(from, to, Collections.singletonList(substitute));
		}

		@SuppressWarnings("unused")
		public SplitParts substitute(int from, int to, SplitParts substitute) {
			return substitute(from, to, substitute.parts);
		}

		private SplitParts substitute(int from, int to, List<SplitPart> substitute) {
			List<SplitPart> substituted = new ArrayList<>(parts.size());
			if (from > 0) {
				substituted.addAll(parts.subList(0, from));
			}
			substituted.addAll(substitute);
			if (to < parts.size()) {
				substituted.addAll(parts.subList(to, parts.size()));
			}
			return new SplitParts(substituted);
		}

		public SplitPart get(int index) {
			return this.parts.get(index);
		}

		public SplitParts subParts(int start, int end) {
			return new SplitParts(parts.subList(start, end));
		}

		public int size() {
			return parts.size();
		}

		public boolean isInBounds(int index) {
			return index >= 0 && index < parts.size();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Iterator<SplitPart> iter = parts.iterator();
			if (iter.hasNext()) {
				sb.append(iter.next());
				while (iter.hasNext()) {
					sb.append(" ").append(iter.next());
				}
			}
			return sb.toString();
		}

		@Override
		public Iterator<SplitPart> iterator() {
			return parts.iterator();
		}

		public Stream<SplitPart> stream() {
			return parts.stream();
		}
	}

	private static class SplitPart {

		private final Optional<String> sequence;
		private final Optional<Operator> operator;
		private final Optional<Operand> operand;

		public SplitPart(String sequence, Operator operator) {
			this.sequence = Optional.of(sequence);
			this.operator = Optional.of(operator);
			this.operand = Optional.empty();
		}

		public SplitPart(String sequence) {
			this.sequence = Optional.of(sequence);
			this.operator = Optional.empty();
			this.operand = Optional.empty();
		}

		public SplitPart(Operand operand) {
			this.sequence = Optional.empty();
			this.operator = Optional.empty();
			this.operand = Optional.of(operand);
		}

		public String getSequence() {
			return sequence.get();
		}

		public boolean isOperator() {
			return operator.isPresent();
		}

		public boolean isSequence() {
			return sequence.isPresent() && !operator.isPresent();
		}

		public boolean isOperand() {
			return operand.isPresent();
		}

		public Operator getOperator() {
			return operator.get();
		}

		public Operand getOperand() {
			return operand.get();
		}

		@Override
		public String toString() {
			return sequence.isPresent() ? sequence.get() : (operand.get().toString());
		}
	}
}
