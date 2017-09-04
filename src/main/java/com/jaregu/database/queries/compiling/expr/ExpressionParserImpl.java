package com.jaregu.database.queries.compiling.expr;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.common.Lexer;
import com.jaregu.database.queries.common.Lexer.LexerPattern;

public class ExpressionParserImpl implements ExpressionParser {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final List<Separator> SEPARATORS = getSeparators();

	private static final LexerPattern OPERAND = Lexer.newPattern().skipAllBetween("'", "'")
			.stopBefore(Lexer.whitespace())
			.stopBeforeAnyOf(SEPARATORS.stream().map(Separator::getSequence).toArray(String[]::new)).stopAtEof();
	private static final LexerPattern WHITESPACE = Lexer.newPattern().stopAfter(Lexer.whitespace()).stopAtEof();

	@Override
	public boolean isLikeExpression(String expression) {
		Parts parts = split(expression);
		// if there is no two sequential sequences - lets answer that this is
		// expression
		Part lastPart = null;
		for (Part part : parts) {
			if (lastPart != null && lastPart.isSequence() && part.isSequence()) {
				return false;
			}
			lastPart = part;
		}
		return true;
	}

	@Override
	public List<Expression> parse(String expression) throws ExpressionParseException {
		return ParsingContext.forExpression(expression).build().withContext(() -> {
			logger.trace("Parsing expression (0): {}", expression);
			Parts parts = split(expression);
			logger.trace("Parsing expression (1) after splitting: {}", parts);
			List<Parts> expressions = getExpressionParts(parts);
			logger.trace("Parsing expression (2) after expression parts: {}", expressions);
			List<ExpressionBlock> result = new ArrayList<>(expressions.size());
			for (Parts expressionPart : expressions) {
				Parts hierarchical = getHierarchical(expressionPart);
				logger.trace("Parsing expression (3) after creating block: {}", parts);
				Operand operand = getOperands(hierarchical);
				logger.trace("Parsing expression (4) after compiling: {}", operand);
				result.add(new ExpressionBlockImpl(operand));
			}
			return result.stream().map(ExpressionImpl::new).collect(Collectors.toList());
		});
	}

	private Parts split(String expression) {
		List<Part> parts = new ArrayList<>();
		Lexer lx = new Lexer(expression);
		boolean found;
		while (lx.hasMore()) {
			found = false;
			for (Separator separator : SEPARATORS) {
				if (lx.lookingAt(separator.getSequence())) {
					lx.expect(separator.getSequence());
					parts.add(new Part(separator));
					found = true;
					break;
				}
			}
			if (!found) {
				String chunk = lx.read(OPERAND);
				if (chunk != null) {
					parts.add(new Part(chunk));
				} else if (lx.lookingAt(Lexer.whitespace())) {
					lx.read(WHITESPACE);
				} else {
					throw new ExpressionParseException("Can't read next token : " + lx);
				}
			}

		}
		return new Parts(parts);
	}

