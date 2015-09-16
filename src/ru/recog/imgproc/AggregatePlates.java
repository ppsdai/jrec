package ru.recog.imgproc;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import ru.recog.Utils;

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
	
	
    private SortedMap<Integer, MatOfRect> sm = new TreeMap<Integer, MatOfRect>();
    //FIXME  data structure has only implicit correspondance between Rect <---> Mat (Image)
    // need to reconfigure so that this correspondance is clear
    private SortedMap<Integer, List<Mat>> smMat = new TreeMap<Integer, List<Mat>>();
    
    private CascadeClassifier classifier;
    
	private List<Plate> finalPlateList = new ArrayList<Plate>();
	private List<Plate> openPlateList  = new ArrayList<Plate>();
	
	private long frameCounter = 0;
	private long detectionCounter = 0;
    
    
    public AggregatePlates() {
    	this(new CascadeClassifier( Utils.CASCADE_LPR.getFile()) );
	}
    
    public AggregatePlates(CascadeClassifier cl) {
    	classifier = cl;
    }

	private void addFrame(String imageFileName) {
	    Mat image = Imgcodecs.imread(imageFileName);
	    MatOfRect plateDetections = new MatOfRect();
	    classifier.detectMultiScale(image, plateDetections,1.05,3,0, new Size(30,10), new Size(120,40));

	    int aInt = getLastDigits(imageFileName);
	    
	    frameCounter++;
	    detectionCounter = detectionCounter + plateDetections.toList().size();
	    
	    sm.put(aInt, plateDetections);
	    
	    // adding images, looks like a FIX, need to change datastructure
	    List<Mat> lm = new ArrayList<Mat>();
	    for (Rect rect : plateDetections.toArray()) 
	    	lm.add(image.submat(rect));
	    smMat.put(aInt, lm);
	}
	
	public long getFrames() {
		return frameCounter;
	}
	
	public long getDetections() {
		return detectionCounter;
	}

	public void loadFolder(String dirName) {
		File cardir = new File(dirName); 
		
		for (File carfile : cardir.listFiles(Utils.FILTER_BMP_PNG))
			addFrame(carfile.getAbsolutePath());
	}
	
	public void process() {
		
		// loop by time on the sorted list
		for (Integer t: sm.keySet() ) {
			//MatOfRect mor = sm.get(t);
			List<Rect> lor = sm.get(t).toList();
			
			// loop on an opened list make a list of closest finds
			List<Integer> nearestInOpenList = new ArrayList<Integer>();
			for (Rect rect : lor) {
				// find minDistance and move to object
				//TODO
				float minDistance = 10000;
				float distance;
				int minDCount = -1;
				for ( int nplt = 0; nplt < openPlateList.size() ; nplt++ ) {
					Rect rect1 = openPlateList.get(nplt).getLastAddedRect();
//					distance = (float) Math.sqrt((rect1.x - rect.x)*(rect1.x - rect.x) + (rect1.y - rect.y)*(rect1.y - rect.y));
					distance = rectDistance(rect, rect1);
					if ( distance < minDistance) {
						minDistance = distance;
						minDCount = nplt;
					}
				}
				nearestInOpenList.add(minDCount); // for every rect add a number in a list
				
			}
			
/*			 check if there are same finds and if there are change them
			for (PlateNumber plNum: openPlateList)
			for (PlateNumber plNum2: openPlateList)
			{
				if ( plNum != plNum2)
			    check if distance is the same
			}
			*/
			
			// add to a list of opened numbers
			for ( int x = 0; x < nearestInOpenList.size() ; x++ ) {
				Rect rect = lor.get(x);
				Mat smallImage = smMat.get(t).get(x); // implicitly should be the same
				// go to a next iteration if -1, i.e. there is no element
				if (nearestInOpenList.get(x) < 0) {
					openPlateList.add(new Plate(t, rect, smallImage) );  //uses a constructor
					continue;
				}
				
				Rect rect1 = openPlateList.get(nearestInOpenList.get(x)).getLastAddedRect();
				// if it is close enough add, otherwise open a new plate in a openList
				float distance = rectDistance(rect1, rect);
//						(float) Math.sqrt((rect1.x - rect.x)*(rect1.x - rect.x) + (rect1.y - rect.y)*(rect1.y - rect.y));
		
				if ( distance  < 200 )
					openPlateList.get(nearestInOpenList.get(x)).add(t, rect, smallImage);
				else //add to a list a new Plate
					openPlateList.add(new Plate(t, rect, smallImage) );  //uses a constructor
			}
			
			// if there was no update, then remove from a list of opened numbers to a final list
			int x=0;
			while ( x < openPlateList.size()) {
				if ( openPlateList.get(x).getLastAddedTime() != t )
					// move to final list and delete from openList
					finalPlateList.add(openPlateList.remove(x));
				else
					x++;
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
	}
    
    
    public void savePlates(String destination, List<Plate> plateList, String prefix ) {
		// write to catalog
	    File destF = new File(destination);
	    if (!destF.exists()) destF.mkdirs();
		
	    // find a refix from dirName V051
//	    int aInt = getLastDigits(dirName);
//	    String pref = "V" + aInt;
	    String pref = prefix;
	    
		for (Plate pn : finalPlateList)
		{
			//System.out.println("Next Number " + finalPlateList.indexOf(pn) );
			for (int xx = 0; xx < pn.getLength(); xx++ )
			{
    	        String newFN = prefix + "N"+finalPlateList.indexOf(pn)+"t"+pn.getTimeOfRecord(xx)+".png";
    	        //System.out.println(destName + newFN);
    	        Mat newM = pn.getPlateImage(xx);
	            Imgcodecs.imwrite(new File(destF, newFN).getAbsolutePath(), newM);
			}
		} 
    }
    
    private static int getLastDigits(String s) {
	    LinkedList<String> timeOfFrame = new LinkedList<String>();
	    Pattern p = Pattern.compile("\\d+");
	    Matcher m = p.matcher(s); 
	    while (m.find()) {
	    	timeOfFrame.add(m.group()); 
	    }
	    //System.out.println( timeOfFrame.get(0) );
	    return Integer.parseInt(timeOfFrame.get( timeOfFrame.size() - 1 ));
    }
    
    private static float rectDistance(Rect r1, Rect r2) {
    	return (float) Math.sqrt((r1.x - r2.x)*(r1.x - r2.x) + (r1.y - r2.y)*(r1.y - r2.y));
    }
    
	
	public static void main(String[] args) throws Exception {
		
//		AggregatePlates ap = new AggregatePlates();
//		ap.loadFolder("/Users/pps/dev/testframes");
//		System.out.println("Total frames: "+ap.getFrames());
//		System.out.println(" Detected Numbers Count: " + ap.getDetections());
//		ap.process();
		
//		
//		if (args.length<2) {
//			System.err.println("DetectUtil picFolder cascadeFile [cascadeFile]");
//			System.exit(1);
//		}
//		
//		CascadeClassifier cl = new CascadeClassifier(
//				args.length<3? Utils.CASCADE_LPR.getFile() : args[args.length-1]);
//		findAndShowNumbers(args[0],args[1], cl);

	}
	
	
	public static List<Plate> convertCatalogToObjects(String dirName){
		
		 List<Plate> finalPlateList = new ArrayList<Plate>();
		 return finalPlateList;
		 
		 
		 
	}
	
	
}
