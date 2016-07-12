package annotatorstub.utils;

import java.lang.Math;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class EmbeddingHelper {
	final static int dim = 300;
	final static String dict_path = "/Users/hanzhichao/Documents/ETH_Courses/NLP/project/eclipse_workspace/query-annotator-stub/glove.6B.300d.txt";
//	final static String dict_path = "/Users/hanzhichao/Documents/ETH_Courses/NLP/project/eclipse_workspace/query-annotator-stub/deps.words";
	static HashMap<String, double[]> dict = null;
	
	public static void main(String[] args) throws IOException{
//		String str1 = "I Like  Keri Hilson song";
//		String str2 =  "vodka sauce";
//		String[] str1 = {"norway", "denmark", "finland", "switzerland", "belgium", "netherlands", "iceland", "slovenia"};
//		String[] str2 = {"Sweden"};
		String[] str1 = {"Default  finance", "Philadelphia", "Philly TV series "};
		String[] str2 = {"c book", "south st stores", "south st stores"};
		for(int i=0; i<str1.length; i++){
			System.out.println(str1[i].split("\\s+").length);
			System.out.println(str2[i].split("\\s+").length);
			double score = EmbeddingHelper.getSimilarityValue2(str1[i].split("\\s+"), str2[i].split("\\s+"));
			System.out.println(i + ", " + str1[i] + ", " + str2[i]);
			System.out.println("score is: " + score);
		}		
		
	}

	/**
	 * Load pre-trained word embeddings. reference:
	 * https://www.cs.bgu.ac.il/~yoavg/publications/nips2014pmi.pdf pre-trained
	 * data: https://github.com/3Top/word2vec-api Wikipedia dependency dataset
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void loadEmbeddings(String path) throws IOException {
		System.out.println("----------------------Start loading word embeddings--------------------\n");
		dict = new HashMap<String, double[]> ();
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str = br.readLine();
		while (str != null && !str.equals("")) {
			String[] slices = str.split(" ", 2);
			String word = slices[0];
			String[] embedding_strs = slices[1].split(" ");
			assert embedding_strs.length == dim;
			double[] embedding = new double[dim];
			for (int i = 0; i < dim; i++) {
				embedding[i] = Double.parseDouble(embedding_strs[i]);
			}
			dict.put(word, embedding);
			str = br.readLine();
		}
		System.out.println("----------------------Finish loading word embeddings--------------------\n\n");
	}

	/**
	 * Compute the Embedding of a String, which contains multiple words by
	 * taking average on each dimension of word embedding.
	 * 
	 * @param str
	 *            The Document to be computed
	 * @return
	 */
	public static double[] computeDocEmbedding(String str) {
		String[] doc = TextHelper.parse(str);
		if(doc.length==0)
			return null;
//		System.out.printf("text words:..............");
//		for(int i=0; i<doc.length; i++){
//			System.out.printf("%s, ", doc[i]);
//		}
//		System.out.printf("\n");
		int numOfWords = 0;
		double[] res = new double[dim];
		for(int i = 0; i < dim; i ++) res[i] = 0;
		for (String word : doc) {
			if (dict.containsKey(word)) {
				numOfWords += 1;
				double[] word_ebd = dict.get(word);
				assert word_ebd.length == dim;
				for (int i = 0; i < dim; i++) {
					res[i] += word_ebd[i];
				}
			}
		}
		for (int i = 0; i < dim; i++)
			res[i] = res[i] / (double) numOfWords;
		return res;
	}
	

	/**
	 * Compute the Cosine Similarity between two documents(centroid of words in doc) in the embedding
	 * space. The larger is this value, the more similar are the two documents.
	 * 
	 * @param doc1
	 *            The String Representation of Document1
	 * @param doc2
	 *            The String Representation of Document2
	 * @throws IOException
	 */
	public static double getSimilarityValue(String doc1, String doc2) throws IOException {
		if (dict == null) {
			loadEmbeddings(dict_path);
		}
		double similarity = 0;
		if (doc1 == null || doc2 == null || doc1.length()==0 || doc2.length()==0) {
			return similarity;
		}
		double[] ebd1 = computeDocEmbedding(doc1);
		double[] ebd2 = computeDocEmbedding(doc2);
		if (ebd1 == null || ebd2 == null) {
			return 0;
		}
		assert ebd1.length == dim && ebd2.length == dim;		
		similarity = cosineSimilarity(ebd1, ebd2);
		return similarity;
	}
	/**
	 * compute Consine similarity of two docs, as the average of word-word distances
	 * @param doc1
	 * @param doc2
	 * @return
	 * @throws IOException
	 */
	public static double getSimilarityValue2(String[] words1, String[] words2) throws IOException{
		if (dict == null) {
			loadEmbeddings(dict_path);
		}
		if (words1.length == 0 || words2.length == 0) {
			return 0;
		}
//		String[] words1 = TextHelper.parse(doc1);
//		String[] words2 = TextHelper.parse(doc2);
		double similarity = 0.0;
		int count = 0;
		for(int i=0; i<words1.length; i++){
			double[] ebd1 = dict.get(words1[i].toLowerCase());			
			for(int j=0; j<words2.length; j++){
				double[] ebd2 = dict.get(words2[j].toLowerCase());
				if ( ebd1 == null || ebd2 == null) {
					similarity += 0;					
				}else{
					similarity += cosineSimilarity(ebd1, ebd2);
					count = count + 1;
				}				
				
			}
		}
		
		similarity = similarity / (words1.length *words2.length);
		return similarity;
	}
	/**
	 * weight getSimilarityValue2, make high score important 
	 * @param words1
	 * @param words2
	 * @return
	 * @throws IOException
	 */
	public static double getSimilarityValue2_1(String[] words1, String[] words2) throws IOException{
		if (dict == null) {
			loadEmbeddings(dict_path);
		}
		if (words1.length == 0 || words2.length == 0) {
			return 0;
		}
//		String[] words1 = TextHelper.parse(doc1);
//		String[] words2 = TextHelper.parse(doc2);
		double similarity = 0.0;
		int count = 0;
		for(int i=0; i<words1.length; i++){
			double[] ebd1 = dict.get(words1[i].toLowerCase());			
			for(int j=0; j<words2.length; j++){
				double[] ebd2 = dict.get(words2[j].toLowerCase());
				if ( ebd1 == null || ebd2 == null) {
					similarity += 0;					
				}else{
					similarity += Math.pow(2,cosineSimilarity(ebd1, ebd2)) * cosineSimilarity(ebd1, ebd2);// here different from getSimilarity2
					count = count + 1;
				}				
				
			}
		}
		
		similarity = similarity / (words1.length *words2.length);
		return similarity;
	}
	


	private static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
//	private 

}