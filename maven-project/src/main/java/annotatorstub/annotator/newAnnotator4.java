package annotatorstub.annotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import org.apache.commons.math3.util.Pair;

import annotatorstub.utils.BingCorrectionHelper;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class newAnnotator4 implements Sa2WSystem {
	private static long lastTime = -1;
	private static float threshold = -1f;
	private static int count = 0;
	final static String dict_path = "/Users/hanzhichao/Documents/ETH_Courses/NLP/project/for_code/features/labels.txt";
	private static HashMap<Integer, Integer> label = null;

	public static void main(String[] args) throws Exception {
		String query = "badminton courts los angeles";
		// query = "south st philly stores";
		// query = "strawberry fields forever";
		// newAnnotator4 ann = new newAnnotator4();
		// HashSet<ScoredAnnotation> annotations = ann.BaseLine(query);
		// for (Annotation a : annotations){
		// System.out.printf("mention: %s: , link is:
		// http://en.wikipedia.org/wiki/index.html?curid=%d\n",
		// query.substring(a.getPosition(), a.getPosition()+a.getLength()),
		// a.getConcept());
		// }
		newAnnotator4 ann = new newAnnotator4();
		ann.solveSa2W(query);
	}

	public long getLastAnnotationTime() {
		return lastTime;
	}

	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, threshold);
	}

	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), threshold);
	}

	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	public List<String> construct_mentions(String[] words) throws Exception {
		// System.out.println("input words: "+words.toString());
		List<String> Mentions = new ArrayList<String>();
		// WikipediaApiInterface api = WikipediaApiInterface.api();
		WATRelatednessComputer.setCache("relatedness.cache");

		int[] mention_length = new int[words.length]; // record the length of longest mention beginning at each postion
		for (int i = 0; i < words.length; i++) {
			mention_length[i] = 0;
		}
		for (int i = 0; i < words.length; i++) {
			// construct mention using words in range [i, j] (including position
			// i and position j)
			for (int j = i; j < words.length; j++) {
				StringBuilder builder = new StringBuilder();
				for (int k = i; k <= j; k++) { //
					if (builder.length() > 0) {
						builder.append(" ");
					}
					builder.append(words[k]);
				}
				String mention_i_j = builder.toString();
				if (WATRelatednessComputer.getLp(mention_i_j) > 0) { 
					if (j - i + 1 > mention_length[i]) {
						mention_length[i] = j - i + 1;
					}
				}
			}
		}
		// compute the longest mention, with the words in range [begin_pos,
		// end_pos] (including begin_pos & end_pos)
		int begin_pos = 0; // begin position of longest mention
		int max_length = 0;// length of longest mention
		for (int i = 0; i < words.length; i++) {
			if (mention_length[i] > max_length) {
				max_length = mention_length[i];
				begin_pos = i;
			}
		}
		if (max_length == 0) {
			return Mentions; // return emtpy Mentions
		}
		int end_pos = begin_pos + max_length - 1; // end position of longest mention
		StringBuilder builder = new StringBuilder();
		for (int i = begin_pos; i <= end_pos; i++) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			builder.append(words[i]);
		}
		String longest_mention = builder.toString();

		// add longest mention in this words list

		if (longest_mention.length() > 0) {
			Mentions.add(longest_mention);
		} else {
			return Mentions;
		}
		// split this words list by the longest mention and compute the Mentions
		// in the first sublist
		if (begin_pos > 0) {// Mentions.subList(beginPos, endPos): in range
							// [beginPos, endPos), beginPos: inclusive; endPos:
							// exclusive
			String[] new_words = Arrays.copyOfRange(words, 0, begin_pos);
			// List<String> previous_Mentions =
			// construct_mentions(words.subList(0, begin_pos));
			List<String> previous_Mentions = construct_mentions(new_words);
			if (previous_Mentions.size() > 0) {
				for (int i = 0; i < previous_Mentions.size(); i++) {
					Mentions.add(previous_Mentions.get(i));
				}
			}
		}
		// compute the Mentions in the second sublist
		if (end_pos < words.length - 1 && end_pos >= 0) {
			String[] new_words = Arrays.copyOfRange(words, end_pos + 1, words.length);
			List<String> following_Mentions = construct_mentions(new_words);
			// List<String> following_Mentions =
			// construct_mentions(words.subList(end_pos+1, words.length));
			if (following_Mentions.size() > 0) {
				for (int i = 0; i < following_Mentions.size(); i++) {
					Mentions.add(following_Mentions.get(i));
				}
			}
		}

		return Mentions;

	}

	// BASELINE
	
	// Call BASELINE
	
	// load result from text (output from the classifier)
	public HashSet<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		System.out.println("QUERY: " + text);
		if (label == null) {
			try {
				loadLabel(dict_path);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		HashSet<ScoredAnnotation> result = new HashSet<>();
		String[] words;
		// split string to words list
		String query = text.replaceAll("[^A-Za-z0-9 ]", " "); // only remain A-Za-z0-9 and replace other charaters with space																
		
//		// use correction
//		Pair<String, HashMap<String,String>> ret = BingCorrectionHelper.correction(query);
//		query = ret.getFirst();
//		HashMap<String, String> map = ret.getSecond();
//		/////////////////
		
		
		words = query.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\w]", "");
		}
		List<String> mentions = new ArrayList<String>();
		try {
			mentions = construct_mentions(words);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < mentions.size(); i++) {
			String men = mentions.get(i);
			int[] id_list = WATRelatednessComputer.getLinks(men);
			boolean flag = false; // mark whether this mention has entity
			for (int j = 0; j < id_list.length && j < 3; j++) {
				int entity_id = id_list[j];
				double score = WATRelatednessComputer.getCommonness(men, entity_id);
				int count_line = newAnnotator4.getCount();
				int y = 0;
				try {
					y = label.get(count_line);
				} catch (Exception e) {
					System.out.println("error " + count_line);
				}
				System.out.printf("%d, %d\n", count_line, y);
				System.out.println(men + ", " + entity_id);

				if (y == 1) {
					if (flag == false) {
						flag = true;// do not add entity anymore
						// add this entity to result
						String[] words_in_mention = men.split("\\s+");
						int len = words_in_mention.length;
						
						
//						/////////////////// use correction ////////////////////
//						int start_pos;
//						int end_pos;
//						if(map.containsKey(words_in_mention[0])) {
//							start_pos = text.indexOf(map.get(words_in_mention[0]));
//						}else {
//							start_pos = text.indexOf(words_in_mention[0]); 
//						}
//						if(map.containsKey(words_in_mention[len - 1])) {
//							end_pos = text.indexOf(map.get(words_in_mention[len - 1]));
//						} else {
//							end_pos = text.indexOf(words_in_mention[len - 1]); 
//						}
//						//////////////////////////////////////////////////////////
						
						
						
						int start_pos = text.indexOf(words_in_mention[0]); //start position of the mention in the text
						int mention_length = men.length();
						int end_pos = start_pos + mention_length; // end position of the mention in the text
						if (entity_id != -1) {
							result.add(new ScoredAnnotation(start_pos, end_pos - start_pos, entity_id, (float) score));
						}
					}
				}
				newAnnotator4.addOne();
			}
		}
		return result;
	}

	private static void addOne() {
		count += 1;
	}

	private static int getCount() {
		return count;
	}
	public static void setCountZero() {
		count = 0;
	}

	public static void loadLabel(String path) throws IOException {
		System.out.println("----------------------Start loading labels--------------------\n");
		label = new HashMap<Integer, Integer>();
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str = br.readLine();
		while (str != null && !str.equals("")) {
			String[] slices = str.split(" ", 2);
			int id = Integer.parseInt(slices[0]);
			int l = Integer.parseInt(slices[1]);
			label.put(id, l);
			str = br.readLine();
		}
		System.out.println("----------------------Finish loading labels--------------------\n\n");
	}

	// original solveSa2W
	public HashSet<ScoredAnnotation> solveSa2W_original(String text) throws AnnotationException {
		lastTime = System.currentTimeMillis();

		int start = 0;
		while (start < text.length() && !Character.isAlphabetic(text.charAt(start)))
			start++;
		int end = start;
		while (end < text.length() && Character.isAlphabetic(text.charAt(end)))
			end++;

		int wid;
		try {
			wid = WikipediaApiInterface.api().getIdByTitle(text.substring(start, end));
		} catch (IOException e) {
			throw new AnnotationException(e.getMessage());
		}

		HashSet<ScoredAnnotation> result = new HashSet<>();
		if (wid != -1)
			result.add(new ScoredAnnotation(start, end - start, wid, 0.1f));

		lastTime = System.currentTimeMillis() - lastTime;
		return result;
	}

	public String getName() {
		return "Simple yet uneffective query annotator";
	}
}
