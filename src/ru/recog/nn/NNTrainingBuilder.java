package ru.recog.nn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.Utils;
import ru.recog.feature.*;
import ru.recog.imgproc.*;
import ru.recog.segment.*;
import ru.recog.segment.SegmentationLog.SegmentationLogEntry;

public class NNTrainingBuilder {
	
//	public static int DIGITS = 15;
	
	private List<Character> characterSet; // = new ArrayList<String>();
	private MultipleFeatureExtractor mfx;
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public NNTrainingBuilder(List<Character> characterSet) {
		this.characterSet = characterSet;
		if (!Utils.FULL_CHARACTERS_SET.containsAll(characterSet)) 
			throw new IllegalArgumentException("Some characters from "+characterSet
					+ " are not in the full list of chars.");
		
//		mfx = new MultipleFeatureExtractor();
//		mfx.addExtractor(new AreaFeatureExtractor());
////		mfx.addExtractor(new EllipseFeatureExtractor());
//		mfx.addExtractor(new XProjectionFeatureExtractor());
//		mfx.addExtractor(new YProjectionFeatureExtractor());
//		mfx.addExtractor(new SymmetryFeatureExtractor());
////		mfx.addExtractor(new BinaryPixelFeatureExtractor(10, 20));
		
//		mfx = new MultipleFeatureExtractor();
//		mfx.addExtractor(new AreaFeatureExtractor());
//		mfx.addExtractor(new GravityGridFeatureExtractor(4, 7));
//		mfx.addExtractor(new SymmetryFeatureExtractor());
//		mfx.addExtractor(new EdgeIntersectionFeatureExtractor(3, 3));
		
		mfx = new MultipleFeatureExtractor();
		mfx.addExtractor(new TileGradientFeatureExtractor());
		
	}
	

	public static void main(String[] args) throws Exception {
		

//		processCharFolders("/Users/pps/segmented/NN", "/Users/pps/dev/NNTrain/newshit");
		
//		processFolders(args[0],args[1]);
//		NNTrainingBuilder trainBuilder = new NNTrainingBuilder(Utils.FULL_CHARACTERS_SET);
		
//		trainBuilder.buildTrainingAndTestingSet("/Users/pps/segmented/NN", "TGS.txt", "/Users/pps/segmented/NN");
		
		buildSegmentTraining("/Users/pps/dev/energy", "nrg", "/Users/pps/dev/test/frames", "/Users/pps/dev/seglog");
		
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
	
	public static void processCharFolders(String sourceFolder, String destFolder) {
		
		CompoundImageProcessor cip = new CompoundImageProcessor();
		cip.addImageProcessor(new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU));
//		cip.addImageProcessor(new ErodingDilator(Imgproc.MORPH_CROSS, new Size(3,3), 2));
		cip.addImageProcessor(new Cropper());
		
		System.out.println("Using CIP: "+cip.toString());
		
		File sourceDir = new File(sourceFolder);
		File destDir = new File(destFolder);
		if (!destDir.exists()) destDir.mkdir();
		System.out.println("Processing all chars from "+sourceDir.getAbsolutePath());
		System.out.println("Moving to "+destDir.getAbsolutePath());
//		for (int i = 0; i<Utils.FULL_CHARACTERS_SET.size(); i++) {
//			String charSource = new File(sourceDir, String.valueOf(i)).getAbsolutePath();
//			String charDest = new File(destFolder, String.valueOf(i)).getAbsolutePath();
//			processFolders(charSource, charDest,cip);
//		}
		for (char c : Utils.FULL_CHARACTERS_SET) {
			String charSource = new File(sourceDir, String.valueOf(c)).getAbsolutePath();
			String charDest = new File(destFolder, String.valueOf(c)).getAbsolutePath();
			processFolders(charSource, charDest,cip);
		}
		
	}
	
	
	
