package ru.recog.feature;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.opencv.core.Mat;

import ru.recog.ImageUtils;


@XmlRootElement(name = "AreaFEX")
public class AreaFeatureExtractor extends FeatureExtractor {

	
	public AreaFeatureExtractor() {
		setDimension(1);	
	}
	
	@Override
	public List<Double> extract(Mat m) {
		return Collections.singletonList( ImageUtils.binaryAreaNorm(m));
	}
	
	@Override
	public String toString() {
		return "Area(1)";
	}

}
