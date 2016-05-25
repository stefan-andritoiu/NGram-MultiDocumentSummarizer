package pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class Tgram {
	
	HashMap<String, HashMap<String, HashMap<String, Integer>>> countTrigramTable;
	HashMap<String, HashMap<String, HashMap<String, Double>>> probabilityTrigramTable;

	public Tgram() {
		countTrigramTable = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
	}

	public void InitFromWordList(ArrayList<String> words) {
		HashMap<String, HashMap<String, Integer>> countBigramTable_forWord;
		HashMap<String, Integer> countUnigramTable_forWord;
		Integer unigramCount_forWord;

		for (int index = 0; index < words.size() - 2; index++) {
			
			String w0 = words.get(index);
			String w1 = words.get(index + 1);
			String w2 = words.get(index + 2);
			
			// Get bigram table for bigrams that are followed by w2
			// If not existent then create it
			countBigramTable_forWord = countTrigramTable.get(w2);
			if (countBigramTable_forWord == null) {
				countBigramTable_forWord = new HashMap<String, HashMap<String, Integer>>();
				countTrigramTable.put(w2, countBigramTable_forWord);
			}

			// Get unigram table for unigrams that are followed by w1w2
			// If not existent then create it
			countUnigramTable_forWord = countBigramTable_forWord.get(w1);
			if (countUnigramTable_forWord == null) {
				countUnigramTable_forWord = new HashMap<String, Integer>();
				countBigramTable_forWord.put(w1, countUnigramTable_forWord);
			}

			// Get count of w0 and increment it
			// If not existent then create it
			unigramCount_forWord = countUnigramTable_forWord.get(w0);
			if (unigramCount_forWord == null) {
				unigramCount_forWord = new Integer(0);
				countUnigramTable_forWord.put(w0, unigramCount_forWord);
			}

			unigramCount_forWord++;
			countUnigramTable_forWord.put(w0, unigramCount_forWord);
		}
	}

	public void calculateProbabilityFromBgram(Bgram bgram) {
		probabilityTrigramTable = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
		
		for (Entry<String, HashMap<String, HashMap<String, Integer>>> eT : countTrigramTable.entrySet()) {
			String w2 = eT.getKey();
			HashMap<String, HashMap<String, Integer>> countBigramTable_forWord = eT.getValue();
			
			for (Entry<String, HashMap<String, Integer>> eB : countBigramTable_forWord.entrySet()) {
				String w1 = eB.getKey();
				HashMap<String, Integer> countUnigramTable_forWord = eB.getValue();

				for (Entry<String, Integer> eU : countUnigramTable_forWord.entrySet()) {
					String w0 = eU.getKey();
					Integer unigramCount_forWord = eU.getValue();

					//System.out.println(w0 + " " + w1 + " " + w2 + " " + unigramCount);
					
					// Get count of w0w1 and w0w1w2
					double w0w1w2_count = (double) unigramCount_forWord;
					double w0w1_count = (double) bgram.getBigramCount(w0, w1);
					
					// Calculate and store probability of a word w2, given w0w1
					double prob = w0w1w2_count/w0w1_count;
					if (Summarizer.USE_LOG_PROB)
						prob = Math.log(prob);
					
					addProbabilityOfWordGivenBigram(prob, w0, w1, w2);
				}
			}
		}
	}
	
	//probability of w2, given already seen w0w1
	public void addProbabilityOfWordGivenBigram(Double prob, String w0, String w1, String w2){
		HashMap<String, HashMap<String, Double>> hashMapBigram;
		HashMap<String, Double> hashMapUnigram;
		
		// Get HashMap of bigrams, preceded by w0
		// If not existent then create it
		hashMapBigram = probabilityTrigramTable.get(w0);
		if (hashMapBigram == null) {
			hashMapBigram = new HashMap<String, HashMap<String, Double>>();
			probabilityTrigramTable.put(w0, hashMapBigram);
		}

		// Get HashMap of unigrams, preceded by w0w1
		// If not existent then create it
		hashMapUnigram = hashMapBigram.get(w1);
		if (hashMapUnigram == null) {
			hashMapUnigram = new HashMap<String, Double>();
			hashMapBigram.put(w1, hashMapUnigram);
		}

		// Put probability of unigrams, given w0w1
		hashMapUnigram.put(w2, prob);
	}
	
	public int getTrigramCount(String w0, String w1, String w2) {
		return countTrigramTable.get(w2).get(w1).get(w0);
	}

	public double getTrigramProbability(String w0, String w1, String w2) {
		HashMap<String, HashMap<String, Double>> hashMapBigram = probabilityTrigramTable.get(w0);
		if (hashMapBigram == null) return 0;
		
		HashMap<String, Double> hashMapUnigram = hashMapBigram.get(w1);
		if (hashMapUnigram == null) return ((Summarizer.USE_LOG_PROB) ? (-Summarizer.UNSEEN_WORD_PROB) : 0);
		
		Double prob = hashMapUnigram.get(w2);
		if (prob == null) return ((Summarizer.USE_LOG_PROB) ? (-Summarizer.UNSEEN_WORD_PROB) : 0);
		
		return prob;
	}

	public Set<Entry<String, Double>> getAllPossibleNextWords(String w0, String w1){
		return probabilityTrigramTable.get(w0).get(w1).entrySet();
	}
	
	public void printTrigramCountTable() {

		for (Entry<String, HashMap<String, HashMap<String, Integer>>> eT : countTrigramTable.entrySet()) {
			String w2 = eT.getKey();
			HashMap<String, HashMap<String, Integer>> countBigramTable_forWord = eT.getValue();

			for (Entry<String, HashMap<String, Integer>> eB : countBigramTable_forWord.entrySet()) {
				String w1 = eB.getKey();
				HashMap<String, Integer> countUnigramTable_forWord = eB.getValue();

				for (Entry<String, Integer> eU : countUnigramTable_forWord.entrySet()) {
					String w0 = eU.getKey();
					Integer unigramCount_forWord = eU.getValue();

					System.out.println(w0 + " " + w1 + " " + w2 + " " + unigramCount_forWord);
				}
			}
		}
	}
	
	public void printTrigramProbabilityTable() {

		for (Entry<String, HashMap<String, HashMap<String, Double>>> eT : probabilityTrigramTable.entrySet()) {
			String w0 = eT.getKey();
			HashMap<String, HashMap<String, Double>> hashMapBigram = eT.getValue();
			
			for (Entry<String, HashMap<String, Double>> eB : hashMapBigram.entrySet()) {
				String w1 = eB.getKey();
				HashMap<String, Double> hashMapUnigram = eB.getValue();

				for (Entry<String, Double> eU : hashMapUnigram.entrySet()) {
					String w2 = eU.getKey();
					Double unigramProbability = eU.getValue();

					System.out.println(w0 + " " + w1 + " " + w2 + " " + unigramProbability);
				}
			}
		}
	}
}
