package com.jaregu.database.queries.compiling.expr;

import static com.jaregu.database.queries.compiling.expr.AssociativityType.LEFT_TO_RIGHT;
import static com.jaregu.database.queries.compiling.expr.AssociativityType.NOT_ASSOCIATIVE;
import static com.jaregu.database.queries.compiling.expr.AssociativityType.RIGHT_TO_LEFT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class AssociativityTypeTest {

	@Test
	public void testLeftToRight() {
		assertThat(LEFT_TO_RIGHT.getIterable(Arrays.asList("1", "2", "3"))).containsExactly("1", "2", "3");
	}

	@Test
	public void testNoAssociation() {
		assertThat(NOT_ASSOCIATIVE.getIterable(Arrays.asList("1", "2", "3"))).containsExactly("1", "2", "3");
	}

	@Test
	public void testRightToLeft() {
		assertThat(RIGHT_TO_LEFT.getIterable(Arrays.asList("1", "2", "3"))).containsExactly("3", "2", "1");
	}
}
