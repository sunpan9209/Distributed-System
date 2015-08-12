package edu.cmu.cs440.p3.example;

import java.util.Iterator;

import edu.cmu.cs440.p3.Interface.Reducer;
import edu.cmu.cs440.p3.io.RecordWriter;

/**
 * count word, key: word, values: a list of "1"
 */
public class WordCountReducer implements Reducer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4826047660645217695L;

	public WordCountReducer() {

	}

	@Override
	public void Reduce(String key, Iterator<String> values, RecordWriter writer)
			throws Exception {
		int total = 0;
		while (values.hasNext()) {
			total += Integer.parseInt(values.next());
		}
		writer.setValue(key + " total " + total + "\n");
		writer.Write();
	}

	@Override
	public int getRecordLength() {
		return 128;
	}

}
