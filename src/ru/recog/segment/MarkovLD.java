package ru.recog.segment;

import java.io.*;
import java.net.URL;
import java.util.*;

import ru.recog.segment.SegmentationLog.SegmentationLogEntry;

public class MarkovLD {
	
	private Distribution[] distributions;
	
	private static MarkovLD defaultMLD;
	
	private static URL DEFAULT_MLD = System.class.getResource("/mld.txt"); 

	
	public static MarkovLD getDefaultMLD() {
		if (defaultMLD == null)
			defaultMLD = create();
		return defaultMLD;
	}


	private static MarkovLD create() {
		
		MarkovLD mld = new MarkovLD();
		try {
			mld = loadMLD();
			mld.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mld;
		
	}
	
	private static MarkovLD loadMLD() throws IOException {
		List<String> dlist = new ArrayList<String>();
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(DEFAULT_MLD.openStream()));
		for (String line; (line = lnr.readLine()) != null;) 
			dlist.add(line);
		
		lnr.close();
		
		
		MarkovLD mld = new MarkovLD();
		mld.distributions = new Distribution[dlist.size()];
		for (int i = 0; i < dlist.size(); i ++) {
			mld.distributions[i] = new Distribution();
			mld.distributions[i].setDistribution(parseToDouble(dlist.get(i)));
		}
		
		return mld;
	}
	
	
	public void print() {
		for (Distribution d : distributions)
			System.out.println(Arrays.toString(d.distrib));
	}
	
	private MarkovLD() {
	}
	
	private static Distribution[] buildDistributions() throws Exception {
		Distribution[] SD = new Distribution[]{new Distribution(),new Distribution(),new Distribution(),
				new Distribution(),new Distribution(),new Distribution()};
		
		long t0 = System.nanoTime();
		
		List<SegmentationLogEntry> totalEntries = new ArrayList<SegmentationLogEntry>();
		totalEntries.addAll(SegmentationLog.readSegmentationLog("/Users/pps/dev/seglog/seglog047.txt"));
		totalEntries.addAll(SegmentationLog.readSegmentationLog("/Users/pps/dev/seglog/seglog049.txt"));
		totalEntries.addAll(SegmentationLog.readSegmentationLog("/Users/pps/dev/seglog/seglog050.txt"));
	
		for (SegmentationLogEntry entry : totalEntries) {
			if (entry.getResult().equals("SUCCESS")) {
				double[] probs = MarkovSegmentation.countProbs(entry);
				for (int i = 0; i < 6; i++)
					SD[i].addSample(probs[i]);
			}
	
		}
		
		System.out.println("Build distribution in "+(System.nanoTime()-t0)/1000000+" ms");
		for (Distribution d : SD) {
			d.norm();
			System.out.println(d);
		}
			
		return SD;
	}

	public double probability(double[] lengths) {
		if (lengths.length != distributions.length)
			throw new IllegalArgumentException("MLD ERROR");
		double p = 1;
		for (int i = 0; i < 6; i++)
			p = p * distributions[i].getProbability(lengths[i]);
		return p;
	}
	
	public static double[] parseToDouble(String stringOfDoubles) {
		List<Double> doubles = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(stringOfDoubles, ",");
		while (st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			doubles.add(Double.valueOf(s));
		}
		double[] d = new double[doubles.size()];
		for (int i = 0; i < d.length; i++)
			d[i] = doubles.get(i);
		return d;
	}
	
	
	public static void main(String args[]) throws Exception {
		MarkovLD mld = loadMLD();
		mld.print();
	}

}
