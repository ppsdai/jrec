package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

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
	
	private static int nPointsMax, nPointsMin,  widthMax, widthMin, heightMax, heightMin;
	private static double density;
	
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
		
	public int getNPoint(){
		return nPoints;
	}
	
	private void changeBoundingRect(int x, int y){
		
		if (x < upperLeft.x) upperLeft.x = x;
		if (y < upperLeft.y) upperLeft.y = y;
		if (x > lowerRight.x) lowerRight.x = x;
		if (y > lowerRight.y) lowerRight.y = y;
	}
	
	public static void configFilter(double d, int[] configValues){
		density = d;
		nPointsMax = configValues[0];
		nPointsMin = configValues[1];
		widthMax = configValues[2];
		widthMin = configValues[3];
		heightMax = configValues[4];
		heightMin = configValues[5];
	}
	
	public static List<BinShape> filter(List<BinShape> inputList){
		
		List<BinShape> outputList = new ArrayList<BinShape>();
		
		for(int i = 0; i < inputList.size(); i++){
			BinShape temp = inputList.get(i);
			boolean passed = true;
			if ((temp.getNPoint() > nPointsMax) || (temp.getNPoint() < nPointsMin)) passed = false;
			Rect tr = temp.getBoundingRect();
			if ((tr.width > widthMax) || (tr.width < widthMin)) passed = false;
			if ((tr.height > heightMax) || (tr.height < heightMin)) passed = false;
			if ( ( (double)temp.getNPoint()/(tr.width * tr.height)) < density ) passed = false; 
		
			if (passed) outputList.add(temp);
		}
		
		return outputList;
	}
}
