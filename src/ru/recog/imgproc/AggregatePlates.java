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
	
	
    private SortedMap<Long, MatOfRect> sm = new TreeMap<Long, MatOfRect>();
    //FIXME  data structure has only implicit correspondance between Rect <---> Mat (Image)
    // need to reconfigure so that this correspondance is clear
    private SortedMap<Long, List<Mat>> smMat = new TreeMap<Long, List<Mat>>();
    
    private CascadeClassifier classifier;
    
	private List<Plate> finalPlateList = new ArrayList<Plate>();
	private List<Plate> openPlateList  = new ArrayList<Plate>();
	
	private long frameCounter = 0;
	private long detectionCounter = 0;
    
    
    public AggregatePlates() {
    	this(new CascadeClassifier( Utils.CASCADE_LPR_PATH) );
	}
    
    public AggregatePlates(CascadeClassifier cl) {
    	classifier = cl;
    }

	private void addFrame(String imageFileName) {
		Mat image = Imgcodecs.imread(imageFileName);
		long timestamp = (long) getLastDigits(imageFileName);
		addFrame(image, timestamp);
		
	}
	
	private void addFrame(Mat image, long timestamp) {
	    MatOfRect plateDetections = new MatOfRect();
	    classifier.detectMultiScale(image, plateDetections,1.05,3,0, new Size(30,10), new Size(120,40));

	    frameCounter++;
	    detectionCounter = detectionCounter + plateDetections.toList().size();
	    
	    sm.put(timestamp, plateDetections);
	    
	    // adding images, looks like a FIX, need to change datastructure
	    List<Mat> lm = new ArrayList<Mat>();
	    for (Rect rect : plateDetections.toArray()) 
	    	lm.add(image.submat(rect));
	    smMat.put(timestamp, lm);
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
		for (long t: sm.keySet() ) {
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
					System.out.println(" Added ");
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
    
    
    public static void savePlates(String destination, List<Plate> plateList, String prefix ) {
		// write to catalog
	    File destF = new File(destination);
	    if (!destF.exists()) destF.mkdirs();
		
		for (Plate pn : plateList)
		{
			//System.out.println("Next Number " + finalPlateList.indexOf(pn) );
			for (int xx = 0; xx < pn.getLength(); xx++ )
			{
    	        String newFN = prefix + "N"+plateList.indexOf(pn)+"t"+pn.getTimeOfRecord(xx)+".png";
    	        //System.out.println(destName + newFN);
    	        Mat newM = pn.getPlateImage(xx);
	            Imgcodecs.imwrite(new File(destF, newFN).getAbsolutePath(), newM);
			}
		} 
    }
    
    /**
     * Reads a folder with plate images with naming scheme V##N##t##
     * @param source  folder with images
     * @return List<Plate> containing plates in these folders
     */
    
    public static List<Plate> readFormattedFolder(String source) {
    	Map<String, Plate> prefixes = new HashMap<String, Plate>();
		File cardir = new File(source); 
		
		for (File carfile : cardir.listFiles(Utils.FILTER_BMP_PNG)) {
			Mat m = Imgcodecs.imread(carfile.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			String filename = carfile.getName();
			String prefix = filename.substring(0, filename.lastIndexOf("t"));
			int time = getLastDigits(filename);
			if (prefixes.containsKey(prefix))
				prefixes.get(prefix).add(time, m);
			else
				prefixes.put(prefix, new Plate(time, Plate.EMPTY_RECT, m));
		}
    	
    	return new ArrayList<Plate>(prefixes.values());
    }
    
    public List<Plate> getPlates() {
    	return finalPlateList;
    }
    
    private static int getLastDigits(String s) {
	    LinkedList<String> timeOfFrame = new LinkedList<String>();
	    Pattern p = Pattern.compile("\\d+");
	    Matcher m = p.matcher(s); 
	    while (m.find()) {
	    	timeOfFrame.add(m.group()); 
	    }
	    return Integer.parseInt(timeOfFrame.get( timeOfFrame.size() - 1 ));
    }
    
    private static float rectDistance(Rect r1, Rect r2) {
    	return (float) Math.sqrt((r1.x - r2.x)*(r1.x - r2.x) + (r1.y - r2.y)*(r1.y - r2.y));
    }
    
	
	public static void main(String[] args) throws Exception {
		
		AggregatePlates ap = new AggregatePlates();
		ap.loadFolder("C:\\dev\\frames\\VNew\\1411");
		//ap.loadFolder("C:\\dev\\frames\\Try");
		System.out.println("Total frames: "+ap.getFrames());
		System.out.println(" Detected Numbers Count: " + ap.getDetections());
		ap.process();
		
		System.out.println(ap.getPlates());
		
		savePlates("C:\\dev\\frames\\VNew\\detected1411", ap.getPlates(), "V1411");

//		System.out.println(readFormattedFolder("/Users/pps/dev/aggr"));
	}
	
	
}
