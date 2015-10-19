package ru.recog;

import java.awt.Point;
import java.util.*;

import org.opencv.core.*;

public class LPTemplate {
	
	/*
	 * 131,37
10,11 24,31
29,4 45,31
50,4 66,31
70,4 86,31
91,11 106,31
110,11 126,31
	 */
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	int width, height;
	
	List<Square> squares;
	
	private double alpha = 0;
	
	public LPTemplate(int width, int height, List<Square> squares) {
		this.width = width;
		this.height = height;
		this.squares = squares;
		calcAlpha();
	}
	
	private void calcAlpha() {
		double S = width * height;
		double B = 0;
		for (Square sq : squares)
			B = B + (sq.point2.x-sq.point1.x+1)*(sq.point2.y-sq.point1.y+1);
		alpha = S/B - 1;
		System.out.println("alpha "+alpha);
	}
	
	public Map<Point, Double> scan(Mat m, LPTemplate lp) {
		Map<Point,Double> map = new HashMap<Point,Double>();
		for (int i = 0; i + lp.height <= m.rows(); i++)
			for (int j = 0; j + lp.width <= m.cols(); j++) {
//				System.out.println("row "+i+" col "+j);
				map.put(new Point(j,i), lp.calculate(m.submat(i, i +lp.height, j, j + lp.width) ) );
			}
		return map;
	}
	
	public void printTemplate() {
		System.out.println("["+width+"x"+height+"]");
		for (Square sq : squares) 
			System.out.println(sq.point1+" "+sq.point2);
	}
	
	public void scaleToHeight(int height) {
		float ratio = (float) height / this.height;
		System.out.println("ratio: "+ratio);
		width = (int) Math.round(width * ratio);
		this.height = height;
		for (Square sq : squares) {
			sq.point1.x = (int) Math.round(sq.point1.x * ratio);
			sq.point1.y = (int) Math.round(sq.point1.y * ratio);
			sq.point2.x = (int) Math.round(sq.point2.x * ratio);
			sq.point2.y = (int) Math.round(sq.point2.y * ratio);
		}
		calcAlpha();
		
	}
	
	public void cutTop(int cut) {
		for (Square sq : squares) {
			sq.point1.y = sq.point1.y - cut;
			sq.point2.y = sq.point2.y - cut;
		}
		height = height - cut;
		calcAlpha();
	}
	
	public void cutBottom(int cut) {
		height = height - cut;
		calcAlpha();
	}
	
	public void cutLeft(int cut) {
		for (Square sq : squares) {
			sq.point1.x = sq.point1.x - cut;
			sq.point2.x = sq.point2.x - cut;
		}
		width = width - cut;
		calcAlpha();
	}
	
	public void cutRight(int cut) {
		width = width - cut;
		calcAlpha();
	}
	
	public Map<Point, Double> betterscan(Mat m) {
		//build sum matrix for m
		Mat sm = Utils.produceSumMat(m);

		// 
		Map<Point,Double> map = new HashMap<Point,Double>();
		for (int i = 0; i + height <= m.rows(); i++)
			for (int j = 0; j + width <= m.cols(); j++) {
				System.out.println("row "+i+" col "+j);
				map.put(new Point(j,i), bettercalc(sm, i, j ) );
			}
		return map;
	}
	
