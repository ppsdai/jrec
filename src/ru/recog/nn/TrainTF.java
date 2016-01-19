package ru.recog.nn;

import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.Utils;
import ru.recog.imgproc.Resizer;

public class TrainTF {
	
	private List<Character> characterSet; // = new ArrayList<String>();
	
	private static Resizer rs = new Resizer(new Size(28,28));
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public TrainTF(List<Character> characterSet) {
		this.characterSet = characterSet;
		if (!Utils.FULL_CHARACTERS_SET.containsAll(characterSet)) 
			throw new IllegalArgumentException("Some characters from "+characterSet
					+ " are not in the full list of chars.");
		
	}
	
	public void buildTrainingAndTestingSet(String trainFilesDestination, String suffix, String source) throws IOException {
		
		
		List<String> csvTestList = new ArrayList<String>();
		List<String> csvTrainList = new ArrayList<String>();

		File sourceDir = new File(source);
		final FilenameFilter ff = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".bmp");
			}
		};
		
		for (int characterIndex = 0; characterIndex<characterSet.size(); characterIndex++) {

			File digitDir = new File(sourceDir, String.valueOf(characterSet.get(characterIndex)));
				
			//find all the files in char directory
			List<String> allFiles = new ArrayList<String>(Arrays.asList(digitDir.list(ff))); // list of all images
			List<String> testFiles = new ArrayList<String>();
			//take 10% to be test files
			int TN = Math.round(allFiles.size()/7);
			Random random = new Random();
			for (int i = 0; i < TN; i++) {
				// find TN random indexes, remove them from allFiles and add to list of test images
				int randIndex = random.nextInt(allFiles.size());
				testFiles.add(allFiles.remove(randIndex)); 
			}
			
			
			for (String file : testFiles) {
//				System.out.println(file);
				csvTestList.add(createTFTrainSample(Utils.fullPath(digitDir,file), characterIndex));
			}
			
			for (String file : allFiles) {
//				System.out.println(file);
				csvTrainList.add(createTFTrainSample(Utils.fullPath(digitDir,file), characterIndex));
			}
			System.out.println("DO TRAIN FILES CONTAIN TEST FILES?:"+allFiles.containsAll(testFiles));
		}
		
		
		System.out.println("Training: "+csvTrainList.size()+" Testing: "+csvTestList.size());
		String trainFilename = "train".concat(suffix);
		String testFilename = "test".concat(suffix);

		NNTrainingBuilder.stringsToFile(trainFilesDestination, trainFilename, csvTrainList);
		NNTrainingBuilder.stringsToFile(trainFilesDestination, testFilename, csvTestList);
		
	}

	
	public static String createTFTrainSample(String filenamePath, int charIndex) {
		
		StringBuilder sb = new StringBuilder();
		
		Mat m = Imgcodecs.imread(filenamePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat img = rs.processImage(m);
		
		sb.append(charIndex).append(","); //adding label
		
		for (int row = 0; row < img.rows(); row++)
			for (int col = 0; col < img.cols(); col++ )
				sb.append(String.valueOf(255-(int)img.get(row, col)[0])).append(",");

		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		TrainTF ttf = new TrainTF(Utils.NUMBERS_SET);
		
		if (args.length < 2) {
			System.out.println("Usage : train picSource trainFilesDestination");
			System.exit(0);
		}
		
		ttf.buildTrainingAndTestingSet(args[1], "data.txt", args[0]);
	}

}
