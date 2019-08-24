package collection;

/**
 * Interval in the mathematically form [begin, end), i.e. the end-value does not belong to this interval.
 * Interval is immutable class.
 */
public class Interval {

    public static final Interval EMPTY = new Interval();
    public static final Interval MAX = new Interval(0, Long.MAX_VALUE);

    private final long begin;
    private final long end;

    private Interval() {
        this.begin = 0;
        this.end = 0;
    }

    /**
     * @throws IllegalArgumentException if begin greater or equal than end
     */
    public Interval(final long begin, final long end) {
        if (begin >= end) {
            throw new IllegalArgumentException("Begin greater or equal than end");
        }
        this.begin = begin;
        this.end = end;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public boolean contains(final long value) {
        return value >= begin && value < end;
    }

    public boolean contains(final Interval target) {
        return begin <= target.begin && end >= target.end && !isEmpty();
    }

    public boolean intersects(final Interval target) {
        return !(target.end <= begin || target.begin >= end);
    }

    public boolean isEmpty() {
        return begin == 0 && end == 0;
    }

    public long getLength() {
        return end - begin;
    }

    public Interval intersect(final Interval input) {
        if (input.contains(this)) {
            return this;
        }

        if (this.contains(input)) {
            return input;
        }

        long newBegin = Math.max(input.begin, begin);
        long newEnd = Math.min(input.end, end);
        if (newBegin >= newEnd) {
            return EMPTY;
        }

        return new Interval(newBegin, newEnd);
    }

    public Interval join(final Interval value) {
        if (value.isEmpty() || this.contains(value)) {
            return this;
        }

        if (isEmpty() || value.contains(this)) {
            return value;
        }

        return new Interval(Math.min(value.begin, begin), Math.max(value.end, end));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;
        return begin == interval.begin && end == interval.end;
    }

    @Override
    public int hashCode() {
        int result = (int) (begin ^ (begin >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "[begin=" + begin + ", end=" + end + ']';
    }
}