	public double bettercalc(Mat sm, int row, int col) {
		
//		if (m.cols() < width || m.rows() < height) throw new IllegalArgumentException("Mat is too small"); //TODO more informative
		
//		Mat sm = Utils.produceSumMat(m);
//		System.out.println(sm.dump());
		
//		double totalI = sm.get(sm.rows()-1, sm.cols()-1)[0];
		double totalI = pointSum(sm, row+height-1, col+width-1) + pointSum(sm, row-1, col-1) 
				- pointSum(sm, row+height-1, col-1) - pointSum(sm, row-1, col+width-1);
		System.out.println("total "+ totalI);
		double bI = 0;
		for (Square sq : squares) 
			bI = bI + pointSum(sm, sq.point2.y, sq.point2.x) + pointSum(sm, sq.point1.y-1, sq.point1.x-1) 
				- pointSum(sm, sq.point2.y, sq.point1.x-1) - pointSum(sm, sq.point1.y-1, sq.point2.x);
		
		/*
		 * Sab = Sb - Sc - Sd + Sa
		 * Sb = S(b)
		 * 
		 * 
		 */
		
		System.out.println("blackI "+ bI);
		System.out.println("right "+ (totalI - bI*(1+alpha)) );


		
		
		return totalI - bI*(1+alpha);
		
	}
	
	public static void main(String args[]) {
		
		LPTemplate sq2In44 = new LPTemplate(4,4, Arrays.asList(sqr(1,1,2,2)));
		
		Mat m5x5_allones = Mat.ones(5, 5, CvType.CV_8UC1);
		Map<Point, Double> results = sq2In44.scan(m5x5_allones, sq2In44);
		System.out.println(results);
		
		Mat m5x5 = m5x5_allones.clone();
		m5x5.put(1, 1, 0);
		m5x5.put(1, 2, 0);
		m5x5.put(2, 1, 0);
		m5x5.put(2, 2, 0);
		
		results = sq2In44.scan(m5x5, sq2In44);
		System.out.println(results);
		results = sq2In44.betterscan(m5x5);
		System.out.println(results);
		
		Mat m6x6 = Mat.ones(6, 6, CvType.CV_8UC1);
		m6x6.put(2, 3, 0);
		m6x6.put(2, 4, 0);
		m6x6.put(3, 3, 0);
		m6x6.put(3, 4, 0);
		results = sq2In44.scan(m6x6, sq2In44);
		System.out.println(results);
		results = sq2In44.betterscan(m6x6);
		System.out.println(results);

		
	}
	
	
	public double calculate(Mat m) {
//		System.out.println(m.dump());
		if (m.cols() < width || m.rows() < height) throw new IllegalArgumentException("Mat is too small"); //TODO more informative
		
		Mat sm = Utils.produceSumMat(m);
//		System.out.println(sm.dump());
		
		double totalI = sm.get(sm.rows()-1, sm.cols()-1)[0];
//		System.out.println("total "+ totalI);
		double bI = 0;
		for (Square sq : squares) 
			bI = bI + pointSum(sm, sq.point2.y, sq.point2.x) 
			+ pointSum(sm, sq.point1.y-1, sq.point1.x-1) 
				- pointSum(sm, sq.point2.y, sq.point1.x-1) 
				- pointSum(sm, sq.point1.y-1, sq.point2.x);
		
		/*
		 * Sab = Sb - Sc - Sd + Sa
		 * Sb = S(b)
		 * 
		 * 
		 */
		
//		System.out.println("blackI "+ bI);
//		System.out.println("right "+ (totalI - bI*(1+alpha)) );


		
		
		return totalI - bI*(1+alpha);
	}
	
	
	public static double pointSum(Mat sum, Point p) {
		return pointSum(sum, p.y, p.x);
	}
	
	public static double pointSum(Mat sum, int row, int col) {
		System.out.println(sum.size());
		System.out.println(row + " "+ col);
		if (row < 0 || col < 0) return 0;
//		else if (row < 0) return sum.get(0, col)[0];
//		else if (col < 0) return sum.get(row, 0)[0];
		else return sum.get(row, col)[0];
	}
	
	public static Square sqr(int x1, int y1, int x2, int y2) {
		return new Square(new Point(x1,y1), new Point(x2,y2));
	}
	
	public static class Square {
		public Point point1, point2;
		
		public Square(Point point1, Point point2) {
			//FIXME always should be point2 > point1 
			this.point1 = point1;
			this.point2 = point2;
		}
	}

}
