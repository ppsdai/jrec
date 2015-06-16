package ru.recog;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;

import org.opencv.imgcodecs.Imgcodecs;
class SimpleSample {

  static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

  public static void main(String[] args) {
    System.out.println("Welcome to OpenCV " + Core.VERSION);
    Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
    System.out.println("OpenCV Mat: " + m);
    Mat mr1 = m.row(1);
    mr1.setTo(new Scalar(1));
    Mat mc5 = m.col(5);
    mc5.setTo(new Scalar(5));
    System.out.println("OpenCV Mat data:\n" + m.dump());
    
    Mat src = Imgcodecs.imread("/Users/pps/dev/fruits.jpg");
    
    
  }

}
/*
/// Load the source image
src = imread( argv[1], 1 );

cout << argv[1];

waitKey(2000);

//  namedWindow( argv[1], WINDOW_AUTOSIZE );
imshow("Unprocessed Image1",src);

dst = src.clone();
GaussianBlur( src, dst, Size( 15, 15 ), 0, 0 );

//namedWindow( window_name2, WINDOW_AUTOSIZE );
imshow("Processed Image2",dst);

waitKey();
return 0;*/