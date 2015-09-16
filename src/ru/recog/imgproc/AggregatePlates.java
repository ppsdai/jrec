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
	
	
	private static final double MIN_DISTANCE = 200; //cause great shaman told us so
	
	
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
			List<Rect> lor = sm.get(t).toList();
			
			for (Rect r : lor) {
				Plate p = findClosestPlate(r, openPlateList);
				Mat smallImage = smMat.get(t).get(lor.indexOf(r));
				if (p!=null) 
					p.add(t, r, smallImage);
				else 
					openPlateList.add(new Plate(t, r, smallImage));
			}
			
			for (Iterator<Plate> it = openPlateList.iterator(); it.hasNext();) {
				Plate p = it.next();
				if (p.getLastAddedTime() != t) {
					finalPlateList.add(p);
					it.remove();
				}
			}
		}
		
		flushPlates(); //FIXME we shall do this separately when we switch to streaming
	}
	
	public void flushPlates() {
		finalPlateList.addAll(openPlateList);
		openPlateList.clear();
	}
	
	private Plate findClosestPlate(Rect r, List<Plate> plateList) {
		Plate closest = null; double distance = 10000;
		for (Plate plate : plateList) {
			double d = rectDistance(r, plate.getLastAddedRect());
			if (distance > d) {
				distance = d; 
				closest = plate;
			}
		}
		return distance <= MIN_DISTANCE? closest : null;
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
		
		AggregatePlates ap = new AggregatePlates();
		ap.loadFolder("/Users/pps/dev/testframes");
		System.out.println("Total frames: "+ap.getFrames());
		System.out.println(" Detected Numbers Count: " + ap.getDetections());
		ap.process();

	}
	
	
}
