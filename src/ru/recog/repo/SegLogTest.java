package ru.recog.repo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.recog.*;
import ru.recog.segment.*;

public class SegLogTest {
	
	@Test
	public void testSeglogsIntegrity() {
		for (String seglog : Repository.getSeglogList()) {
			SegLog sl = (SegLog)XML.fromXML(new File(Repository.seglogFolderFile, seglog));
			Assert.assertTrue(Repository.getVideoList().contains(sl.getVideoId()), "Video "+sl.getVideoId()+" doesn't exist in video folder");
			int count = 0;
			for (SegLogEntry sle : sl.getEntries()) {
				count++;
				Assert.assertTrue(new File(Repository.getPlateFolderFile(sl.getPlatesLocation()), sle.getFilename()).exists(), 
						"File "+sle.getFilename()+" doesn't exist in "+sl.getPlatesLocation());
			}
			System.out.println("Checked files: "+count);
		}
	}
	
	@Test
	public void testLegacy() {
		List<String> excludePlateFolders = Arrays.asList(new String [] {"051"}); //FIXME figure out what's up with 051 or redo it
		for (String seglog : Repository.getSeglogList()) {
			SegLog sl = (SegLog)XML.fromXML(new File(Repository.seglogFolderFile, seglog));
			if (!excludePlateFolders.contains(sl.getPlatesLocation()))
				testSegmenter(SegmentationFactory.getLegacySegmentation(), sl, false);
		}
	}
	
	public static void testSegmenter(Segmentation segmenter, SegLog seglog, boolean showErrors) 
			 {
		LabelFrame lf = null;
		if (showErrors) {
			lf = new LabelFrame(seglog.getPlatesLocation());
//			lf.pack();
//			lf.setVisible(true);
		}
		File picDir = Repository.getPlateFolderFile(seglog.getPlatesLocation());
		
		System.out.println("Checking "+seglog.getPlatesLocation()+" with "+segmenter);
		
		int total = 0;
		int wrong = 0;
		for (SegLogEntry entry : seglog.getEntries()) {
			if (entry.getResult() != ResultEnum.SUCCESS) continue;
			total++;
			
			String name = entry.getFilename();

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = segmenter.segment(m);
		
			boolean isValid = SegmentationLog.weakBorderTest(entry, sr.getCutPoints());
			if (!isValid) {
				if (showErrors) {
					lf.addImage(ImageUtils.drawSegLines(m, sr), "segmentation", 3);
					
					Mat c = ImageUtils.bin2color(m);
					for (Rect r : entry.getRectangles())
						Imgproc.rectangle(c, r.tl(), r.br(), new Scalar(0,255,0));
					lf.addImage(c, entry.toString(), 3);
				}
				wrong++;
			}
		}
		if (showErrors && wrong > 0) {
			lf.pack();
			lf.setVisible(true);
		}
		
		Assert.assertEquals(wrong, 0, "Seglog for "+seglog.getPlatesLocation()+" Total: "+total);
		System.out.println("Total: "+total+" wrong: "+wrong);

	}
	
	public static void main(String args[]) {
		testSegmenter(SegmentationFactory.getLegacySegmentation(), (SegLog)XML.fromXML(new File(Repository.seglogFolderFile, args[0])), true);
	}
	

}
