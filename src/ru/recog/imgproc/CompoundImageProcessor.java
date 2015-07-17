package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;


public class CompoundImageProcessor implements ImageProcessor {
	
	private List<ImageProcessor> stages = new ArrayList<ImageProcessor>();
	private List<Mat> stageImages = new ArrayList<Mat>();
	private Mat originalImage;
	

	public CompoundImageProcessor() {
	}
	
	public void addImageProcessor(ImageProcessor proc) {
		stages.add(proc);
	}
	
	public List<ImageProcessor> getStages() {
		return stages;
	}
	
	public List<Mat> getStageImages() {
		return stageImages;
	}
	
	
	public Mat getStageResult(ImageProcessor processor) {
		return stageImages.get(stages.indexOf(processor));
	}

	@Override
	public Mat processImage(Mat m) {
		stageImages.clear();
		originalImage = m.clone();
		Mat pm = m.clone();
		
		for (ImageProcessor ip : stages) {
			pm = ip.processImage(pm);
			stageImages.add(pm.clone());
		}
		return pm;
	}

	
	public Mat getOriginalImage() {
		return originalImage;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ImageProcessor[");
		for (ImageProcessor ip : stages)
			sb.append(ip).append(",");
		sb.append("]");
		return sb.toString();
	}
}
