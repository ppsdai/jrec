package ru.recog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.Videoio;

public class FrameBuilder {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	private File frameFolder;
	
	private BasicVideoCapture videoCapture;
	
	public FrameBuilder(String videoURL, String folderURL) throws IOException {
//		makeDir(videoURL);
		videoCapture = new BasicVideoCapture(videoURL);
		
	}
	
	private void makeDir(String folderURL) {
		frameFolder = new File(folderURL);
		if (!frameFolder.exists()) frameFolder.mkdir();
		else System.out.println("Warning. Folder exists already: "+frameFolder.getAbsolutePath());
	}
	
	
	public long buildFrames() {
		long t1 = System.currentTimeMillis();
		
		long ms = 0;
//		for (;;) {
			int frameCount = 0;
//			while (videoCapture.grab())
//		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("Took "+(t2-t1)+" ms to build.");
		return t2-t1;
	}
	
	
	public void buildFramesX() {
		long pos = 0;
		Mat m = new Mat();
		boolean grab = videoCapture.read(m);
		while (grab) {
			long mspos2 = (long) (pos/videoCapture.getFPS()*1000);
			System.out.println("Grabbing at pos: "+pos+" ms: "+mspos2+" "
					+grab+" avi"+videoCapture.get(Videoio.CAP_PROP_POS_AVI_RATIO)+" double ms "
					+videoCapture.get(Videoio.CAP_PROP_POS_MSEC));
			Imgcodecs.imwrite("/Users/pps/dev/frames4/frame".concat(String.valueOf(mspos2)).concat(".png"), m);
			for (int i = 0; (grab && i<24);i++) {
				grab = videoCapture.read(m);
				pos++;
			}
		}
	}
	
	private boolean absEquals(Mat m1, Mat m2) {
		Mat d = new Mat();
		Core.absdiff(m1, m2, d);
		Scalar s = Core.sumElems(d.mul(d));
		return Scalar.all(0).equals(s);
	}
	
	public void buildFramesY() {
		long pos = 0;
		long skipCount = 0;
		long totalSkipCount = 0;
		
		Mat frame0 = new Mat();
		Mat frame1 = new Mat();
		
		while (videoCapture.read(frame1)) {
			if (pos == 0) {
				frame0 = frame1.clone();
				saveFrame(frame1,pos);
			}
			if (!absEquals(frame1,frame0)) {
				saveFrame(frame1,pos);
				frame0 = frame1.clone();
				totalSkipCount+=skipCount;
				System.out.println("Skipped "+skipCount);
				skipCount = 0;
			} else
				skipCount++;
			pos++;
		}
		System.out.println("Total skip count: "+totalSkipCount);
	}
	
	
	private void saveFrame(Mat frame, long pos) {
		String name = "/Users/pps/dev/frames3/frame".concat(String.valueOf(pos)).concat(".png");
		Imgcodecs.imwrite(name, frame);
		System.out.println("Saving "+name);
	}
	
	private static void saveFrame(Mat m, String fullname) {
		Imgcodecs.imwrite(fullname, m);
	}
	
	private static String getFullPath(File dir, String name) {
		return dir.getAbsolutePath().concat(File.separator).concat(name);
	}
	
