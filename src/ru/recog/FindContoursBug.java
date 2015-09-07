package ru.recog;

import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class FindContoursBug {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	// Two files differ in one pixel, one dies, another works
	public static void main(String[] args) {

		
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
// Works with 45Bin 			
			//Mat b = Imgcodecs.imread("c:\\dev\\PlatesSegmentation\\45Bin.bmp", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
// DIES with 46Bin
			Mat b = Imgcodecs.imread("c:\\dev\\PlatesSegmentation\\46Bin.bmp", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Imgproc.findContours(b.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			
			System.out.println("Total contours: "+contours.size());


	}
	
	
}
