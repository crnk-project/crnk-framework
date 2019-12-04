package io.crnk.meta.model;

import java.util.HashMap;
import java.util.Map;

public class MetaNature {
	private Map<String, String> strings;
	private Map<String, String[]> stringArrays;
	private Map<String, Integer> ints;
	private Map<String, Integer[]> intArrays;
	private Map<String, Double> doubles;
	private Map<String, Double[]> doubleArrays;
	private Map<String, Boolean> booleans;
	private Map<String, Boolean[]> booleanArrays;

	public Map<String, String> getStrings() {
		if (strings == null) {
			strings = new HashMap<>();
		}
		return strings;
	}

	public void setStrings(Map<String, String> strings) {
		this.strings = strings;
	}

	public Map<String, String[]> getStringArrays() {
		if (stringArrays == null) {
			stringArrays = new HashMap<>();
		}
		return stringArrays;
	}

	public void setStringArrays(Map<String, String[]> stringArrays) {
		this.stringArrays = stringArrays;
	}

	public Map<String, Integer> getInts() {
		if (ints == null) {
			ints = new HashMap<>();
		}
		return ints;
	}

	public void setInts(Map<String, Integer> ints) {
		this.ints = ints;
	}

	public Map<String, Integer[]> getIntArrays() {
		if (intArrays == null) {
			intArrays = new HashMap<>();
		}
		return intArrays;
	}

	public void setIntArrays(Map<String, Integer[]> intArrays) {
		this.intArrays = intArrays;
	}

	public Map<String, Double> getDoubles() {
		if (doubles == null) {
			doubles = new HashMap<>();
		}
		return doubles;
	}

	public void setDoubles(Map<String, Double> doubles) {
		this.doubles = doubles;
	}

	public Map<String, Double[]> getDoubleArrays() {
		if (doubleArrays == null) {
			doubleArrays = new HashMap<>();
		}
		return doubleArrays;
	}

	public void setDoubleArrays(Map<String, Double[]> doubleArrays) {
		this.doubleArrays = doubleArrays;
	}

	public Map<String, Boolean> getBooleans() {
		if (booleans == null) {
			booleans = new HashMap<>();
		}
		return booleans;
	}

	public void setBooleans(Map<String, Boolean> booleans) {
		this.booleans = booleans;
	}

	public Map<String, Boolean[]> getBooleanArrays() {
		if (booleanArrays == null) {
			booleanArrays = new HashMap<>();
		}
		return booleanArrays;
	}

	public void setBooleanArrays(Map<String, Boolean[]> booleanArrays) {
		this.booleanArrays = booleanArrays;
	}
}
