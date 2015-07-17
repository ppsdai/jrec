package ru.recog.feature;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

public class EllipseFeatureExtractor extends FeatureExtractor {

	public EllipseFeatureExtractor() {
		setDimension(3);	
	}
	
	public List<Double> extract(Mat m) {
		//apparently fitEllipse requires 32f mat
		Mat pointsf = new Mat();
		m.clone().convertTo(pointsf, CvType.CV_32FC1);
		RotatedRect rect = Imgproc.fitEllipse(new MatOfPoint2f(pointsf));	
		double minor, major;
		if (rect.size.height > rect.size.width) {
			minor = rect.size.width;
			major = rect.size.height;
		} else {
			minor = rect.size.height;
			major = rect.size.width;
		}
		double diagonal = Math.sqrt(m.rows()*m.rows()+m.cols()*m.cols());
		return Arrays.asList(minor/diagonal, major/diagonal, rect.angle/ 360); //FIXME find out how angle is returned
		
	}

}
