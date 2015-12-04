package ru.recog.repo;

import java.io.*;
import java.util.*;

import org.opencv.core.Core;

public class Repository {
	
	
	private static final String REPONAME = "repo";
	private static final String VIDEOFOLDER = "video";
	private static final String FRAMEFOLDER = "frames";
	private static final String NNFOLDER = "NN";

	

	
	private static File repoFile = null;
	private static File videoFolderFile = null;
	private static File frameFolderFile = null;
	private static File nnFolderFile = null;
	static File networksFolderFile = null;


	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		initialize();
	}
	
	
	
	private static void initialize() {
		File repoFile = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.class.getResourceAsStream("/repository.loc")));
			repoFile = new File(br.readLine());
		} catch (IOException e) {
			throw new IllegalStateException("Could not read repository location: "+e);
		}

		if (!checkDirFile(repoFile))
			throw new IllegalStateException("Could not find repo directory");
		videoFolderFile = new File(repoFile, VIDEOFOLDER);
		if (!checkDirFile(videoFolderFile))
			throw new IllegalStateException("Could not find video directory inside repository= "+repoFile.getAbsolutePath());
		frameFolderFile = new File(repoFile, FRAMEFOLDER);
		if (!checkDirFile(frameFolderFile))
			throw new IllegalStateException("Could not find frame directory inside repository= "+repoFile.getAbsolutePath());
		nnFolderFile = new File(repoFile, NNFOLDER);
		if (!checkDirFile(nnFolderFile))
			throw new IllegalStateException("Could not find NN directory inside repository= "+repoFile.getAbsolutePath());
		networksFolderFile = new File(nnFolderFile, "networks");
		if (!checkDirFile(networksFolderFile))
			throw new IllegalStateException("Could not find NN/networks directory inside repository= "+repoFile.getAbsolutePath());
	}
	
	public static String getPath() {
		return repoFile.getAbsolutePath();
	}
	
	
	public static List<String> getVideoList() {
		return Arrays.asList(videoFolderFile.list());
	}
	
	public static File getVideoFile(String videoName) {
		if (!getVideoList().contains(videoName))
			throw new IllegalArgumentException("Video "+videoName+" doesn't exist in video folder: "+videoFolderFile.getAbsolutePath());
		return new File(videoFolderFile, videoName);
	}
	
	
	public static String getVideoFilePath(String videoName) {
		return getVideoFile(videoName).getAbsolutePath();
	}
	
	public static File getFrameFolder(String videoName) {
		if (!getVideoList().contains(videoName))
			throw new IllegalArgumentException("Video "+videoName+" doesn't exist in video folder: "+videoFolderFile.getAbsolutePath());
		File frameFile = new File(frameFolderFile, videoName);
		if (!frameFile.exists()) frameFile.mkdir();
		return frameFile;
	}
	
	public static String getFrameFolderString(String videoName) {
		return getFrameFolder(videoName).getAbsolutePath();
	}
	
	public static List<File> getNetworkFiles() {
		File networksDir = new File(nnFolderFile, "networks");
		return Arrays.asList(networksDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		}));
	}
	
	public static File getNetworkFile(String id) {
		return new File(networksFolderFile, id);
	}
	
	
	public static void main(String args[] ) {
		Properties p = System.getProperties();
		for (String key : p.stringPropertyNames())
			System.out.println(key+"="+p.getProperty(key));
		
		for (String key : System.getenv().keySet())
			System.out.println(key+"="+System.getenv(key));

		
//		System.out.println(System.getProperties());
//		
//		System.out.println("repo: "+repoFile.getAbsolutePath());
//		System.out.println("video: "+videoFolderFile.getAbsolutePath());
//		System.out.println("frames: "+frameFolderFile.getAbsolutePath());
//
//		for (String s : getVideoList())
//			System.out.println(s);
//		
//		for (File f : getNetworkFiles())
//			System.out.println(f.getName());
//		
//		System.out.println(Core.getBuildInformation());
	}
	
	private static boolean checkDirFile(File dir) {
		return !(dir == null || !dir.exists() || !dir.isDirectory());
	}

}
