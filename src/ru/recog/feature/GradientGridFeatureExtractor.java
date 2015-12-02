package ru.recog.feature;

import javax.xml.bind.annotation.XmlElement;

import org.opencv.core.Mat;

public abstract class GradientGridFeatureExtractor extends FeatureExtractor<Mat> {
	
	@XmlElement
	protected int width;
	@XmlElement
	protected int height;
	protected double[][] grid;
	
	
	public GradientGridFeatureExtractor(int width, int height) {
		this.width = width;
		this.height = height;
	}

	
	protected double[] gradientAtPoint(int i, int j) {
		double dx1 = grid[i][j] - grid[i+1][j];
		double dx2 = grid[i][j+1] - grid[i+1][j+1];
		double dy1 = grid[i][j] - grid[i][j+1];
		double dy2 = grid[i+1][j] - grid[i+1][j+1];
		
		double dx = dx1+dx2; double adx = Math.abs(dx1)+Math.abs(dx2);
		double dy = dy1+dy2; double ady = Math.abs(dy1)+Math.abs(dy2);
		
		double r = Math.sqrt(dx*dx + dy*dy + adx*adx + ady*ady);

		return (r == 0)? new double[] {0.0, 0.0, 0.0, 0.0}
						: new double[] {dx/r, dy/r, adx/r, ady/r};
	}
	
	protected double[][] fillGrid(Mat m) {
		grid = new double[width][height];
		
		int mw = m.cols();
		int mh = m.rows();
		double[][] fusion = new double[width*mw][height*mh];
		for (int row = 0; row < mh; row++)
			for (int col = 0; col < mw; col++) {
				//fill out square of width x height with particular value
				double val = m.get(row, col)[0];
				for (int i = 0; i < width; i++ )
					for (int j = 0; j < height; j++) {
						fusion[col*width+i][row*height+j] = val;
					}
			}
		
		for (int i = 0; i < width; i++) 
			for (int j = 0; j < height; j++) {
				double sum = 0;
				for (int ii = 0; ii < mw; ii++)
					for (int jj = 0; jj < mh; jj++) 
						sum = sum + fusion[i*mw+ii][j*mh+jj];
				grid[i][j] = sum/(mw*mh);
			}
		
		return grid;
	}
	
	@Override
	public String toString() {
		return "GradientGrid["+width+"x"+height+"]("+getDimension()+")";
	}

}
