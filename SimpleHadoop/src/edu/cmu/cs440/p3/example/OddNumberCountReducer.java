package edu.cmu.cs440.p3.example;

import java.io.IOException;
import java.util.Iterator;

import edu.cmu.cs440.p3.Interface.Reducer;
import edu.cmu.cs440.p3.io.RecordWriter;

/**
 * A reducer example to count the total number of odds
 * 
 */
public class OddNumberCountReducer implements Reducer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5260529551849657942L;

	@Override
	public int getRecordLength() {
		return 128;
	}

	@Override
	public void Reduce(String key, Iterator<String> values, RecordWriter writer)
			throws Exception {
		int total = 0;
		while (values.hasNext()) {
			values.next();
			total++;
		}
		writer.setValue(key + " total " + total + "\n");
		try {
			writer.Write();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
