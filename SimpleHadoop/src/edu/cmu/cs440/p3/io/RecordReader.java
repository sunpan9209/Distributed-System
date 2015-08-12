package edu.cmu.cs440.p3.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Closeable;

public class RecordReader implements Closeable {
	/**
	 * Make the recoder according to the size of request and make record
	 */
	private long key;
	private byte[] bytes;
	private String value = null;
	private InputSplit splitblock;
	private int length;
	private RandomAccessFile file;
	private long position;
	private long end;

	/**
	 * make a record for small size file
	 */
	public void makeRecord() throws IOException {

		long start = splitblock.getstart();
		long end = start + splitblock.getlength();

		final String path = splitblock.getpath();

		file = new RandomAccessFile(path, "r");

		boolean skipFirstLine = false;

		if (start != 0) {
			skipFirstLine = true;
			--start;
			file.seek(start);
		}

		if (skipFirstLine) {
			String none = file.readLine();
			start = start + none.length();
			file.seek(start);
		}
		// Position is the actual start for each line
		this.key = start;
		if (start <= end) {
			this.bytes = readlines(file, (int) start, (int) (end));
		}
		this.value = new String(this.bytes);
	}

	/**
	 * Accoring to the byte-baesd split to form record
	 */
	public byte[] readlines(RandomAccessFile file, int position, int end)
			throws IOException {
		int byteconsum = 0;
		ByteArrayBuffer mReadBuffer = new ByteArrayBuffer(0);

		while (position < end) {
			String s = file.readLine();
			int offset = 0;
			if (s != null) {
				byte[] buf = s.getBytes();
				mReadBuffer.append(buf, 0, buf.length);
				offset = buf.length;
			} else {
				offset = 500;
			}
			position = position + offset;
			byteconsum = byteconsum + offset;
		}
		this.length = byteconsum;
		return mReadBuffer.buffer();

	}

	public RecordReader(InputSplit splitblock) {
		this.splitblock = splitblock;
	}

	public long getkey() {

		return key;
	}

	public byte[] getbytes() {

		return bytes;
	}

	public String getvalue() {

		return value;
	}

	/**
	 * Get the record line by line
	 */
	public Record getRecord() throws IOException {
		long start = splitblock.getstart();
		this.end = start + splitblock.getlength();

		final String path = splitblock.getpath();

		file = new RandomAccessFile(path, "r");

		boolean skipFirstLine = false;

		if (start != 0) {
			skipFirstLine = true;
			--start;
			file.seek(start);
		}

		if (skipFirstLine) {
			String none = file.readLine();
			start = start + none.length();
			file.seek(start);

		}
		this.position = start;
		// Position is the actual start
		this.key = start;

		if (start <= end) {
			this.bytes = readline(file, (int) position);
		}
		if (this.bytes != null) {
			this.value = new String(this.bytes);
		}

		Record record = new Record();
		record.setKey(position);
		record.setValue(value);

		return record;

	}

	public Record nextRecord() throws IOException {

		Record record = new Record();
		record.setKey(position);
		record.setValue(value);

		file.seek(this.position);
		if (this.position < this.end) {
			this.bytes = readline(this.file, (int) this.position);
			this.value = new String(this.bytes);

			record.setValue(value);

			return record;
		} else {
			return null;
		}
	}

	/**
	 * read a line for mapper to iteration
	 */
	public byte[] readline(RandomAccessFile file, int position)
			throws IOException {
		ByteArrayBuffer mReadBuffer = new ByteArrayBuffer(0);

		file.seek(this.position);
		String s = file.readLine();
		int offset = 0;
		if (s != null) {
			byte[] buf = s.getBytes();

			mReadBuffer.append(buf, 0, buf.length);
			offset = buf.length;

			if (offset == 0) {
				this.position = this.position + 1;
				return readline(file, (int) (this.position));
			}

		} else {
			offset = 500;
		}

		this.position = this.position + offset;
		return mReadBuffer.buffer();

	}

	public int getLength() {
		return length;
	}

	public void close() throws IOException {
		this.file.close();
	}

}
