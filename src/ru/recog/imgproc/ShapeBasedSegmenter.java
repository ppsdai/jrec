package ru.recog.imgproc;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;


/**
 *  
         Shape Based Segmenter.
         Input: Mat - which should be a plate Image.
         Output: Some shapes, which are ready for recognition.
         Segments a typical plate image into digits and symbols.
 *
 * @version      
         1.00 19 September 2015  * @author          
         Alexey Denisov  */

public class ShapeBasedSegmenter {
	
	
	private static List<Integer> numbCorr;
	
	
    /**  
    method for which this class was created *
    returns a final list for recognition    *
    plImg - plate Image should be grayScale */
	public static List<BinShape> getFinalShapes(Mat plImg){
		
		
		//List<BinShape> shapeList = new ArrayList<BinShape>();
		
		// binarise image
		Mat binImg = plImg.clone(); //ImageUtils.localbin(plImg.clone(), 0.6);
		// get all bin shapes
		List<BinShape> allShapeList = getAllShapes(binImg);
		
		
		
		return allShapeList;
	}
	
    /**  
    method returns all shapes on a binary image */
	public static List<BinShape> getAllShapes(Mat binImg){
		
	
		numbCorr = new ArrayList<Integer>();
		
		//cycle through binImg and fill an array with numbers
		int SizeX = binImg.cols();
		int SizeY = binImg.rows();
		int[][] CMatrix = new int[SizeX][SizeY];

		// makes a 0 1 array 1 - white
		for (int x = 0; x < SizeX; x++)
			for (int y = 0; y < SizeY; y++)
				CMatrix[x][y] = (int) ( binImg.get(y, x)[0] /255)  ;
		
		//printArrDebug(CMatrix, SizeX, SizeY);

		// put numbers into the array
		int currentNumber = 2;  // integer that we use to number components
		//List<Integer> numbCorr = new ArrayList<Integer>();
		numbCorr.add(0); numbCorr.add(1); // so that we fill it first with i
		
		boolean isFound;
		int valueFound=1;
		for (int y = 0; y < SizeY; y++)
		{   
			for (int x = 0; x < SizeX; x++)
			{
				// for CMatrix[x][y]
				if ( CMatrix[x][y] == 1 )
				{
					isFound = false;
					//checks up and left
					if ( (y >= 1) && (x >= 1) )
						if ( CMatrix[x - 1][y - 1] > 1 )
						{
							CMatrix[x][y] = CMatrix[x - 1][y - 1];
							valueFound = CMatrix[x - 1][y - 1];
							isFound = true;
						}
					//checks up
					if ( y >= 1 )
						if ( CMatrix[x][y - 1] > 1 )
						{
							if (!isFound)
							{
								CMatrix[x][y] = CMatrix[x][y - 1];
							    valueFound = CMatrix[x][y - 1];
							    isFound = true;
							}
							else // add to correspondence map the lowest first
							{
								//add
								int temp = CMatrix[x][y - 1]; 
								int min = Math.min(temp, valueFound);
								int max = Math.max(temp, valueFound);
								// first iteration
								//numbCorr.set(max, numbCorr.get(min));
								//second iteration
								/*if ( numbCorr.get(max) < min)
									numbCorr.set( min, numbCorr.get(max));
								else 
									numbCorr.set(max, numbCorr.get(min));*/
								
								//recursive
/*								if ( numbCorr.get(max) < min)
								{
									temp = max;
									max = min; 
								    min = numbCorr.get(temp);
								  	numbCorr.set( max, min);
								}
								else 
									numbCorr.set(max, numbCorr.get(min));*/
								
								
								//recursive substitution back to the beggining
								recursiveSubstitution(max, min);
								
							}
								
						}
					// checks left
					if ( x >= 1 )
						if ( CMatrix[x - 1][y] > 1 )
						{
							if (!isFound)
							{
								CMatrix[x][y] = CMatrix[x - 1][y];
							    valueFound = CMatrix[x - 1][y];
							    isFound = true;
							}
							else // add to correspondence map the lowest first
							{
								//add
								int temp = CMatrix[x - 1][y]; 
								int min = Math.min(temp, valueFound);
								int max = Math.max(temp, valueFound);
								// first iteration
								//numbCorr.set(max, numbCorr.get(min));
								// second iteration
/*								if ( numbCorr.get(max) < min)
									numbCorr.set( min, numbCorr.get(max));
								else 
									numbCorr.set(max, numbCorr.get(min));*/
								//recursive
/*								if ( numbCorr.get(max) < min)
								{
									temp = max;
									max = min; 
								    min = numbCorr.get(temp);
								  	numbCorr.set( max, min); //Call Function; //
								}
								else 
									numbCorr.set(max, numbCorr.get(min));*/
								
								//recursive substitution back to the beggining
								recursiveSubstitution(max, min);
								
							}
						}
					
					// checks right up
					if ( (y >= 1 ) && (x < (SizeX -1)) )
						if ( CMatrix[x + 1][y-1] > 1 )
						{
							if (!isFound)
							{
								CMatrix[x][y] = CMatrix[x +1][y-1];
							    valueFound = CMatrix[x + 1][y-1];
							    isFound = true;
							}
							else // add to correspondence map the lowest first
							{
								//add
								int temp = CMatrix[x + 1][y - 1]; 
								int min = Math.min(temp, valueFound);
								int max = Math.max(temp, valueFound);
								
								//recursive substitution back to the beggining
								recursiveSubstitution(max, min);
								
							}
						}
					// if no marked elements to the right and to the left
					if (!isFound)
					{
						CMatrix[x][y] = currentNumber;
						numbCorr.add(currentNumber);
						currentNumber ++;
					}
				}
			
			}
		}
		
		//printArrDebug(CMatrix, SizeX, SizeY);
		
		//before sorting
		//System.out.println(numbCorr);
		
		// sort the map
		boolean isOnceReplaced = true;
		while (isOnceReplaced)
		{
			//System.out.println();
			isOnceReplaced = false;
			for (int i=0; i<numbCorr.size(); i++)
			{
				int pointsTo = numbCorr.get(i);
				int valuePointed = numbCorr.get(pointsTo);
				//System.out.print(" " + i + " " + pointsTo  + " " + valuePointed);
				if ( pointsTo != valuePointed)
				{
					isOnceReplaced = true;
					numbCorr.set(i, valuePointed);
				}
			}
		}

	
		//System.out.println();
		// after sorting
		System.out.println("sorted: " + numbCorr);
		

		
		// replace in array
		for (int y = 0; y < SizeY; y++)
			for (int x = 0; x < SizeX; x++)
				CMatrix[x][y] = numbCorr.get(CMatrix[x][y]);
		
		printArrDebug(CMatrix, SizeX, SizeY);
		
		//determine what numbers correspond to unique shapes
		Collections.sort(numbCorr);
		System.out.println("sorted: " + numbCorr);
		
		// Map numbers to shapes
		Map<Integer, BinShape> mapOfShapes = new HashMap<Integer, BinShape>();
		for (int i = 2; i < numbCorr.size(); i++)
			if (! mapOfShapes.containsKey(numbCorr.get(i)) )
				mapOfShapes.put(numbCorr.get(i), new BinShape());
	
		
		// go over array and calculate shapes properties
		for (int y = 0; y < SizeY; y++)
			for (int x = 0; x < SizeX; x++)
				if ( mapOfShapes.containsKey( CMatrix[x][y] ))
				{
					BinShape shp = mapOfShapes.get(CMatrix[x][y]);
					shp.addPoint(x, y);	
				}
		int nObjects = 0;
		for(int key:mapOfShapes.keySet()){
			System.out.println( " Shape N = " + (nObjects++));
			BinShape shp = mapOfShapes.get(key);
			System.out.println(" N oF Points = " + shp.getNPoint());
			System.out.println(" Bouinding Rect = " + shp.getBoundingRect());
		}
		
		// transform map to list
		List<BinShape> shapeList = new ArrayList<BinShape>(mapOfShapes.values());	
		return shapeList;
	}
	
