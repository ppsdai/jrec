package ru.recog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.imgproc.CompoundImageProcessor;
import ru.recog.imgproc.ImageProcessor;
import ru.recog.segment.SegmentationResult;

public class ImageUtils {
	
	public static final Scalar GREEN = new Scalar(0,255,0);
	public static final Scalar BLUE = new Scalar(255,0,0);
	public static final Scalar RED = new Scalar(0,0,255);

	
	
	public static Image concatImages(BufferedImage...images) {
		int maxwidth = 0;
		int height = 0;
		for (BufferedImage i : images) {
//			BufferedImage i = (BufferedImage)pp.getImage();
			if (i.getWidth()>maxwidth) maxwidth = i.getWidth();
			height = height+i.getHeight();
		}
		BufferedImage sumImage = new BufferedImage(maxwidth, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = sumImage.createGraphics();
		height = 0;
		for (BufferedImage i : images) {
//			BufferedImage i = (BufferedImage)pp.getImage();
			g2.drawImage(i, 0, height, Color.DARK_GRAY, null);
			height = height+i.getHeight();
		}
		g2.dispose();
		
		return sumImage;
	}
	
	public static Mat bin2color(Mat m) {
		Mat cvt = new Mat();
		Imgproc.cvtColor(m, cvt, Imgproc.COLOR_GRAY2RGB);
		return cvt;
	}
	
	public static Mat drawSegLines(Mat m, SegmentationResult result) {
		Mat cvt = bin2color(m);
		List<Integer> points = result.getCutPoints();
		for (int p : points)
			if (points.indexOf(p)==0)
				Imgproc.line(cvt, new Point(p, 0), new Point(p, cvt.rows()-1), RED);
			else if (points.indexOf(p)==points.size()-1)
				Imgproc.line(cvt, new Point(p, 0), new Point(p, cvt.rows()-1), BLUE);
			else
				Imgproc.line(cvt, new Point(p, 0), new Point(p, cvt.rows()-1), GREEN);

		
		Imgproc.line(cvt, new Point(0,result.getData().getUpperBound()), new Point(cvt.cols()-1, result.getData().getUpperBound()),
				new Scalar(0,255,0));
		Imgproc.line(cvt, new Point(0,result.getData().getLowerBound()), new Point(cvt.cols()-1, result.getData().getLowerBound()),
				new Scalar(0,255,0));
		return cvt;
	}
	
	public static Mat drawSegRectangles(Mat m, SegmentationResult result) {
		Mat cvt = bin2color(m);
		for (Rect r : result.getRevisedRectangles())
			Imgproc.rectangle(cvt, r.tl(), r.br(), GREEN);
		return cvt;
	}
	
	
	
	public static Mat crop(Mat m) {
	//		Mat cropped = new Mat(m.size(), CvType.CV_8UC1);
			int top =-1, left =-1, right = -1, bottom = -1; 
			for (int col = 0; col<m.cols() && left < 0; col++) {
				for (int row = 0; row < m.rows()&& left < 0; row++) {
					if (m.get(row, col)[0] > 0 )
						left = col;
				}
			}
			for (int col = m.cols()-1; col>=0 && right < 0; col--) {
				for (int row = 0; row < m.rows() && right < 0; row++) {
					if (m.get(row, col)[0] > 0 )
						right = col;
				}
			}
			for (int row = 0; row < m.rows() && top < 0; row ++)
				for (int col = 0; col < m.cols() && top < 0; col++)
					if (m.get(row, col)[0] > 0)
						top = row;
			
			for (int row = m.rows()-1; row >= 0 && bottom < 0; row --)
				for (int col = 0; col < m.cols() && bottom < 0; col++)
					if (m.get(row, col)[0] > 0)
						bottom = row;
			
//			System.out.println("top "+top+" bottom "+bottom+" left "+left+" right "+right);
			Mat cropped;
			if (top < 0 || bottom < 0 || left < 0 || right < 0) {
				System.err.println("top "+top+" bottom "+bottom+" left "+left+" right "+right);
				System.err.println("Could not crop, binarization ate it all, returning original one");
				cropped = m.clone();
			} else 
				cropped = m.submat(top, bottom+1, left, right+1);
			return cropped;
	}
	
	
	public static LabelFrame showProcessStages(String filename) {
		return showProcessStages(Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE));
	}
	
