package annotatorstub.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import org.apache.commons.math3.util.Pair;

import annotatorstub.annotator.newAnnotator4;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.datasetPlugins.YahooWebscopeL24Dataset;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.Pair;

public class GeneFeatures{
	public static void main(String[] args) throws Exception{
//		GenerateFeatures();
		TestDataSet();
//		String entity_title = "Vodka";
//		int entity_id = 32787;
//		String mention = "allegro";
//		String query = "metronome setting of allegro";
//		double score  = cmptContext2(entity_id, mention, query);
//		System.out.println("score: " + score);
	}
	
	public static void TestDataSet() throws Exception{
		A2WDataset ds = DatasetBuilder.getGerdaqTest();// load data set
//		String dsPath = "/Users/hanzhichao/Downloads/out-domain-dataset-new.xml";
//		A2WDataset ds = new YahooWebscopeL24Dataset(dsPath);
		WATRelatednessComputer.setCache("relatedness.cache");
		String directory = "/Users/hanzhichao/Documents/ETH_Courses/NLP/project/for_code/features/";
		String filename = "newDataSet_5features.txt";
		PrintWriter writer = new PrintWriter(directory+filename, "UTF-8");
		newAnnotator4 ann = new newAnnotator4();
		int count = 0;
		for (String doc: ds.getTextInstanceList()){
			System.out.println(doc);
			String query = doc.replaceAll("[^A-Za-z0-9 ]", " "); // only remain A-Za-z0-9 and replace other charaters with space
			
//			// use correction
//			Pair<String, HashMap<String,String>> ret = BingCorrectionHelper.correction(query);
//			query = ret.getFirst();
//			/////////////////
			
			
			
			String[] words = query.split("\\s+");
			for(int i=0; i<words.length; i++){
				words[i] = words[i].replaceAll("[^\\w]", "");
			}
			List<String> mentions = ann.construct_mentions(words);
			for(int i=0; i<mentions.size(); i++){
				String men = mentions.get(i);
				HashMap<Integer, Pair<String, Double>> id_properties = CrawlerHelper.getWikiTitle(men);
				int[] id_list = WATRelatednessComputer.getLinks(men);
				double[] scores_comm = new double[3];
				double[] scores_list1 = new double[3];
				double[] scores_list2 = new double[3];
				double[] scores_list3 = new double[3];
				double[] scores_list4 = new double[3];
				for(int j=0; j<id_list.length && j<3; j++){
					int id = id_list[j];
					String entity_title = id_properties.get(id).first;
					scores_comm[j] = id_properties.get(id).second;
					scores_list1[j] = cmptContext1(entity_title, men, query);
					scores_list2[j] = cmptContext2(id, men, query);
					scores_list3[j] = cmptContext1_1(entity_title, men, query);
					scores_list4[j] = cmptContext2_1(id, men, query);
				}
				double sum_score1 = 0.0;
				double sum_score2 = 0.0;
				double sum_score3 = 0.0;
				double sum_score4 = 0.0;
				for(int j=0; j<id_list.length && j<3; j++){
					sum_score1 += scores_list1[j];
					sum_score2 += scores_list2[j];
					sum_score3 += scores_list3[j];
					sum_score4 += scores_list4[j];
				}
					
				for(int j=0; j<id_list.length && j<3; j++){
					int length = id_list.length;
					double score1 = scores_list1[j];
					double score2 = scores_list2[j];
					double score3 = scores_list3[j];
					double score4 = scores_list4[j];					
					writer.printf("%f %f %f %f %f\n", scores_comm[j], score1, score2, score3, score4);
					count += 1;
				}
				
			}
		}
		writer.close();
	}

