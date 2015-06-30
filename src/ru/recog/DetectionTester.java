package ru.recog;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

public class DetectionTester {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	

	public static void main(String[] args) throws Exception {
		
		DetectionTester dt = new DetectionTester();
		dt.testSimpleDigit("", "/Volumes/Pavel Perfilov’s Public Folder/Good.dat", "", "");
		
		// TODO Auto-generated method stub

	}
	
	public void testSimpleDigit(String digit, String descriptionFile, String picFolder, String cascadeFile) throws IOException, FileNotFoundException {
		File dir = new File(picFolder);
//		CascadeClassifier classifier = new CascadeClassifier(cascadeFile);
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(descriptionFile)));
		String line;
		do {
			line = lnr.readLine();
			StringTokenizer st = new StringTokenizer(line, " ");

			System.out.println(line+" "+st.countTokens());
		} while (line != null);
		
		
		
	}
	
	public boolean areRectanglesEqual(Mat frame, Rect detected, Rect reference) {
		
		Rectangle detR = new Rectangle(detected.x, detected.y, detected.width, detected.height);
		Rectangle refR = new Rectangle(reference.x, reference.y, reference.width, reference.height);
		Rectangle intersection = detR.intersection(refR);
		if (intersection.isEmpty()) return false;
		// FIXME better way of asserting if rectangles are close enough
		return (intersection.height*intersection.width >= 0.5*refR.height*refR.width); 
	}

}
