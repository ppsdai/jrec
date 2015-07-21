package ru.recog.feature;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;

public class EllipseFeatureExtractor extends FeatureExtractor {

	public EllipseFeatureExtractor() {
		setDimension(5);	
	}
	
	public List<Double> extract(Mat m) {
		//apparently fitEllipse requires 32f mat
//		m.clone().convertTo(pointsf, CvType.CV_32FC2);
		MatOfPoint2f points = new MatOfPoint2f();
		points.fromList(ImageUtils.mat2PointList(m));
		RotatedRect rect = Imgproc.fitEllipse(points);	
		double minor, major;
/*		if (rect.size.height > rect.size.width) {
			minor = rect.size.width;
			major = rect.size.height;
		} else {
			minor = rect.size.height;
			major = rect.size.width;
		}*/
		return Arrays.asList(rect.center.x, rect.center.y, rect.size.width, rect.size.height, Math.min(rect.angle, 180 - rect.angle) );
//		return Arrays.asList(minor, major, Math.min(rect.angle, 180 - rect.angle) );
/*		double diagonal = Math.sqrt(m.rows()*m.rows()+m.cols()*m.cols());
		return Arrays.asList(minor/diagonal, major/diagonal, rect.angle/ 360); //FIXME find out how angle is returned
*/		
	}

}
