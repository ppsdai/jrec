package ru.recog;

import java.awt.Point;
import java.io.File;
import java.util.*;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.imgproc.SegmentationResult;
import ru.recog.imgproc.Segmenter;

public class TemplateRunner {
	
	LPTemplate big;
	
	public void buildTemplate() {
		List<LPTemplate.Square> squares = new ArrayList<LPTemplate.Square>();
		squares.add(LPTemplate.sqr(10,11,24,31));
		squares.add(LPTemplate.sqr(29,4,45,31));
		squares.add(LPTemplate.sqr(50,4,66,31));
		squares.add(LPTemplate.sqr(70,4,86,31));
		squares.add(LPTemplate.sqr(91,11,106,31));
		squares.add(LPTemplate.sqr(110,11,126,31));
		big = new LPTemplate(131, 37, squares);
//		big.cutTop(4);
//		big.cutBottom(6);
	}

	public static void main(String[] args) {
		doit();
//		TemplateRunner tr = new TemplateRunner();
//		tr.buildTemplate();
//		Mat m = Imgcodecs.imread("/Users/pps/dev/TEMPLATE/check2.bmp", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//		System.out.println("m.size "+m.size());
//		SegmentationResult sr = Segmenter.segment(m);
//		Mat m1 = m.submat(sr.getUpperBound(), sr.getLowerBound()+1, 0, m.cols()).clone();
//		tr.big.printTemplate();
//		tr.big.scaleToHeight(m1.rows()-1);
//		tr.big.printTemplate();
//
//		
//		System.out.println("m1.size "+m1.size());
//
//		long t1 = System.currentTimeMillis();
//		Map<Point, Double> map = tr.big.scan(m1, tr.big);
//		System.out.println(map);
//		long t2 = System.currentTimeMillis();
//
//		List<Map.Entry<Point, Double>> list = new ArrayList<Map.Entry<Point,Double>>();
//		for (Map.Entry<Point, Double> entry : map.entrySet()) {
//			list.add(entry);
//		}
//		Collections.sort(list, new Comparator<Map.Entry<Point,Double>>() {
//			public int compare(Map.Entry<Point,Double> o1, Map.Entry<Point,Double> o2) {
//				return o1.getValue().compareTo(o2.getValue());
//			};
//		});
//		
//		for (Map.Entry<Point, Double> entry : list)
//			System.out.println(entry.getValue()+" at "+entry.getKey().x+" "+entry.getKey().y);
//		
//		Point p = list.get(list.size()-1).getKey();
//		drawTemplateAtPoint(tr.big, p, m1);
//		
//		LabelFrame lf = new LabelFrame("hi");
//		lf.addImage(m, "haha");
//		lf.addImage(m1, "cut");
//
//		lf.pack();
//		lf.setVisible(true);
//
//		
//		System.out.println("size: "+map.size());
//		System.out.println("time: "+(t2-t1));
		
		
	}
	
	public static void doit() {
		List<File> files = Utils.getOrderedList("/Users/pps/dev/TEMPLATE");
		LabelFrame lf = new LabelFrame("Hola!");
//		lf.pack();
//		lf.setVisible(true);
		
		for (File f : files) {
			Mat m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			TemplateRunner tr = new TemplateRunner();
			tr.buildTemplate();
			tr.big.printTemplate();
			Point p = getTemplatePoint(m, tr.big);
			drawTemplateAtPoint(tr.big, p, m);
			lf.addImage(m, f.getName(), 3);

		}
		
//		drawTemplateAtPoint(tr.big, p, m1);
//		
//		LabelFrame lf = new LabelFrame("hi");
//		lf.addImage(m, "haha");
//		lf.addImage(m1, "cut");

		lf.pack();
		lf.setVisible(true);
	}
	
	
	public static Point getTemplatePoint(Mat m, LPTemplate template) {
		SegmentationResult sr = Segmenter.segment(m);
//		Mat m1 = m.submat(sr.getUpperBound(), sr.getLowerBound()+1, 0, m.cols()).clone();
		Mat m1 = m.clone();
//		tr.big.printTemplate();
//		template.scaleToHeight(m1.rows()-1);
//		tr.big.printTemplate();

		
		System.out.println("m1.size "+m1.size());

		long t1 = System.currentTimeMillis();
//		Map<Point, Double> map = template.scan(m1, template);
		Map<Point, Double> map = template.betterscan(m1);//, template);

		System.out.println(map);
		long t2 = System.currentTimeMillis();

		List<Map.Entry<Point, Double>> list = new ArrayList<Map.Entry<Point,Double>>();
//		for (Map.Entry<Point, Double> entry : map.entrySet()) {
//			list.add(entry);
//		}
		list.addAll(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Point,Double>>() {
			public int compare(Map.Entry<Point,Double> o1, Map.Entry<Point,Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			};
		});
		
//		for (Map.Entry<Point, Double> entry : list)
//			System.out.println(entry.getValue()+" at "+entry.getKey().x+" "+entry.getKey().y);
		Point p = list.get(list.size()-1).getKey();
		p.y = p.y + sr.getUpperBound();
		return p;
		
//		return null;
	}
	
	public static void drawTemplateAtPoint(LPTemplate template, Point p, Mat m) {
        Imgproc.rectangle(m, new org.opencv.core.Point(p.x, p.y), 
        			new org.opencv.core.Point(p.x + template.width, p.y + template.height), new Scalar(0, 255, 0));
        
        for (LPTemplate.Square sq : template.squares)
        	Imgproc.rectangle(m, new org.opencv.core.Point(p.x+sq.point1.x, p.y+sq.point1.y), 
        			new org.opencv.core.Point(p.x + sq.point2.x, p.y + sq.point2.y), new Scalar(0, 255, 0));
        	

	}

}
