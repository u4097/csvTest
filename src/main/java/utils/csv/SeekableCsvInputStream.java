package utils.csv;

import sun.nio.ch.ChannelInputStream;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SeekableCsvInputStream extends ChannelInputStream {

	private static final int READING_STATUS_CONTINUE = 0;
	private static final int READING_STATUS_EOF = 1;
	private static final int READING_STATUS_TRIMMED = 2;

	private static final byte CR = '\r';
	private static final byte LF = '\n';

	private final long maxBytesRead;

	private long bytesRead = 0;
	private long currentPosition = 0;
	private int readingStatus = READING_STATUS_CONTINUE;

	public SeekableCsvInputStream(Path csvPath, long startPosition, long maxBytesRead) throws IOException {
		super(buildChannel(csvPath, startPosition));

		this.maxBytesRead = maxBytesRead;
		this.currentPosition = startPosition;
	}

	public SeekableCsvInputStream(Path csvPath, long startPosition) throws IOException {
		this(csvPath, startPosition, Long.MAX_VALUE);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (readingStatus != READING_STATUS_CONTINUE) {
			return -1;
		}

		int count = super.read(b, off, len);
		if (count == -1) {
			readingStatus = READING_STATUS_EOF;
		} else {
			bytesRead += count;
			if (bytesRead >= maxBytesRead) {
				int pos;
				if (count > (bytesRead - maxBytesRead)) {
					int fromIndex = (int) (count - (bytesRead - maxBytesRead));
					fromIndex = indexOfNotNewLine(b, fromIndex - 1, off + count);
					pos = lastIndexOfNewLine(b, fromIndex, off);
					if (pos == -1) {
						pos = indexOfEndNewLine(b, fromIndex, off + count);
					}
				} else {
					pos = indexOfEndNewLine(b, off, off + count);
				}

				if (pos != -1) {
					count = pos - off + 1;
					readingStatus = READING_STATUS_TRIMMED;
				}
			}

			currentPosition += count;
		}

		return count;
	}

	public long getCurrentPosition() {
		return currentPosition;
	}

	public boolean isEndReached() {
		return readingStatus == READING_STATUS_EOF;
	}

	private static ReadableByteChannel buildChannel(Path csvPath, long startPosition) throws IOException {
		SeekableByteChannel channel = Files.newByteChannel(csvPath, StandardOpenOption.READ);
		try {
			channel.position(startPosition);
			return channel;
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException ignore) {
			}
			throw e;
		}
	}

	private static int lastIndexOfNewLine(byte[] source, int from, int to) {
		boolean existsNotNewLineChar = false;
		for (int i = from; i >= to; --i) {
			if (isNewLine(source[i])) {
				if (existsNotNewLineChar) {
					return i;
				}
			} else {
				existsNotNewLineChar = true;
			}
		}

		return -1;
	}

	private static int indexOfEndNewLine(byte[] source, int from, int to) {
		boolean existsNewLine = false;
		for (int i = from; i < to; ++i) {
			if (isNewLine(source[i])) {
				existsNewLine = true;
			} else if (existsNewLine) {
				return i - 1;
			}
		}

		return -1;
	}

	private static int indexOfNotNewLine(byte[] b, int from, int to) {
		for (int i = from; i < to; ++i) {
			if (!isNewLine(b[i])) {
				return i;
			}
		}

		return from;
	}

	private static boolean isNewLine(byte b) {
		switch (b) {
			case CR:
			case LF:
				return true;
			default:
				return false;
		}
	}
}
