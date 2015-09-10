package ru.recog.nn;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;
import ru.recog.feature.*;

public class NNAnalysis {

	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	private static final String OUTPUT_STRING = "Output:";
	private static final String DESIRED_STRING = "Desired output:";
	private static final String ERROR_STRING = "Error:";
	private static final String INPUT_STRING = "Input:";

	
	private static final double EPSILON = 0.02;



	public static void main(String[] args)  throws Exception {
		
		readErrorFile("/Users/pps/segmented/NN/errorGS.txt", "testFilesGS.txt");
		
		
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
	
	public static int readIndexFromOutput(String desired) {
		
		List<Double> nums = parseToDouble(desired);
		for (int i = 0; i < nums.size(); i++)
			if (nums.get(i)>0) return i;
		return -1;
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
		
		NNWrapper nn = new NNWrapper("/Users/pps/dev/NNTrain/goodshit/Net496021.nnet", 
				new MultipleFeatureExtractor(new AreaFeatureExtractor(),
						new GravityGridFeatureExtractor(10, 20),
						new SymmetryFeatureExtractor(),
						new EdgeIntersectionFeatureExtractor(3, 3)));
		
		
		
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
