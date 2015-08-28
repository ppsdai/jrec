package ru.recog.video;
import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import ru.recog.Mat2Image;

public class VideoCap {
    
	static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();

    public VideoCap(){
        cap = new VideoCapture();
        cap.open(0);
    } 
    
    VideoCap(String s) {
    	if (!s.isEmpty())
    		cap = new VideoCapture(s);
    	else {
    		 cap = new VideoCapture();
    		 cap.open(0);
    	}
    }
 
    public BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
//        System.out.println(cap.get(Videoio.CAP_PROP_POS_FRAMES));
        return mat2Img.getImage(mat2Img.mat);
    }
}