package ru.recog;

import java.io.*;

import ru.recog.feature.*;
import ru.recog.repo.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XML {
	
	private static XStream xstream;
	
	static {
		createContext();
	}
	
	private static void createContext() {
		xstream = new XStream(new DomDriver());
		
		xstream.alias("NN",Network.class);
		xstream.useAttributeFor(Network.class, "id");
		
		xstream.alias("seglog", SegLog.class);
		xstream.alias("sle", SegLogEntry.class);
		
		xstream.useAttributeFor(FeatureExtractor.class, "dimension");
	
		xstream.alias("mfx", MultipleFeatureExtractor.class);
		xstream.addImplicitCollection(MultipleFeatureExtractor.class, "featureList");
		
		xstream.alias("OverlapGradient", OverlapGradientGridFeatureExtractor.class);
		
		xstream.alias("Rect", org.opencv.core.Rect.class);
		xstream.useAttributeFor(org.opencv.core.Rect.class, "x");
		xstream.useAttributeFor(org.opencv.core.Rect.class, "y");
		xstream.useAttributeFor(org.opencv.core.Rect.class, "width");
		xstream.useAttributeFor(org.opencv.core.Rect.class, "height");

	}
	
	public static void toXML(Object obj, OutputStream out) {
		xstream.toXML(obj, out);
	}
	
	public static void toXML(Object obj, Writer w) {
		xstream.toXML(obj, w);
	}
	
	public static Object fromXML(File file) {
		return xstream.fromXML(file);
	}
	
	
}
