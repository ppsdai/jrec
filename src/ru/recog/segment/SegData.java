package ru.recog.segment;

import java.util.*;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 *  
         Segmentation Data
         Input: Mat - which should be a plate Image.
         Output: Useful properties independent of algorithm,
         i.e. quasi library that can be used to construct different algorithms
 *
 * @version      
         1.00 19 September 2015  * @author          
         Alexey Denisov  */

public class SegData {

	private int[] projX;
	private double[] sobelXprojY;
	private List<Integer> localMinimums; 
	private List<Integer> localMaximums;
	private List<Integer> minDepth;
	private Mat originalM;
	private List<List<Integer>> cutPointsList;
	
	private int upperBound, lowerBound; //upper is smaller, as axis Y points downward
	
	
    /**  
    constructor takes only matrix *
    as a parameter */
	public SegData(Mat m, int ub, int lb){
		
		originalM = m.clone();
		setUpperBound(ub);
		setLowerBound(lb);
		this.calculateProjection();
		this.calculateLocalMaximums();
		this.calculateLocalMinimums();
		this.calculateMinDepth();
		//FIXME
		// need to add boundary checks on minimums and maximums calculation
	}
	
    /**  
    method generates cuts *
    that are stored in cutPointsList */
	public void generateCuts(int startPoint){
	
		// start to go forward
		
		
	}
	
	public int[] getProjection() {
		
	    return projX;
	}
	
	public double[] getSobelXProjection() {
		
	    return sobelXprojY;
	}
	
	
    /**  
    method calculates a projection *
    of gray scale array */
	public void calculateProjection() {

		projX = new int[originalM.cols()];
		Arrays.fill(projX, 0);
		for (int col = 0; col < originalM.cols(); col++)
			for (int row = upperBound; row <= lowerBound; row++)
				projX[col] += 255 - (int) originalM.get(row, col)[0];
	}
	
    /**  
    method calculates a projection *
    of SobleX onto axisY *
    of gray scale array */
	public void calculateSobelXProjectonY() {

		Mat sobelx = new Mat(originalM.size(), originalM.type());
		Imgproc.Sobel(originalM.clone(), sobelx, CvType.CV_32F, 1, 0);
		
		sobelXprojY = new double[sobelx.rows()];
		Arrays.fill(sobelXprojY, 0);
		for (int row = 0; row < sobelx.rows(); row++)
			for (int col = 0; col < sobelx.cols(); col++)
				sobelXprojY[row] = sobelXprojY[row] + Math.abs(sobelx.get(row, col)[0]);
	}
	
	  /**  
    method calculates a position of *
    left and right boundary of max Area in SobelX projection on axis Y*
    given the height of symbols */
	public void calculateUpperAndLowerBoundary( int symbolHeight) {
		
		// check if height of the input array is bigger than symbol height
		int arrLength = sobelXprojY.length;
		if (arrLength < symbolHeight) return;
		
		// calculate the summ starting from x=0
		double sumTemp = 0;
		for ( int i=0; i < symbolHeight; i++ )
			sumTemp += sobelXprojY[i];
		
		// calculate summ for other positions and take the max
		double maxSum = sumTemp;
		int xEnd = symbolHeight - 1;
		for ( int i=symbolHeight; i < arrLength; i++ ){
			sumTemp = sumTemp  + sobelXprojY[i] - sobelXprojY[i - symbolHeight];
			if (sumTemp > maxSum){
				maxSum = sumTemp;
				xEnd = i;
			}
		}
		
		// set new Y-borders
		setUpperBound(xEnd - symbolHeight);
		setLowerBound(xEnd);
	}
	
	public List<Integer> getLocalMinimums() {
			
	    return localMinimums;
	}
	/**  
    method calculates *
    list of local minimums */
	public void calculateLocalMinimums() {
	
	int projX_Length = projX.length;
	localMinimums = new ArrayList<Integer>();
		        		
	// check first point
	if (projX[1] < projX[2])
		localMinimums.add(1);

	// Look inside omitting edge points
	int IsPlato = 0; 
	
	for (int x = 2; x < (projX_Length - 2); x++)
	{
		if ((IsPlato == 0) && (projX[x + 1] > projX[x]) && (projX[x - 1] > projX[x]))
		{
			localMinimums.add(x);
		
		}
		if ( (IsPlato == 0) && (projX[x + 1] == projX[x]) && (projX[x - 1] > projX[x]))
		{
			IsPlato = 1;
			//XStart = x;
		}
		if ((IsPlato == 1) && (projX[x + 1] > projX[x]))
		{
			IsPlato = 0;
			localMinimums.add(x); // (int)((x + XStart) / 2);
			
		}

	}
	// check last point
	if (projX[projX_Length - 2] > projX[projX_Length - 1])
	{
		localMinimums.add(projX_Length-2); 
		
	}
		
	}
	
	
	public List<Integer> getLocalMaximums() {
		
	    return localMaximums;
	}
	
