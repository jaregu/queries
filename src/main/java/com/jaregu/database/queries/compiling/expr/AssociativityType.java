package com.jaregu.database.queries.compiling.expr;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

public enum AssociativityType {

	LEFT_TO_RIGHT(Function.identity()), NOT_ASSOCIATIVE(Function.identity()), RIGHT_TO_LEFT(AssociativityType::reverse);

	private Function<Iterable<? extends Object>, Iterable<? extends Object>> orderFunction;

	private <T> AssociativityType(Function<Iterable<?>, Iterable<?>> orderFunction) {
		this.orderFunction = orderFunction;
	}

	private static <T> Iterable<T> reverse(Iterable<T> iterable) {
		Deque<T> output = StreamSupport.stream(iterable.spliterator(), false)
				.collect(Collector.of(ArrayDeque::new, (deq, t) -> deq.addFirst(t), (d1, d2) -> {
					d2.addAll(d1);
					return d2;
				}));
		return output;
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getIterable(Iterable<T> iterable) {
		return (Iterable<T>) orderFunction.apply(iterable);
	}
}
