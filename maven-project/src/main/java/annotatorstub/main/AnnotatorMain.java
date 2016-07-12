package annotatorstub.main;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.HashSet;

//import annotatorstub.annotator.FakeAnnotator;
import annotatorstub.annotator.newAnnotator4;

public class AnnotatorMain {

	public static void main(String[] args) throws IOException {
//		FakeAnnotator ann = new FakeAnnotator();
		newAnnotator4 ann = new newAnnotator4();
		String query = "luxury apartments san francisco";
//		HashSet<ScoredAnnotation> annotations = ann.BaseLine(query);
		HashSet<Annotation> annotations = ann.solveA2W(query);
		for (Annotation a : annotations) {
			int wid = a.getConcept();
			String title = WikipediaApiInterface.api().getTitlebyId(a.getConcept());
			System.out.printf(
					"found annotation: %s -> %s (id %d) link: http://en.wikipedia.org/wiki/index.html?curid=%d%n",
					query.substring(a.getPosition(), a.getPosition() + a.getLength()), title, wid, wid);
		}
		WikipediaApiInterface.api().flush();
	}
		
	
}