	public static void GenerateFeatures() throws Exception{
		A2WDataset ds = DatasetBuilder.getGerdaqDevel();// load data set
		WATRelatednessComputer.setCache("relatedness.cache");
		String directory = "/Users/hanzhichao/Documents/ETH_Courses/NLP/project/for_code/features/";
		String filename = "devel_dataset.txt";
		PrintWriter writer = new PrintWriter(directory+filename, "UTF-8");
		List<HashSet<Annotation>> truthAnno = ds.getA2WGoldStandardList();
		List<String> queries = ds.getTextInstanceList();
		for(int i=0; i<truthAnno.size(); i++){
			HashSet<Annotation> Ann = truthAnno.get(i);
			System.out.println(i);
			String query = queries.get(i);
			for(Annotation a: Ann){
				int entity_id = a.getConcept();
				int beginPos = a.getPosition();
				int len = a.getLength();
				String mention = query.substring(beginPos, beginPos+len);
				HashMap<Integer, Pair<String, Double>> id_properties = CrawlerHelper.getWikiTitle(mention);
//				writer.printf("%s --> %d, ", mention, entity_id);								
//				System.out.println(mention);				
				
				int[] id_list = WATRelatednessComputer.getLinks(mention);
				double[] score_comm = new double[3];
				double[] score_list1 = new double[3];
				double[] score_list2 = new double[3];
				double[] score_list3 = new double[3];
				double[] score_list4 = new double[3];
				for(int j=0; j<id_list.length && j<3; j++){
					int id = id_list[j];
					String entity_title = id_properties.get(id).first;
					score_comm[j] = id_properties.get(id).second; // the commonness
					score_list1[j] = cmptContext1(entity_title, mention, query);
					score_list2[j] = cmptContext2(id, mention, query);
					score_list3[j] = cmptContext1_1(entity_title, mention, query);// context score 3 (5, 7)
					score_list4[j] = cmptContext2_1(id, mention, query); // context score 4 (6, 8), using weighted score
				}
				double sum_comm = 0.0;
				double sum_score1 = 0.0;
				double sum_score2 = 0.0;
				double sum_score3 = 0.0;
				double sum_score4 = 0.0;
				for(int j=0; j<id_list.length && j<3; j++){
					sum_score1 += score_list1[j];
					sum_score2 += score_list2[j];
					sum_score3 += score_list3[j];
					sum_score4 += score_list4[j];
				}
				for(int j=0; j<id_list.length && j<3; j++){
					int id = id_list[j];
					int flag;
					double score1, score2, score3, score4;
					if(id==entity_id){
						flag = 1;						
					}
					else{
						flag = 0;						
					}
					score1 = score_list1[j];
					score2 = score_list2[j];
					score3 = score_list3[j];
					score4 = score_list4[j];
					writer.printf("%f %f %f %f %f %d\n", score_comm[j], score1, score2, score3, score4, flag);											
				}
				
				
				
				
				/*
				for(int j=0;  j<id_list.length && j<3; j++){
					int id = id_list[j];
					// features here:
					String entity_title = id_properties.get(id).first; // the title of entity
					int rank = j+1; // the ranking in id list
//					double score1 = id_properties.get(id).second; // the commonness
//					double score2 = cmptContext1(entity_title, mention, query);// the context score 1
//					double score3 = cmptContext2(id, mention, query);// the context score 2
//					double score4 = cmptContext1(entity_title, mention, query); // context score 3, using weighted score
//					double score5 = cmptContext2(id, mention, query); // context score 4, using weighted score
//					double score6 = 

					// the positive class, output features
					if(id==entity_id){
						int flag = 1; //the positive class
//						writer.printf("%s, %d, %f, %d\n", mention, id, score1, flag);
//						writer.printf("%d, %f, %f, %d\n", rank, score1, score2, flag);
//						writer.printf("%d %f %d\n", i, score4, flag);
					}
					// the negative class, output features
					else{
						int flag = 0; // the negative class
//						writer.printf("%s, %d, %f, %d\n", mention, id, score1, flag);
//						writer.printf("%d, %f, %f, %d\n", rank, score1, score2, flag);
//						writer.printf("%d %f %d\n", i, score4, flag);
					}
				}
				*/
				
				
			}
		}
		writer.close();
	}
	/**
	 * compute the similarity of entity_title and the remaining text, using getSimilarityValue2()
	 * @param entity_title
	 * @param query
	 * @return
	 * @throws IOException 
	 */
	public static double cmptContext1(String entity_title, String mention, String query) throws IOException{
		double score = 0.0;
		// contruct the remaining context: remove the mention from query
		ArrayList<String> remaining_words = new ArrayList<String>(Arrays.asList(query.replaceAll("[^A-Za-z0-9 ]", " ").split("\\s+")));
		for(String s: mention.split("\\s+")){
			remaining_words.remove(s);
		}
		StringBuilder builder = new StringBuilder();
		for(int loop=0; loop<remaining_words.size(); loop++){ // 
			if(builder.length()>0){
				builder.append(" ");
			}					
			builder.append(remaining_words.get(loop));
		}
		String remaining_context = builder.toString();	
		String entity_context = entity_title.replaceAll("[^A-Za-z0-9 ]", " ");
		String[] words1 = entity_context.split("\\s+");
		String[] words2 = remaining_context.split("\\s+");
		System.out.println(entity_context + ", " + remaining_context);
		score =  EmbeddingHelper.getSimilarityValue2(words1, words2);		
		return score;
	}
	public static double cmptContext1_1(String entity_title, String mention, String query) throws IOException{
		double score = 0.0;
		// contruct the remaining context: remove the mention from query
		ArrayList<String> remaining_words = new ArrayList<String>(Arrays.asList(query.replaceAll("[^A-Za-z0-9 ]", " ").split("\\s+")));
		for(String s: mention.split("\\s+")){
			remaining_words.remove(s);
		}
		StringBuilder builder = new StringBuilder();
		for(int loop=0; loop<remaining_words.size(); loop++){ // 
			if(builder.length()>0){
				builder.append(" ");
			}					
			builder.append(remaining_words.get(loop));
		}
		String remaining_context = builder.toString();	
		String entity_context = entity_title.replaceAll("[^A-Za-z0-9 ]", " ");
		String[] words1 = entity_context.split("\\s+");
		String[] words2 = remaining_context.split("\\s+");
		System.out.println(entity_context + ", " + remaining_context);
		score =  EmbeddingHelper.getSimilarityValue2_1(words1, words2);		
		return score;
	}
	/** compute the score, using the first paragraph in Wiki and the remaining words of query, getSimilarityValue2()
	 * 
	 * @param entity_id
	 * @param mention
	 * @param query
	 * @return
	 * @throws IOException 
	 */
	public static double cmptContext2(int entity_id, String mention, String query) throws IOException{
		double score = 0;
		String text_entity = CrawlerHelper.getWikiPageDescription(entity_id);
		
//		String entity_context = text_entity.substring(begin_pos, end_pos+1).replaceAll("[^A-Za-z0-9 ]", " ");
		if(text_entity==null){
			System.out.println("Problem: " + entity_id);
			return score;
		}
			
		String entity_context = text_entity.replaceAll("[^A-Za-z0-9 ]",  " ");
//		System.out.println(text_entity);
//		System.out.println(entity_context);
		ArrayList<String> remaining_words = new ArrayList<String>(Arrays.asList(query.replaceAll("[^A-Za-z0-9 ]", " ").split("\\s+")));
		for(String s: mention.split("\\s+")){
			remaining_words.remove(s);
		}
		StringBuilder builder = new StringBuilder();
		for(int loop=0; loop<remaining_words.size(); loop++){ // 
			if(builder.length()>0){
				builder.append(" ");
			}					
			builder.append(remaining_words.get(loop));
		}
		String remaining_context = builder.toString();
		String[] words1 = entity_context.split("\\s+");
		String[] words2 = remaining_context.split("\\s+");
		System.out.println(entity_context + ", " + remaining_context);
		score =  EmbeddingHelper.getSimilarityValue2(words1, words2);		
		return score;
	}
	public static double cmptContext2_1(int entity_id, String mention, String query) throws IOException{
		double score = 0;
		String text_entity = CrawlerHelper.getWikiPageDescription(entity_id);
		
//		String entity_context = text_entity.substring(begin_pos, end_pos+1).replaceAll("[^A-Za-z0-9 ]", " ");
		if(text_entity==null){
			System.out.println("Problem: " + entity_id);
			return score;
		}
			
		String entity_context = text_entity.replaceAll("[^A-Za-z0-9 ]",  " ");
//		System.out.println(text_entity);
//		System.out.println(entity_context);
		ArrayList<String> remaining_words = new ArrayList<String>(Arrays.asList(query.replaceAll("[^A-Za-z0-9 ]", " ").split("\\s+")));
		for(String s: mention.split("\\s+")){
			remaining_words.remove(s);
		}
		StringBuilder builder = new StringBuilder();
		for(int loop=0; loop<remaining_words.size(); loop++){ // 
			if(builder.length()>0){
				builder.append(" ");
			}					
			builder.append(remaining_words.get(loop));
		}
		String remaining_context = builder.toString();
		String[] words1 = entity_context.split("\\s+");
		String[] words2 = remaining_context.split("\\s+");
		System.out.println(entity_context + ", " + remaining_context);
		score =  EmbeddingHelper.getSimilarityValue2_1(words1, words2);		
		return score;
	}
}