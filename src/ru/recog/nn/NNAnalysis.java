package ru.recog.nn;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.LabelFrame;
import ru.recog.Utils;
import ru.recog.feature.*;
import ru.recog.ui.FrameProcessor;

public class NNAnalysis {

	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	private static final String OUTPUT_STRING = "Output:";
	private static final String DESIRED_STRING = "Desired output:";
	private static final String ERROR_STRING = "Error:";
	private static final String INPUT_STRING = "Input:";

	
	private static final double EPSILON = 0.02;



	public static void main(String[] args)  throws Exception {
		
		
		double d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 0;
		int avglength = 0;
		
		List<String> rects = readSeglog("/Users/pps/dev/seglog/seglog.txt");
		for (String s : rects) {
			System.out.println(string2rect(s));
			List<Rect> rrs = string2rect(s); 
			int length = rrs.get(5).x+rrs.get(5).width-rrs.get(0).x;
			avglength+=length;
			d1 = d1 + (double)(rrs.get(1).x-rrs.get(0).x)/length;
			d2 = d2 + (double)(rrs.get(2).x-rrs.get(0).x)/length;
			d3 = d3 + (double)(rrs.get(3).x-rrs.get(0).x)/length;
			d4 = d4 + (double)(rrs.get(4).x-rrs.get(0).x)/length;
			d5 = d5 + (double)(rrs.get(5).x-rrs.get(0).x)/length;
		}
		
		System.out.println("avg length: "+(double)avglength/rects.size());
		System.out.println("d1: "+(double)d1/rects.size());
		System.out.println("d2: "+(double)d2/rects.size());
		System.out.println("d3: "+(double)d3/rects.size());
		System.out.println("d4: "+(double)d4/rects.size());
		System.out.println("d5: "+(double)d5/rects.size());






		
//		readErrorFile("/Users/pps/AllSegmented/NN/50kerror72.txt", "testFilesTGS.txt");

		
//		checkCharFolder("/Users/pps/dev/NNTrain/NNet724021.nnet", "/Users/pps/symbols/3");
		
//		printAverageFeatures("/Users/pps/dev/NNTrain/goodshit");
//		List<FeatureExtractor> fexlist = new ArrayList<FeatureExtractor>();
//		fexlist.add(new AreaFeatureExtractor());
////		fexlist.add(new EllipseFeatureExtractor());
//		fexlist.add(new XProjectionFeatureExtractor());
//		fexlist.add(new YProjectionFeatureExtractor());
//		fexlist.add(new SymmetryFeatureExtractor());
//		MultipleFeatureExtractor mfx = new MultipleFeatureExtractor(fexlist);
//		
//		System.out.println(mfx);
		
		

	}
	
	
	public static void checkCharFolder(String nnPath, String sourceDir) {
		int c = 0;
		File sourceFile = new File(sourceDir);
		NNWrapper nn = new NNWrapper(nnPath, new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor()));
		for (String fn : sourceFile.list(Utils.FILTER_BMP_PNG)) {
			Mat m = Imgcodecs.imread(Utils.fullPath(sourceFile, fn));
			System.out.println(nnOutputToSymbol(nn.getNNOutputArray(m)));
			
		}
	}
	
	public static void printAverageFeatures(String pathToFullSetofData) {
		List<FeatureExtractor> fexlist = new ArrayList<FeatureExtractor>();
//		fexlist.add(new AreaFeatureExtractor());
//		fexlist.add(new EllipseFeatureExtractor());
//		fexlist.add(new XProjectionFeatureExtractor());
//		fexlist.add(new YProjectionFeatureExtractor());
//		fexlist.add(new SymmetryFeatureExtractor());
//		fexlist.add(new GravityCenterFeatureExtractor());
		fexlist.add(new EdgeIntersectionFeatureExtractor(3, 3));

		
		for (FeatureExtractor fex : fexlist) {
			
			System.out.println(fex.getClass()+" dimensions: "+fex.getDimension());

			for (int i = 0; i < Utils.FULL_CHARACTERS_SET.size(); i++) {
				List<List<Double>> lll = extractAverages(fex, pathToFullSetofData.concat(File.separator).concat(String.valueOf(i)));
		//		for (List<Double> list : lll)
		//			System.out.println(list);
				System.out.println("for character: "+getChar(i));
				System.out.println("min: "+lll.get(0));
				System.out.println("max: "+lll.get(1));
				System.out.println("avg: "+lll.get(2));
			}
		}
	}
	
	public static List<List<Double>> extractAverages(FeatureExtractor fex, String sourceFolder) {
		int dimension = fex.getDimension();
		List<Double> minimum = new ArrayList<Double>(), maximum =  new ArrayList<Double>(), average =  new ArrayList<Double>();
		File[] files = new File(sourceFolder).listFiles(Utils.FILTER_BMP);
		for (File file : files) {
			Mat m = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			List<Double> results = fex.extract(m);
			if (minimum.isEmpty())
				minimum.addAll(results);
			if (maximum.isEmpty())
				maximum.addAll(results);
			if (average.isEmpty())
				average.addAll(results);
			else
			for (int i = 0; i < results.size(); i++) {
				if (results.get(i) < minimum.get(i)) minimum.set(i, results.get(i));
				if (results.get(i) > maximum.get(i)) maximum.set(i, results.get(i));
				average.set(i, results.get(i)+average.get(i));
			}
		}
		for (int i = 0; i < dimension; i++)
			average.set(i, average.get(i)/files.length);
		List<List<Double>> returns = new ArrayList<List<Double>>();
		returns.add(minimum);
		returns.add(maximum);
		returns.add(average);
		return returns;
	}
	
	
	public static List<Double> parseToDouble(String stringOfDoubles) {
		List<Double> doubles = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(stringOfDoubles, ";");
		NumberFormat nf = NumberFormat.getInstance(); //work around, neuroph uses ',' as decimal separator
		while (st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			try {
				Number n = nf.parse(s);
				doubles.add(n.doubleValue());
			} catch (ParseException e) {
				System.err.println("Could not parse '"+s+"' in a string '"+stringOfDoubles+"'");
				e.printStackTrace();
			}
		}
		return doubles;
	}
	
	public static List<Integer> parseToInteger(String stringOfDoubles) {
		List<Integer> doubles = new ArrayList<Integer>();
		StringTokenizer st = new StringTokenizer(stringOfDoubles, ";");
		NumberFormat nf = NumberFormat.getInstance(); //work around, neuroph uses ',' as decimal separator
		while (st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			try {
				Number n = nf.parse(s);
				doubles.add(n.intValue());
			} catch (ParseException e) {
				System.err.println("Could not parse '"+s+"' in a string '"+stringOfDoubles+"'");
				e.printStackTrace();
			}
		}
		return doubles;
	}
	
	public static int readIndexFromOutput(String desired) {
		List<Double> nums = parseToDouble(desired);
		for (int i = 0; i < nums.size(); i++)
			if (nums.get(i)>0) return i;
		return -1;
	}
	
	public static List<String> readSeglog(String segLogPath) throws Exception {
		List<String> segLines = new ArrayList<String>();
		LineNumberReader lnr = new LineNumberReader(new FileReader(segLogPath));
		for (String line; (line = lnr.readLine()) != null;) {
			if (!line.endsWith(FrameProcessor.RFAULT) && !line.endsWith(FrameProcessor.SFAULT)) {
				int firstindex = line.indexOf(";");
				segLines.add(line.substring(line.indexOf(";",firstindex+1)+1));
			}
		}
		return segLines;
	}
	
	public static List<Rect> string2rect(String seglogString) throws Exception {
		List<Rect> rectangles = new ArrayList<Rect>();
		List<Integer> rects = parseToInteger(seglogString);
		if (rects.size() % 4 != 0) throw new IllegalArgumentException("amount of numbers should be dividable by 4");
		for (int i = 0; i < rects.size() / 4; i++) {
			int x = rects.get(i*4);
			int y = rects.get(i*4+1);
			int width  =rects.get(i*4+2);
			int height = rects.get(i*4+3);
			Rect r = new Rect(x, y, width, height);
			rectangles.add(r);
		}
		return rectangles;
		
	}
	
	
	public static void readErrorFile(String errorFile, String testFilesList) throws Exception {
		
		List<String> errorList = new ArrayList<String>();
		List<String> testFileList = getTestFilesListFromFile(
				new File (new File(errorFile).getParent(), testFilesList ).getAbsolutePath() );
		LineNumberReader lnr = new LineNumberReader(new FileReader(errorFile));
		for (String line; (line = lnr.readLine()) != null;)
			errorList.add(line);
		
		System.out.println(errorList.get(errorList.size()-1));
		errorList.remove(errorList.size()-1);
		lnr.close();
		
		System.out.println("Read lines: "+errorList.size());
		

				new NNWrapper("c:\\dev\\frames\\AllSegmented\\NN\\BSS724021.nnet",
						new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor()));
		
		
		
		int errorCount = 0;
		int smallErrorCount = 0; //in case when we have erros, but there is still >0.95 probability present
		
		System.out.println("Total entries in report: "+errorList.size()+" total test files: "
				+testFileList.size());
		LabelFrame lf = new LabelFrame("ZB", true);
		
