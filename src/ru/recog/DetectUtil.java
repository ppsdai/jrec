package ru.recog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.PrintStream;

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


public class DetectUtil {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	public static String picURL1 = "/Users/pps/dev/huy2.jpg";
	public static String CASCADE_LPRHAAR16 = "/Users/pps/dev/opencv-3.0.0/data/haarcascades/haarcascade_licence_plate_rus_16stages.xml";
	public static String CASCADE_FRONTALFACE = "/Users/pps/dev/opencv-3.0.0/data/lbpcascades/lbpcascade_frontalface.xml";
	public static String CASCADE_LPRHAAR = "/Users/pps/dev/opencv-3.0.0/data/haarcascades/haarcascade_russian_plate_number.xml";
	public static String CASCADE_LEXA = "/Users/pps/dev/DETECT_INSIDE/haarcascade_0_5/cascade.xml";

	public static void detectNumber() {
		System.out.println("\nRunning Detect Demo");

	    // Create a face detector from the cascade file in the resources
	    // directory.
	    CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_LPRHAAR16);
	    
	    System.out.println(faceDetector);

	    System.out.println("FD FT size: "+faceDetector.getOriginalWindowSize().toString());
	    Mat image = Imgcodecs.imread(picURL1);

	    // Detect faces in the image.
	    // MatOfRect is a special container class for Rect.
	    MatOfRect faceDetections = new MatOfRect();
	    faceDetector.detectMultiScale(image, faceDetections);

	    System.out.println(String.format("Detected %s numbers", faceDetections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : faceDetections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    }
	    
	    String filename = "/Users/pps/dev/number2.png";
	    System.out.println(String.format("Writing %s", filename));
	    Imgcodecs.imwrite(filename, image);

	}
	
	public static void detectNumber(String imageFileName) {
		System.out.println("Detecting in "+imageFileName);

	    // Create a face detector from the cascade file in the resources
	    // directory.
	    CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_LPRHAAR16);
	    
	    System.out.println(faceDetector);

	    System.out.println("FD FT size: "+faceDetector.getOriginalWindowSize().toString());
	    Mat image = Imgcodecs.imread(imageFileName);

	    // Detect faces in the image.
	    // MatOfRect is a special container class for Rect.
	    MatOfRect faceDetections = new MatOfRect();
	    faceDetector.detectMultiScale(image, faceDetections);

	    System.out.println(String.format("Detected %s numbers", faceDetections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : faceDetections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    }
	    
//	    String filename = "/Users/pps/dev/number2.png";
	    System.out.println(String.format("Writing %s", imageFileName));
	    Imgcodecs.imwrite(imageFileName, image);

	}
	
