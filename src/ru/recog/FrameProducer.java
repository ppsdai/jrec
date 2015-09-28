package ru.recog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import ru.recog.video.BasicVideoCapture;

public class FrameProducer implements Runnable {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private BasicVideoCapture videoCapture;
	
	BackgroundSubtractor subtractor = Video.createBackgroundSubtractorMOG2();
	
	private static long MASK_THRESHOLD = 1000000;
	
	private BlockingQueue<Mat> queue;
	
	public FrameProducer(String videoURL, BlockingQueue<Mat> queue)  {
		videoCapture = new BasicVideoCapture(videoURL);
		this.queue = queue;
	}
	
	@Override
	public void run() {
		buildFramesZ();
	}
	
	
	public void buildFramesZ() {
		long t0 = System.currentTimeMillis();
		long totalFrameCount=0,savedFrameCount = 0;
		long pos = 0;
		Mat m = new Mat();//new Size(1280,720), CvType.CV_8UC1);
		boolean grab = videoCapture.read(m);
		while (grab) {
//			long mspos2 = (long) (pos/videoCapture.getFPS()*1000); //calculating postiion in milliseconds, since opencv getter for ms doesn't work
//			System.out.println("Grabbing at pos: "+pos+" ms: "+mspos2+" "
//					+grab+" avi"+videoCapture.get(Videoio.CAP_PROP_POS_AVI_RATIO)+" double ms "
//					+videoCapture.get(Videoio.CAP_PROP_POS_MSEC));
			totalFrameCount++;
			if (!IsCloseToBackground(m)) {
				Mat gray = new Mat(m.size(), CvType.CV_8UC1);
				Imgproc.cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);

				queue.add(gray);
				savedFrameCount++;
			}
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
		System.out.println(queue.size());


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
	
	
	public static void main(String args[]) throws Exception {
		BlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
		FrameProducer producer = new FrameProducer(args[0], queue);
		Thread t1 = new Thread(producer);
		t1.start();
	}

	
	
	
	
}

