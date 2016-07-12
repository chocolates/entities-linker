package annotatorstub.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

//import org.apache.commons.math3.util.Pair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import it.unipi.di.acube.batframework.utils.Pair;


public class CrawlerHelper {
	final static String wikiUrlPrefix = "http://en.wikipedia.org/wiki/index.html?curid=";

	public static void main(String[] args) throws Exception {
		System.out.println(getWikiPageDescription(32787));
//		HashMap<Integer, Pair<String, Double>> id_title = getWikiTitle("apple");
//		for(int id: id_title.keySet()){
//			System.out.println(id + ": " + id_title.get(id));
//		}

	}

	/**
	 * Get Wikipedia entity description given the entity id
	 * 
	 * @param entity_id
	 * @return String: the description of this entity in wikipedia
	 * @throws IOException
	 */
	public static String getWikiPageDescription(int entity_id) {
		Document doc;
		String s = null;
		try {
			doc = Jsoup.connect(wikiUrlPrefix + entity_id).get();

			Element wikipart = doc.select("div.mw-content-ltr").first();
			Element wikipara = wikipart.select("p").first();
			s = wikipara.text().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return s;
	}
	
	// get all entity id and name for the given mention (in wikiSense)
	public static HashMap<Integer, Pair<String, Double>> getWikiTitle(String mention) {
		String URL_TEMPLATE_SPOT = "http://wikisense.mkapp.it/tag/spot?text=%s";
		// given a streturn the id
		HashMap<Integer, Pair<String, Double>> id_title = new HashMap<Integer, Pair<String, Double>>();// first field is id; second field is "title"
		try {
			String url = String.format(URL_TEMPLATE_SPOT, URLEncoder.encode(mention, "utf-8"));
			
			JSONObject obj = Utils.httpQueryJson(url);
			JSONArray spots = obj.getJSONArray("spots");
			for (int i = 0; i < spots.length(); i++) {				
				JSONObject objI = spots.getJSONObject(i);
				JSONArray ranking = objI.getJSONArray("ranking");
				
				for (int j = 0; j < ranking.length(); j++) {
					JSONObject candidate = ranking.getJSONObject(j);
					int widC = candidate.getInt("id");
					String title = candidate.getString("title");
					Double comm = candidate.getDouble("commonness");
					
					id_title.put(widC, new Pair<String, Double>(title, comm));					
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return id_title;
	}
	
	
}
