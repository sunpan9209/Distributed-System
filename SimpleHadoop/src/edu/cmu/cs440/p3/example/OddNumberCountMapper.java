package edu.cmu.cs440.p3.example;

import edu.cmu.cs440.p3.Interface.Mapper;
import edu.cmu.cs440.p3.util.Emitter;

/**
 * A mapper example to output only odd numbers
 */
public class OddNumberCountMapper implements Mapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7268382027411404737L;

	public OddNumberCountMapper() {

	}

	@Override
	public int getNumMapper() {
		return 3;
	}

	@Override
	public int getNumReducer() {
		return 2;
	}

	@Override
	public void Map(long key, String value, Emitter collector) throws Exception {
		String[] words = value.split(" ");
		for (String s : words) {
			@SuppressWarnings("unused")
			boolean flag = false;
			flag = s.matches("\\d+");
			if ((s == " ") || (s == "")) {
				flag = false;
			}
			if (flag = true) {
				if ((Integer.parseInt(s)) % 2 == 1) {
					collector.collect(s, "1");
				}
			}
		}
	}

}
