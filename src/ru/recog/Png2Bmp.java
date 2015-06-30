package ru.recog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;


public class Png2Bmp {

	
	public static void main(String args[]) throws Exception {
		
		if (args.length<2) {
			System.err.println("Usage: png2bmp: sourceDir destinationDir");
			System.exit(1);
		}
		
		File dir = new File(args[0]);
		File dest = new File(args[1]);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".bmp");
			}
		});
		if (!dest.exists()) dest.mkdir();
		
		System.out.println(dir.getAbsolutePath()+" "+files.length);
		for (File f : files) {
			String source = dir.getAbsolutePath().concat(File.separator).concat(f.getName());
			String destination = dest.getAbsolutePath().concat(File.separator).concat(f.getName());
			System.out.println(source+" ---> "+destination);
			BufferedImage img = ImageIO.read(f);
			ImageIO.write(img, "BMP", new File(destination));

		}

	}
}
