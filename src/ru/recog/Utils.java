package ru.recog;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opencv.core.*;

public class Utils {
	
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public static final FilenameFilter FILTER_BMP = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".bmp");
		}
	};
	public static final FilenameFilter FILTER_BMP_PNG = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".bmp") || name.endsWith(".png");
		}
	};
	
	public static final FilenameFilter FILTER_BMP_PNG_JPG = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".bmp") || name.endsWith(".png") || name.endsWith(".jpg");
		}
	};
	public static final List<Character> FULL_CHARACTERS_SET
	= Arrays.asList('0','1','2','3','4','5','6','7','8','9', 
			'A', 'B', 'C', 'E', 'H', 'K', 'M', 'P', 'T', 'X', 'Y');
	
	public static final List<Character> NUMBERS_SET
	= Arrays.asList('0','1','2','3','4','5','6','7','8','9'); 
	
	public static final List<Character> LETTERS_SET
	= Arrays.asList('A', 'B', 'C', 'E', 'H', 'K', 'M', 'O', 'P', 'T', 'X', 'Y');
	
	public static final List<Character> LETTERS_SET_PLUS_0
	= Arrays.asList('A', 'B', 'C', 'E', 'H', 'K', 'M', 'O', 'P', 'T', 'X', 'Y', '0');
	
	
	public static String checkNumber(String number) {
		return check(number, LETTERS_SET, NUMBERS_SET);
	}
	
	public static String checkNumberWith0(String number) {
		return check(number, LETTERS_SET_PLUS_0, NUMBERS_SET);
	}
	
	public static boolean isNumber(String number) {
		return checkNumber(number) == null;
	}
	
	public static boolean isNumberWith0(String number) {
		return checkNumberWith0(number) == null;
	}
	
	private static String check(String number, List<Character> letterSet, List<Character> digitSet) {
		if (number == null) return "number is null";
		if (number.length()!=6) return "number length is "+number.length();
		
		String upper = number.toUpperCase();
		
		if (letterSet.containsAll(Arrays.asList(upper.charAt(0), upper.charAt(4), upper.charAt(5)))
				&& digitSet.containsAll(Arrays.asList(upper.charAt(1), upper.charAt(2), upper.charAt(3))))
			return null;
		else return "wrong number";
		
	} 
	
	public static String fullPath(File parent, String name) {
		return parent.getAbsolutePath().concat(File.separator)
				.concat(name);
	}
	
//	public static File[] getOrderedFiles(String dir) {
//		return getOrderedList(dir).toArray();
//	}
	
	public static List<File> getOrderedList(String dir) {
		File cardir = new File(dir); 
		
		List<File> fl = Arrays.asList(cardir.listFiles(FILTER_BMP_PNG_JPG));
		Collections.sort(fl, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.compare(getLastDigits(o1.getName()), getLastDigits(o2.getName()));
			}
		});
		return fl;
	}
	
	private static Pattern digitPattern = null;
	
    public static int getLastDigits(String s) {
	    if (digitPattern == null)
	    	digitPattern = Pattern.compile("\\d+");
	    Matcher m = digitPattern.matcher(s); 
	    String digitStr = "-1";
	    while (m.find()) 
	    	digitStr = m.group();
	    
	    return Integer.parseInt(digitStr);
    }

	
	public static Mat produceSumMat(Mat m) {
		Mat sums = new Mat(m.size(), CvType.CV_32F);
		
		for (int row = 0; row < m.rows(); row ++)
			for (int col = 0; col < m.cols(); col++)
				if (row == 0 && col == 0)
					sums.put(row, col, m.get(row, col));
				else if (row == 0)
					sums.put(row, col, addArrays(m.get(row, col), sums.get(row, col-1)));
				else if (col ==0)
					sums.put(row, col, addArrays(m.get(row, col), sums.get(row-1, col)));
				else // Sum(i,j) = Sum(i-1,j)+Sum(i,j-1)-Sum(i-1, j-1) + m(i,j)
					sums.put(row, col, addArrays(m.get(row, col), subArrays( addArrays(sums.get(row-1, col), sums.get(row, col-1)), sums.get(row-1, col-1))));
		
		return sums;
	}
	
	public static double[][] produceSumArr(Mat m) {
//		Mat sums = new Mat(m.size(), CvType.CV_32F);
		double[][] sumArr = new double[m.rows()][m.cols()];
		
		for (int row = 0; row < m.rows(); row ++)
			for (int col = 0; col < m.cols(); col++)
				if (row == 0 && col == 0)
					sumArr[0][0] = m.get(0, 0)[0];
				else if (row == 0)
					sumArr[row][col] = m.get(row, col)[0] + sumArr[row][col-1];
//					sums.put(row, col, addArrays(m.get(row, col), sums.get(row, col-1)));
				else if (col ==0)
					sumArr[row][col] = m.get(row, col)[0] + sumArr[row-1][col];
//					sums.put(row, col, addArrays(m.get(row, col), sums.get(row-1, col)));
				else // Sum(i,j) = Sum(i-1,j)+Sum(i,j-1)-Sum(i-1, j-1) + m(i,j)
					sumArr[row][col] = m.get(row, col)[0] + sumArr[row-1][col] +sumArr[row][col-1] - sumArr[row-1][col-1];
//					sums.put(row, col, addArrays(m.get(row, col), subArrays( addArrays(sums.get(row-1, col), sums.get(row, col-1)), sums.get(row-1, col-1))));
		
		return sumArr;
	}

	
	public static double[] addArrays(double[] a, double[] b) {
		double[] c = new double[Math.min(a.length, b.length)];
		for (int i=0; i < c.length; i++)
			c[i] = a[i] + b[i];
		return c;
	}
	
	public static double[] subArrays(double[] a, double[] b) {
		double[] c = new double[Math.min(a.length, b.length)];
		for (int i=0; i < c.length; i++)
			c[i] = a[i] - b[i];
		return c;
	}
	
	public static String arrStr(double[] a) {
		return Arrays.stream(a).boxed().collect(Collectors.toList()).toString();
	}
	
	public static void main(String[] args) {
		
		System.out.println(CASCADE_LPR);
		
//		//Test addArray
//		double[] a = new double[] {1,2,3};
//		double[] b = new double[] {3,2,1};
//		System.out.println(Arrays.stream(addArrays(a,b)).boxed().collect(Collectors.toList()));
//		
//		//Test subArray
//		double[] c = new double[] {1,2,3};
//		double[] d = new double[] {1,2,3};
//		System.out.println(Arrays.stream(subArrays(c,d)).boxed().collect(Collectors.toList()));
//		
//		//Test produceSumMat
//		Mat m = Mat.ones(4, 4, CvType.CV_8UC1);
//		System.out.println(m.dump());
//		System.out.println(produceSumMat(m).dump());
//		double[][] arr = produceSumArr(m);
//		System.out.println(Arrays.deepToString(arr));

	}
	
	public static String URL2FString(URL url) {
		try {
			return new File(url.toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static URL CASCADE_LPR = System.class.getResource("/russianLP.xml"); //TODO is this right?
	public static String CASCADE_LPR_PATH = URL2FString(CASCADE_LPR);


}
