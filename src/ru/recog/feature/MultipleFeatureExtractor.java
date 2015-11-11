package ru.recog.feature;

import java.util.*;

import javax.xml.bind.annotation.*;

import org.opencv.core.Mat;

@XmlRootElement (name = "MultipleFEX")
public class MultipleFeatureExtractor<T> extends FeatureExtractor<T> {
	
	List<FeatureExtractor<T>> featureList = new ArrayList<>();

	
	public MultipleFeatureExtractor() {}
	
	public MultipleFeatureExtractor(List<FeatureExtractor<T>> list) {
		featureList = list;
	}
	
	public MultipleFeatureExtractor(FeatureExtractor<T>... extractors) {
		this(Arrays.asList(extractors));
//		featureList = list;
	}

	@Override
	public List<Double> extract(T t) {
		List<Double> vals = new ArrayList<Double>();
		for (FeatureExtractor<T> fe : featureList) 
			vals.addAll(fe.extract(t));
		return vals;
	}
	
	@XmlElementRef
	public List<FeatureExtractor<T>> getFeatureExtractors() {
		return featureList;
	}
	
	public void addExtractor(FeatureExtractor<T> fe) {
		featureList.add(fe);
	}
	
	public int getDimension() {
		int dim = 0;
		for (FeatureExtractor<T> fe : featureList) 
			dim = dim + fe.getDimension();
		return dim;
	}
	
//	@XmlTransient
//	public void setDimension(int dimension) {
////		throw new IllegalArgumentException("Cannot set dimension on MultipleFeatureExtractor");
//		setDimension(getDimension());
//	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MultipleFeatures(");
		sb.append(getDimension()).append(")[");
		for (FeatureExtractor fe : featureList) 
			sb.append(fe.toString()).append(", ");
		
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);

		sb.append("]");

		return sb.toString();
	}

}
