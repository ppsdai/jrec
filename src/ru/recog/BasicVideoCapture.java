package ru.recog;

import java.io.PrintStream;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class BasicVideoCapture extends VideoCapture {
	
	public BasicVideoCapture(String filename) {
		super(filename);
	}
	//FIXME there might be issues converting doubles to longs like this
	
	
	//TODO this doesn't work for some reason, always returns 0
	public long getPositionMs() {
		return (long) get(Videoio.CAP_PROP_POS_MSEC);
	}
	
	public long getPositionFrames() { 
		return (long) get(Videoio.CAP_PROP_POS_FRAMES);
	}
	
	public double getFPS() {
		return get(Videoio.CAP_PROP_FPS);
	}
	
	public void setPositionFrames(long frame) {
		set(Videoio.CAP_PROP_POS_FRAMES, frame);
	}
	
	public double getPosiotionAviRatio() {
		return get(Videoio.CAP_PROP_POS_AVI_RATIO);

	}
	
	public long getTotalFrameCount() {
		return (long) get(Videoio.CAP_PROP_FRAME_COUNT);
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
}
