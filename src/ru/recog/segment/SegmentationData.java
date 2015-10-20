package ru.recog.segment;

import java.util.*;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;

/**
 * 
 Segmentation Data Input: Mat - which should be a plate Image. Output: Useful
 * properties independent of algorithm, i.e. quasi library that can be used to
 * construct different algorithms
 *
 * @version 1.00 19 September 2015 * @author Alexey Denisov
 */

public class SegmentationData {

	private int[] projX;
	private List<Integer> localMinimums;
	private List<Integer> localMaximums;
	private List<Integer> minDepth;
	private Mat originalM;

	private int upperBound, lowerBound, centerLine;

	/**
	 * constructor takes only matrix * as a parameter
	 */
	public SegmentationData(Mat m) {
		originalM = m.clone();
		calculateVerticalCrop();
		this.calculateProjection();
		this.calculateLocalMaximums();
		this.calculateLocalMinimums();
		// this.calculateMinDepth();
		// FIXME
		// need to add boundary checks on minimums and maximums calculation
	}

	void calculateVerticalCrop() {
		Mat m = originalM;
		int[] blackLength = new int[m.rows()];

		Mat b = ImageUtils.localbin(m.clone(), 0.6);

		for (int row = 0; row < b.rows(); row++) { // loop on y
			int col = 0;
			int maxBlackLength = 0;
			int countStart = 0;
			while (col < (b.cols() - 1)) { // looking inside a line
				if (b.get(row, col)[0] == 0) { // if it is black then start to
												// look for a line
					countStart = col;
					while ((!((b.get(row, col)[0] == 255) && (b.get(row,
							col + 1)[0] == 255))) && (col < (b.cols() - 1)))
						// checks whether it is a line of two white in a row
						col++;
					blackLength[row] = col - countStart;
					if (blackLength[row] > maxBlackLength)
						maxBlackLength = blackLength[row];
				} else
					col++; // else go to the next pixel
			}
			blackLength[row] = maxBlackLength;
		}

		Mat sobelx = new Mat(m.size(), m.type());
		Imgproc.Sobel(m.clone(), sobelx, CvType.CV_32F, 1, 0);

		double rowAvg = 0;
		double sumI = 0;
		double sqrI = 0;
		for (int row = 0; row < sobelx.rows(); row++) {
			double sobelI = 0;
			for (int col = 0; col < sobelx.cols(); col++)
				sobelI = sobelI + Math.abs(sobelx.get(row, col)[0]);
			rowAvg = rowAvg + row * sobelI;
			sqrI = sqrI + row * row * sobelI;
			sumI = sumI + sobelI;
		}
		rowAvg = rowAvg / sumI;
		sqrI = Math.sqrt(sqrI / sumI - rowAvg * rowAvg);

		int UpperPoint, LowerPoint;

		UpperPoint = (int) Math.floor(rowAvg);
		while ((UpperPoint > 0)
				&& (blackLength[UpperPoint] < 5 * Math.round(sqrI)))
			// 4
			UpperPoint--;

		LowerPoint = (int) Math.ceil(rowAvg);
		while ((LowerPoint < m.rows())
				&& (blackLength[LowerPoint] < 6 * Math.round(sqrI)))
			// 4
			LowerPoint++;

		upperBound = UpperPoint;
		lowerBound = LowerPoint;
		centerLine = (int) Math.floor(rowAvg);
	}


	public int[] getProjection() {
		return projX;
	}

	public int getCenterLine() {
		return centerLine;
	}

	/**
	 * method calculates a projection * of gray scale array
	 */
	public void calculateProjection() {

		projX = new int[originalM.cols()];
		Arrays.fill(projX, 0);
		for (int col = 0; col < originalM.cols(); col++)
			for (int row = upperBound; row <= lowerBound; row++)
				projX[col] += 255 - (int) originalM.get(row, col)[0];
	}

	public List<Integer> getLocalMinimums() {

		return localMinimums;
	}

	/**
	 * method calculates * list of local minimums
	 */
	public void calculateLocalMinimums() {

		int projX_Length = projX.length;
		localMinimums = new ArrayList<Integer>();

		// check first point
		if (projX[1] < projX[2])
			localMinimums.add(1);

		// Look inside omitting edge points
		int IsPlato = 0;

		for (int x = 2; x < (projX_Length - 2); x++) {
			if ((IsPlato == 0) && (projX[x + 1] > projX[x])
					&& (projX[x - 1] > projX[x])) {
				localMinimums.add(x);

			}
			if ((IsPlato == 0) && (projX[x + 1] == projX[x])
					&& (projX[x - 1] > projX[x])) {
				IsPlato = 1;
				// XStart = x;
			}
			if ((IsPlato == 1) && (projX[x + 1] > projX[x])) {
				IsPlato = 0;
				localMinimums.add(x); // (int)((x + XStart) / 2);

			}

		}
		// check last point
		if (projX[projX_Length - 2] > projX[projX_Length - 1]) {
			localMinimums.add(projX_Length - 2);

		}

	}

	public List<Integer> getLocalMaximums() {

		return localMaximums;
	}

	/**
	 * method calculates * list of local maximums * , adds maximums on first and
	 * last location
	 */
	public void calculateLocalMaximums() {

		int projX_Length = projX.length;
		localMaximums = new ArrayList<Integer>();

		// first point is always zero
		{
			localMaximums.add(0);

		}
		// Look inside omitting edge points
		int IsPlato = 0;
		// int XStart;
		for (int x = 1; x < (projX_Length - 2); x++) {
			if ((IsPlato == 0) && (projX[x + 1] < projX[x])
					&& (projX[x - 1] < projX[x]) && (x != 1)) {
				localMaximums.add(x);

			}
			if ((IsPlato == 0) && (projX[x + 1] == projX[x])
					&& (projX[x - 1] < projX[x])) {
				IsPlato = 1;
				// XStart = x;
			}
			if ((IsPlato == 1) && (projX[x + 1] < projX[x])) {
				IsPlato = 0;
				localMaximums.add(x); // (int)((x + XStart) / 2);
				// CountMax++;
			}
		}
		// last point is always (Length - 1)
		{
			localMaximums.add(projX_Length - 1);

		}

	}

	/**
	 * method calculates * minimums depth * = summ of difference up to the
	 * nearest local maximums
	 */
	public void calculateMinDepth() {

		minDepth = new ArrayList<Integer>();

		// fills localMaximums with a last value so that it is one element
		// larger than localMinimums
		while (localMaximums.size() <= localMinimums.size())
			localMaximums.add(localMaximums.get(localMaximums.size() - 1));

		for (int x = 0; x < localMinimums.size(); x++)
			minDepth.add(projX[localMaximums.get(x)]
					+ projX[localMaximums.get(x + 1)] - 2
					* projX[localMinimums.get(x)]);

	}

	/**
	 * Higher placed line
	 */
	public int getUpperBound() {
		return upperBound;
	}

	/**
	 * Higher placed line
	 */
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Lower placed line
	 */
	public int getLowerBound() {
		return lowerBound;
	}

	/**
	 * Lower placed line
	 */
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

}
