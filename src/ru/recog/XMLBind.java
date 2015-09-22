package ru.recog;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import ru.recog.feature.*;
import ru.recog.nn.NNWrapper;

public class XMLBind {

	private static Marshaller itsMarshaller = null;
	private static Unmarshaller itsUnmarshaller = null;
	private static JAXBContext itsContext = null;
	
	static {
		createContext();
	}
	
	private static void createContext() {
//		try {
//			itsContext = JAXBContext.newInstance(XMLBind.class,
//					AreaFeatureExtractor.class,
//					SymmetryFeatureExtractor.class,
//					FeatureExtractor.class,
//					MultipleFeatureExtractor.class,
//					GradientGridFeatureExtractor.class,
//					OverlapGradientGridFeatureExtractor.class,
//					NNWAdapterType.class,
//					NNWAdapter.class,
//					NNWrapper.class
//					);
////			itsContext = JAXBContext.newInstance("ru.recog.feature");
//			itsMarshaller = itsContext.createMarshaller();
//			itsMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//			itsUnmarshaller = itsContext.createUnmarshaller();
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
	}
	
	public static String toXML(Object o) throws JAXBException {
		StringWriter sw = new StringWriter();
		toXML(o, sw);
		return sw.toString();
	}
	
	public static void toXML(Object o, Writer w) throws JAXBException {
		itsMarshaller.marshal(o, w);
	}
	
	public static void toXML(Object o, OutputStream os) throws JAXBException {
		itsMarshaller.marshal(o, os);
	}
	
	public static Object fromXML(Reader r) throws JAXBException {
		return itsUnmarshaller.unmarshal(r);
	}
	
	public static void main(String[] args) throws JAXBException {
		
//		AreaFeatureExtractor afex = new AreaFeatureExtractor();
//		OverlapGradientGridFeatureExtractor oggfx = new OverlapGradientGridFeatureExtractor();
//		
//		MultipleFeatureExtractor mfx = new MultipleFeatureExtractor();
//		mfx.addExtractor(afex);
//		mfx.addExtractor(oggfx);
//		
//		NNWrapper nnw = new NNWrapper("/Users/pps/dev/NNet724021.nnet", mfx);
//		
//		
//		
//		XMLBind.toXML(nnw, System.out);
//
//		StringWriter sw = new StringWriter();
//		XMLBind.toXML(nnw, sw); 
//		System.out.println(sw.toString());
//
//		Object o = XMLBind.fromXML(new StringReader(sw.toString()));
//		System.out.println(o);
		
//		NNWAdapterType na = new NNWAdapterType();
//		na.path = "/path/NN";
//		na.mfx = mfx;
//		XMLBind.toXML(na, System.out);
		
		itsContext = JAXBContext.newInstance(/*XMLBind.class,
				AreaFeatureExtractor.class,
				SymmetryFeatureExtractor.class,
				FeatureExtractor.class,
				MultipleFeatureExtractor.class,
				GradientGridFeatureExtractor.class,
				OverlapGradientGridFeatureExtractor.class,
				NNWAdapterType.class,
				NNWAdapter.class,*/
				NNWrapper.class
				);

	}
	
	@XmlRootElement
	public static class NNWAdapterType {
		public String path;
		public MultipleFeatureExtractor mfx;
	}
	
	public static class NNWAdapter extends XmlAdapter<NNWAdapterType, NNWrapper> {
		@Override
		public NNWAdapterType marshal(NNWrapper v) throws Exception {
			NNWAdapterType at = new NNWAdapterType();
			at.path = v.getPathToNN();
			at.mfx = v.getFeatureExtractor();
			return at;
		}
		
		@Override
		public NNWrapper unmarshal(NNWAdapterType v) throws Exception {
			return new NNWrapper(v.path, v.mfx);
		}
	}

}
