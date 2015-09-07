package ru.recog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import ru.recog.feature.EllipseFeatureExtractor;
import ru.recog.feature.FeatureExtractor;

public class RunClassifier {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private CascadeClassifier itsClassifier;
	boolean newfile = false;
	
	public RunClassifier(CascadeClassifier cl) {
		itsClassifier = cl;
	}

	public static void main(String[] args) {
//		if (args.length<2) {
//			System.err.println("DetectUtil picFolder cascadeFile");
//			System.exit(1);
//		}
//		
//		CascadeClassifier  cl = new CascadeClassifier(args[1]);
//		findAndShowNumbers(args[0],cl);
		
		
//		Mat m = Imgcodecs.imread("/Users/pps/dev/NNTrain/full1020/5/0.bmp",Imgcodecs.IMREAD_GRAYSCALE);
/*		Mat m = Imgcodecs.imread("/Users/pps/dev/fives.bmp",Imgcodecs.IMREAD_GRAYSCALE);

		System.out.println("M: "+m.type()+" depth: "+m.depth()+" size: "+m.size());

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(m, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		System.out.println(contours);
		
		
		
		for (MatOfPoint points : contours) {
//			System.out.println(points.toList());
			
			MatOfPoint2f points2f = new MatOfPoint2f(points.toArray());
//			points2f.fromArray(points.toArray());
			RotatedRect rr = Imgproc.fitEllipse(points2f);
			System.out.println(rr);
		}
		
		Mat m1 = new Mat(m, new Range(0,33));
		Mat m2 = new Mat(m, new Range(34,66));
		Mat m3 = new Mat(m, new Range(67,99));
		
		MatOfPoint2f ar1 = new MatOfPoint2f();
		ar1.fromList(ImageUtils.mat2PointList(m1));
		
		MatOfPoint2f ar2 = new MatOfPoint2f();
		ar2.fromList(ImageUtils.mat2PointList(m2));
		
		MatOfPoint2f ar3 = new MatOfPoint2f();
		ar3.fromList(ImageUtils.mat2PointList(m3));
		
		RotatedRect rr1 = Imgproc.fitEllipse(ar1);
		System.out.println(rr1);
		
		RotatedRect rr2 = Imgproc.fitEllipse(ar2);
		System.out.println(rr2);
		
		RotatedRect rr3 = Imgproc.fitEllipse(ar3);
		System.out.println(rr3);*/
		
		
		
		File dir = new File("/Users/pps/dev/NNTrain/full1020");
		for (int i = 0; i < Utils.FULL_CHARACTERS_SET.size(); i++) {
			File charDir = new File(dir, String.valueOf(i));
			File imgFile = null;
			for (int n = 0; !(imgFile = new File(charDir, String.valueOf(n).concat(".bmp"))).exists() ;n++);
			Mat img = Imgcodecs.imread(imgFile.getAbsolutePath(),Imgcodecs.IMREAD_GRAYSCALE);
			System.out.println(imgFile.getAbsolutePath());
			MatOfPoint2f m2f = new MatOfPoint2f();
			m2f.fromList(ImageUtils.mat2PointList(img));;
			System.out.println(Imgproc.fitEllipse(m2f));
		}
		
		//		Mat otsu = new Mat();
//		double threshold = 0;
//		Mat sobelx = sobel8U(m, 1, 0), sobely = sobel8U(m,0,1);
//		Mat sobel2x = sobel8U(m, 2, 0), sobel2y = sobel8U(m,0,2);
//
//		
//		
//		
////		Imgproc.Sobel(m, sobely, CvType.CV_16S, 0, 1);
////		Imgproc.Sobel(m, sobely, CvType.CV_16S, 0, 1);
//////		System.out.println(sobel.depth());
////		double min = 0, max = 0;
////		
////		MinMaxLocResult r = Core.minMaxLoc(sobel);
//		
////		System.out.println(r.maxVal+" "+r.minVal);
//
////		Mat convSobel = new Mat();
////		Core.convertScaleAbs(sobel, convSobel, 255/(Math.max(r.maxVal,r.minVal)), 0);
//		Mat sobelxy = new Mat();
//		Core.add(sobelx, sobely, sobelxy);
//		
//		Mat sobel2xy = new Mat();
//		Core.add(sobel2x, sobel2y, sobel2xy);
//		
//		Imgproc.threshold(sobelxy, otsu, threshold, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
//		float[] ar = histx(otsu);
//		System.out.println(ar.length);
//
//		System.out.println(Arrays.asList(ar).toString());
//		for (int i = 0; i< ar.length;i++)
//			System.out.println(ar[i]);
//		
//		
		LabelFrame frame = new LabelFrame("OTSU");
//		
//		frame.addImage(m,"original");
//		frame.addImage(sobelx, "sobel x");
//		frame.addImage(sobely, "sobel y");
//		frame.addImage(sobelxy, "sobel x+y");
//		frame.addImage(sobel2x, "sobel 2 x");
//		frame.addImage(sobel2y, "sobel 2 y");
//		frame.addImage(sobel2xy, "sobel 2 x+y");
//		frame.addImage(otsu, "otsu");
		
		
		frame.pack();
		frame.setVisible(true);

	}
	
	
	
