package collection;

import java.util.function.IntConsumer;

public class MinMaxIntStatistic implements IntConsumer {

	private int min = Integer.MAX_VALUE;
	private int max = Integer.MIN_VALUE;

	MinMaxIntStatistic() {
	}

	/**
	 * Records a new value into the summary information
	 *
	 * @param value the input value
	 */
	@Override
	public void accept(int value) {
		min = Math.min(min, value);
		max = Math.max(max, value);
	}

	/**
	 * Combines the state of another {@code IntSummaryStatistics} into this one.
	 *
	 * @param other another {@code IntSummaryStatistics}
	 * @throws NullPointerException if {@code other} is null
	 */
	public void combine(MinMaxIntStatistic other) {
		min = Math.min(min, other.min);
		max = Math.max(max, other.max);
	}

	public final int getMin() {
		return min;
	}

	public final int getMax() {
		return max;
	}
}
