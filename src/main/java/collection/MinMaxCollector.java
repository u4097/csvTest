package collection;

import java.util.Collections;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class MinMaxCollector<T, A, R> implements Collector<T, A, R> {

	private final Supplier<A> supplier;
	private final BiConsumer<A, T> accumulator;
	private final BinaryOperator<A> combiner;
	private final Function<A, R> finisher;
	private final Set<Characteristics> characteristics;

	private MinMaxCollector(Supplier<A> supplier,
							BiConsumer<A, T> accumulator,
							BinaryOperator<A> combiner,
							Function<A, R> finisher,
							Set<Characteristics> characteristics) {
		this.supplier = supplier;
		this.accumulator = accumulator;
		this.combiner = combiner;
		this.finisher = finisher;
		this.characteristics = characteristics;
	}

	private MinMaxCollector(Supplier<A> supplier,
							BiConsumer<A, T> accumulator,
							BinaryOperator<A> combiner,
							Set<Characteristics> characteristics) {
		this(supplier, accumulator, combiner, castingIdentity(), characteristics);
	}

	@Override
	public Supplier<A> supplier() {
		return supplier;
	}

	@Override
	public BiConsumer<A, T> accumulator() {
		return accumulator;
	}

	@Override
	public BinaryOperator<A> combiner() {
		return combiner;
	}

	@Override
	public Function<A, R> finisher() {
		return finisher;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

	@SuppressWarnings("unchecked")
	private static <I, R> Function<I, R> castingIdentity() {
		return i -> (R) i;
	}

	public static <T> Collector<T, ?, MinMaxIntStatistic> calculateInt(ToIntFunction<? super T> mapper) {
		return new MinMaxCollector<>(
				MinMaxIntStatistic::new,
				(r, t) -> r.accept(mapper.applyAsInt(t)),
				(l, r) -> {
					l.combine(r);
					return l;
				},
				Collections.singleton(Characteristics.IDENTITY_FINISH));
	}
}