	public static void detectNumber(String imageFileName, CascadeClassifier classifier) {
		System.out.println("Detecting in "+imageFileName+" FD FT size: "+classifier.getOriginalWindowSize().toString());

	    // Create a face detector from the cascade file in the resources
	    // directory.
	    

	    Mat image = Imgcodecs.imread(imageFileName);

	    // Detect faces in the image.
	    // MatOfRect is a special container class for Rect.
//	    Mat doubled = new Mat();
//	    Imgproc.resize(image, doubled, new Size(), 2.0, 2.0, Imgproc.INTER_LINEAR);
//	    Mat doubled = Imgproc.resize(image, doubled, dsize, fx, fy, interpolation);
	    MatOfRect faceDetections = new MatOfRect();
	    classifier.detectMultiScale(image, faceDetections);
//	    classifier.detectMultiScale(image, faceDetections,1.05,0,0, new Size(3,4), new Size(48,60));

	    System.out.println(String.format("Detected %s numbers", faceDetections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : faceDetections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		    Imgcodecs.imwrite(imageFileName, image);

	    }
	    
//	    String filename = "/Users/pps/dev/number2.png";
//	    if (faceDetections.toArray().length > 0 ) {
//	    	String newName = imageFileName.replaceAll(".bmp", "_.bmp");
//		    System.out.println(String.format("Writing %s", imageFileName));
//		    Imgcodecs.imwrite(newName, doubled);
//	    }

	}
	
	public static void detectNumberAndSave(String imageFileName, String dest, CascadeClassifier classifier) {
		System.out.println("Detecting in "+imageFileName+" FD FT size: "+classifier.getOriginalWindowSize().toString());

	    // Create a face detector from the cascade file in the resources
	    // directory.
	    

	    Mat image = Imgcodecs.imread(imageFileName);

	    // Detect faces in the image.
	    // MatOfRect is a special container class for Rect.
//	    Mat doubled = new Mat();
//	    Imgproc.resize(image, doubled, new Size(), 2.0, 2.0, Imgproc.INTER_LINEAR);
//	    Mat doubled = Imgproc.resize(image, doubled, dsize, fx, fy, interpolation);
	    MatOfRect faceDetections = new MatOfRect();
//	    classifier.detectMultiScale(image, faceDetections);
	    classifier.detectMultiScale(image, faceDetections,1.05,3,0, new Size(30,10), new Size(120,40));

	    System.out.println(String.format("Detected %s numbers", faceDetections.toArray().length));

	    // Draw a bounding box around each face.
	    int i = 1;
    	File f = new File(imageFileName);
    	String fs = f.getName();
    	String ext = fs.substring(fs.lastIndexOf('.'));
    	String name = fs.substring(0, fs.lastIndexOf('.'));
	    for (Rect rect : faceDetections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
//	        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    	Mat newM = image.submat(rect);
	    	String newFN = name.concat(String.valueOf(i)).concat(ext);
		    Imgcodecs.imwrite(new File(dest, newFN).getAbsolutePath(), newM);
		    i++;

	    }
	    
	}
	
    public static void displayImage(Image img2) {   
	    //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
	    ImageIcon icon=new ImageIcon(img2);
	    JFrame frame=new JFrame();
	    frame.setLayout(new FlowLayout());        
	    frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
	    JLabel lbl=new JLabel();
	    lbl.setIcon(icon);
	    frame.add(lbl);
    	
//    	SimpleViewer frame = new SimpleViewer("YOYO");
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    
    }
    
    
    public static BufferedImage Mat2BufferedImage(Mat m) {
	// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	// Fastest code
	// The output can be assigned either to a BufferedImage or to an Image
	
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( m.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels()*m.cols()*m.rows();
	    byte [] b = new byte[bufferSize];
	    m.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;

    }
    
    public static void printVCInfo(VideoCapture vc, PrintStream ps) {
    	ps.println (vc.get(Videoio.CAP_PROP_POS_MSEC)+" Current position of the video file in milliseconds or video capture timestamp");
    	ps.println (vc.get(Videoio.CAP_PROP_POS_FRAMES)+" 0-based index of the frame to be decoded/captured next.");
    	ps.println(vc.get(Videoio.CAP_PROP_POS_AVI_RATIO)+" Relative position of the video file: 0 - start of the film, 1 - end of the film.");
    	ps.println (vc.get(Videoio.CAP_PROP_FRAME_WIDTH) +" Width of the frames in the video stream.");
    	ps.println (vc.get(Videoio.CAP_PROP_FRAME_HEIGHT) +" Height of the frames in the video stream.");
    	ps.println(vc.get(Videoio.CAP_PROP_FPS)+" Frame rate.");
    	ps.println(vc.get(Videoio.CAP_PROP_FOURCC)+" 4-character code of codec.");
    	ps.println(vc.get(Videoio.CAP_PROP_FRAME_COUNT)+" Number of frames in the video file.");
    	ps.println(vc.get(Videoio.CAP_PROP_FORMAT)+" Format of the Mat objects returned by retrieve() .");
    }
    
    public static void buildFramesFolder(VideoCapture vc, String prefix, PrintStream ps) 
    		throws Exception 
    {
//    	File dir = new File(targetFolder);
    	
    	long totalframes = (long)vc.get(Videoio.CAP_PROP_FRAME_COUNT); //TODO check if this is right
    	ps.println("TF: "+totalframes+" double: "+vc.get(Videoio.CAP_PROP_FRAME_COUNT));
    	long framecount = 0;
    	long totalcount = 0;
    	Mat frame = new Mat();
    	while (vc.read(frame)) {
//    		ps.println("frame # "+totalcount+" framecount");
    		framecount++; totalcount++;
    		if (framecount >= 120) {
    			framecount = 0;
    			saveFrame(frame, totalcount, prefix);
    		}
    	}
    	
    }
    
    public static void buildFrames(BasicVideoCapture bvc) {
    	BasicVideoCapture.printVCInfo(bvc, System.out);
    	Mat frame0 = new Mat();
    	int equalCount = 0;
    	Scalar zero = Scalar.all(0);
    	for (int i = 0; ; i++) {
    		Mat m = new Mat();
    		if (!bvc.read(m)) break;;
    		if (i==0) frame0 = m.clone();
    		
    			Mat d = new Mat();
    			Core.absdiff(frame0, m, d);
    			d = d.mul(d);
    			Scalar s = Core.sumElems(d);
//    			System.out.println("scalar: "+s.toString());
    			if (zero.equals(s)) {
    				equalCount++;
    			} else {
    				System.out.println("Total: "+i+" equal frames: "+equalCount);
    				frame0 = m.clone();
    				equalCount = 0;
    			}
    		
//    		saveFrame(m, i, "/Users/pps/dev/exp/");
    	}
    	
    }
    
    public static void saveFrame(Mat frame, long totalcount, String prefix) {
    	
//    	System.out.println("SAving "+targetFolder+"/"+prefix+totalcount);
    	String s = new String(prefix.concat(String.valueOf(totalcount)).concat(".jpg"));
    	System.out.println(s);

    	Imgcodecs.imwrite(s, frame);
    	
//    	System.out.println("SAving "+targetFolder+"/"+prefix+totalcount);
    	
    }
    
    public static void findAndShowNumbers(String dirName) {
		File cardir = new File(dirName); //"/Users/pps/dev/cars"
		System.out.println(cardir.isDirectory());
		String[] carlist = cardir.list();
		for (int i = 0; i< carlist.length; i++)
			System.out.println(carlist[i]);
		File[] carfiles = cardir.listFiles();
		for (int i = 0; i< carfiles.length; i++)
			detectNumber(carfiles[i].getAbsolutePath());
    }
    
    public static void findAndShowNumbers(String dirName, CascadeClassifier classifier) {
		File cardir = new File(dirName); //"/Users/pps/dev/cars"
		System.out.println(dirName+" "+cardir.isDirectory());
		String[] carlist = cardir.list();
		for (int i = 0; i< carlist.length; i++)
			System.out.println(carlist[i]);
		File[] carfiles = cardir.listFiles();
		for (int i = 0; i< carfiles.length; i++)
			detectNumber(carfiles[i].getAbsolutePath(), classifier);
    }
    
    public static void findAndShowNumbers(String dirName, String destName, CascadeClassifier classifier) {
		File cardir = new File(dirName); //"/Users/pps/dev/cars"
		System.out.println(dirName+" "+cardir.isDirectory());
		String[] carlist = cardir.list();
		for (int i = 0; i< carlist.length; i++)
			System.out.println(carlist[i]);
		File[] carfiles = cardir.listFiles();
		for (int i = 0; i< carfiles.length; i++)
			detectNumberAndSave(carfiles[i].getAbsolutePath(), destName, classifier);
    }
    
	
	public static void main(String[] args) throws Exception {
		
		
//		buildFramesFolder(vc, "/Users/pps/dev/frames/snap", System.out);
		
/*		if (args.length<2) {
			System.err.println("DetectUtil picFolder cascadeFile");
			System.exit(1);
		}
		
		
		CascadeClassifier  cl = new CascadeClassifier(args[1]);
		findAndShowNumbers(args[0],cl);*/
		
		CascadeClassifier cl = new CascadeClassifier(CASCADE_LPRHAAR);
		findAndShowNumbers(args[0],"/Users/pps/dev/detected", cl);

		
		
		
	}
}
