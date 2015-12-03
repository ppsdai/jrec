package ru.recog.repo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.neuroph.core.NeuralNetwork;

import ru.recog.XML;
import ru.recog.nn.NNWrapper;

public class RepositoryTest {
	
	
	public static void testNetworksIntegrity() {
		
		for ( File ns : Repository.getNetworkFiles())
			testNN(ns);
	}
	
	public static void testNN(File nnFile) {
		Network n = (Network) XML.fromXML(nnFile);
		NNWrapper nnw = n.getWrapper();
		NeuralNetwork<?> nn = nnw.getNN();
		System.out.println(NNWrapper.description(nn));
		System.out.println(n.getFeatureExtractor());
		System.out.println("Input neurons # = FEX dimension is "+(nn.getInputsCount()==n.getFeatureExtractor().getDimension()));
	}
	
	public static void testVideoList() {
		List<String> canonicalList = Arrays.asList(new String[] {
				"ador7_37-2015-06-19-01-41.avi",
				"ador7_52_2015-06-19-01-44.avi",
				"ador7_68_2015-06-19-00-35.avi",
				"ador7_81_2015-06-19-01-54.avi",
				"ador7_96_2015-06-19-01-52.avi",
				"video-000.avi",
				"video-041.avi",
				"video-046.avi",
				"video-047.avi",
				"video-049.avi",
				"video-050.avi",
				"video-051.avi",
				"video-052.avi",
				"video-1.25-16.23-020615-1.avi",
				"video-1.41-16.23-020615-1.avi",
				"video7038_16072015_1040.avi",
				"video7039_03072015_1539.avi",
				"video7039_10082015_1145.avi",
				"video7039_31072015_1548.avi",
				"video7050_16072015_1041.avi",
				"video7083_03072015_1540.avi",
				"video7083_10072015_1123.avi",
				"video7083_10082015_1144.avi",
				"video7083_31072015_1548.avi",
				"video7_39.avi",
				"video7_83.avi",
				"video_ador7_39-20150625-15-06.avi",
				"video_ador7_42-20150625-15-08.avi",
				"video_ador7_44-20150625-15-06.avi",
				"video_ador7_66_20150812_11-35.avi",
				"video_ador7_83-20150625-15-05.avi",
				"video_ador7_92_20150807_09-30.avi",
				"video_ador7_92_20150811_16-01.avi"
		});
		System.out.println("ALL videos from canonical list are in repo: "+Repository.getVideoList().containsAll(canonicalList));
	}
	
	
	public static void main(String[] args) {
		testVideoList();
		testNetworksIntegrity();
	}

}
