package utils.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class CsvParser implements Closeable {

	private final Reader reader;
	private final CSVParser parser;

	public CsvParser(Reader reader, CsvSettings csvSettings) throws CsvParseException {
		this.reader = reader;
		CSVFormat settings = CSVFormat.DEFAULT
				.withDelimiter(csvSettings.getDelimiter())
				.withRecordSeparator(csvSettings.getRecordSeparator());
		try {
			parser = new CSVParser(reader, settings);
		} catch (IOException e) {
			throw new CsvParseException(e);
		}
	}

	public Iterator<CsvRecord> iterator() {
		return new CsvRecordIterator(parser.iterator());
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
