package ru.recog;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

public class Skip24VideoCap extends DetectingVideoCap {
	
	
    public Skip24VideoCap(String videoURL, String cascadeURL) {
		super(videoURL, cascadeURL);	
	}


	public BufferedImage getOneFrame() {
    	skip24frames();
    	return super.getOneFrame(); 
    }
    
    
    private void skip24frames() {
    	Mat m = new Mat();
    	//FIXME better syntax for loop, variable amount of skipping frames, return last frame if we run out of frames
    	for (int i = 0; i<24; i++)
    		cap.read(m);
    }

}
