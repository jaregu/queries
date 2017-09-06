package com.jaregu.database.queries.compiling.expr;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

public class OperatorTypeTest {

	@Test
	public void testOperators() {
		assertThat(OperatorType.getOperators()).isNotEmpty();
		assertThat(OperatorType.getOperators().size()).isEqualTo(OperatorType.values().length);
		assertThat(OperatorType.getOperators()).containsAll(Arrays.asList(OperatorType.values()));
		assertThat(OperatorType.getOperators()).isSameAs(OperatorType.getOperators());
	}

	@Test
	public void testOperatorsByPrecedence() {
		assertThat(OperatorType.getOperatorsByPrecedence()).isNotEmpty();
		assertThat(OperatorType.getOperatorsByPrecedence().size()).isLessThanOrEqualTo(OperatorType.values().length);
		assertThat(OperatorType.getOperatorsByPrecedence()).isSameAs(OperatorType.getOperatorsByPrecedence());
		Entry<Integer, List<Operator>> previous = null;
		for (Entry<Integer, List<Operator>> current : OperatorType.getOperatorsByPrecedence().entrySet()) {
			if (previous != null) {
				assertThat(current.getKey()).isLessThan(previous.getKey());
			}
			assertThat(current.getValue()).isNotEmpty();
			assertThat(current.getValue()).allMatch(o -> o.getPrecedence() == current.getKey().intValue());
			previous = current;
		}

		List<Operator> previousList = null;
		for (List<Operator> currentList : OperatorType.getOperatorsByPrecedence().values()) {
			assertThat(currentList).isNotEmpty();
			assertThat(currentList).allMatch(o -> o.getPrecedence() == currentList.get(0).getPrecedence());
			if (previousList != null) {
				int previousPrecedence = previousList.get(0).getPrecedence();
				assertThat(currentList).allMatch(o -> o.getPrecedence() < previousPrecedence);
			}
			previousList = currentList;
		}
	}

	@Test
	public void testOrderedSequenceToOperator() {
		assertThat(OperatorType.getOperatorsBySequence()).isNotEmpty();
		assertThat(OperatorType.getOperatorsBySequence().size()).isGreaterThanOrEqualTo(OperatorType.values().length);
		assertThat(OperatorType.getOperatorsBySequence()).isSameAs(OperatorType.getOperatorsBySequence());

		Entry<String, Operator> previous = null;
		for (Entry<String, Operator> current : OperatorType.getOperatorsBySequence().entrySet()) {
			if (previous != null) {
				assertThat(current.getKey().length()).isLessThanOrEqualTo(previous.getKey().length());
			}
			assertThat(current.getValue().getSequences()).contains(current.getKey());
			previous = current;
		}
	}

}