	/**  
    method calculates *
    list of local maximums *
	, adds maximums on first and last location */
	public void calculateLocalMaximums() {
		
		int projX_Length = projX.length;
		localMaximums = new ArrayList<Integer>();
		
        
		// first point is always zero
		{
			localMaximums.add(0);
			
		}
		// Look inside omitting edge points
		int IsPlato = 0;
		//int XStart;
		for (int x = 1; x < (projX_Length - 2); x++)
		{
			if ((IsPlato == 0) && (projX[x + 1] < projX[x]) && (projX[x - 1] <projX[x]) && (x!=1))
			{
				localMaximums.add(x);
				
			}
			if ((IsPlato == 0) && (projX[x + 1] == projX[x]) && (projX[x - 1] < projX[x]))
			{
				IsPlato = 1;
			//	XStart = x;
			}
			if ((IsPlato == 1) && (projX[x + 1] < projX[x]))
			{
				IsPlato = 0;
				localMaximums.add(x); //  (int)((x + XStart) / 2);
				//CountMax++;
			}
		}
		// last point is always (Length - 1)
		{
			localMaximums.add(projX_Length-1);
			
		}
		
	}
	
	
	/**  
    method calculates *
    minimums depth *
    = summ of difference up to the nearest local maximums */
	public void calculateMinDepth() {
	
	minDepth = new ArrayList<Integer>();
	
	// fills localMaximums with a last value so that it is one element
	// larger than localMinimums
	while (localMaximums.size() <= localMinimums.size())
	  localMaximums.add(localMaximums.get(localMaximums.size() - 1));
		
	for (int x = 0; x <  localMinimums.size() ; x++)
		minDepth.add( projX[localMaximums.get(x)] + projX[localMaximums.get(x + 1)]
			- 2 * projX[localMinimums.get(x)] );
		
	}

	/**  
    Higher placed line */
	public int getUpperBound() {
		return upperBound;
	}
	
	/**  
    Higher placed line */
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}
	
	/**  
    Lower placed line */
	public int getLowerBound() {
		return lowerBound;
	}
	
	/**  
    Lower placed line */
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	
	/**  
    method finds an intra symbols space *
    according to some method *
    */
	public int findStartPoint( int findMethod, double approxPos) {
	
		int pointStart; 
		int x;
		int x_Max;
		float ValueMax;
		int x_Start;
		
		switch (findMethod){
		case 0:  // old method of choosing the deepest minimumu
			
			pointStart = (int) Math.round(approxPos * originalM.cols());
			x = 1;
			while ((localMinimums.get(x) < pointStart) && x < localMinimums.size()  ) x++;

			 x_Max = x;
			 ValueMax = minDepth.get(x);
			 x_Start = x;
			for (x = 0; x < 5 && (x_Start - x > 0); x++) {
				if (ValueMax < minDepth.get(x_Start - x)) {
					ValueMax = minDepth.get(x_Start - x);
					x_Max = x_Start - x;
				}
			}
			return (x_Max);
			//break;
			
		case 1:  // choosing position by absolute minimum
			
			pointStart = (int) Math.round(approxPos * originalM.cols());
			x = 1;
			while ((localMinimums.get(x) < pointStart) && x < localMinimums.size()  ) x++;
			
			x_Max = x;
			ValueMax = projX[localMinimums.get(x)];
			x_Start = x;
			for (x = 0; x < 5 && (x_Start - x > 0); x++) {
				if (ValueMax > projX[localMinimums.get(x_Start - x)]) {            // actually looking for a minimum
					ValueMax = projX[localMinimums.get(x_Start - x)];
					x_Max = x_Start - x;
				}
			}
			return (x_Max);
			//break;
		
		case 2:  // choosing position between shapes
			 
			break;

		case 3:  // choosing position in minimums of sobel
	
			//Mat mTemp = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			//Imgproc.Sobel(mTemp.clone(), m, CvType.CV_32F, 0, 1);
			//Core.absdiff(m, Scalar.all(0), m);
            break;			
			
		default:
			break;
		
			
		}
		
		return 0;
		
	}
}