//		Map<String,String> 
		for (int index = 0; index < errorList.size(); index++) {
			String line = errorList.get(index);
			String inputString = line.substring(line.indexOf(INPUT_STRING)+INPUT_STRING.length(), 
					line.indexOf(OUTPUT_STRING)).trim();
			String outputString = line.substring(line.indexOf(OUTPUT_STRING)+OUTPUT_STRING.length(), 
											line.indexOf(DESIRED_STRING)).trim();
			String desiredString = line.substring(line.indexOf(DESIRED_STRING)+DESIRED_STRING.length(), 
												line.indexOf(ERROR_STRING)).trim();
			String errorString = line.substring(line.indexOf(ERROR_STRING)+ERROR_STRING.length()).trim();
			
			
			List<Double> input = parseToDouble(inputString);
			List<Double> output = parseToDouble(outputString);
			List<Double> desired = parseToDouble(desiredString);
			List<Double> error = parseToDouble(errorString);
			
			if (checkErrors2(error, 0.05) != null) {
				
				
				errorCount++;
				for (double d : output) if (d > 0.94) { smallErrorCount++; break; }
				String errLabel = getChar(desiredString)+": "+output+" "+testFileList.get(index);
				System.out.println(errLabel);
				String imgFileName = new File(
						new File(errorFile).getParent() , String.valueOf(getChar(desiredString))
						).getAbsolutePath().concat(File.separator).concat(testFileList.get(index));
//				File f = new File(new File("/Users/pps/dev/NNTrain/full1020"), 
//						String.valueOf(readIndexFromOutput(desiredString)), testFileList.get(index));
				System.out.println(imgFileName);
				
				int desIndex = readIndexFromOutput(desiredString);
				
				String sss = getChar(desIndex)+" - "+output.get(desIndex);
				
//				List<Double> nnout = nn.getNNOutput(Imgcodecs.imread(imgFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE));
				
				// post processing work
				
				Mat m = Imgcodecs.imread(imgFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				
//				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

				
//				Imgproc.findContours(m.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
				
				
				//***
				
				
				lf.addImage(Imgcodecs.imread(imgFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE), sss+" !!! "+checkErrors2(error, 0.05)/*+" contours: "+contours.size()
						+" nn: "+checkErrors2(nnout,0.05)*/, 5);
				
//				Collections.sort(contours, Contours.RECT_COMPARATOR);
//				if (contours.size()>1 || (contours.size()==1 
//						&& Contours.getContourRect(contours.get(0)).height<m.rows()
//						&& Contours.getContourRect(contours.get(0)).width<m.cols()))
//				for (MatOfPoint mop : contours) {
//					Rect r = Contours.getContourRect(mop);
//					
//					if (r.height>=m.rows()/2 && r.width>=m.cols()/3) {
//						Mat cm = m.clone().submat(r.y,r.y+r.height+1,r.x, r.x+r.width+1);
//						List<Double> nnout = nn.getNNOutput(cm);
//						lf.addImage(cm, checkErrors2(nnout, 0.05),3);
//					}
//				}
				
				
			}

		}
		
		System.out.println("Total errors: "+errorCount);
		System.out.println("Small errors: "+smallErrorCount);
		int size = errorList.size();
		System.out.println("Total: "+size);

		double percentage = (double)(size-errorCount)/size;
		System.out.println("Correct percentage: "+percentage);
		
		System.out.println("Small error percentage: "+(double)(size-errorCount+smallErrorCount)/size);
		lf.pack();
		lf.setVisible(true);

	}
	
	public static char getChar(int index) {
		return Utils.FULL_CHARACTERS_SET.get(index);
	}
	
	public static char getChar(String output) {
		return Utils.FULL_CHARACTERS_SET.get(readIndexFromOutput(output));
	}
	
	public static String convertNNOutputToString(double[] nnoutput) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nnoutput.length; i++) 
			if (nnoutput[i] > EPSILON) sb.append(getChar(i))
				.append("(").append(String.format("%.3f", nnoutput[i])).append(") ");
		
		return sb.toString();
	}
	
	public static String nnOutputToSymbol(double[] nnoutput) {
		StringBuffer sb = new StringBuffer("[");
		boolean found = false;
		for (int i = 0; i < nnoutput.length; i ++) {
			if (nnoutput[i]> 0.9) {
				found = true;
				sb.append(getChar(i));
			}
		}
		if (!found) sb.append("*");
		sb.append("]");
		return sb.toString();
	}
	
	
	public static List<String> getTestFilesListFromFile(String testFile) throws Exception {
		List<String> listOfFiles = new ArrayList<String>();
		LineNumberReader lnr = new LineNumberReader(new FileReader(testFile));
		for (String line; (line = lnr.readLine()) != null;)
			listOfFiles.add(line);

		lnr.close();
		return listOfFiles;
	}
	
	public static boolean checkErrors(List<Double> errors, double epsilon) {
		for (double d : errors)
			if (d > epsilon) return false;
		return true;
	}
	
	//FIXME assumes full character set
	public static String checkErrors2(List<Double> errors, double epsilon) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < errors.size(); i++)
//		for (double d : errors)
			if (errors.get(i) > epsilon) {
				sb.append(Utils.FULL_CHARACTERS_SET.get(i)).append(" ").append(errors.get(i)).append("; ");
			}
		if (sb.length() > 0) return sb.toString();
		else return null;
	}

}
