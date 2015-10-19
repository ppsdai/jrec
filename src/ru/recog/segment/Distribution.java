package ru.recog.segment;

import java.util.Arrays;

public class Distribution {
	
	double[] distrib;
	long N = 0;
	
	double low, high;
	int bins;


	
	public Distribution(double low, double high, int bins) {
		distrib = new double[bins];
		this.low = low;
		this.high = high;
		this.bins = bins;
		Arrays.fill(distrib, 0);
	}
	
	public Distribution() {
		this(0.5, 1.5, 10);
	}
	
	private int getBinIndex(double length) {
		if (length < low || length >= high) return -1;

		return (int)Math.floor((length - low)/(high - low)*bins);
	}
	
		
	public void addSample(double length) {
		N++;
		int index = getBinIndex(length);
		if (index == -1) return;
		distrib[index] += 1;
	}
		
	public double getProbability(double length) {
		int index = getBinIndex(length);
		return index == -1? 0 : distrib[index];

	}
		
	@Override
	public String toString() {
		return "D(" + String.valueOf(N) + ") " + Arrays.toString(distrib)
				+ " Total: " + total();
	}

	public void norm() {
		for (int i = 0; i < 10; i++)
			distrib[i] = distrib[i] / N;
	}

	public double total() {
		return Arrays.stream(distrib).sum();
	}
	
	void setDistribution(double[] distrib) {
		this.distrib = distrib;
	}
		
}