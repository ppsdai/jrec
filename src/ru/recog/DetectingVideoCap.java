package ru.recog;
import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;


public class DetectingVideoCap extends VideoCap {
	

	CascadeClassifier detector;// = new CascadeClassifier(DetectUtil.CASCADE_FRONTALFACE);	
	
	DetectingVideoCap(){
        super();
    } 
    
	DetectingVideoCap(String streamURL) {
    	this(streamURL, DetectUtil.CASCADE_LPRHAAR16);
    }
	
	DetectingVideoCap(String streamURL, String cascadeURL) {
		super(streamURL);
		detector = new CascadeClassifier(cascadeURL);
	}
	
	//@override
    BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
	    MatOfRect detections = new MatOfRect();
	    detector.detectMultiScale(mat2Img.mat, detections);

	    System.out.println(String.format("Detected %s numbers", detections.toArray().length));

	    // Draw a bounding box around each face.
	    for (Rect rect : detections.toArray()) {
	    	System.out.println(rect.x+" "+rect.y);
	        Imgproc.rectangle(mat2Img.mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    }
        
//        System.out.println(cap.get(Videoio.CAP_PROP_POS_FRAMES));
        return mat2Img.getImage(mat2Img.mat);
    }
	
 /*   CascadeClassifier faceDetector = new CascadeClassifier(cascadeURL);
    
    System.out.println(faceDetector);

    System.out.println("FD FT size: "+faceDetector.getOriginalWindowSize().toString());
    Mat image = Imgcodecs.imread(imageFileName);

    // Detect faces in the image.
    // MatOfRect is a special container class for Rect.
    MatOfRect faceDetections = new MatOfRect();
    faceDetector.detectMultiScale(image, faceDetections);*/

}
