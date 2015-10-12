package ru.recog;

import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.Videoio;

import ru.recog.video.BasicVideoCapture;

public class FrameBuilder {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	private File frameFolder;
	
	private BasicVideoCapture videoCapture;
	
	BackgroundSubtractor subtractor = Video.createBackgroundSubtractorMOG2();
	
	private static long MASK_THRESHOLD = 1000000;
	private static String IMG_EXTENSION = ".png";
	
	public FrameBuilder(String videoURL, String folderURL) throws IOException {
//		makeDir(videoURL);
		videoCapture = new BasicVideoCapture(videoURL);
		BasicVideoCapture.printVCInfo(videoCapture, System.out);
		makeDir(folderURL);
		
	}
	
	private void makeDir(String folderURL) {
		frameFolder = new File(folderURL);
		if (!frameFolder.exists()) frameFolder.mkdir();
		else System.out.println("Warning. Folder exists already: "+frameFolder.getAbsolutePath());
	}
	
	
//	public void buildFramesX() {
//		long pos = 0;
//		Mat m = new Mat();
//		boolean grab = videoCapture.read(m);
//		while (grab) {
//			long mspos2 = (long) (pos/videoCapture.getFPS()*1000);
//			System.out.println("Grabbing at pos: "+pos+" ms: "+mspos2+" "
//					+grab+" avi"+videoCapture.get(Videoio.CAP_PROP_POS_AVI_RATIO)+" double ms "
//					+videoCapture.get(Videoio.CAP_PROP_POS_MSEC));
//			Imgcodecs.imwrite("/Users/pps/dev/frames4/frame".concat(String.valueOf(mspos2)).concat(".png"), m);
//			for (int i = 0; (grab && i<24);i++) {
//				grab = videoCapture.read(m);
//				pos++;
//			}
//		}
//	}
	
	
	public void buildFramesZ() {
		long t0 = System.currentTimeMillis();
		long totalFrameCount=0,savedFrameCount = 0;
		long pos = 0;
		Mat m = new Mat();
		boolean grab = videoCapture.read(m);
		while (grab) {
			long mspos2 = (long) (pos/videoCapture.getFPS()*1000); //calculating postiion in milliseconds, since opencv getter for ms doesn't work
			System.out.println("Grabbing at pos: "+pos+" ms: "+mspos2+" "
					+grab+" avi"+videoCapture.get(Videoio.CAP_PROP_POS_AVI_RATIO)+" double ms "
					+videoCapture.get(Videoio.CAP_PROP_POS_MSEC));
			totalFrameCount++;
			if (!IsCloseToBackground(m)) {
				Imgcodecs.imwrite(fullPath("frame".concat(String.valueOf(mspos2))),m);
				savedFrameCount++;
			}
//			Imgcodecs.imwrite("/Users/pps/dev/frames4/frame".concat(String.valueOf(mspos2)).concat(".png"), m);
			for (int i = 0; (grab && i<24);i++) {
				grab = videoCapture.read(m);
				pos++;
			}
		}
		long t1 = System.currentTimeMillis();
		System.out.println("=========");
		System.out.println("Total frame count: "+totalFrameCount+ " saved frames count: "+savedFrameCount);
		System.out.println("Processed in "+(t1-t0)/1000 +" s");
		System.out.println("=========");


	}
	
	private boolean IsCloseToBackground(Mat frame) {
		Mat fgMask = new Mat();
		Mat blur = new Mat();
		subtractor.apply(frame, fgMask); // subtract background from frame to get foreground mask
		Imgproc.medianBlur(fgMask, blur, 25); // rudimentary noise cancellation

		Scalar s = Core.sumElems(blur);
		System.out.println("sum="+s.val[0]);
		return s.val[0] < MASK_THRESHOLD;
		
	}
	
	

	
	private String fullPath(String name) {
		return frameFolder.getAbsolutePath().concat(File.separator)
				.concat(name).concat(IMG_EXTENSION);
	}
	

	
	public static void main(String args[]) throws Exception {
		
		FrameBuilder fb = new FrameBuilder(args[0], args[1]);
		fb.buildFramesZ();
		
	}

	
	
	
	
}
