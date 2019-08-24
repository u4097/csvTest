package utils.csv;

import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.Iterator;

public class CsvRecordIterator implements Iterator<CsvRecord> {

	private final Iterator<CSVRecord> internalIterator;

	CsvRecordIterator(Iterator<CSVRecord> internalIterator) {
		this.internalIterator = internalIterator;
	}

	@Override
	public boolean hasNext() {
		try {
			return internalIterator.hasNext();
		} catch (Throwable t) {
			if (t.getCause() == null || !(t.getCause() instanceof IOException)) {
				throw t;
			}

			throw new CsvParseException(t.getCause());
		}
	}

	@Override
	public CsvRecord next() {
		try {
			return new CsvRecord(internalIterator.next());
		} catch (Throwable t) {
			if (t.getCause() == null || !(t.getCause() instanceof IOException)) {
				throw t;
			}

			throw new CsvParseException(t.getCause());
		}
	}
}