	public static LabelFrame showProcessStages(Mat m) {
		LabelFrame lf = new LabelFrame("process");
		
		Mat ib = new Mat(m.size(),CvType.CV_8UC1);
		Imgproc.threshold(m, ib, 80, 255, Imgproc.THRESH_BINARY_INV);
		Mat cropped = ImageUtils.crop(ib);
		Mat linear = new Mat(new Size(10,20), CvType.CV_8UC1);
		Imgproc.resize(cropped, linear, new Size(10,20), 0, 0, Imgproc.INTER_AREA);
		Mat lb = new Mat();
		Imgproc.threshold(linear, lb, 1, 255, Imgproc.THRESH_BINARY);
		
		lf.addImage(m, "orig", 3);
		lf.addImage(ib, "bin80", 3);
		lf.addImage(cropped, "crop", 3);
		lf.addImage(linear, "resized", 3);
		lf.addImage(lb,"2nd bin",3);
		
		return lf;

	}
	
	
	public static LabelFrame showProcessStages(CompoundImageProcessor processor, int scale) {
		LabelFrame lf = new LabelFrame("processing");
		
		lf.addImage(processor.getOriginalImage(), "original", scale);
		for (ImageProcessor ip : processor.getStages())
			lf.addImage(processor.getStageResult(ip), ip.toString(), scale);
		
		return lf;
	}

	public static Mat scale(Mat m, int w, int h, double threshhold) {
			
			Mat matrix = new Mat(m.rows()*h, m.cols()*w, CvType.CV_8UC1);
	//		int[][] matrix = //new byte[5][10];
	//			new int[m.cols()*w][m.rows()*h];
			
			for (int col =0; col < m.cols(); col++)
				for (int row =0; row < m.rows(); row++) {
					int val = m.get(row, col)[0]>0? 255 : 0;
					System.out.println("val "+val);
					for (int i = 0; i < w; i++)
						for (int j =0; j< h; j++) {
							matrix.put(h*row+j, w*col+i, val);
							System.out.println("putting into row "+row+"+j "+j+" col "+col+"+i"+i+" val "+val);
	//						matrix[col+i][row+j]=val;
						}
				}
			
			System.out.println("hw "+h+" "+w);
			System.out.println("matrix "+matrix.size());
			LabelFrame lf = new LabelFrame("GG");
			lf.addImage(matrix, "gg");
			lf.pack();
			lf.setVisible(true);
	
			Mat scaled = new Mat(h, w, CvType.CV_8UC1);
			for (int col = 0; col < w; col++) 
				for (int row = 0; row < h; row++) {
					long total = 0;
					for (int i = 0; i < m.cols(); i++)
						for (int j = 0; j < m.rows(); j++)
	//						total = total + matrix[col+i][row+j];
							total = total +(int)matrix.get(m.rows()*row+j, m.cols()*col+i)[0];
					System.out.println("total "+total / (m.cols() *m.rows()*255));
					int value = total / (m.cols() *m.rows()*255) > threshhold?
							255 : 0;
					scaled.put(row, col, value);
				}
			
			return scaled;
			
	}
	
