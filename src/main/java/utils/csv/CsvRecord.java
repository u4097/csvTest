package utils.csv;

import com.google.common.collect.Iterators;
import org.apache.commons.csv.CSVRecord;

import java.lang.reflect.Constructor;

public class CsvRecord {

	private final CSVRecord record;

	public CsvRecord(CSVRecord record) {
		this.record = record;
	}

	public CsvRecord(String[] values) {
		Constructor<?> constructor = CSVRecord.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		try {
			this.record = (CSVRecord) constructor.newInstance(values, null, null, 0, 0);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public int size() {
		return record.size();
	}

	public String get(int i) {
		return record.get(i);
	}

	@Override
	public String toString() {
		return Iterators.toString(record.iterator());
	}
}
