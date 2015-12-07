package ru.recog.repo;

import ru.recog.XML;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.nn.NNWrapper;

public class Network {
	
	private String id;
	private MultipleFeatureExtractor extractor;
	
	
	public Network(String id, MultipleFeatureExtractor extractor) {
		this.id = id;
		this.extractor = extractor;
	}
	
	public NNWrapper getWrapper() {
		NNWrapper nnw = new NNWrapper(Repository.getNetworkFile(id).getAbsolutePath(), extractor);
		return nnw;
	}
	
	public static Network getDefaultNetwork() {
		return (Network) XML.fromXML(Repository.getNetworkFile("NN2883521.xml"));
	}
	
	public static NNWrapper getDefaultWrapper() {
		return getDefaultNetwork().getWrapper();
	}
	
	
	public MultipleFeatureExtractor<?> getFeatureExtractor() {
		return extractor;
	}
	
	public static void main(String[] args) throws Exception {
		for (String key : System.getenv().keySet())
			System.out.println(key+"="+System.getenv(key));
	}
	
}