    /**  
    This is a scary KOLDUNSTVO, a recursive method that looks through all branches
    of the tree and corrects so that numbers are in their proper places */
	private static void recursiveSubstitution(int max, int min){
	
	if ( numbCorr.get(max) < min)
	{
		int temp = max;
		max = min; 
	    min = numbCorr.get(temp);
	    recursiveSubstitution( max, min);
	}
	else 
	{
	    // correct previous if there is one
		if (( max > numbCorr.get(max)) && (numbCorr.get(max) > numbCorr.get(min)))
		{
			recursiveSubstitution( numbCorr.get(max), numbCorr.get(min));
		  
		}
		
		numbCorr.set(max, numbCorr.get(min));
	}
	}
	
    /**  
    method prints an array for Debug purposes */
	private static void printArrDebug (int[][] arr, int SizeX, int SizeY){
		
		System.out.println();
		for (int y = 0; y < SizeY; y++)
		{
		   System.out.println();
		   for (int x = 0; x < SizeX; x++)
			  if ( arr[x][y] <=9) 
		        System.out.print(" " + arr[x][y] + " ");
			  else
		        System.out.print(arr[x][y] + " ");
		}
		System.out.println();
	}
	

    
	public static void main(String[] args){
	
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		File dir = new File("c:\\dev\\TestImages"); 
		
		LabelFrame lf = new LabelFrame("GOOD", true);

		    String filestr = "46Bin.bmp"; // "4.bmp";  //"46Bin.bmp"; //"test.bmp";
			String filename = new File(dir, filestr).getAbsolutePath();
			
			System.out.println("File: " + filestr);
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			//Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			//Mat b = ImageUtils.localbin(m.clone(), 0.6);  //0.6		  
			
			//ShapeBasedSegmenter shBsSeg = new ShapeBasedSegmenter();
			//List<BinShape> shapes = getAllShapes(m);
			//ShapeBasedSegmenter.getFinalShapes(m);
			
	
			//System.out.println("N of Shapes = " + shapes.size() );

			
			
		
			
			lf.addImage(m, filestr, 5);			
			//lf.addImage(b, filestr, 5);			

	
		lf.pack();
		lf.setVisible(true);
		
       //Standard Tests
		//doStandardTests();
	
	}
	
