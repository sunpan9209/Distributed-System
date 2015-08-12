package edu.cmu.cs440.p3.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.cs440.p3.Interface.Mapper;
import edu.cmu.cs440.p3.util.Emitter;

/**
 * count words, key: word value: 1
 */
public class WordCountMapper implements Mapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 402064830499139757L;

	public WordCountMapper() {

	}

	@Override
	public void Map(long key, String value, Emitter collector) throws Exception {
		String str = value.replaceAll("\\pP\\p{Punct}\\(\\)\\*\\$\\°", "");
		Pattern p = Pattern
				.compile("[.,\"\\?!:'\\,\\[\\!\\/\\{\\}\\;\\(\\)\\-\\*\\@\\#\\$\\^\\&\\°]");
		Matcher m = p.matcher(str);
		String str1 = m.replaceAll("");
		String[] words = str1.split(" ");
		for (String s : words) {
			collector.collect(s.trim(), "1");
		}

	}

	@Override
	public int getNumMapper() {
		return 3;
	}

	@Override
	public int getNumReducer() {
		return 2;
	}
}