	private void purgeFrames() {
		File f = new File ("/Users/pps/dev/frames4");
		List<String> files = Arrays.asList(f.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith("m.png");
			}
		}));
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				long n1 = Long.valueOf(o1.substring(5,o1.indexOf("m.png")));
				long n2 = Long.valueOf(o2.substring(5,o2.indexOf("m.png")));
				return n1==n2? 0 : n1-n2 < 0? -1 : 1;
			}

		});
		
		Mat frame = new Mat();
		Mat mask = new Mat();
		for (String s : files) {
			frame = Imgcodecs.imread(FrameBuilder.getFullPath(f, s));
			Imgproc.medianBlur(frame, mask, 25);
			System.out.println(s+" sum: "+Core.sumElems(mask));
		}
		
	}
	
	private void subtractBackground() {
//background subtraction
		
		File f = new File ("/Users/pps/dev/frames4");
		List<String> files = Arrays.asList(f.list());
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				long n1 = Long.valueOf(o1.substring(5,o1.indexOf(".png")));
				long n2 = Long.valueOf(o2.substring(5,o2.indexOf(".png")));
				return n1==n2? 0 : n1-n2 < 0? -1 : 1;
			}

		});
		BackgroundSubtractor bs = Video.createBackgroundSubtractorMOG2();
		
		Mat frame = new Mat();
		Mat mask = new Mat();
		for (String s : files) {
			System.out.println(s);
			frame = Imgcodecs.imread(FrameBuilder.getFullPath(f, s));
			bs.apply(frame, mask);
			String fullname = getFullPath(f, s.substring(0,s.indexOf(".png")).concat("m.png"));
			FrameBuilder.saveFrame(mask, fullname);
		}
	}
	
	public static void main(String args[]) throws Exception {
		
		File f = new File ("/Users/pps/dev/frames4");
		List<String> files = Arrays.asList(f.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith("m.png");
			}
		}));
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				long n1 = Long.valueOf(o1.substring(5,o1.indexOf("m.png")));
				long n2 = Long.valueOf(o2.substring(5,o2.indexOf("m.png")));
				return n1==n2? 0 : n1-n2 < 0? -1 : 1;
			}

		});
		
		Mat frame = new Mat();
		Mat mask = new Mat();
		for (String s : files) {
			frame = Imgcodecs.imread(FrameBuilder.getFullPath(f, s));
			Imgproc.medianBlur(frame, mask, 25);
			System.out.println(s+" sum: "+Core.sumElems(mask));
		}
		
		
		/* background subtraction
		
		File f = new File ("/Users/pps/dev/frames4");
		List<String> files = Arrays.asList(f.list());
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				long n1 = Long.valueOf(o1.substring(5,o1.indexOf(".png")));
				long n2 = Long.valueOf(o2.substring(5,o2.indexOf(".png")));
				return n1==n2? 0 : n1-n2 < 0? -1 : 1;
			}

		});
		BackgroundSubtractor bs = Video.createBackgroundSubtractorMOG2();
		
		Mat frame = new Mat();
		Mat mask = new Mat();
		for (String s : files) {
			System.out.println(s);
			frame = Imgcodecs.imread(FrameBuilder.getFullPath(f, s));
			bs.apply(frame, mask);
			String fullname = getFullPath(f, s.substring(0,s.indexOf(".png")).concat("m.png"));
			FrameBuilder.saveFrame(mask, fullname);
		}
		*/
		
		
		
		
		
		
		/*
		 *     //read the first file of the sequence
    frame = imread(fistFrameFilename);
    if(frame.empty()){
        //error in opening the first image
        cerr << "Unable to open first image frame: " << fistFrameFilename << endl;
        exit(EXIT_FAILURE);
    }
    //current image filename
    string fn(fistFrameFilename);
    //read input data. ESC or 'q' for quitting
    while( (char)keyboard != 'q' && (char)keyboard != 27 ){
        //update the background model
        pMOG2->apply(frame, fgMaskMOG2);
        //get the frame number and write it on the current frame
        size_t index = fn.find_last_of("/");
        if(index == string::npos) {
            index = fn.find_last_of("\\");
        }
        size_t index2 = fn.find_last_of(".");
        string prefix = fn.substr(0,index+1);
        string suffix = fn.substr(index2);
        string frameNumberString = fn.substr(index+1, index2-index-1);
        istringstream iss(frameNumberString);
        int frameNumber = 0;
        iss >> frameNumber;
        rectangle(frame, cv::Point(10, 2), cv::Point(100,20),
                  cv::Scalar(255,255,255), -1);
        putText(frame, frameNumberString.c_str(), cv::Point(15, 15),
                FONT_HERSHEY_SIMPLEX, 0.5 , cv::Scalar(0,0,0));
        //show the current frame and the fg masks
        imshow("Frame", frame);
        imshow("FG Mask MOG 2", fgMaskMOG2);
        //get the input from the keyboard
        keyboard = waitKey( 0 );
        //search for the next image in the sequence
        ostringstream oss;
        oss << (frameNumber + 1);
        string nextFrameNumberString = oss.str();
        string nextFrameFilename = prefix + nextFrameNumberString + suffix;
        //read the next frame
        frame = imread(nextFrameFilename);
        if(frame.empty()){
            //error in opening the next image in the sequence
            cerr << "Unable to open image frame: " << nextFrameFilename << endl;
            exit(EXIT_FAILURE);
        }
        //update the path of the current frame
        fn.assign(nextFrameFilename);
    }
		 * */
		 
		

		
//		File f = new File ("/Users/pps/dev/frames4");
//		List<String> files = Arrays.asList(f.list());
//		Collections.sort(files, new Comparator<String>() {
//			@Override
//			public int compare(String o1, String o2) {
//				// TODO Auto-generated method stub
//				long n1 = Long.valueOf(o1.substring(5,o1.indexOf(".png")));
//				long n2 = Long.valueOf(o2.substring(5,o2.indexOf(".png")));
//				return n1==n2? 0 : n1-n2 < 0? -1 : 1;
//			}
//
//		});
//		for (String s : files) {
//			System.out.println(s);
//		}
			
			
//		for (String s : f.list())
//			System.out.println(s);
		
//		BasicVideoCapture vc = new BasicVideoCapture("/Users/pps/dev/vid/video-000.avi");
//		BasicVideoCapture.printVCInfo(vc, System.out);
//		
//		FrameBuilder fb = new FrameBuilder("/Users/pps/dev/vid/video-000.avi", "");
//		fb.buildFramesX();


		
//		
//		JFrame frame = new JFrame();
//		ImageIcon ii = new ImageIcon();
//		frame.getContentPane().add(new JLabel(ii));
//		frame.pack();
//		frame.setVisible(true);		
		
		
	}

	
	
	
	
}