	private static void doStandardTests(){
		
			if (test1Passed("/ShapeSegmenterTest1.bmp")) System.out.println("Test1 Passed");
			else System.out.println("Test1 FAIL");
			if (test2Passed("/ShapeSegmenterTest2.bmp")) System.out.println("Test2 Passed");
			else System.out.println("Test2 FAIL");
			if (test3Passed("/ShapeSegmenterTest3.bmp")) System.out.println("Test3 Passed");
			else System.out.println("Test3 FAIL");
			if (test4Passed("/ShapeSegmenterTest4.bmp")) System.out.println("Test4 Passed");
			else System.out.println("Test4 FAIL");
	}
	
    /**  
    does tests on a first Image */
	private static boolean test1Passed(String URL_NAME){
		
		boolean temp = true;
		
		    String str = Utils.URL2FString(System.class.getResource(URL_NAME)); 
			System.out.println("File: " + str);
			Mat m = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			List<BinShape> shapes = getAllShapes(m);
			if (shapes.size() != 1) temp = false;
			if (shapes.get(0).getNPoints() != 600) temp = false;
			
		return temp;
	}
    /**  
    does tests on a first Image */
	private static boolean test2Passed(String URL_NAME){
		
		boolean temp = true;
		
		    String str = Utils.URL2FString(System.class.getResource(URL_NAME)); 
			System.out.println("File: " + str);
			Mat m = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			List<BinShape> shapes = getAllShapes(m);
			if (shapes.size() != 0) temp = false;
			
			
		return temp;
	}
    /**  
    does tests on a first Image */
	private static boolean test3Passed(String URL_NAME){
		
		boolean temp = true;
		Rect testRect = new Rect(15, 3, 4, 10);
		
		    String str = Utils.URL2FString(System.class.getResource(URL_NAME)); 
			System.out.println("File: " + str);
			Mat m = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			List<BinShape> shapes = getAllShapes(m);
			if (shapes.size() != 6) temp = false;
			if (shapes.get(2).getNPoints() != 26) temp = false;
			if (!testRect.equals( shapes.get(2).getBoundingRect())) temp = false;
			
		return temp;
	}
    /**  
    does tests on a first Image */
	private static boolean test4Passed(String URL_NAME){
		
		boolean temp = true;
		Rect testRect = new Rect(9, 5, 47, 36);
		
		    String str = Utils.URL2FString(System.class.getResource(URL_NAME)); 
			System.out.println("File: " + str);
			Mat m = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			List<BinShape> shapes = getAllShapes(m);
			if (shapes.size() != 5) temp = false;
			if (shapes.get(0).getNPoints() != 338) temp = false;
			if (!testRect.equals( shapes.get(0).getBoundingRect())) temp = false;
			
		return temp;
	}
}


