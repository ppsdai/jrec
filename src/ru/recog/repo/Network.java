package ru.recog.repo;

import java.io.File;
import java.io.FileWriter;

import ru.recog.XML;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNWrapper;

public class Network {
	
	private String id;
	private MultipleFeatureExtractor extractor;
	
	
	public Network(String id, MultipleFeatureExtractor extractor) {
		this.id = id;
		this.extractor = extractor;
	}
	
	public NNWrapper getWrapper() {
		System.out.println(id);
		NNWrapper nnw = new NNWrapper(Repository.getNetworkFile(id).getAbsolutePath(), extractor);
		//TODO check dimensions
		return nnw;
	}
	
	
	public MultipleFeatureExtractor<?> getFeatureExtractor() {
		return extractor;
	}
	
	public static void main(String[] args) throws Exception {
		for (String key : System.getenv().keySet())
			System.out.println(key+"="+System.getenv(key));
//		XStream xs = new XStream(new StaxDriver());
//		MultipleFeatureExtractor<?> mfx = new MultipleFeatureExtractor<>(new OverlapGradientGridFeatureExtractor(7,13));
//		Network n = new Network("NN2882521.nnet", mfx);
//		XML.toXML(n, new FileOutputStream(new File("/Users/pps/dev/111new.xml")));
//		XML.toXML(n, new FileWriter(new File(Repository.networksFolderFile, "NN2882521.xml")));
//		xs.toXML(n, new FileOutputStream(new File("/Users/pps/dev/111new2.xml")));

		
		
//		StringWriter sw = new StringWriter();
//		xs.toXML(n, sw);
//		System.out.println(sw.toString());
//		Network n2 = (Network)xs.fromXML(sw.toString());
//		System.out.println(n2.id);
//		System.out.println(n2.extractor);
//		
//		OverlapGradientGridFeatureExtractor oggfx = new OverlapGradientGridFeatureExtractor(7, 13);
//		sw.close();
//		sw = new StringWriter();
//		xs.toXML(oggfx, sw);
//		System.out.println(sw.toString());
//		FeatureExtractor<?> fx= (FeatureExtractor<?>)xs.fromXML(sw.toString());
//		System.out.println(fx);
//		
	}

//	public FeatureExtractor<?> getExtractor() {
//		return extractor;
//	}
//
//	public void setExtractor(FeatureExtractor<?> extractor) {
//		this.extractor = extractor;
//	}
//	
//	public String getId() {
//		return id;
//	}
//	
//	public void setId(String id) {
//		this.id = id;
//	}
	
}
