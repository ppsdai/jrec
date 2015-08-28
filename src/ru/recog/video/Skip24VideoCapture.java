package ru.recog.video;

import org.opencv.core.Mat;

public class Skip24VideoCapture extends BasicVideoCapture {

	public Skip24VideoCapture(String filename) {
		super(filename);
	}
	
	
	public Mat nextFrame() {
		//FIXME properly handle cases when we run out of frames
		Mat m = new Mat();
		for (int i = 0; i < 24; i++)
			read(m);
		return m;
	}

}
