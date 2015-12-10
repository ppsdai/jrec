package ru.recog.segment;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SegmentationTests {
	
	@BeforeClass
	private void setup() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="^Upper(.*)")
	public void testSegDataUpperBound() {
		Mat m = new Mat(10,10, CvType.CV_8UC1);
		new SegmentationData(m, -1, 9);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="^Lower(.*)")
	public void testSegDataLowerBound() {
		Mat m = new Mat(10,10, CvType.CV_8UC1);
		new SegmentationData(m, 0, 15);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="^MS.segment(.*)")
	public void testMarkovUseWidthArgumentsException() {
		Mat m = new Mat(10,10, CvType.CV_8UC1);
		SegmentationFactory.getMarkovSegmentation().segment(m, MarkovSegmentation.USE_WIDTH, 1, 2, 3, 4);
	}
	
	@Test
	public void testWeakBorders() {
		List<Integer> proper = Arrays.asList(new Integer[] {5, 10, 15, 20, 25});
		List<Integer> test1 = Arrays.asList(new Integer[] {5, 10, 15, 20, 25});
		Assert.assertTrue(SegmentationLog.weakBorderEquals(proper, test1), "Identical lists are not found equal");
		
		List<Integer> test2 = Arrays.asList(new Integer[] {3, 10, 15, 20, 27});
		Assert.assertTrue(SegmentationLog.weakBorderEquals(proper, test2), "Similar lists are not found equal");
		
		List<Integer> test3 = Arrays.asList(new Integer[] {6, 10, 15, 20, 23});
		Assert.assertTrue(SegmentationLog.weakBorderEquals(proper, test3), "Similar lists are not found equal");
		
		List<Integer> test4 = Arrays.asList(new Integer[] {1, 10, 15, 20, 23});
		Assert.assertTrue(!SegmentationLog.weakBorderEquals(proper, test4), "Different lists are found equal");
	}
	
}