	static void processFolders(String sourceFolder, String destFolder, CompoundImageProcessor cip) {
		File sourceDir = new File(sourceFolder);
		File destDir = new File(destFolder);
		if (!destDir.exists()) destDir.mkdirs();
		System.out.println("from "+sourceDir.getAbsolutePath());
		System.out.println("to "+destDir.getAbsolutePath());
		
		
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
	
	
	static void processFolders(String sourceFolder, String destFolder) {
		File sourceDir = new File(sourceFolder);
		File destDir = new File(destFolder);
		if (!destDir.exists()) destDir.mkdirs();
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
		
//		final FilenameFilter testingFilenameFileter = new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				if (!ff.accept(dir, name)) return false;
//				int index = name.indexOf("(");
//				if (index == -1) index = name.indexOf(".");
//				int number;
//				try {
//					 number = Integer.valueOf(name.substring(0, index).trim());
//				} catch (NumberFormatException nfe) {
//					nfe.printStackTrace(System.out);
//					return false;
//				}
//				return (number >= 0 && number<=100);
//			}
//		};
//		FilenameFilter trainingFilenameFilter = new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				return ff.accept(dir, name) && !testingFilenameFileter.accept(dir,name);
//			}
//		};
		
		
		for (int characterIndex = 0; characterIndex<characterSet.size(); characterIndex++) {
			
			
			File digitDir = new File(sourceDir, 
//					String.valueOf(Utils.FULL_CHARACTERS_SET.indexOf(characterSet.get(characterIndex))));
					String.valueOf(characterSet.get(characterIndex)));


			
			//		for (char c : characterSet) {
//			File digitDir = new File(sourceDir, String.valueOf(c))
			
			//find all the files in char directory
			List<String> allFiles = new ArrayList<String>(Arrays.asList(digitDir.list())); // list of all images
			List<String> testFiles = new ArrayList<String>();
			//take 10% to be test files
			int TN = Math.round(allFiles.size()/10);
			Random random = new Random();
			for (int i = 0; i < TN; i++) {
				// find TN random indexes, remove them from allFiles and add to list of test images
				int randIndex = random.nextInt(allFiles.size());
				testFiles.add(allFiles.remove(randIndex)); 
			}
			
			
			for (String file : testFiles) {
				System.out.println(file);
				csvTestList.add(createCSVTrainSampleFromImage(Utils.fullPath(digitDir,file), characterIndex));
				csvTestFileList.add(file);
			}
			
			for (String file : allFiles) {
				System.out.println(file);
				csvTrainList.add(createCSVTrainSampleFromImage(Utils.fullPath(digitDir,file), characterIndex));
			}
			System.out.println("DO TRAIN FILES CONTAIN TEST FILES?:"+allFiles.containsAll(testFiles));
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
	
	public static void buildSegmentTraining(String trainFilesDestination, String suffix, String picRoot, String seglogFolder) 
		throws Exception {
		List<String> csvTestList = new ArrayList<String>();
		List<String> csvTestFileList = new ArrayList<String>();
		List<String> csvTrainList = new ArrayList<String>();

		String seglogFilename = SegmentationLog.properPath(seglogFolder, "seglog047.txt");
		File picDir = new File(picRoot, "processed047");
		for (SegmentationLogEntry sle : SegmentationLog
				.readSegmentationLog(seglogFilename)) {
			if ("SUCCESS".equals(sle.getResult()))
				csvTrainList.addAll(createCSVTrainSampleFromSLE(picDir,
						seglogFilename, sle));
		}

		seglogFilename = SegmentationLog.properPath(seglogFolder,
				"seglog050.txt");
		picDir = new File(picRoot, "processed050");
		for (SegmentationLogEntry sle : SegmentationLog
				.readSegmentationLog(seglogFilename)) {
			if ("SUCCESS".equals(sle.getResult()))

				csvTrainList.addAll(createCSVTrainSampleFromSLE(picDir,
						seglogFilename, sle));
		}

		Collections.shuffle(csvTrainList);

		seglogFilename = SegmentationLog.properPath(seglogFolder,
				"seglog049.txt");
		picDir = new File(picRoot, "processed049");
		for (SegmentationLogEntry sle : SegmentationLog
				.readSegmentationLog(seglogFilename)) {
			if ("SUCCESS".equals(sle.getResult())) {

				csvTestList.addAll(createCSVTrainSampleFromSLE(picDir,
						seglogFilename, sle));
				csvTestFileList.add(SegmentationLog.properPath(
						picDir.getAbsolutePath(), sle.getFilename()));
			}
		}
		
		
		System.out.println("Training: "+csvTrainList.size()+" Testing: "+csvTestList.size());
		String trainFilename = "train".concat(suffix);
		String testFilename = "test".concat(suffix);
		String testFilesFilename = "testFiles".concat(suffix);

		String infoFilename = "info".concat(suffix);
		
		stringsToFile(trainFilesDestination, trainFilename, csvTrainList);
		stringsToFile(trainFilesDestination, testFilename, csvTestList);
		stringsToFile(trainFilesDestination, testFilesFilename, csvTestFileList);
		
	}
	
	public static void stringsToFile(String destPath, String filename, List<String> strings) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(new File(destPath,filename)));
		for (String s : strings) 
			pw.println(s);
		pw.close();
	}
	
	private static String charFolder(Character c) {
		return String.valueOf(Utils.FULL_CHARACTERS_SET.indexOf(c));

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
	
	public static List<String> createCSVTrainSampleFromSLE(File picDir, String seglogFilename, SegmentationLogEntry sle) {
		
			
		String name = sle.getFilename().substring(
				sle.getFilename().lastIndexOf("\\") + 1);

		Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name),
				Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
//		Mat img = Imgcodecs.imread(sle.getFilename(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		SegmentationData data = new SegmentationData(m);
		if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
		
		Map<CutData, Double> cutMap = MarkovSegmentation.buildCuts(data, MarkovLD.getDefaultMLD());
		
		List<Integer> sleCuts = sle.getCuts();
//		double maxEnergy = 0;
//		for (CutData c: cutMap.keySet()) if (c.calcEnergy(data)>maxEnergy) maxEnergy = c.calcEnergy(data);
		
		List<String> samples = new ArrayList<String>();
		for (CutData cut : cutMap.keySet()) {
			double p = cutMap.get(cut);
			double e = cut.calcEnergy(data);
			String result = cut.isEqual(sleCuts)? "1" : "0";
			samples.add(String.valueOf(p).concat(",").concat(String.valueOf(e)).concat(",").concat(result));
		}
		return samples;

	}
	


}
