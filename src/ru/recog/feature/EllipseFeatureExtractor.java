package ru.recog.feature;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;

public class EllipseFeatureExtractor extends FeatureExtractor<Mat> {

	public EllipseFeatureExtractor() {
		setDimension(5);	
	}
	
	public List<Double> extract(Mat m) {
		MatOfPoint2f points = new MatOfPoint2f();
		points.fromList(ImageUtils.mat2PointList(m));
		RotatedRect rect = Imgproc.fitEllipse(points);	

		//TODO make sure that rectangle sizes are not switched, otherwise we will get values > 1
		return Arrays.asList(rect.center.x/m.cols(), rect.center.y/m.rows(), rect.size.width/m.cols(), 
				rect.size.height/m.rows(), Math.min(rect.angle, 180 - rect.angle)/90 );
	}
	
	@Override
	public String toString() {
		return "Ellipse(5)";
	}

}
