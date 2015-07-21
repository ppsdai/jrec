package ru.recog.nn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.feature.*;
import ru.recog.imgproc.*;

public class NNTrainingBuilder {
	
//	public static int DIGITS = 15;
	
	public static final List<Character> FULL_CHARACTERS_SET
		= Arrays.asList('0','1','2','3','4','5','6','7','8','9', 
				'A', 'B', 'C', 'E', 'H', 'K', 'M', 'P', 'T', 'X', 'Y');
	
	
	public static final FilenameFilter FILTER_BMP = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".bmp");
		}
	};

	
	private List<Character> characterSet; // = new ArrayList<String>();
	private MultipleFeatureExtractor mfx;
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public NNTrainingBuilder(List<Character> characterSet) {
		this.characterSet = characterSet;
		if (!FULL_CHARACTERS_SET.containsAll(characterSet)) 
			throw new IllegalArgumentException("Some characters from "+characterSet
					+ " are not in the full list of chars.");
		
		mfx = new MultipleFeatureExtractor();
		mfx.addExtractor(new AreaFeatureExtractor());
//		mfx.addExtractor(new EllipseFeatureExtractor());
		mfx.addExtractor(new XProjectionFeatureExtractor());
		mfx.addExtractor(new YProjectionFeatureExtractor());
		mfx.addExtractor(new SymmetryFeatureExtractor());
//		mfx.addExtractor(new BinaryPixelFeatureExtractor(10, 20));
		
	}
	

	public static void main(String[] args) throws Exception {
		

//		processFolders(args[0],args[1]);
		NNTrainingBuilder trainBuilder = new NNTrainingBuilder(
				Arrays.asList('0','1','2','3','4','5','6','7','8','9' 
						 ));
		trainBuilder.buildTrainingAndTestingSet("/Users/pps/dev/NNTrain/full1020", "digits.txt", "/Users/pps/dev/NNTrain/full1020");
		
	}
	
	static void copy(String digit) throws IOException {
		File source = new File("/Users/pps/dev/".concat(digit));
		File check = new File("/Users/pps/dev/NNTrain/".concat(digit));
		List<String> sourcelist = Arrays.asList(source.list());
		List<String> checklist = Arrays.asList(check.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".bmp");
			}
		}));
		
		File dest = new File("/Users/pps/dev/NNTrain/samples/".concat(digit));

		System.out.println(sourcelist.size()+" check "+checklist.size());
		int c = 0;
		for (String s : sourcelist)
			if (checklist.contains(s)) {
				c++;
				File tocopy = new File(source, s);
				File d = new File(dest, s);
				Files.copy(tocopy.toPath(),d.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		System.out.println("c "+c);

	}

	
	static void processFolders(String sourceFolder, String destFolder) {
		File sourceDir = new File(sourceFolder);
		File destDir = new File(destFolder);
		if (!destDir.exists()) destDir.mkdir();
		System.out.println("from "+sourceDir.getAbsolutePath());
		System.out.println("to "+destDir.getAbsolutePath());
		
		CompoundImageProcessor cip = new CompoundImageProcessor();
		cip.addImageProcessor(new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU));
		cip.addImageProcessor(new ErodingDilator(Imgproc.MORPH_CROSS, new Size(3,3), 2));
		cip.addImageProcessor(new Cropper());
		cip.addImageProcessor(new Resizer(new Size(10,20)));
		cip.addImageProcessor(new Binarization(25,255,Imgproc.THRESH_BINARY));
		
		Mat m = new Mat();
		FilenameFilter bmp = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".bmp");
			}
		};
		List<File> files = Arrays.asList(sourceDir.listFiles(bmp));
		for (File f : files) {
			System.out.println("reading from"+f.getAbsolutePath());

			m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat pm = cip.processImage(m);

			String filename = destDir.getAbsolutePath().concat(File.separator).concat(f.getName());
			System.out.println("writing to "+filename);
			Imgcodecs.imwrite(filename, pm);

			
		}
	}
	
	
	public void buildTrainingAndTestingSet(String trainFilesDestination, String suffix, String source) throws IOException {
		
		System.out.println(mfx.getDimension());

		
		List<String> csvTestList = new ArrayList<String>();
		List<String> csvTestFileList = new ArrayList<String>();
		List<String> csvTrainList = new ArrayList<String>();

		File sourceDir = new File(source);
		final FilenameFilter ff = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".bmp");
			}
		};
		final FilenameFilter testingFilenameFileter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (!ff.accept(dir, name)) return false;
				int index = name.indexOf("(");
				if (index == -1) index = name.indexOf(".");
				int number;
				try {
					 number = Integer.valueOf(name.substring(0, index).trim());
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace(System.out);
					return false;
				}
				return (number >= 0 && number<=100);
			}
		};
		FilenameFilter trainingFilenameFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return ff.accept(dir, name) && !testingFilenameFileter.accept(dir,name);
			}
		};
		
		
		for (int characterIndex = 0; characterIndex<characterSet.size(); characterIndex++) {
			File digitDir = new File(sourceDir, 
					String.valueOf(FULL_CHARACTERS_SET.indexOf(characterSet.get(characterIndex))));
			
			for (String file : digitDir.list(testingFilenameFileter)) {
				System.out.println(file);
				csvTestList.add(createCSVTrainSampleFromImage(fullPath(digitDir,file), characterIndex));
				csvTestFileList.add(file);
			}
			
			for (String file : digitDir.list(trainingFilenameFilter)) {
				System.out.println(file);
				csvTrainList.add(createCSVTrainSampleFromImage(fullPath(digitDir,file), characterIndex));
			}
		}
		
		
		Collections.shuffle(csvTrainList);
		System.out.println("Training: "+csvTrainList.size()+" Testing: "+csvTestList.size());
		String trainFilename = "train".concat(suffix);
		String testFilename = "test".concat(suffix);
		String testFilesFilename = "testFiles".concat(suffix);

		String infoFilename = "info".concat(suffix);
		
		stringsToFile(trainFilesDestination, trainFilename, csvTrainList);
		stringsToFile(trainFilesDestination, testFilename, csvTestList);
		stringsToFile(trainFilesDestination, testFilesFilename, csvTestFileList);
		
		List<String> description = new ArrayList<String>();
		description.add("Characters: "+characterSet);
		description.add("Total Features: "+mfx.getDimension()+" Outputs: "+characterSet.size());
		for (FeatureExtractor fex : mfx.getFeatureExtractors())
			description.add(fex.toString());
		
		stringsToFile(trainFilesDestination, infoFilename, description);
		
	}
	
	public static void stringsToFile(String destPath, String filename, List<String> strings) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(new File(destPath,filename)));
		for (String s : strings) 
			pw.println(s);
		pw.close();
	}
	
	public static String fullPath(File parent, String name) {
		return parent.getAbsolutePath().concat(File.separator)
				.concat(name);
	}
	
	private static String charFolder(Character c) {
		return String.valueOf(FULL_CHARACTERS_SET.indexOf(c));

	}
	
	
	public String createCSVTrainSampleFromImage(String filename, int characterIndex) {
		StringBuilder sb = new StringBuilder();
		
		Mat img = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
		for (double feature : mfx.extract(img)) 
			sb.append(feature).append(",");
		
		String[] responses = new String[characterSet.size()];
		Arrays.fill(responses, "0");
		responses[characterIndex]="1";
		for (String r : responses)
			sb.append(r).append(",");
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	


}
