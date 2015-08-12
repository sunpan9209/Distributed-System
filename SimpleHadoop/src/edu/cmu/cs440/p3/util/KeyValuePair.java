package edu.cmu.cs440.p3.util;

import java.io.Serializable;

/**
 * output from map task
 */
public class KeyValuePair implements Serializable, Comparable<KeyValuePair> {
	private static final long serialVersionUID = -3076818555292211066L;

	private String mKey;
	private String mValue;

	public KeyValuePair(String key, String value) {
		mKey = key;
		mValue = value;
	}

	public String getKey() {
		return mKey;
	}

	public String getValue() {
		return mValue;
	}

	public void setKey(String key) {
		mKey = key;
	}

	public void setValue(String value) {
		mValue = value;
	}

	@Override
	public String toString() {
		return String.format("<%s: key=%s, value=%s>",
				KeyValuePair.class.getSimpleName(), mKey, mValue);
	}

	@Override
	public int compareTo(KeyValuePair o) {
		return this.mKey.compareTo(o.mKey);
	}

}
