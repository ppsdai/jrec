package ru.recog.imgproc;

import java.io.File;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import java.util.Arrays;

public class ImagesSumm {

	public static void main(String[] args) {
	
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		File dir = new File("c:\\dev\\Summ");
		LabelFrame lf = new LabelFrame("GOOD", true);
	    
		
		String filestr = "1.png";
		String filename = new File(dir, filestr).getAbsolutePath();
		Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);

		System.out.println("File: " + filestr);
		
		lf.addImage(m1, filestr, 5);
		lf.pack();
		lf.setVisible(true);
	}
	
}
