package ru.recog.imgproc;

import java.util.*;

import org.opencv.core.Point;
import org.opencv.core.Rect;


/**
 *  
         Object that contains essential properties of shapes.
         At the start it is only a rect, but some other features can be added if needed
 *
 * @version      
         1.00 19 September 2015  * @author          
         Alexey Denisov  */

public class BinShape {

	private Point upperLeft, lowerRight;

	private int nPoints;
	
	public BinShape(){
		nPoints = 0;
		upperLeft = new Point(1000, 1000);
		lowerRight = new Point(0, 0);
	}
	
	public Point getULPoint(){
		return upperLeft;
	}
	
	public Point getLRPoint(){
		return lowerRight;
	}
	
	public Rect getBoundingRect(){
		
		Rect boundingRect = new Rect((int) upperLeft.x, (int) upperLeft.y, (int) (lowerRight.x - upperLeft.x+ 1), (int) (lowerRight.y - upperLeft.y  + 1));
		return boundingRect;
	}
	
	public int getNPoints(){
		
		return nPoints;
	}
	
	public void addPoint(int x, int y){
		nPoints++;
		this.changeBoundingRect(x, y);
	}
	
	public void addPoint(Point p) {
		addPoint((int)p.x, (int)p.y);
	}
	
	public void addShape(BinShape shape) {
		addPoint(shape.getULPoint());
		addPoint(shape.getLRPoint());
		nPoints+=shape.getNPoints();
	}
	
	public boolean intersects(BinShape shape) {
		Rect r = shape.getBoundingRect();
		return r.width > 0 && r.height > 0 && getBoundingRect().width > 0 && getBoundingRect().height > 0
			       && r.x <= lowerRight.x && r.x + r.width >= upperLeft.x
			       && r.y <= lowerRight.y && r.y + r.height >= upperLeft.y;
			       
//			       return r.width > 0 && r.height > 0 && width > 0 && height > 0
//			    		   546:       && r.x < x + width && r.x + r.width > x
//			    		   547:       && r.y < y + height && r.y + r.height > y;	       
			       
	}
		
	private void changeBoundingRect(int x, int y){
		
		if (x < upperLeft.x) upperLeft.x = x;
		if (y < upperLeft.y) upperLeft.y = y;
		if (x > lowerRight.x) lowerRight.x = x;
		if (y > lowerRight.y) lowerRight.y = y;
	}
	
	public static void sortShapes(List<BinShape> shapes) {
		Collections.sort(shapes, new Comparator<BinShape>() {
			@Override
			public int compare(BinShape o1, BinShape o2) {
				return Double.compare(o1.getULPoint().x, o2.getULPoint().x);
			}
		});
	}
}
