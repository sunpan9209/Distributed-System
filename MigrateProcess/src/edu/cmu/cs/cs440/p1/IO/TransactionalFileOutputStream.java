package edu.cmu.cs.cs440.p1.IO;

import java.io.*;

/**
 * serializable fileoutputstream
 */
public class TransactionalFileOutputStream extends OutputStream implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileName;
	private long pos;
	private boolean migrated;
	private transient RandomAccessFile fileStream;

	/**
	 * constructor
	 */
	public TransactionalFileOutputStream() {
	}

	public TransactionalFileOutputStream(String fileName) {
		this.fileName = fileName;
		this.pos = 0L;
		migrated = true;
		try {
			fileStream = new RandomAccessFile(fileName, "rws");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getPos() {
		return pos;
	}

	public void setPos(long pos) {
		this.pos = pos;
	}

	@Override
	public String toString() {
		return "TransactionalOutputStream [fileName=" + fileName + ", pos="
				+ pos + "]";
	}

	/**
	 * Writes the specified byte to this file output stream.
	 */
	@Override
	public void write(int b) throws IOException {
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		fileStream.seek(pos++);
		fileStream.write(b);
	}

	/**
	 * Writes b.length bytes
	 */
	@Override
	public void write(byte[] b) throws IOException {
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		fileStream.seek(pos);
		fileStream.write(b);
		pos += b.length;
	}

	/**
	 * Writes len bytes from the specified byte array starting at offset off to
	 * this file output stream.
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		fileStream.seek(pos);
		fileStream.write(b, off, len);
		pos += len;
	}

	/**
	 * close the Stream
	 */
	public void closeStream() {
		try {
			fileStream.close();
		} catch (IOException e) {
			System.out.println("Can't close input file");
			e.printStackTrace();
		}
	}

	public boolean getMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}
}
