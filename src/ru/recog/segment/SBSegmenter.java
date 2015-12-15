package ru.recog.segment;

import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.imgproc.*;

public class SBSegmenter implements Segmentation {
	
	
	public static final ImageProcessor BIN_OTSU = new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
	
	public static final ImageProcessor BIN_LOCAL06 = new Binarization(0.6, 255, Binarization.BIN_LOCAL_TYPE);

	public static final ImageProcessor BIN_LOCAL08 = new Binarization(0.8, 255, Binarization.BIN_LOCAL_TYPE);

	private ImageProcessor processor;
	
	
	public SBSegmenter(ImageProcessor processor) {
		this.processor = processor;
	}
	
	public SBSegmenter() {
		this(BIN_LOCAL06);
	}
	

	public static void main(String[] args) throws IllegalArgumentException {
		
		
	}

	public SegmentationResult sobelsegment(Mat m) {
//		SegmentationResult segResult = new SegmentationResult();
//		segResult.setOriginalMat(m.clone());
//		verticalCut(segResult);
		
		SegmentationData data = new SegmentationData(m);
		
		Mat b = processor.processImage(m);

		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = data.getUpperBound(); row <= data.getLowerBound(); row++)
				projX[col] += 255 - (int) m.get(row, col)[0];
		
//		segResult.setIntensity(new MatOfInt(projX));

		// Calculate local minimum

//		int minCount = 0; int maxCount = 0;
		List<Integer> localMinimums = new ArrayList<Integer>();
		List<Integer> localMaximums = new ArrayList<Integer>();
		for (int x = 1; x < m.cols()-1; x++) {
			if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
		}
		
		List<Integer> sobelXMinimums = sobelCurve(m, 1, 0);
		List<Integer> sobelYMinimums = sobelCurve(m, 0, 1);

