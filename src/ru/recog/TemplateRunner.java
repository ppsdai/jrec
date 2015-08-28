package ru.recog;

import java.awt.Point;
import java.util.*;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
	}

	public static void main(String[] args) {
		TemplateRunner tr = new TemplateRunner();
		tr.buildTemplate();
		Mat m = Imgcodecs.imread("/Users/pps/dev/TEMPLATE/baa.bmp"/*, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE*/);
		System.out.println("m.size "+m.size());
		long t1 = System.currentTimeMillis();
		Map<Point, Double> map = tr.big.scan(m, tr.big);
		long t2 = System.currentTimeMillis();

		List<Map.Entry<Point, Double>> list = new ArrayList<Map.Entry<Point,Double>>();
		for (Map.Entry<Point, Double> entry : map.entrySet()) {
			list.add(entry);
		}
		Collections.sort(list, new Comparator<Map.Entry<Point,Double>>() {
			public int compare(Map.Entry<Point,Double> o1, Map.Entry<Point,Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			};
		});
		
		for (Map.Entry<Point, Double> entry : list)
			System.out.println(entry.getValue()+" at "+entry.getKey().x+" "+entry.getKey().y);
		
		Point p = list.get(list.size()-1).getKey();
		drawTemplateAtPoint(tr.big, p, m);
//		drawTemplateAtPoint(tr.big, new Point(15,20), m);
		
		LabelFrame lf = new LabelFrame("hi");
		lf.addImage(m, "haha");
		lf.pack();
		lf.setVisible(true);

		
		System.out.println("size: "+map.size());
		System.out.println("time: "+(t2-t1));
		
		
	}
	
	public static void drawTemplateAtPoint(LPTemplate template, Point p, Mat m) {
        Imgproc.rectangle(m, new org.opencv.core.Point(p.x, p.y), 
        			new org.opencv.core.Point(p.x + template.width, p.y + template.height), new Scalar(0, 255, 0));
        
        for (LPTemplate.Square sq : template.squares)
        	Imgproc.rectangle(m, new org.opencv.core.Point(p.x+sq.point1.x, p.y+sq.point1.y), 
        			new org.opencv.core.Point(p.x + sq.point2.x, p.y + sq.point2.y), new Scalar(0, 255, 0));
        	

	}

}
