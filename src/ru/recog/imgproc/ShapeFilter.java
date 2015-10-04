package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Rect;

public class ShapeFilter {
	
	private int nPointsMax, nPointsMin,  widthMax, widthMin, heightMax, heightMin;
	private double density;
	
	private static final ShapeFilter defaultShapeFilter =
			new ShapeFilter(0.3, 100, 10, 16, 3, 26, 6);
	
	
	public ShapeFilter(double density, int nPointsMax, int nPointsMin, 
			int widthMax, int widthMin, int heightMax, int heightMin){
		this.density = density;
		this.nPointsMax = nPointsMax;//configValues[0];
		this.nPointsMin = nPointsMin;
		this.widthMax = widthMax;
		this.widthMin = widthMin;
		this.heightMax = heightMax;
		this.heightMin = heightMin;
	}
	
	public boolean accept(BinShape shape) {
		if ((shape.getNPoint() > nPointsMax) || (shape.getNPoint() < nPointsMin)) return false;
		Rect tr = shape.getBoundingRect();
		if ((tr.width > widthMax) || (tr.width < widthMin)) return false;
		if ((tr.height > heightMax) || (tr.height < heightMin)) return false;
		if ( ( (double)shape.getNPoint()/(tr.width * tr.height)) < density ) return false; 
		return true;
	}
	
	public List<BinShape> filterList(List<BinShape> inputList) {
		List<BinShape> outputList = new ArrayList<BinShape>();
		for (BinShape shape : inputList)
			if (accept(shape)) outputList.add(shape);
		
		return outputList;
	}
	
	public static List<BinShape> defaultFilterList(List<BinShape> inputList) {
		return defaultShapeFilter.filterList(inputList);
	}

}
