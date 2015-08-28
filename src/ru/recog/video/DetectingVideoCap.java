package ru.recog.video;
import java.awt.image.BufferedImage;

import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import ru.recog.DetectUtil;


public class DetectingVideoCap extends VideoCap {
	

	CascadeClassifier detector;// = new CascadeClassifier(DetectUtil.CASCADE_FRONTALFACE);	
	
	public DetectingVideoCap(){
        super();
    } 
    
	public DetectingVideoCap(String streamURL) {
    	this(streamURL, DetectUtil.CASCADE_LPRHAAR16);
    }
	
	public DetectingVideoCap(String streamURL, String cascadeURL) {
		super(streamURL);
		detector = new CascadeClassifier(cascadeURL);
	}
	
	//@override
    public BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
	    MatOfRect detections = new MatOfRect();
	    long t1 = System.currentTimeMillis();
//	    detector.detectMultiScale(mat2Img.mat, detections);
	    detector.detectMultiScale(mat2Img.mat, detections, 1.3, 3, 0, new Size(64,16), new Size(160,40));
	    long t2 = System.currentTimeMillis();
	    System.out.println((t2-t1)+" ms");

	    System.out.println(String.format("Detected %s numbers", detections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : detections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(mat2Img.mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    }
        
//        System.out.println(cap.get(Videoio.CAP_PROP_POS_FRAMES));
        return mat2Img.getImage(mat2Img.mat);
    }
	

}