	private static Mat sobel8U(Mat m, int dx, int dy) {
		Mat s = new Mat();
		Imgproc.Sobel(m, s, CvType.CV_16S, dx, dy);
		
		MinMaxLocResult r = Core.minMaxLoc(s);
		
		System.out.println(r.maxVal+" "+r.minVal);

		Mat convSobel = new Mat();
		Core.convertScaleAbs(s, convSobel, 255/(Math.max(r.maxVal,r.minVal)), 0);
		return convSobel;
	}
	
	
	public static float[] histx(Mat m) {
		float[] sums = new float[m.cols()];
//		System.out.println(sums);
//		System.out.println(m.size()+" "+m.rows()+" "+m.cols());
		for (int i=0; i< m.cols();i++)
			for (int j=0; j<m.rows();j++) {
//				System.out.println(i+" "+j+" "+ m.get(j, i));
				sums[i]+=m.get(j, i)[0];
			}
		return sums;
	}
	
    public static void findAndShowNumbers(String dirName, CascadeClassifier classifier) {
		File cardir = new File(dirName); //"/Users/pps/dev/cars"
		System.out.println(dirName+" "+cardir.isDirectory());
		String[] carlist = cardir.list();
		for (int i = 0; i< carlist.length; i++)
			System.out.println(carlist[i]);
		File[] carfiles = cardir.listFiles();
		for (int i = 0; i< carfiles.length; i++)
			detectNumber(carfiles[i].getAbsolutePath(), classifier);
    }
    
    
	public static void detectNumber(String imageFileName, CascadeClassifier classifier) {
		System.out.println("Detecting in "+imageFileName+" FD FT size: "+classifier.getOriginalWindowSize().toString());

	    // Create a face detector from the cascade file in the resources
	    // directory.
	    

	    Mat image = Imgcodecs.imread(imageFileName);

	    // Detect faces in the image.
	    // MatOfRect is a special container class for Rect.
//	    Mat doubled = new Mat();
//	    Imgproc.resize(image, doubled, new Size(), 2.0, 2.0, Imgproc.INTER_LINEAR);
//	    Mat doubled = Imgproc.resize(image, doubled, dsize, fx, fy, interpolation);
	    MatOfRect faceDetections = new MatOfRect();
	    classifier.detectMultiScale(image, faceDetections);
//	    classifier.detectMultiScale(image, faceDetections,1.05,0,0, new Size(3,4), new Size(48,60));

	    System.out.println(String.format("Detected %s numbers", faceDetections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : faceDetections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		    Imgcodecs.imwrite(imageFileName, image);

	    }
	    
//	    String filename = "/Users/pps/dev/number2.png";
//	    if (faceDetections.toArray().length > 0 ) {
//	    	String newName = imageFileName.replaceAll(".bmp", "_.bmp");
//		    System.out.println(String.format("Writing %s", imageFileName));
//		    Imgcodecs.imwrite(newName, doubled);
//	    }

	}

}