		System.out.println(localMinimums);
		System.out.println(sobelXMinimums);
		System.out.println(sobelYMinimums);

		
		List<Integer> cutPoints = new ArrayList<Integer>();
		for (Integer i : localMinimums)
			if (isMinimum(sobelXMinimums, i) && isMinimum(sobelYMinimums, i))
				cutPoints.add(i);

		
		return new SegmentationResult(data, new CutData(cutPoints));
		
	}
	
	
	public static List<Integer> sobelCurve(Mat m, int dx, int dy) {
		Mat sobel = new Mat(m.size(), m. type());
		Imgproc.Sobel(m.clone(), sobel, CvType.CV_32F, dx, dy);
		
		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = 0; row <= sobel.rows()-1; row++)
				projX[col] += Math.abs(sobel.get(row, col)[0]);
		
		List<Integer> sobelMinimums = new ArrayList<Integer>();
		for (int x = 1; x < m.cols()-1; x++) {
			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) sobelMinimums.add(x);
		}
		
		return sobelMinimums;
		
	}
	
	public static boolean isMinimum(List<Integer> minimums, int point) {
		return minimums.contains(new Integer(point)) 
				|| minimums.contains(new Integer(point+1))
				|| minimums.contains(new Integer(point-1)) ;
	}
	
	
	public ImageProcessor getProcessor() {
		return processor;
	}

	public SegmentationResult segment(Mat m, ImageProcessor processor) {
//		SegmentationResult segResult = new SegmentationResult();
//		segResult.setOriginalMat(m.clone());
//		verticalCut(segResult);
		
		SegmentationData data = new SegmentationData(m);
//		
		Mat b = processor.processImage(m);
//
//		int[] projX = new int[m.cols()];
//		Arrays.fill(projX, 0);
//		for (int col=0; col < m.cols(); col ++)
//			for (int row = .getUpperBound(); row <= segResult.getLowerBound(); row++)
//				projX[col] += 255 - (int) m.get(row, col)[0];
//		
////		segResult.setIntensity(new MatOfInt(projX));
//
//		// Calculate local minimum
//
////		int minCount = 0; int maxCount = 0;
//		List<Integer> localMinimums = new ArrayList<Integer>();
//		List<Integer> localMaximums = new ArrayList<Integer>();
//		for (int x = 1; x < m.cols()-1; x++) {
//			if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
//			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
//		}
		
//		ShapeFilter filter = new ShapeFilter(0.1, 300, 4, 50, 2, 40, 2);
		
		List<BinShape> shapes = uniteShapes(ShapeBasedSegmenter.getFinalShapes(
				b.submat(data.getUpperBound(), data.getLowerBound()+1, 0, b.cols()),
				ShapeFilter.WEAK));
		
//		segResult.shapes = shapes;
		
		
//		List<BinShape> moreshapes = new ArrayList<BinShape>();
//		b = ImageUtils.localbin(m.clone(), 0.4);
//		
//		List<BinShape> thinnerShapes = ShapeBasedSegmenter.getFinalShapes(
//				b.submat(segResult.getUpperBound(), segResult.getLowerBound()+1, 0, b.cols()));
//		
//		for (BinShape shape : thinnerShapes) 
//			if (!covered(shape, shapes)) moreshapes.add(shape);
//		
//		shapes.addAll(moreshapes);
		
		List<Integer> divPoints = new ArrayList<Integer>();

		for (int min : data.getMinimums())
			if (between(shapes, min)) divPoints.add(min);
		
		double lengthEstimate =  0.66 * (data.getLowerBound() - data.getUpperBound());

		ShapeFilter one = new ShapeFilter(ShapeFilter.WEAK);
		one.setWidthMin(3);
		one.setWidthMax((int)Math.round(lengthEstimate*1.2));
		
		ShapeFilter two = new ShapeFilter(ShapeFilter.WEAK);
		two.setWidthMin(one.getWidthMax()+1);
		two.setWidthMax((int)Math.round(lengthEstimate*2.4));
		
		ShapeFilter three = new ShapeFilter(ShapeFilter.WEAK);
		three.setWidthMin(two.getWidthMax()+1);
		three.setWidthMax((int)Math.round(lengthEstimate*3.3));
		
		
		TreeSet<Integer> mins = new TreeSet<Integer>(data.getMinimums());
//		System.out.println(localMinimums);
//		System.out.println(mins);
		
		
		for (BinShape shape : shapes) {
			if (two.accept(shape)) {
//				System.out.println("Adding "+shape);
				int point = (int)Math.round(0.5*(shape.getLRPoint().x + shape.getULPoint().x) );
//				System.out.println("Adding at "+point+" "+shape);
//				System.out.println("before "+divPoints);
//				int floor = mins.floor(point);
//				int higher = mins.higher(point);
//				
//				addCutPoint(divPoints, point-floor<=higher-point? floor : higher);
//				System.out.println(point);
				addCutPoint(divPoints, findCutPoint(mins, point));
//				System.out.println("after "+divPoints);

//				divPoints.add( point );
			} else if (three.accept(shape)) {
				int point1 = (int)Math.round(0.33*(shape.getLRPoint().x - shape.getULPoint().x)+shape.getULPoint().x );
				int point2 = (int)Math.round(0.67*(shape.getLRPoint().x - shape.getULPoint().x)+shape.getULPoint().x );
				addCutPoint(divPoints, findCutPoint(mins, point1));
				addCutPoint(divPoints, findCutPoint(mins, point2));
			}
		}
		
//		segResult.setCutPoints(uniteClosePoints(divPoints));
		
		return new SegmentationResult(data, new CutData(divPoints));
	}
	
	
	private static int findCutPoint(TreeSet<Integer> mins, int point) {
		if (point < mins.first()) return mins.first();
		if (point>= mins.last()) return mins.last();
		int floor = mins.floor(point);
		int higher = mins.higher(point);
		
		return point-floor<=higher-point? floor : higher;
	}
	
	private static void addCutPoint(List<Integer> cutPoints, int cutpoint) {
		for (int i = 0; i < cutPoints.size();i++)
			if (cutpoint < cutPoints.get(i)) {
				cutPoints.add(i, cutpoint);
				break;
			}
	}
	
	private static boolean covered(BinShape shape, List<BinShape> existingShapes) {
		for (BinShape presentShape : existingShapes) {
			Rect r = shape.getBoundingRect();
			Rect pr = presentShape.getBoundingRect();
			if (r.x >= pr.x && r.x<=pr.x+pr.width) return true;
			if (r.x+r.width >= pr.x && r.x+r.width <=pr.x+pr.width) return true;
		}
		return false;
	}
	
	private static List<BinShape> uniteShapes(List<BinShape> shapes) {
		List<BinShape> united = new ArrayList<BinShape>();
		BinShape.sortShapes(shapes);
		
		for (int i = 0; i < shapes.size()-1; i++) {
			BinShape shape = shapes.get(i);
			int count = 0;
			for (int j = i+1; shape.intersects(shapes.get(j)) && j < shapes.size()-1;j++) {
				shape.addShape(shapes.get(j));
				count++;
			}
			united.add(shape);
			i+=count;
		}
		return united;
	}
	
	private static List<Integer> uniteClosePoints(List<Integer> divPoints) {
		List<Integer> finalPoints = new ArrayList<Integer>();
		
		for (int i = 0; i < divPoints.size() - 1; i++) {
			int x1 = divPoints.get(i);
			int x2 = divPoints.get(i+1);
			if (x2 - x1 <= 3) {
				finalPoints.add((x2+x1)/2);
				i++;
			}
			else finalPoints.add(x1);
			
		}
		
		return finalPoints;
	}
	
	private static boolean between(List<BinShape> shapes, int x) {
		for (BinShape shape : shapes)
			if (x <= shape.getLRPoint().x-1 && x >= shape.getULPoint().x+1)
				return false;
		
		return true;
	}

	@Override
	public SegmentationResult segment(Mat m) {
		return segment(m, processor);
	}

	@Override
	public SegmentationResult segment(Mat m,
			double... parameters) {
		// FIXME Auto-generated method stub
		return null;
	}

	@Override
	public SegmentationResult segment(SegmentationData data) {
		// FIXME Auto-generated method stub
		return null;
	}

	@Override
	public SegmentationResult segment(SegmentationData data,
			double... parameters) {
		// FIXME Auto-generated method stub
		return null;
	}


}
