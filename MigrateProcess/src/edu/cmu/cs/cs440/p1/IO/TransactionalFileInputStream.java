package edu.cmu.cs.cs440.p1.IO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * open file, seek the position, read and close the file.
 */
public class TransactionalFileInputStream extends InputStream implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4892910270490519157L;
	private String fileName;
	private long pos;
	private boolean migrated; // flag for migration
	private transient RandomAccessFile fileStream;

	/**
	 * constructor
	 */
	public TransactionalFileInputStream() {
	}

	public TransactionalFileInputStream(String fileName) {
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

	/**
	 * read a byte the file from specified position.
	 */
	@Override
	public int read() throws IOException {
		int returnVal = 0;
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		fileStream.seek(pos);
		returnVal = fileStream.read();
		if (returnVal != -1)
			pos++;
		return returnVal;
	}

	/**
	 * close the file handler
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

	@Override
	public String toString() {
		return "TransactionalInputStream [fileName=" + fileName + ", pos="
				+ pos + "]";
	}
}
