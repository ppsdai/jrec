package ru.recog;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.imgproc.Plate;

public class Aggregator implements Runnable {
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private static final double MIN_DISTANCE = 200; //cause great shaman told us so
	
    private Detector detector;
    
	private List<Plate> sendPlateList = new ArrayList<Plate>();

	private List<Plate> openPlateList  = new ArrayList<Plate>();
	
	private long frameCounter = 0;
	private long detectionCounter = 0;
	
	private BlockingQueue<Mat> queue;
	
	private PlateProcessor plateProcessor;
    
    
    public PlateProcessor getPlateProcessor() {
		return plateProcessor;
	}

	public void setPlateProcessor(PlateProcessor plateProcessor) {
		this.plateProcessor = plateProcessor;
	}

	public Aggregator() {
    	this(new Detector());
	}
    
    public Aggregator(Detector d) {
    	detector = d;
    }

	private void addFrame(String imageFileName) {
		System.out.println("opening "+imageFileName);
		Mat image = Imgcodecs.imread(imageFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		addFrame(image, System.nanoTime());
		
	}
	
	public void setQueue(BlockingQueue<Mat> queue) {
		this.queue = queue;
	}
	
	@Override
	public void run() {
		while (true) {
			Mat m = queue.poll();
			if (m !=null) {
//				System.out.println(m.size()+" "+CvType.typeToString(m.type()));
				addFrame(m, System.nanoTime());
			}
		}
	}
	
	
	private void addFrame(Mat image, long timestamp) {
//		System.out.println("detections: "+detectionCounter+"tmstmp: "+timestamp);
	    MatOfRect plateDetections = detector.detect(image);

	    frameCounter++;
	    if (plateDetections.empty()) {
	    	return;
	    }
	    detectionCounter = detectionCounter + plateDetections.toList().size();
		for (Rect r : plateDetections.toArray()) {
			Plate p = findClosestPlate(r, openPlateList);
			Mat smallImage = image.submat(r);
			if (p!=null) 
				p.add(timestamp, r, smallImage);
			else 
				openPlateList.add(new Plate(timestamp, r, smallImage));
		}
		
		for (Iterator<Plate> it = openPlateList.iterator(); it.hasNext();) {
			Plate p = it.next();
			if (p.getLastAddedTime() != timestamp) {
//				finalPlateList.add(p);
				sendPlateList.add(p);
				it.remove();
			}
		}
		if (!sendPlateList.isEmpty()) sendPlates();
	    
	}
	
	public long getFrames() {
		return frameCounter;
	}
	
	public long getDetections() {
		return detectionCounter;
	}

	public void loadFolder(String dirName) {
		for (File carfile : Utils.getOrderedList(dirName))
			addFrame(carfile.getAbsolutePath());
	}
	
	public void sendPlates() {
		for (Plate p : sendPlateList) {
			plateProcessor.processPlate(p);
		}
		sendPlateList.clear();
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
			int time = Utils.getLastDigits(filename);
			if (prefixes.containsKey(prefix))
				prefixes.get(prefix).add(time, m);
			else
				prefixes.put(prefix, new Plate(time, Plate.EMPTY_RECT, m));
		}
    	
    	return new ArrayList<Plate>(prefixes.values());
    }
    
    public List<Plate> getPlates() {
    	return null;//finalPlateList;
    }
    
    private static float rectDistance(Rect r1, Rect r2) {
    	return (float) Math.sqrt((r1.x - r2.x)*(r1.x - r2.x) + (r1.y - r2.y)*(r1.y - r2.y));
    }
   
}
