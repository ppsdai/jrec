package ru.recog.video;
import java.awt.image.BufferedImage;

import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import ru.recog.DetectUtil;


public class MultipleDetectorsVideoCap extends VideoCap {
	

	CascadeClassifier detector;// = new CascadeClassifier(DetectUtil.CASCADE_FRONTALFACE);	
	CascadeClassifier detector2;
	
	MultipleDetectorsVideoCap(){
        super();
    } 
    
	MultipleDetectorsVideoCap(String streamURL) {
    	this(streamURL, DetectUtil.CASCADE_LPRHAAR16,DetectUtil.CASCADE_LPRHAAR);
    }
	
	MultipleDetectorsVideoCap(String streamURL, String cascadeURL,String cascade2URL) {
		super(streamURL);
		detector = new CascadeClassifier(cascadeURL);
		detector2 = new CascadeClassifier(cascade2URL);
	}
	
	//@override
    public BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
	    MatOfRect detections = new MatOfRect();
	    MatOfRect detections2 = new MatOfRect();

	    detector.detectMultiScale(mat2Img.mat, detections);
	    detector2.detectMultiScale(mat2Img.mat, detections2);


	    System.out.println(String.format("1st: Detected %s numbers", detections.toArray().length));

	    // Draw a bounding box around each detection.
	    for (Rect rect : detections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(mat2Img.mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    }
	    
	    System.out.println(String.format("2nd: Detected %s numbers", detections2.toArray().length));

	    
	    for (Rect rect : detections2.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(mat2Img.mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0));
	    }
        
        return mat2Img.getImage(mat2Img.mat);
    }
	

}
