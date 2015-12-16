package ru.recog.segment;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import ru.recog.XML;

/**
 *  
     Class holds calibration Points
     of a specific video 
 *
 * @version      
         1.00 7 December 2015  
 * @author          
         ALexey Denisov  */

public class CalibrationPoint{

	private double x;
	private double y;
	private double height;
	private double length;
	private double alfa;
	
	public CalibrationPoint( double x, double y, double height, double length, double alfa){
		this.x = x;
		this.y = y;
		this.height = height;
		this.length = length;
		this.alfa = alfa;
	}
	
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getHeight(){
		return height;
	}
	
//	public void setHeight(double height){
//		this.height = height;
//	}
	
	public double getLength(){
		return length;
	}
	
//	public void setLength( double length){
//		this.length = length;
//	}
	
	public double getAlfa(){
		return alfa;
	}
	
//	public void setAlfa(double alfa){
//		this.alfa = alfa;
//	}
	
	@Override
	public String toString(){
		
		String str= "";
		str = str.concat("X= ").concat( Double.toString(x));
		str = str.concat(" Y= ").concat( Double.toString(y));
		str = str.concat(" H= ").concat( Double.toString(height));
		str = str.concat(" L= ").concat( Double.toString(length));
		str = str.concat(" Alfa= ").concat( Double.toString(alfa));
		return str;
	}
	
	public static void main(String[] args) throws Exception {
		CalibrationPoint cp = new CalibrationPoint(54, 362, 21, 15, 0);
		CalibrationPoint cp2 = new CalibrationPoint(171, 454, 22, 16, 0);
		CalibrationPoint cp3 = new CalibrationPoint(443, 664, 24, 17, -1);
		CalibrationPoint cp4 = new CalibrationPoint(603, 785, 25, 17, -2);
		List<CalibrationPoint> cpl = Arrays.asList(new CalibrationPoint[]{cp, cp2, cp3, cp4});
		CalibrationLine calLine1 = new CalibrationLine( cpl );
		
		CalibrationPoint ocp = new CalibrationPoint(365, 15, 16, 9, 4);
		CalibrationPoint ocp2 = new CalibrationPoint(750, 223, 18, 10, 2);
		CalibrationPoint ocp3 = new CalibrationPoint(1102, 411, 20, 11, 0);

		List<CalibrationPoint> ocpl = Arrays.asList(new CalibrationPoint[]{ocp, ocp2, ocp3});
		CalibrationLine calLine2 = new CalibrationLine( ocpl );
		


		XML.toXML(cpl, System.out);
		XML.toXML(cpl, new FileWriter(new File("c:\\dev\\list.xml")));
		
		List<CalibrationPoint> cpl2 = (List<CalibrationPoint>)XML.fromXML(new File("c:\\dev\\list.xml"));
		System.out.println(cpl2);
		System.out.println(cpl.equals(cpl2));
		XML.toXML(cpl2, System.out);


		
	}
	
}
