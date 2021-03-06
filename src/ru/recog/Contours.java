package ru.recog;

import java.awt.Dimension;
import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.imgproc.*;
//import ru.recog.segment.SBSegmenter;
import ru.recog.segment.*;

public class Contours {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


	public static Comparator<MatOfPoint> RECT_COMPARATOR = new Comparator<MatOfPoint>() {
		@Override
		public int compare(MatOfPoint o1, MatOfPoint o2) {
			Rect r1 = ImageUtils.getContourRect(o1);
			Rect r2 = ImageUtils.getContourRect(o2);
			return Integer.compare(r1.x, r2.x);
		}
	};
	

	public static void darkside(String source) {
		List<File> files = Utils.getOrderedList(source);
		LabelFrame lf = new LabelFrame("Hola!");
		lf.setPreferredSize(new Dimension(800,600));
		lf.setSize(800,600);
		lf.setVisible(true);
		
		
		int count =0;
		SBSegmenter sbs = new SBSegmenter();
		for (File f : files) {
			count++;
			if (count > 100) break;
			
			Mat m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m);
			SegmentationResult lsr = SegmentationFactory.getLegacySegmentation().segment(m);
			lf.addImage(ImageUtils.drawSegLines(m, lsr), "legacy", 3);
			
			for (int i = 0; i < sr.getPossibleCuts().size() && i < 15; i++)
//			for (CutData cut : sr.getPossibleCuts())
//				lf.addImage(ImageUtils.drawSegLines(m, cut), cut.toString(), 3);
				lf.addImage(ImageUtils.drawSegLines(m, sr.getPossibleCuts().get(i)),"i="+i,3);
			
////			sbs.sobelsegment(m);
//			
////			Mat b6 = ImageUtils.localbin(m, 0.6);
////			List<BinShape> shapes = ShapeBasedSegmenter.getAllShapes(b6); //FinalShapes(b6);
////			Mat c6 = ImageUtils.bin2color(b6);
////			for (BinShape shape : shapes) {
////				Imgproc.rectangle(c6, shape.getULPoint(), shape.getLRPoint(), new Scalar(0,255,0));
////			}
////			lf.addImage(c6, f.getName(), 3);
//			
//			
////			Mat b4 = ImageUtils.localbin(m, 0.4);
//////			Mat b4 = SBSegmenter.BIN_OTSU.processImage(m);
////			
////			shapes = ShapeBasedSegmenter.getFinalShapes(b4);
////			Mat c4 = ImageUtils.bin2color(b4);
////			for (BinShape shape : shapes) {
////				Imgproc.rectangle(c4, shape.getULPoint(), shape.getLRPoint(), new Scalar(0,255,0));
////			}
////			lf.addImage(c4, f.getName(), 3);
//			
//			SegmentationResult sr0 = SegmentationFactory.getLegacySegmentation().segment(m);
//			lf.addImage(ImageUtils.drawSegLines(m, sr0), "orig", 3);
//			
//    		SegmentationResult sr1 = sbs.sobelsegment(m);
////			SegmentationResult sr1 = SBSegmenter.segment(m, SBSegmenter.BIN_OTSU);
//
//			lf.addImage(ImageUtils.drawSegLines(m, sr1), "shm", 3);
//			
//			Mat sobelx = new Mat(m.size(), m. type());
//			Imgproc.Sobel(m.clone(), sobelx, CvType.CV_32F, 1, 0);
//			Mat mx = new Mat();
//			sobelx.convertTo(mx, CvType.CV_8U);
//			lf.addImage(mx, "x",3);
//			
//			
//			Mat sobely = new Mat(m.size(), m. type());
//			Imgproc.Sobel(m.clone(), sobely, CvType.CV_32F, 0, 1);
//			Mat my = new Mat();
//			sobely.convertTo(my, CvType.CV_8U);
//			lf.addImage(my, "y",3);
//			
//			Mat sobelxy = new Mat(m.size(), m. type());
//			Imgproc.Sobel(m.clone(), sobelxy, CvType.CV_32F, 1, 2);
//			Mat mxy = new Mat();
//			sobely.convertTo(mxy, CvType.CV_8U);
//			lf.addImage(mxy, "1x2y",3);
//			
////			Mat sobely = new Mat(m.size(), m. type());
////			Imgproc.Sobel(m.clone(), sobely, CvType.CV_32F, 0, 1);
////			lf.addImage(sobely, "y",3);
		}
		
	}
	
	public static void main(String[] args) {
		
		darkside(args[0]);
		/*
		//load image
		Mat m = Imgcodecs.imread("/Users/pps/dev/detected/frame100001.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		// binarization 
		Mat otsu = new Mat(m.size(), m.type());
		Imgproc.threshold(m, otsu, 0, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY_INV);
		//find countours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(otsu.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		//output
		System.out.println(contours);
		Size s = new Size(); Point p = new Point();
		for (MatOfPoint mop : contours) {
			mop.locateROI(s, p);
			System.out.println(p+" size: "+s);
		}
		
		Mat c = Mat.zeros(m.size(), m.type());
		
		Imgproc.drawContours(c, contours, -1, new Scalar(255,255,255));
		
		LabelFrame lf = new LabelFrame("LF", true);
		lf.addImage(m, "", 5);
		lf.addImage(otsu, "otsu", 5);
		lf.addImage(c, "c", 5);
		
		lf.pack();
		lf.setVisible(true);
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		*/
		
//		MultiLayerPerceptron neuroph = (MultiLayerPerceptron)NeuralNetwork.createFromFile("/Users/pps/dev/TheBest60.nnet");
//		
//		
//		System.out.println(neuroph);
//		System.out.println(neuroph.getNetworkType());
//		System.out.println(neuroph.getLearningRule());
//		for (Layer layer : neuroph.getLayers())
//			System.out.println(layer.getNeuronsCount());
		
//		List<FeatureExtractor> fexlist = new ArrayList<FeatureExtractor>();
//		fexlist.add(new AreaFeatureExtractor());
////		fexlist.add(new EllipseFeatureExtractor());
//		fexlist.add(new XProjectionFeatureExtractor());
//		fexlist.add(new YProjectionFeatureExtractor());
//		fexlist.add(new SymmetryFeatureExtractor());
//		MultipleFeatureExtractor mfx = new MultipleFeatureExtractor(fexlist);
		
		
//		CompoundImageProcessor cmp = new CompoundImageProcessor();
//		cmp.addImageProcessor(new Resizer(new Size(10,20)));
//		cmp.addImageProcessor(new Binarization(1, 255, Imgproc.THRESH_BINARY));
//		Resizer resizer = new Resizer(new Size(10,20));
/*		NNWrapper nnw = new NNWrapper("/Users/pps/dev/NNTrain/goodshit/GoodNet375021.nnet");
		
//		System.out.println(mfx);
		
		LabelFrame lf = new LabelFrame("basic", false);
		LabelFrame rf = new LabelFrame("frames", false);
		LabelFrame cf = new LabelFrame("chars", true);
		LabelFrame bf = new LabelFrame("binchars", true);

		
		
		Mat m = Imgcodecs.imread("/Users/pps/dev/detected/frame87601.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat m1 = Imgcodecs.imread("/Users/pps/dev/detected/frame87601.png", Imgcodecs.CV_LOAD_IMAGE_COLOR);
//		Mat m = Imgcodecs.imread("c:\\dev\\PlatesSegmentation\\32.bmp", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//		Mat m1 = Imgcodecs.imread("c:\\dev\\PlatesSegmentation\\32.bmp", Imgcodecs.CV_LOAD_IMAGE_COLOR);

		lf.addImage(m, "orig", 3);
		
		for (double k = 0.2; k < 1.6; k+=0.2) {
			Mat b = ImageUtils.localbin(m, k);
//			Mat bb = new Mat(m.size(), CvType.CV_8UC1);
			Mat bb = m1.clone();
//			b.copyTo(bb);
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			List<MatOfPoint> goodContours = new ArrayList<MatOfPoint>();

			
			Imgproc.findContours(b.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			for (MatOfPoint mop : contours)
				if (isContourGood(mop)) goodContours.add(mop);
			Collections.sort(goodContours, new Comparator<MatOfPoint>() {
				@Override
				public int compare(MatOfPoint o1, MatOfPoint o2) {
					Rect r1 = getContourRect(o1);
					Rect r2 = getContourRect(o2);
					return Integer.compare(r1.x, r2.x);
				}
			});
			System.out.println("Total contours: "+contours.size()+" good: "+goodContours.size());

			for (MatOfPoint mop: goodContours) {
				Rect r = getContourRect(mop);
				System.out.println(r);
				Imgproc.rectangle(bb, r.tl(), r.br(), new Scalar(0,255,0));
				Mat cm = b.clone().submat(r.y,r.y+r.height+1,r.x, r.x+r.width+1);
//				Mat rm = cmp.processImage(cm);
				Mat rm = cm.clone();

//				List<Double> nnoutput = nnw.getNNOutput(rm);
//				Mat cm = b.clone().submat(r);
				cf.addImage(cm, r.toString(), 8);
				//bf.addImage(rm, NNAnalysis.convertNNOutputToString(nnw.getNNOutputArray(rm)), 3);
			}
		
			rf.addImage(bb, String.valueOf(k), 3);

			
			lf.addImage(b, String.valueOf(k), 3);
//			break;
		}
		
		lf.pack();
		lf.setVisible(true);
		
		rf.pack();
		rf.setVisible(true);
		
		cf.pack();
		cf.setVisible(true);
		
		bf.pack();
		bf.setVisible(true);


*/

	}
	
//	public static boolean isContourGood(MatOfPoint mop) {
//		Rect r = getContourRect(mop);
//		return r.height>=6 && r.height<=25 && r.width>=4 && r.width<=15;
//		
//	}

}
