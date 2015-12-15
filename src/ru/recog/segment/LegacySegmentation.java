package ru.recog.segment;

import java.util.*;

import org.opencv.core.Mat;

public class LegacySegmentation implements Segmentation {
	
	private static final int MAX_HOPS = 5; //cause great shaman told us so
	private static final int MAX_CHAR_WIDTH = 16;
	private static final int MIN_CHAR_WIDTH = 6;

	@Override
	public SegmentationResult segment(Mat m) {
		SegmentationData data = new SegmentationData(m);
		
		return segment(new SegmentationData(m));
		
/*		int[] mins = new int[data.getMinimums().size()];
		int[] maxs = new int[data.getMaximums().size()];
		for (int i=0; i < data.getMinimums().size(); i++)
			mins[i] = data.getMinimums().get(i);
		for (int i=0; i < data.getMaximums().size(); i++)
			maxs[i] = data.getMaximums().get(i);

		// Calculation of the minimums Depth = summ of difference up to the nearest local maximums

		int [] minD = new int[mins.length];
		
		int maxi = mins[0]<maxs[0]? -1 : 0;

		
		for (int i = 0; i < mins.length && maxi < maxs.length; i++, maxi++) {
//			System.out.println(i+" "+maxi);
			if (maxi==-1)
				minD[i] = data.getProjection()[maxs[0]]+data.getProjection()[0] - 2*data.getProjection()[mins[0]];
			else if (maxi>=maxs.length-1)
				minD[i] = data.getProjection()[maxs[maxi]]+data.getProjection()[data.getProjection().length-1] - 2*data.getProjection()[mins[i]];
			else
				minD[i] = data.getProjection()[maxs[maxi]]+data.getProjection()[maxs[maxi+1]] - 2*data.getProjection()[mins[i]];
		
		}

		int pointStart = (int) Math.round(0.55 * m.cols());
//		System.out.println("PS: "+pointStart);

		int x = 1;
		while (mins[x] < pointStart) x++;

		int x_Max = x;
		float ValueMax = minD[x];
		int x_Start = x;
		for (x = 0; x < MAX_HOPS && x_Start-x>=0; x++) {
			if (ValueMax < minD[x_Start - x]) {
				ValueMax = minD[x_Start - x];
				x_Max = x_Start - x;
			}
		}

		// going to beginning
		List<Integer> divPoints = new ArrayList<Integer>();
		int LengthEstimate;
		int diff1, diff2;

		LengthEstimate = Math.round(2 * (data.getLowerBound() - data.getUpperBound()) / 3);
		if (LengthEstimate < 9) LengthEstimate = 9; // if (LengthEstimate < 8) LengthEstimate = 8; //if (LengthEstimate < 9) LengthEstimate = 9;
		if (LengthEstimate > 14) LengthEstimate = 14;// if (LengthEstimate > 14) LengthEstimate = 14;

		x = x_Max;
		divPoints.add(mins[x_Max]);
		

		// Going back
		while (x > 1) {
			x--;
			diff1 = Math.abs((mins[x + 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs((mins[x + 1] - mins[x - 1]) - LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x - 1])

					diff2++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
				{
					divPoints.add(0, mins[x]);
					x = x + 0; // FIXME LOL?
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
				{
					divPoints.add(0, mins[x - 1]);
					x = x - 1;
				}

			}
		}

		// Going Forward
		x = x_Max;
		while (x < mins.length - 2) {
			x++;
			diff1 = Math.abs(Math.abs(mins[x - 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs(Math.abs(mins[x - 1] - mins[x + 1])
					- LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x + 1])

					diff2++;
				else
					diff1++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
				{
					divPoints.add(mins[x]);
					x = x + 0; // FIXME ROFLCOPTER
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
				{
					divPoints.add(mins[x + 1]);
					x = x + 1;
				}

			}
		}
		
		if (!divPoints.contains(0)) divPoints.add(0, 0);
		return new SegmentationResult(data, new CutData(divPoints)); */
		
	}

	@Override
	public SegmentationResult segment(Mat m,
			double... parameters) {
		return segment(m);
	}

	@Override
	public SegmentationResult segment(SegmentationData data) {
		int[] mins = new int[data.getMinimums().size()];
		int[] maxs = new int[data.getMaximums().size()];
		for (int i=0; i < data.getMinimums().size(); i++)
			mins[i] = data.getMinimums().get(i);
		for (int i=0; i < data.getMaximums().size(); i++)
			maxs[i] = data.getMaximums().get(i);

		// Calculation of the minimums Depth = summ of difference up to the nearest local maximums

		int [] minD = new int[mins.length];
		
		int maxi = mins[0]<maxs[0]? -1 : 0;

		
		for (int i = 0; i < mins.length && maxi < maxs.length; i++, maxi++) {
//			System.out.println(i+" "+maxi);
			if (maxi==-1)
				minD[i] = data.getProjection()[maxs[0]]+data.getProjection()[0] - 2*data.getProjection()[mins[0]];
			else if (maxi>=maxs.length-1)
				minD[i] = data.getProjection()[maxs[maxi]]+data.getProjection()[data.getProjection().length-1] - 2*data.getProjection()[mins[i]];
			else
				minD[i] = data.getProjection()[maxs[maxi]]+data.getProjection()[maxs[maxi+1]] - 2*data.getProjection()[mins[i]];
		
		}

		int pointStart = (int) Math.round(0.55 * data.getOriginalMat().cols());
//		System.out.println("PS: "+pointStart);

		int x = 1;
		while (mins[x] < pointStart) x++;

		int x_Max = x;
		float ValueMax = minD[x];
		int x_Start = x;
		for (x = 0; x < MAX_HOPS && x_Start-x>=0; x++) {
			if (ValueMax < minD[x_Start - x]) {
				ValueMax = minD[x_Start - x];
				x_Max = x_Start - x;
			}
		}

		// going to beginning
		List<Integer> divPoints = new ArrayList<Integer>();
		int LengthEstimate;
		int diff1, diff2;

		LengthEstimate = Math.round(2 * (data.getLowerBound() - data.getUpperBound()) / 3);
		if (LengthEstimate < 9) LengthEstimate = 9; // if (LengthEstimate < 8) LengthEstimate = 8; //if (LengthEstimate < 9) LengthEstimate = 9;
		if (LengthEstimate > 14) LengthEstimate = 14;// if (LengthEstimate > 14) LengthEstimate = 14;

		x = x_Max;
		divPoints.add(mins[x_Max]);
		

		// Going back
		while (x > 1) {
			x--;
			diff1 = Math.abs((mins[x + 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs((mins[x + 1] - mins[x - 1]) - LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x - 1])

					diff2++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
				{
					divPoints.add(0, mins[x]);
					x = x + 0; // FIXME LOL?
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
				{
					divPoints.add(0, mins[x - 1]);
					x = x - 1;
				}

			}
		}

		// Going Forward
		x = x_Max;
		while (x < mins.length - 2) {
			x++;
			diff1 = Math.abs(Math.abs(mins[x - 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs(Math.abs(mins[x - 1] - mins[x + 1])
					- LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x + 1])

					diff2++;
				else
					diff1++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
				{
					divPoints.add(mins[x]);
					x = x + 0; // FIXME ROFLCOPTER
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
						&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
				{
					divPoints.add(mins[x + 1]);
					x = x + 1;
				}

			}
		}
		
		if (!divPoints.contains(0)) divPoints.add(0, 0);
		return new SegmentationResult(data, new CutData(divPoints));

	}

	@Override
	public SegmentationResult segment(SegmentationData data,
			double... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
