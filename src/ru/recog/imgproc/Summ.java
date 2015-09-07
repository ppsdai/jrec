package ru.recog.imgproc;

import java.io.File;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;

public class Summ {

	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		File dir = new File("c:\\dev\\Summ");
		LabelFrame lf = new LabelFrame("Summ", true);

		//for (String filestr : dir.list()) 
		{
		    String filestr1 = "1.png";
			String filename1 = new File(dir, filestr1).getAbsolutePath();
			Mat m1 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			
			String filestr2 = "2.png";
			String filename2 = new File(dir, filestr2).getAbsolutePath();
			Mat m2 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			
			String filestr3 = "3.png";
			String filename3 = new File(dir, filestr3).getAbsolutePath();
			Mat m3 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			
			String filestr4 = "4.png";
			String filename4 = new File(dir, filestr4).getAbsolutePath();
			Mat m4 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			
			System.out.println("File: " + filestr1 + m1.size());
			System.out.println("File: " + filestr2 + m2.size());
			
			lf.addImage(m1, filestr1, 5);		
			lf.addImage(m2, filestr2, 5);	
			lf.addImage(m3, filestr1, 5);		
			lf.addImage(m4, filestr2, 5);
			
			Mat dst = Mat.zeros(m1.size(), m1.type()) ;
			Mat dst2 = Mat.zeros(m1.size(), m1.type()) ;
			Mat dst3 = Mat.zeros(m1.size(), m1.type()) ;
			
			
			Core.multiply(m2, new Scalar(0.5, 0.5, 0.5, 0.5) , m2);
			Core.multiply(m1, new Scalar(0.5, 0.5, 0.5, 0.5) , m1);
			
			Core.add(m1, m2, dst);
			lf.addImage(dst, filestr2, 5);
			
			Core.multiply(m3, new Scalar(0.5, 0.5, 0.5, 0.5) , m3);
			Core.multiply(m4, new Scalar(0.5, 0.5, 0.5, 0.5) , m4);
			Core.add(m3, m4, dst2);
			lf.addImage(dst2, filestr2, 5);	
			
			Core.multiply(dst, new Scalar(0.5, 0.5, 0.5, 0.5) , dst);
			Core.multiply(dst2, new Scalar(0.5, 0.5, 0.5, 0.5) , dst2);			
	    	Core.add(dst, dst2, dst3);
			
			
			lf.addImage(dst3, filestr2, 5);	
				
		}
	
		lf.pack();
		lf.setVisible(true);
	}
}
