package ru.recog.segment;


import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import ru.recog.XML;

/**
 *  
     Class holds calibration lines
     of a specific video with methods
     to extract proximity, etc. 
 *
 * @version      
         1.00 8 December 2015  
 * @author          
         ALexey Denisov  */

public class CalibrationLine {
	
	private List<CalibrationPoint> calLine;
	
	public CalibrationLine( List<CalibrationPoint> calLine  ){
		this.calLine = calLine;
	}
	
	public List<CalibrationPoint> getCalLine() {
		return calLine;
	}
	
    /**
     * Finds a smallest distance to a pair of points
     * on a calibration line
     * returns a "quasi" calibration point on a calibration line
     */
	public CalibrationPoint findMinDistancePoint( CalibrationPoint searchP) {
		
		CalibrationPoint qcp = new CalibrationPoint(0, 0, 0, 0, 0);
		
		double minDist  = 1024;
		for (int i=0; i< (calLine.size() - 1); i++) {
			
			CalibrationPoint cp1 = calLine.get(i);
			CalibrationPoint cp2 = calLine.get(i+1);
			
			// find closest point
			CalibrationPoint clP = findClosestPoint( searchP, cp1, cp2);
			
//			System.out.println("I= " + i); 
//			System.out.println(	cp1.toString() );
//			System.out.println(	cp2.toString() );
//			System.out.println(	clP.toString() );
			
			
			// find distance
			double dist = findDistance(searchP, clP);
//			double dist = Math.sqrt( (searchP.getX() - clP.getX()) * (searchP.getX() - clP.getX()) +
//					                 (searchP.getY() - clP.getY()) * (searchP.getY() - clP.getY()) );
			
			// if less than minimum, replace the closest point
			if ( dist < minDist ){
			  minDist = dist;
			  qcp = clP;
			}
			
		}
		
		return qcp;
		
	}
	
	public double findDistance( CalibrationPoint p1, CalibrationPoint p2 ){
	
	double dist = Math.sqrt( (p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) +
            (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()) );
	return dist;
	
	}
	
	private CalibrationPoint findClosestPoint( CalibrationPoint cp0, 
			                                   CalibrationPoint cp1, CalibrationPoint cp2){
		
		// doing some analytical geometry
		
		double absR1 = Math.sqrt( (cp2.getX() - cp1.getX())*(cp2.getX() - cp1.getX()) +
				                  (cp2.getY() - cp1.getY())*(cp2.getY() - cp1.getY()) );
		double r0_Proj = (cp2.getX()-cp1.getX())*(cp0.getX() - cp1.getX()) +
				         (cp2.getY()-cp1.getY())*(cp0.getY() - cp1.getY());
		r0_Proj = r0_Proj / absR1;
		double fraction = r0_Proj/absR1;
		
		
		double Rc_X = cp1.getX() + (cp2.getX() - cp1.getX()) * fraction;
		double Rc_Y = cp1.getY() + (cp2.getY() - cp1.getY()) * fraction;
		// if fraction < 0, closest point will be cp1
		if ( fraction < 0 ) {
			Rc_X = cp1.getX();
			Rc_Y = cp1.getY();
		}
		// if fraction > 1, closest point will be cp2
		if ( fraction > 1 ) {
			Rc_X = cp2.getX();
			Rc_Y = cp2.getY();
		}
		
		double height = cp1.getHeight() * (1 - fraction) + cp2.getHeight() * fraction;
		double length = cp1.getLength() * (1 - fraction) + cp2.getLength() * fraction;
		double alfa   = cp1.getAlfa()   * (1 - fraction) + cp2.getAlfa()   * fraction;
		
		return new CalibrationPoint(Rc_X, Rc_Y, height , length, alfa);
	}
	
	
	public static void main(String[] args) throws Exception {
		CalibrationPoint cp =  new CalibrationPoint(93, 244, 14, 65.0/6, -4);
		CalibrationPoint cp2 = new CalibrationPoint(262, 422, 16, 74.0/6, -5);
		CalibrationPoint cp3 = new CalibrationPoint(499, 666, 18, 87.0/6, -6);
		//CalibrationPoint cp4 = new CalibrationPoint( 4, 407, 18, 65/6, -2);
		List<CalibrationPoint> cpl = Arrays.asList(new CalibrationPoint[]{cp, cp2, cp3});
		CalibrationLine calLine1 = new CalibrationLine( cpl );
		
		CalibrationPoint ocp = new CalibrationPoint(1155, 391, 18, 65.0/6,  0);
		CalibrationPoint ocp2 = new CalibrationPoint(944, 261, 16, 59.0/6,  1);
		CalibrationPoint ocp3 = new CalibrationPoint(775, 158, 15, 54.0/6,  2);
		CalibrationPoint ocp4 = new CalibrationPoint(641,  75, 14, 50.0/6, 2.5);
		CalibrationPoint ocp5 = new CalibrationPoint(524,   2, 13, 47.0/6,  3);
		
		List<CalibrationPoint> ocpl = Arrays.asList(new CalibrationPoint[]{ocp, ocp2, ocp3, ocp4, ocp5});
		CalibrationLine calLine2 = new CalibrationLine( ocpl );
		
		
		// test with CalibrationLine1
		
		CalibrationPoint testPoint = new CalibrationPoint(353, 455, 0, 0, 0);
		//CalibrationPoint testPoint = new CalibrationPoint(170, 450, 0, 0, 0);
		CalibrationPoint foundPoint =calLine1.findMinDistancePoint( testPoint);
		
		System.out.println( foundPoint.toString() );
		

		XML.toXML(calLine1, new FileWriter(new File("C:\\dev\\frames\\VNew\\cal76635\\Cal76635\\CalLine1.xml")));
		XML.toXML(calLine2, new FileWriter(new File("C:\\dev\\frames\\VNew\\cal76635\\Cal76635\\CalLine2.xml")));
		
		
		CalibrationLine xmlCalLine1 = (CalibrationLine) XML.fromXML(new File("C:\\dev\\frames\\VNew\\cal76635\\Cal76635\\CalLine1.xml"));
		
		System.out.println(xmlCalLine1);
		
		foundPoint =xmlCalLine1.findMinDistancePoint( testPoint);
		System.out.println( foundPoint.toString() );
		
		
	}
	
}
