package ru.recog.imgproc;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import ru.recog.video.BasicVideoCapture;


/**
 *  
     Class is made to transform plate numbers into list of objects, that contain
     all the necessary information about plate number -> Look for info into class PlateNumber 
    
    Main Usage:
    1.  findAndShowNumbers(args[0],args[1], cl); 
      takes a catalog with pictures of autos, parses it, makes a directory with sorted number plates,
      sorted into vehicle appearance. Main adjustable parameter is the distance between detection in
      findAndShowNumbers(String dirName, String destName, CascadeClassifier classifier)
       if ( distance  < 200 )
    
    2.  public static List<PlateNumber> convertCatalogToObjects(String dirName);   
    List<PlateNumber> finalPlateList = new ArrayList<PlateNumber>();
 *
 * @version      
         1.00 15 September 2015  * @author          
         ALexey Denisov  */


public class AggregatePlates {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	

	public static String CASCADE_LPRHAAR16 = "/Users/pps/dev/opencv-3.0.0/data/haarcascades/haarcascade_licence_plate_rus_16stages.xml";
	public static String CASCADE_FRONTALFACE = "/Users/pps/dev/opencv-3.0.0/data/lbpcascades/lbpcascade_frontalface.xml";
	public static String CASCADE_LPRHAAR = "/Users/pps/dev/opencv-3.0.0/data/haarcascades/haarcascade_russian_plate_number.xml";
	public static String CASCADE_LEXA = "/Users/pps/dev/DETECT_INSIDE/haarcascade_0_5/cascade.xml";

	
	public static void detectNumber(String imageFileName, String dest, CascadeClassifier classifier, 
			SortedMap<Integer, MatOfRect> sm, SortedMap< Integer, List<Mat> > smMat ) {
		System.out.println("Detecting in "+imageFileName+" FD FT size: "+classifier.getOriginalWindowSize().toString());

  
	    Mat image = Imgcodecs.imread(imageFileName);

	    // Detect plate numbers in the image.

	    MatOfRect plateDetections = new MatOfRect();

	    classifier.detectMultiScale(image, plateDetections,1.05,3,0, new Size(30,10), new Size(120,40));

	    System.out.println(String.format("Detected %s numbers", plateDetections.toArray().length));

   
	    // this part extracts an Integer from a string
	    LinkedList<String> timeOfFrame = new LinkedList<String>();
	    Pattern p = Pattern.compile("\\d+");
	    Matcher m = p.matcher(imageFileName); 
	    while (m.find()) {
	    	timeOfFrame.add(m.group()); 
	    }
	    //System.out.println( timeOfFrame.get(0) );
	    int aInt = Integer.parseInt(timeOfFrame.get( timeOfFrame.size() - 1 ));
	    System.out.println( aInt );
	    
	    sm.put(aInt, plateDetections);
	    
	    // adding images, looks like a FIX, need to change datastructure
	    List<Mat> lm = new ArrayList<Mat>();
	    for (Rect rect : plateDetections.toArray()) 
	    {
	    	Mat newM = image.submat(rect);
	    	lm.add(newM);
	    }
	    smMat.put(aInt, lm);

	}
	  

    
    public static void findAndShowNumbers(String dirName, String destName, CascadeClassifier classifier) {
		File cardir = new File(dirName); //"/Users/pps/dev/cars"
		System.out.println(dirName+" "+cardir.isDirectory());
		String[] carlist = cardir.list();
		for (int i = 0; i< carlist.length; i++)
			System.out.println(carlist[i]);
		File[] carfiles = cardir.listFiles();
		
		 // try to form a structure
	    SortedMap<Integer, MatOfRect> sm = new TreeMap<Integer, MatOfRect>();
	    //FIXME  data structure has only implicit correspondance between Rect <---> Mat (Image)
	    // need to reconfigure so that this correspondance is clear
	    SortedMap< Integer, List<Mat> > smMat = new TreeMap<Integer, List<Mat>>();
	    
		for (int i = 0; i< carfiles.length; i++)
			detectNumber(carfiles[i].getAbsolutePath(), destName, classifier, sm, smMat);
		
		int detectCounter = 0;
		for (Integer t: sm.keySet() )
		{
			//MatOfRect mor = sm.get(t);
			//for (Rect rect : mor.toArray())
			// System.out.println("Key " + t + " " + rect.x+" "+rect.y);
			
			List<Rect> lor = sm.get(t).toList();
			
			for ( int xx=0; xx < lor.size() ;xx++)	
			{
			  System.out.println("Key " + t + " " + lor.get(xx).x+" "+lor.get(xx).y);
			  detectCounter++;
			}
		}
		
		System.out.println(" Detected Numbers Count: " + detectCounter);
		
		// here we do processing
		List<Plate> finalPlateList = new ArrayList<Plate>();
		List<Plate> openPlateList  = new ArrayList<Plate>();
		
		// loop by time on the sorted list
		for (Integer t: sm.keySet() )
		{
			//MatOfRect mor = sm.get(t);
			List<Rect> lor = sm.get(t).toList();
			
			// loop on an opened list make a list of closest finds
			List<Integer> nearestInOpenList = new ArrayList<Integer>();
			for (Rect rect : lor)
			{
				// find minDistance and move to object
				//TODO
				float minDistance = 10000;
				float distance;
				int minDCount = -1;
				for ( int nplt = 0; nplt < openPlateList.size() ; nplt++ )
				{
					Rect rect1 = openPlateList.get(nplt).getLastAddedRect();
					distance = (float) Math.sqrt((rect1.x - rect.x)*(rect1.x - rect.x) + (rect1.y - rect.y)*(rect1.y - rect.y));
					if ( distance < minDistance)
					{
						minDistance = distance;
						minDCount = nplt;
					}
				}
				nearestInOpenList.add(minDCount); // for every rect add a number in a list
				
			}
			
			// check if there are same finds and if there are change them
			//for (PlateNumber plNum: openPlateList)
			//for (PlateNumber plNum2: openPlateList)
			//{
			//	if ( plNum != plNum2)
			//    check if distance is the same
			//}
			
			// add to a list of opened numbers
			for ( int x = 0; x < nearestInOpenList.size() ; x++ )
			{
	
				Rect rect = lor.get(x);
				Mat smallImage = smMat.get(t).get(x); // implicitly should be the same
				// go to a next iteration if -1, i.e. there is no element
				if (nearestInOpenList.get(x) < 0) 
				{
					openPlateList.add(new Plate(t, rect, smallImage) );  //uses a constructor
					continue;
				}
				
				Rect rect1 = openPlateList.get(nearestInOpenList.get(x)).getLastAddedRect();
				// if it is close enough add, otherwise open a new plate in a openList
				float distance = (float) Math.sqrt((rect1.x - rect.x)*(rect1.x - rect.x) + (rect1.y - rect.y)*(rect1.y - rect.y));
		
				if ( distance  < 200 )
				{
					openPlateList.get(nearestInOpenList.get(x)).add(t, rect, smallImage);
				}
				else //add to a list a new Plate
				{
					openPlateList.add(new Plate(t, rect, smallImage) );  //uses a constructor
				}
			}
			
			// if there was no update, then remove from a list of opened numbers to a final list
			int x=0;
			while ( x < openPlateList.size())
			{
				if ( openPlateList.get(x).getLastAddedTime() != t )
				{
					// move to final list and delete from openList
					finalPlateList.add(openPlateList.remove(x));
				
				}
				else
				{
					x++;
				}
			}
			
		}
		
	
		
		// in the end move the rest from open list to a final list
		for (int x=0; x < openPlateList.size(); x++)
			finalPlateList.add(openPlateList.remove(x));
		
		
		// make an output
		int outputCounter = 0;
		for (Plate pn : finalPlateList)
		{
			System.out.println("Next Number " + finalPlateList.indexOf(pn) );
			for (int xx = 0; xx < pn.getLength(); xx++ )
			{
				System.out.println("Time " + pn.getTimeOfRecord(xx)  + 
					" " + pn.getPositionRect(xx).x +" " + 
					pn.getPositionRect(xx).y);
			    outputCounter++;
			}
		}
		System.out.println(" Rearranged Numbers Count: " + outputCounter);
		
		// write to catalog
	    File destF = new File(destName);
	    if (!destF.exists()) destF.mkdirs();
		
	    // find a refix from dirName V051
	    
	    // this part extracts an Integer from a string
	    LinkedList<String> timeOfFrame = new LinkedList<String>();
	    Pattern p = Pattern.compile("\\d+");
	    Matcher m = p.matcher(dirName); 
	    while (m.find()) {
	    	timeOfFrame.add(m.group()); 
	    }
	    //System.out.println( timeOfFrame.get(0) );
	    int aInt = Integer.parseInt(timeOfFrame.get( timeOfFrame.size() - 1 ));
	    
		outputCounter = 0;
		for (Plate pn : finalPlateList)
		{
			//System.out.println("Next Number " + finalPlateList.indexOf(pn) );
			for (int xx = 0; xx < pn.getLength(); xx++ )
			{
    	        String newFN = "V" + aInt + "N"+finalPlateList.indexOf(pn)+"t"+pn.getTimeOfRecord(xx)+".png";
    	        //System.out.println(destName + newFN);
    	        Mat newM = pn.getPlateImage(xx);
	            Imgcodecs.imwrite(new File(destName, newFN).getAbsolutePath(), newM);
			}
		}
    }
    
	
	public static void main(String[] args) throws Exception {
		
		
		if (args.length<2) {
			System.err.println("DetectUtil picFolder cascadeFile [cascadeFile]");
			System.exit(1);
		}
		
		CascadeClassifier cl = new CascadeClassifier(
				args.length<3? CASCADE_LPRHAAR : args[args.length-1]);
		findAndShowNumbers(args[0],args[1], cl);

			
	}
	
	
	public static List<Plate> convertCatalogToObjects(String dirName){
		
		 List<Plate> finalPlateList = new ArrayList<Plate>();
		 return finalPlateList;
		 
		 
		 
	}
	
	
}
