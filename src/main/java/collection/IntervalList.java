package collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntervalList {

	@FunctionalInterface
	public interface Action {
		boolean process(Interval interval);
	}

	class SingleItr implements Iterator<Interval> {

		int nextIndex = 0;
		private Interval matchMask = null;
		private Interval next = null;

		SingleItr() {
		}

		SingleItr(Interval matchMask) {
			startFind(matchMask, 0);
		}

		void startFind(Interval matchMask, int firstIndex) {
			this.next = null;

			if (isEmpty()) {
				this.matchMask = null;
				this.nextIndex = 0;
				return;
			}

			this.matchMask = matchMask;
			this.nextIndex = startFindRightNearest(matchMask.getBegin(), firstIndex);
			if (get(nextIndex).getEnd() <= matchMask.getBegin()) {
				++nextIndex;
			}

			nextFind();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Interval next() {
			return next != null ? nextFind() : null;
		}

		private Interval nextFind() {
			Interval result = next;

			if (nextIndex == size()) {
				next = null;
			} else {
				next = get(nextIndex).intersect(matchMask);
				if (next.isEmpty()) {
					next = null;
				} else {
					++nextIndex;
				}
			}

			return result;
		}
	}

	class MultiItr extends SingleItr {

		private final IntervalList matchMask;
		private int maskIndex;

		MultiItr(IntervalList matchMask) {
			this.matchMask = matchMask;
			this.maskIndex = 0;
			nextFind();
		}

		@Override
		public Interval next() {
			Interval result = super.next();
			if (!hasNext()) {
				nextFind();
			}

			return result;
		}

		private void nextFind() {
			for (; maskIndex < matchMask.size() && !hasNext(); ++maskIndex) {
				startFind(matchMask.get(maskIndex), nextIndex == 0 ? 0 : --nextIndex);
			}
		}
	}

	class Setter {
		int beginHoleIndex = 0;
		int endHoleIndex = 0;

		boolean isEmpty() {
			return beginHoleIndex == endHoleIndex;
		}

		int add(final Interval item, int pos) {
			if (this.isEmpty()) {
				sortedList.add(pos, item);
			} else {
				if (endHoleIndex < pos) {
					moveHolesToRightAt(pos - endHoleIndex);
				}

				sortedList.set(--pos, item);
				--endHoleIndex;
			}

			return pos;
		}

		void remove(int beginIndex, int endIndex) {
			if (isEmpty()) {
				beginHoleIndex = beginIndex;
			} else if (beginIndex != endHoleIndex) {
				moveHolesToRightAt(beginIndex - endHoleIndex);
			}

			endHoleIndex = endIndex;
		}

		void collapseHoles() {
			if (!this.isEmpty()) {
				sortedList.subList(beginHoleIndex, endHoleIndex).clear();
				beginHoleIndex = endHoleIndex = 0;
			}
		}

		private void moveHolesToRightAt(int countPositions) {
			while (countPositions != 0) {
				sortedList.set(beginHoleIndex, sortedList.get(endHoleIndex));
				sortedList.set(endHoleIndex, null);
				++beginHoleIndex;
				++endHoleIndex;
				--countPositions;
			}
		}
	}

	private final ArrayList<Interval> sortedList;

	public IntervalList() {
		sortedList = new ArrayList<>();
	}

	public IntervalList(int initialCapacity) {
		sortedList = new ArrayList<>(initialCapacity);
	}

	public IntervalList(final Interval... intervals) {
		this(intervals.length);
		for (Interval i : intervals) {
			add(i);
		}
	}

	public void ensureCapacity(int minCapacity) {
		sortedList.ensureCapacity(minCapacity);
	}

	public Interval get(int i) {
		return sortedList.get(i);
	}

	public Interval getFirst() {
		return sortedList.get(0);
	}

	public Interval getLast() {
		return sortedList.get(size() - 1);
	}

	public int size() {
		return sortedList.size();
	}

	public boolean isEmpty() {
		return sortedList.isEmpty();
	}

	public void add(final Interval item) {
		if (item.isEmpty()) {
			return;
		}

		Setter setter = new Setter();
		addNearby(item, 0, startFindRightNearest(item.getEnd(), 0), setter);
		setter.collapseHoles();
	}

	public void addAll(final IntervalList source) {
		if (isEmpty()) {
			this.sortedList.addAll(source.sortedList);
		} else {
			this.addAll(source.iterator());
		}
	}

	public void addAll(final IntervalList source, final Interval matchMask) {
		addAll(source.findAsc(matchMask));
	}

	public void addAll(final IntervalList source, final IntervalList matchMask) {
		addAll(source.findAsc(matchMask));
	}

	public SplitResult split(final IntervalList matchMask) {
		IntervalList disjointRes = new IntervalList();
		IntervalList overlappingRes = new IntervalList();

		if (isEmpty()) {
			return new SplitResult(disjointRes, overlappingRes);
		}

		if (matchMask.isEmpty()) {
			disjointRes.addAll(this);
			return new SplitResult(disjointRes, overlappingRes);
		}

		Interval prevMatchedInterval = Interval.EMPTY;
		int prevIndex = 0;

		MultiItr itr = new MultiItr(matchMask);
		while (itr.hasNext()) {
			final int index = itr.nextIndex == 0 ? 0 : itr.nextIndex - 1;
			final Interval matchedInterval = itr.next();
			overlappingRes.add(matchedInterval);

			// добавим предыдущий "затронутый" период
			if (prevIndex != index) {
				Interval interval = get(prevIndex);
				long end = Math.min(interval.getEnd(), matchedInterval.getBegin());
				if (prevMatchedInterval.getEnd() > interval.getBegin() && prevMatchedInterval.getEnd() < end) {
					disjointRes.add(new Interval(prevMatchedInterval.getEnd(), end));
					++prevIndex;
				}

				// добавим "незатронутые" периоды
				for (; prevIndex < index; ++prevIndex) {
					interval = get(prevIndex);
					if (interval.getEnd() >= matchedInterval.getBegin()) {
						break;
					}
					disjointRes.add(interval);
				}
			}

			// добавим "затронутый" период
			Interval affectedPeriod = get(prevIndex);
			long begin = Math.max(affectedPeriod.getBegin(), prevMatchedInterval.getEnd());
			long end = Math.min(matchedInterval.getBegin(), affectedPeriod.getEnd());
			if (begin < end) {
				disjointRes.add(new Interval(begin, end));
			}

			prevIndex = index;
			prevMatchedInterval = matchedInterval;

			if (get(prevIndex).getEnd() <= matchedInterval.getEnd()) {
				++prevIndex;
				if (size() == prevIndex) {
					return new SplitResult(disjointRes, overlappingRes);
				}
			}
		}

		// добавим предыдущий "затронутый" период
		Interval interval = get(prevIndex);
		if (prevMatchedInterval.getEnd() > interval.getBegin() && prevMatchedInterval.getEnd() < interval.getEnd()) {
			disjointRes.add(new Interval(prevMatchedInterval.getEnd(), interval.getEnd()));
			++prevIndex;
		}

		// добавим "незатронутые" периоды
		for (; prevIndex < size(); ++prevIndex) {
			disjointRes.add(get(prevIndex));
		}

		return new SplitResult(disjointRes, overlappingRes);
	}

	public void remove(final Interval matchMask) {
		if (!isEmpty()) {
			removeByMask(matchMask, size() - 1);
		}
	}

	public void remove(final IntervalList matchMask) {
		int lastIndex = size() - 1;
		for (int i = matchMask.size() - 1; lastIndex > -1 && i > -1; --i) {
			lastIndex = removeByMask(matchMask.get(i), lastIndex);
		}
	}

	private int removeByMask(final Interval matchMask, int lastIndex) {
		int beginIndex = findRightNearest(matchMask.getBegin(), 0, lastIndex);
		int endIndex = findRightNearest(matchMask.getEnd(), beginIndex, lastIndex);

		if (0 == endIndex && matchMask.getEnd() <= get(beginIndex).getBegin()) {
			return -1;
		}

		Interval interval = get(beginIndex);
		lastIndex = beginIndex;

		if (matchMask.getBegin() < interval.getEnd()) {
			if (matchMask.getBegin() > interval.getBegin()) {
				sortedList.set(beginIndex, new Interval(interval.getBegin(), matchMask.getBegin()));

				if (beginIndex == endIndex && matchMask.getEnd() < interval.getEnd()) {
					sortedList.add(beginIndex + 1, new Interval(matchMask.getEnd(), interval.getEnd()));
					return lastIndex;
				}

				++beginIndex;
			}
		} else {
			++beginIndex;
		}

		interval = get(endIndex);
		if (interval.contains(matchMask.getEnd())) {
			sortedList.set(endIndex, new Interval(matchMask.getEnd(), interval.getEnd()));
		} else {
			++endIndex;
		}

		if (beginIndex < endIndex) {
			sortedList.subList(beginIndex, endIndex).clear();
			if (isEmpty()) {
				lastIndex = -1;
			} else if (lastIndex == size()) {
				--lastIndex;
			}
		}

		return lastIndex;
	}

	public void stretchIntervals(long length) {
		if (size() < 2 || length <= 0) {
			return;
		}

		Interval current = get(0);
		int lastIndex = 0;
		long newBegin = current.getBegin();
		long newEnd = 0;
		for (int i = 1; i < size(); ++i) {
			Interval next = get(i);

			if ((next.getBegin() - current.getEnd()) <= length) {
				newEnd = next.getEnd();
				current = next;
				continue;
			}

			if (newEnd != 0) {
				sortedList.set(lastIndex, new Interval(newBegin, newEnd));
				newEnd = 0;
			} else if ((lastIndex + 1) != i) {
				sortedList.set(lastIndex, next);
			}

			current = next;
			newBegin = current.getBegin();
			++lastIndex;
		}

		if ((lastIndex + 1) == size()) {
			return;
		}

		if (newEnd != 0) {
			sortedList.set(lastIndex, new Interval(newBegin, newEnd));
		} else {
			sortedList.set(lastIndex, current);
		}

		sortedList.subList(++lastIndex, size()).clear();
	}

	public void clear() {
		sortedList.clear();
	}

	public Iterator<Interval> iterator() {
		return sortedList.iterator();
	}

	public Iterator<Interval> findAsc(final Interval matchMask) {
		return new SingleItr(matchMask);
	}

	public Iterator<Interval> findAsc(final IntervalList matchMask) {
		return new MultiItr(matchMask);
	}

	public void forEachInvertingAsc(Action action) {
		forEachInvertingAsc(null, iterator(), action);
	}

	public void forEachInvertingAsc(boolean addMargins, final Interval matchMask, Action action) {
		forEachInvertingAsc(addMargins ? matchMask : null, findAsc(matchMask), action);
	}

	private void forEachInvertingAsc(final Interval matchMask, Iterator<Interval> iter, Action action) {
		if (iter.hasNext()) {
			Interval current = iter.next();
			if (matchMask != null && matchMask.getBegin() < current.getBegin()) {
				if (!action.process(new Interval(matchMask.getBegin(), current.getBegin()))) {
					return;
				}
			}

			long prevEnd = current.getEnd();
			while (iter.hasNext()) {
				current = iter.next();
				if (!action.process(new Interval(prevEnd, current.getBegin()))) {
					return;
				}
				prevEnd = current.getEnd();
			}

			if (matchMask != null && prevEnd < matchMask.getEnd()) {
				action.process(new Interval(prevEnd, matchMask.getEnd()));
			}
		} else if (matchMask != null && !matchMask.isEmpty()) {
			action.process(matchMask);
		}
	}

	public Interval findLast(final Interval matchMask) {
		if (isEmpty()) {
			return Interval.EMPTY;
		}

		int pos = startFindRightNearest(matchMask.getEnd(), 0);
		return get(pos).intersect(matchMask);
	}

	public long sumTotalLength() {
		return sumLength(iterator());
	}

	public long sumTotalLength(final Interval matchMask) {
		return sumLength(findAsc(matchMask));
	}

	public long sumTotalLength(final IntervalList matchMask) {
		return sumLength(findAsc(matchMask));
	}

	public boolean intersects(final Interval target) {
		return findAsc(target).hasNext();
	}

	public boolean contains(final Interval target) {
		Iterator<Interval> i = findAsc(target);
		return i.hasNext() && i.next().equals(target);
	}

	public Interval getTotalInterval() {
		if (isEmpty()) {
			return Interval.EMPTY;
		}
		if (size() == 1) {
			return getFirst();
		}
		return new Interval(getFirst().getBegin(), getLast().getEnd());
	}

	public Interval getTotalInterval(final Interval matchMask) {
		if (isEmpty()) {
			return Interval.EMPTY;
		}

		int endIndex = startFindRightNearest(matchMask.getEnd(), 0);
		int beginIndex = findRightNearest(matchMask.getBegin(), 0, endIndex);

		if (beginIndex == endIndex) {
			return get(endIndex).intersect(matchMask);
		}

		long newEnd = Math.min(matchMask.getEnd(), get(endIndex).getEnd());

		Interval interval = get(beginIndex);
		if (interval.getEnd() < matchMask.getBegin()) {
			return new Interval(get(beginIndex + 1).getBegin(), newEnd);
		}

		return new Interval(Math.max(matchMask.getBegin(), interval.getBegin()), newEnd);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntervalList that = (IntervalList) o;
		return sortedList.equals(that.sortedList);
	}

	@Override
	public int hashCode() {
		return sortedList.hashCode();
	}

	public boolean isEqualList(final List<Interval> list) {
		if (list == null || size() != list.size()) {
			return false;
		}

		for (int i = 0; i < size(); ++i) {
			if (!get(i).equals(list.get(i))) {
				return false;
			}
		}

		return true;
	}

	private void addAll(Iterator<Interval> ascIterator) {
		Setter setter = new Setter();
		int beginIndex = 0, endIndex = -1;
		while (ascIterator.hasNext()) {
			final Interval insertingInterval = ascIterator.next();

			if (endIndex == -1) {
				beginIndex = 0;
				endIndex = startFindRightNearest(insertingInterval.getEnd(), 0);
			} else {
				beginIndex = endIndex;
				endIndex = nextFindRightNearest(insertingInterval.getEnd(), endIndex);
			}

			endIndex = addNearby(insertingInterval, beginIndex, endIndex, setter);
		}

		setter.collapseHoles();
	}

	private int addNearby(final Interval item, int beginIndex, int rightBorderNearestIndex, Setter setter) {
		if (isEmpty()) {
			sortedList.add(item);
			return 0;
		}

		Interval interval = get(rightBorderNearestIndex);
		//если попали в начало интервалов
		if (rightBorderNearestIndex == beginIndex && item.getEnd() < interval.getBegin()) {
			return setter.add(item, beginIndex);
		}

		//если попали в конец интервалов
		if (rightBorderNearestIndex == (size() - 1) && interval.getEnd() < item.getBegin()) {
			setter.collapseHoles();
			sortedList.add(item);
			return size() - 1;
		}

		beginIndex = findRightNearest(item.getBegin(), beginIndex, rightBorderNearestIndex);
		// если попали в промежуток между интервалами
		if (rightBorderNearestIndex == beginIndex && interval.getEnd() < item.getBegin()) {
			return setter.add(item, beginIndex + 1);
		}

		// случай пересечения входного интервала
		long newEnd = Math.max(item.getEnd(), interval.getEnd());
		interval = get(beginIndex);

		if (interval.getEnd() < item.getBegin()) {
			interval = get(++beginIndex);
		}

		if (item.getBegin() < interval.getBegin()) {
			interval = new Interval(item.getBegin(), newEnd);
		} else if (interval.getEnd() != newEnd) {
			interval = new Interval(interval.getBegin(), newEnd);
		}

		sortedList.set(rightBorderNearestIndex, interval);

		if (beginIndex < rightBorderNearestIndex) {
			setter.remove(beginIndex, rightBorderNearestIndex);
		}

		return rightBorderNearestIndex;
	}

	private int startFindRightNearest(long value, int firstIndex) {
		return findRightNearest(value, firstIndex, isEmpty() ? 0 : size() - 1);
	}

	private int nextFindRightNearest(long value, int startIndex) {
		if (isEmpty()) {
			return startIndex;
		}

		Interval current = get(startIndex);
		if (value < current.getBegin()) {
			return startIndex;
		}

		Interval next;
		for (; startIndex < size() - 1; ++startIndex) {
			next = get(startIndex + 1);

			if (current.getBegin() <= value && value < next.getBegin()) {
				break;
			}

			current = next;
		}

		return startIndex;
	}

	private int findRightNearest(long value, int firstIndex, int lastIndex) {
		if (isEmpty() || value <= get(firstIndex).getEnd()) {
			return firstIndex;
		}

		if (value >= get(lastIndex).getBegin()) {
			return lastIndex;
		}

		return binarySearch(value, firstIndex, lastIndex + 1);
	}

	private int binarySearch(long value, int firstIndex, int lastIndex) {
		while (firstIndex < lastIndex) {
			// COMMENT: В отличие от более простого (first + last) / 2, этот код устойчив к переполнениям
			int midIndex = firstIndex + (lastIndex - firstIndex) / 2;
			Interval midInterval = get(midIndex);

			if (value < midInterval.getBegin()) {
				if (value >= get(midIndex - 1).getBegin()) {
					return midIndex - 1;
				}

				lastIndex = midIndex;
			} else if (value > midInterval.getEnd()) {
				if (value < get(midIndex + 1).getBegin()) {
					return midIndex;
				}

				firstIndex = midIndex + 1;
			} else {
				return midIndex;
			}
		}

		return lastIndex;
	}

	/**
	 * @param intervalsList must be contains nonintersected intervals
	 * @param length
	 * @throws IllegalArgumentException if the intervalsList is contains intersected intervals
	 */
	public static void stretchIntervals(final List<IntervalList> intervalsList, long length) {
		if (length <= 0) {
			return;
		}

		Position[] positions = new Position[intervalsList.size()];
		for (int i = 0; i < positions.length; ++i) {
			positions[i] = new Position();
		}

		int currIndex = getNextLeftNearestIndex(intervalsList, positions);
		if (currIndex == -1) {
			return;
		}

		int currentIntervalIndex = positions[currIndex].index;
		Interval currentInterval = intervalsList.get(currIndex).get(currentIntervalIndex);
		while (true) {
			int nextIndex = getNextLeftNearestIndex(intervalsList, positions);
			if (nextIndex == -1) {
				break;
			}

			int nextIntervalIndex = positions[nextIndex].index;
			Interval nextInterval = intervalsList.get(nextIndex).get(nextIntervalIndex);
			long diff = nextInterval.getBegin() - currentInterval.getEnd();
			if (diff < 0) {
				throw new IllegalArgumentException("intervalsList is contains intersected intervals.");
			}

			Position currentPos = positions[currIndex];
			final ArrayList<Interval> currIntervals = intervalsList.get(currIndex).sortedList;
			if (diff != 0 && diff <= length) {
				if (currIndex == nextIndex) {
					nextInterval = new Interval(currentInterval.getBegin(), nextInterval.getEnd());
					currIntervals.set(nextIntervalIndex, nextInterval);
					currentPos.addHole(currentIntervalIndex);
				} else {
					currentInterval = new Interval(currentInterval.getBegin(), nextInterval.getBegin());
					if (currentPos.existsHole()) {
						currIntervals.set(currentPos.firstHoleIndex++, currentInterval);
					} else {
						currIntervals.set(currentIntervalIndex, currentInterval);
					}
				}
			} else if (currentPos.existsHole()) {
				currIntervals.set(currentPos.firstHoleIndex++, currentInterval);
			}

			currIndex = nextIndex;
			currentIntervalIndex = nextIntervalIndex;
			currentInterval = nextInterval;
		}

		for (int i = 0; i < positions.length; ++i) {
			Position pos = positions[i];
			if (pos.existsHole()) {
				intervalsList.get(i).sortedList.subList(pos.firstHoleIndex, pos.firstHoleIndex + pos.holeCount).clear();
			}
		}
	}

	private static int getNextLeftNearestIndex(final List<IntervalList> intervalsList, final Position[] positions) {
		int minIndex = -1;
		long minBegin = Long.MIN_VALUE;

		for (int i = 0; i < positions.length; ++i) {
			IntervalList currIntervals = intervalsList.get(i);
			int currPos = positions[i].index + 1;
			if (currPos >= currIntervals.size()) {
				continue;
			}

			long currBegin = currIntervals.get(currPos).getBegin();
			if (minIndex == -1 || currBegin < minBegin) {
				minIndex = i;
				minBegin = currBegin;
			}
		}

		if (minIndex != -1) {
			++positions[minIndex].index;
		}

		return minIndex;
	}

	private static long sumLength(final Iterator<Interval> i) {
		long result = 0;
		while (i.hasNext()) {
			result += i.next().getLength();
		}
		return result;
	}

	public static class SplitResult {
		private final IntervalList disjointList;
		private final IntervalList overlappingList;

		SplitResult(IntervalList disjointList, IntervalList overlappingList) {
			this.disjointList = disjointList;
			this.overlappingList = overlappingList;
		}

		public IntervalList getDisjointList() {
			return disjointList;
		}

		public IntervalList getOverlappingList() {
			return overlappingList;
		}
	}

	private static class Position {
		int index = -1;
		int firstHoleIndex = 0;
		int holeCount = 0;

		void addHole(int holeIndex) {
			if (holeCount == 0) {
				firstHoleIndex = holeIndex;
			}
			++holeCount;
		}

		boolean existsHole() {
			return holeCount != 0;
		}
	}
}