	public static Mat process10x20(Mat m) {
		Mat ib = new Mat(m.size(),CvType.CV_8UC1);
		Imgproc.threshold(m, ib, 80, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
		Mat cropped = ImageUtils.crop(ib);
		Mat linear = new Mat(new Size(5,10), CvType.CV_8UC1);
		Imgproc.resize(cropped, linear, new Size(10,20), 0, 0, Imgproc.INTER_AREA);
		Mat lb = new Mat();
		Imgproc.threshold(linear, lb, 1, 255, Imgproc.THRESH_BINARY);
		return lb;
	}
	
	
	public static double contrastRMS(Mat om) {
		if (om.channels() > 1) {
			System.out.println("We only work with single channel images for now");
			return -1;
		}
		
		Mat m = om.clone();
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++)
				m.put(row, col, m.get(row, col)[0]/255);
		
		double mean = Core.mean(m).val[0];
		double sum = 0;
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++)
				sum+=Math.pow(m.get(row, col)[0]-mean, 2);
		return Math.sqrt(sum/m.rows()/m.cols());
	}
	
	public static Mat localbin(Mat m, double threshK) {
		//TODO remove copying from Mat and back to it
		
		
		int SizeX = m.cols();
		int SizeY = m.rows();
		int[][] CMatrix = new int[SizeX][SizeY];

		for (int x = 0; x < SizeX; x++)
			for (int y = 0; y < SizeY; y++)
//			  CMatrix[x][y] = src.at<uchar>(y, x);
				CMatrix[x][y] = (int)m.get(y, x)[0];


		int CenterLine = 1;
		int d = 5; // 5; // 8;
		
		
		 double k =  - 0.0;  // - 0.2
//		 double k2 = 0.1;
//		 int R = 40;
		 int y_smooth;

		//d = round( 0.75 * ISqr_av);
//		cout << " Size d = " << d;

		// variables for calculating mean white and mean black

		double W_1_av, WI_av, WISqr_av;
		double B_1_av, BI_av, BISqr_av;
		
		int[][] CMatrix_Out = new int[SizeX][SizeY];

		for (int x = 0; x < SizeX; x++)
			for (int y = 0; y < SizeY; y++)
				CMatrix_Out[x][y] = 0;

		for (int x = d; x < (SizeX - d); x++)
			for (int y = d; y < (SizeY - d); y++)
			{
				double _1_av = 0; double I_av = 0; double ISqr_av = 0;
				for (int i = (-d); i <= d; i++)
					for (int j = (-d); j <= d; j++)
					{
						y_smooth = CenterLine + (y - CenterLine) / 1; // 1.5;
						_1_av = _1_av + 1;
						I_av = I_av + CMatrix[x + i][y_smooth + j]; // try to count around centerline  CMatrix[x + i][y + j]
						ISqr_av = ISqr_av + CMatrix[x + i][y_smooth + j] * CMatrix[x + i][y_smooth + j]; //CMatrix[x + i][y + j] * CMatrix[x + i][y + j]
					}
				I_av = I_av / _1_av;
				ISqr_av = (ISqr_av / _1_av) - I_av * I_av;
				ISqr_av = Math.sqrt(ISqr_av);
				double local_threshhold = I_av + k * ISqr_av;

				//local_threshhold = I_av *( 1 + k2 * ( ISqr_av/R  - 1));
				// calculate mean values for W and B, and take threshhold inbetween
				// supposing that they both have the same gaussian distribution
				
				B_1_av = 0; BI_av = 0; BISqr_av = 0;
				W_1_av = 0; WI_av = 0; WISqr_av = 0;

				for (int i = (-d); i <= d; i++)
					for (int j = (-d); j <= d; j++)
					{
						y_smooth = CenterLine + (y - CenterLine) / 1; // 1.5;
						if (CMatrix[x + i][y_smooth + j] > local_threshhold)
						{
							W_1_av = W_1_av + 1;
							WI_av = WI_av + CMatrix[x + i][y_smooth + j]; // try to count around centerline  CMatrix[x + i][y + j]
							WISqr_av = WISqr_av + CMatrix[x + i][y_smooth + j] * CMatrix[x + i][y_smooth + j]; //CMatrix[x + i][y + j] * CMatrix[x + i][y + j]
						}
						else
						{
							B_1_av = B_1_av + 1;
							BI_av = BI_av + CMatrix[x + i][y_smooth + j]; // try to count around centerline  CMatrix[x + i][y + j]
							BISqr_av = BISqr_av + CMatrix[x + i][y_smooth + j] * CMatrix[x + i][y_smooth + j]; //CMatrix[x + i][y + j] * CMatrix[x + i][y + j]
						}

					}
				BI_av = BI_av / B_1_av;
				WI_av = WI_av / W_1_av;

				BISqr_av = (BISqr_av / B_1_av) - BI_av * BI_av;
				BISqr_av = Math.sqrt(BISqr_av);
				
				WISqr_av = (WISqr_av / W_1_av) - WI_av * WI_av;
				WISqr_av = Math.sqrt(WISqr_av);

				//local_threshhold = (BI_av + WI_av)/2;
				
				local_threshhold = BI_av + (WI_av - BI_av) * BISqr_av / (BISqr_av + WISqr_av) * threshK; // 1
				// zaplatka na malue variaziju intensivnosti
				//if (ISqr_av < 15) local_threshhold = 128;

			//		cout << " WI " << WI_av;
			//	    cout << " BI " << BI_av;
				// here the threshhold
				if (CMatrix[x][y] > local_threshhold) CMatrix_Out[x][y] = 0;
				else CMatrix_Out[x][y] = 255;

		}

		//for (x = 0; x < SizeX; x++)
		//	for (y = 0; y < SizeY; y++)
		//		src_out.at<uchar>(y, x) = (uchar)CMatrix_Out[x][y];


		Mat out = new Mat(m.size(), m.type());
		for (int x = 0; x < SizeX; x++)
			for (int y = 0; y < SizeY; y++)
//				src.at<uchar>(y, x) = (uchar)(CMatrix_Out[x][y]);
				out.put(y, x, CMatrix_Out[x][y]);
		
		return out;
		
	}
 	
	public static double binaryAreaNorm(Mat m) {
		return (double) Core.countNonZero(m)/ (m.cols()*m.rows());
	}
	
	public static Mat scaleUp(Mat m, int scaleFactor) {
//		System.out.println("scaling m "+m.cols()+"x"+m.rows());
		Mat sm = new Mat(m.rows()*scaleFactor, m.cols()*scaleFactor, m.type());
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++) {
//				double val = m.get(row, col)[0];
				double[] val = m.get(row, col);
				for (int srow = 0; srow < scaleFactor; srow++)
					for (int scol = 0; scol < scaleFactor; scol++) {
//						System.out.println("row "+row+" col "+col+" srow "+srow+" scol"
//								+scol+" tr "+row*scaleFactor+srow+ " tc "+col*scaleFactor+scol);
						sm.put(row*scaleFactor+srow, col*scaleFactor+scol, val);
					}
			}
		
		return sm;
				
	}
	
	public static List<Point> mat2PointList(Mat m) {
		//TODO assuming 1 channel mat binary image
		List<Point> points = new ArrayList<Point>();
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++)
				if (m.get(row, col)[0]>0)
					points.add(new Point((float) col, (float)row ));
		return points;
	}
	
	public static void main(String[] args) {
		
/*		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		LabelFrame lf = new LabelFrame("", true);
		Mat m = Imgcodecs.imread("/Users/pps/dev/detected/frame401.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		lf.addImage(m, "orig", 3);
		for (double k = 0.2; k < 1.6; k+=0.2) {
			Mat b = localbin(m, k);
			lf.addImage(b, String.valueOf(k), 3);
		}
		
		lf.pack();
		lf.setVisible(true);
		
		Mat tri = Mat.zeros(new Size(100,100), CvType.CV_8UC1);
		Imgproc.rectangle(tri, new Point(20,20), new Point(80,80), new Scalar(255,255,255));
		LabelFrame exp = new LabelFrame();
		exp.addImage(tri, "");
		exp.pack();
		exp.setVisible(true);
		
		List<MatOfPoint> l = new ArrayList<MatOfPoint>();
		Imgproc.findContours(tri, l, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		for (MatOfPoint mop : l)
			System.out.println(mop);
			
		*/

		
		
		
	}


	public static BufferedImage mat2Image(Mat m) {
	// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	// Fastest code
	// The output can be assigned either to a BufferedImage or to an Image
	
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( m.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels()*m.cols()*m.rows();
	    byte [] b = new byte[bufferSize];
	    m.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;
	
	}

	public static Rect getContourRect(MatOfPoint mop) {
		Point p1 = null, p2 = null;
		for (Point p : mop.toList()) {
			//TODO think about the cases when points have negative values
			if (p1 == null) {
				p1 = new Point(p.x, p.y);
				p2 = new Point(p.x, p.y);
			} else {
				if (p.x < p1.x) p1.x = p.x;
				if (p.y < p1.y) p1.y = p.y;
				if (p.x > p2.x) p2.x = p.x;
				if (p.y > p2.y) p2.y = p.y;
			}
		}
		return p1==null? new Rect(0,0,0,0) : new Rect(p1, p2);
		
	}
	
	//TODO for testing scale
	
//	Mat m44 = new Mat(4,4,CvType.CV_8UC1);
//	m44.put(0, 0, 1);
//	m44.put(0, 1, 1);
//	m44.put(0, 2, 1);
//	m44.put(0, 3, 1);
//	
//	m44.put(1, 0, 1);
//	m44.put(1, 1, 1);
//	m44.put(1, 2, 1);
//	m44.put(1, 3, 1);
//	
//	m44.put(2, 0, 0);
//	m44.put(2, 1, 0);
//	m44.put(2, 2, 0);
//	m44.put(2, 3, 0);
//	
//	m44.put(3, 0, 0);
//	m44.put(3, 1, 0);
//	m44.put(3, 2, 0);
//	m44.put(3, 3, 0);
//	
//	
//	Mat m22 = scale(m44, 2,2, 0.3);
//	
//	System.out.println(m22.get(0, 0)[0]);
//	System.out.println(m22.get(0, 1)[0]);
//	System.out.println(m22.get(1, 0)[0]);
//	System.out.println(m22.get(1, 1)[0]);

}
