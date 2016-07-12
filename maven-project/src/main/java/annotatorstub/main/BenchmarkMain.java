package annotatorstub.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

import annotatorstub.annotator.newAnnotator4;
import annotatorstub.utils.Utils;
import it.unipi.di.acube.batframework.cache.BenchmarkCache;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.datasetPlugins.YahooWebscopeL24Dataset;
import it.unipi.di.acube.batframework.metrics.Metrics;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.metrics.StrongAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.StrongTagMatch;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.DumpData;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class BenchmarkMain {
	public static void main(String[] args) throws Exception {
		System.setOut(new PrintStream(new FileOutputStream("result_opendataset.txt")));
		
		WikipediaApiInterface wikiApi = WikipediaApiInterface.api();
//		A2WDataset ds = DatasetBuilder.getGerdaqTest();
		
		String dsPath = "/Users/hanzhichao/Downloads/out-domain-dataset-new.xml";
		String groupName = "DUMMYGROUP";
		A2WDataset ds = new YahooWebscopeL24Dataset(dsPath);

		
		newAnnotator4 ann = new newAnnotator4();
		
		List<HashSet<Tag>> resTag = BenchmarkCache.doC2WTags(ann, ds);
		newAnnotator4.setCountZero();
		List<HashSet<Annotation>> resAnn = BenchmarkCache.doA2WAnnotations(ann, ds);
		DumpData.dumpCompareList(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), resAnn, wikiApi);
//
//		Metrics<Tag> metricsTag = new Metrics<>();
//		MetricsResultSet C2WRes = metricsTag.getResult(resTag, ds.getC2WGoldStandardList(), new StrongTagMatch(wikiApi));
//		Utils.printMetricsResultSet("C2W", C2WRes, ann.getName());
//
//		Metrics<Annotation> metricsAnn = new Metrics<>();
//		MetricsResultSet rsA2W = metricsAnn.getResult(resAnn, ds.getA2WGoldStandardList(), new StrongAnnotationMatch(wikiApi));
//		Utils.printMetricsResultSet("A2W-SAM", rsA2W, ann.getName());
//		
//		Utils.serializeResult(ann, ds, new File("annotations.bin"));
		Utils.serializeResult(ann, ds, new File("annotation-" + groupName + ".bin"));
		wikiApi.flush();
	}

}