package utils.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CsvSettings {

	public static final CsvSettings DEFAULT = new CsvSettings(';', "\r\n", StandardCharsets.UTF_8);

	private final char delimiter;
	private final String recordSeparator;
	private final Charset encoding;

	public CsvSettings(char delimiter, String recordSeparator, Charset encoding) {
		this.delimiter = delimiter;
		this.recordSeparator = recordSeparator;
		this.encoding = encoding;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public String getRecordSeparator() {
		return recordSeparator;
	}

	public Charset getEncoding() {
		return encoding;
	}
}