	/*
	 * Splits expression into multiple expressions expr1[;expr2]...
	 * 
	 * @param parts
	 * 
	 * @return
	 */
	private List<Parts> getExpressionParts(Parts parts) {
		List<Parts> expressionParts = new LinkedList<>();
		int index = 0;
		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			if (part.isSeparator() && part.getSeparator() == ExpressionSeparator.SEMICOLON) {
				if (i - index > 0) {
					expressionParts.add(parts.subParts(index, i));
					index = i + 1;
				}
			}
		}
		if (index < parts.size()) {
			expressionParts.add(parts.subParts(index, parts.size()));
		}
		return expressionParts;
	}

	/*
	 * Splits flat part list to hierarchical list using existing brackets. Like
	 * a - ( b + c) -> a - block(b + c)
	 * 
	 * @param parts
	 * 
	 * @return
	 */
	private Parts getHierarchical(Parts parts) {
		List<Part> hierarchical = new ArrayList<>(parts.size());
		int bracketCount = 0;
		int bracketStartIndex = 0;
		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			if (part.isSeparator() && part.getSeparator() == BlockSymbol.START) {
				if (bracketCount == 0) {
					bracketStartIndex = i + 1;
				}
				bracketCount++;
			}
			if (part.isSeparator() && part.getSeparator() == BlockSymbol.END) {
				if (bracketCount == 0) {
					throw new ExpressionParseException(
							"Can't parse expression, there is one closing bracket when there was no opening one in block: "
									+ parts + "!");
				}
				bracketCount--;
				if (bracketCount == 0) {
					if (bracketStartIndex == i) {
						throw new ExpressionParseException(
								"Can't parse expression, there is one opening bracket followed with closing one in block: "
										+ parts + "!");
					}
					hierarchical.add(new Part(getHierarchical(parts.subParts(bracketStartIndex, i))));
					bracketStartIndex = i + 1;
				}

			} else if (bracketCount == 0) {
				hierarchical.add(part);
			}
		}
		return new Parts(hierarchical);
	}

	/*
	 * Splits everything as operand and operation blocks consisting of tree
	 * elements using operation precedence.
	 */
	private Operand getOperands(Parts parts) {
		if (parts.size() == 1) {
			return toOperand(parts.get(0));
		} else if (parts.size() % 2 == 0) {
			throw new ExpressionParseException(
					"Can't parse expression, there is one operand missing in block: " + parts + "!");
		} else if (parts.size() == 3) {
			if (!parts.get(1).isSeparator()) {
				throw new ExpressionParseException(
						"Can't parse expression, there is error in block with operation sequence: " + parts + "!");
			}
			Operand first = toOperand(parts.get(0));
			OperationType operation = (OperationType) parts.get(1).getSeparator();
			Operand second = toOperand(parts.get(2));

			return new ExpressionBlockImpl(first, operation, second);
		} else {

			int index = -1;
			int precedence = -1;
			for (int i = 0; i < parts.size(); i++) {
				Part part = parts.get(i);
				if (part.isSeparator()) {
					if ((i + 1) % 2 != 0) {
						throw new ExpressionParseException(
								"Can't parse expression, there is error with operation sequences part index: " + i
										+ " parts: " + parts + "!");
					}
					OperationType operation = (OperationType) part.getSeparator();
					if (precedence < 0 || operation.getPrecedence() < precedence) {
						index = i;
						precedence = operation.getPrecedence();
					}
				}
			}

			if (index < 0) {
				throw new ExpressionParseException(
						"Can't parse expression, there is no operators in block: " + parts + "!");
			} else if (index + 2 > parts.size()) {
				throw new ExpressionParseException(
						"Can't parse expression, expression ends with operator: " + parts + "!");
			}

			List<Part> replacedParts = new ArrayList<>(parts.size());
			replacedParts.addAll(parts.subParts(0, index - 1).getParts());
			replacedParts.add(new Part(getOperands(parts.subParts(index - 1, index + 2))));
			replacedParts.addAll(parts.subParts(index + 2, parts.size()).getParts());

			return getOperands(new Parts(replacedParts));
		}
	}

	public Operand toOperand(Part part) {
		if (part.isOperand()) {
			return part.getOperand();
		} else if (part.hasChildren()) {
			return getOperands(part.getChildren());
		} else if (part.isSequence()) {
			return (Constant.parse(part.getSequence())).map(c -> (Operand) c)
					.orElseGet(() -> Variable.parse(part.getSequence()).orElseThrow(() -> new ExpressionParseException(
							"Can't parse expression, unknown character sequence: " + this + "!")));

		} else {
			throw new ExpressionParseException("Can't parse expression as operand, not operand: " + this + "!");
		}
	}

	private static List<Separator> getSeparators() {
		List<Separator> separators = new ArrayList<>(OperationType.values().length + BlockSymbol.values().length);
		separators.addAll(Arrays.asList(OperationType.values()));
		separators.addAll(Arrays.asList(BlockSymbol.values()));
		separators.addAll(Arrays.asList(ExpressionSeparator.values()));
		// sort separators using their length in reverse order, to match longest
		// separators first,
		// because longest can contain shortest ones
		Collections.sort(separators, (s1, s2) -> {
			return Integer.compare(s2.getSequence().length(), s1.getSequence().length());
		});
		return separators.stream().collect(Collectors.toList());
	}

	private static class Parts implements Iterable<Part> {

		private List<Part> parts;

		public Parts(List<Part> parts) {
			this.parts = parts;
		}

		public Part get(int index) {
			return this.parts.get(index);
		}

		public List<Part> getParts() {
			return parts;
		}

		public Parts subParts(int start, int end) {
			return new Parts(parts.subList(start, end));
		}

		public int size() {
			return parts.size();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Part part : parts) {
				if (sb.length() > 0) {
					sb.append("|");
				}
				sb.append(part);
			}
			return sb.toString();
		}

		@Override
		public Iterator<Part> iterator() {
			return parts.iterator();
		}
	}

	private static class Part {

		private Separator separator;
		private String sequence;
		private Parts children;
		private Operand operand;

		public Part(Separator separator) {
			this.separator = separator;
		}

		public Part(String sequence) {
			this.sequence = sequence;
		}

		public Part(Parts children) {
			this.children = children;
		}

		public Part(Operand operand) {
			this.operand = operand;
		}

		boolean isSeparator() {
			return separator != null;
		}

		boolean isSequence() {
			return sequence != null;
		}

		boolean isOperand() {
			return operand != null;
		}

		boolean hasChildren() {
			return children != null;
		}

		public Separator getSeparator() {
			return separator;
		}

		public String getSequence() {
			return sequence;
		}

		public Operand getOperand() {
			return operand;
		}

		public Parts getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return separator != null ? separator.getSequence()
					: (children != null ? "(" + children.toString() + ")"
							: (operand != null ? operand.toString() : sequence));
		}
	}
}
