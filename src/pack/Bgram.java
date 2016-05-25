package pack;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class Bgram {
	
	
	HashMap<String, HashMap<String, Integer>> countBigramTable;
	HashMap<String, HashMap<String, Double>> probabilityBigramTable;

	public Bgram() {
		countBigramTable = new HashMap<String, HashMap<String, Integer>>();
	}

	// This function creates a bigram count for each w1w2 by summing up all
	// trigram counts that end in w1w2
	// Count(w1w2) = Sum Count(w0w1w2); where w0 E vocabulary
	public void InitFromTgram(Tgram tgram) {
		for (Entry<String, HashMap<String, HashMap<String, Integer>>> eT : tgram.countTrigramTable.entrySet()) {
			String w2 = eT.getKey();
			HashMap<String, HashMap<String, Integer>> countBigramTable_forWord_fromTrigram = eT.getValue();

			HashMap<String, Integer> countUnigramTable_forWord = new HashMap<String, Integer>();
			countBigramTable.put(w2, countUnigramTable_forWord);

			for (Entry<String, HashMap<String, Integer>> eB : countBigramTable_forWord_fromTrigram.entrySet()) {
				String w1 = eB.getKey();
				HashMap<String, Integer> countUnigramTable_forWord_fromTrigram = eB.getValue();

				Integer unigramCount_forWord = new Integer(0);
				countUnigramTable_forWord.put(w1, unigramCount_forWord);

				for (Entry<String, Integer> eU : countUnigramTable_forWord_fromTrigram.entrySet()) {
					String w0 = eU.getKey();
					Integer unigramCount_forWord_fromTrigram = eU.getValue();

					unigramCount_forWord += unigramCount_forWord_fromTrigram;
				}

				countUnigramTable_forWord.put(w1, unigramCount_forWord);
			}
		}
	}

	public void calculateProbabilityFromUgram(Ugram ugram) {
		probabilityBigramTable = new HashMap<String, HashMap<String, Double>>();

		for (Entry<String, HashMap<String, Integer>> eB : countBigramTable.entrySet()) {
			String w1 = eB.getKey();
			HashMap<String, Integer> countUnigramTable_forWord = eB.getValue();

			for (Entry<String, Integer> eU : countUnigramTable_forWord.entrySet()) {
				String w0 = eU.getKey();
				Integer unigramCount_forWord = eU.getValue();

				// Get count of w0 and w0w1
				double w0w1_count = (double) unigramCount_forWord;
				double w0_count = (double) ugram.getUnigramCount(w0);

				// Calculate and store probability of a word w1, given w0
				double prob = w0w1_count / w0_count;
				if (Summarizer.USE_LOG_PROB)
					prob = Math.log(prob);
				
				addProbabilityOfWordGivenUnigram(prob, w0, w1);
			}
		}
	}

	// probability of w1, given already seen w0
	public void addProbabilityOfWordGivenUnigram(Double prob, String w0, String w1) {
		HashMap<String, Double> hashMapUnigram;

		// Get HashMap of unigrams, preceded by w0
		// If not existent then create it
		hashMapUnigram = probabilityBigramTable.get(w0);
		if (hashMapUnigram == null) {
			hashMapUnigram = new HashMap<String, Double>();
			probabilityBigramTable.put(w0, hashMapUnigram);
		}

		// Put probability of unigrams, given w0
		hashMapUnigram.put(w1, prob);
	}

	public void incBigram(String w0, String w1) {
		HashMap<String, Integer> countUnigramTable_forWord;
		Integer unigramCount_forWord;

		// Get unigram table for unigrams that are followed by w1w2
		// If not existent then create it
		countUnigramTable_forWord = countBigramTable.get(w1);
		if (countUnigramTable_forWord == null) {
			countUnigramTable_forWord = new HashMap<String, Integer>();
			countBigramTable.put(w1, countUnigramTable_forWord);
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

	public int getBigramCount(String w0, String w1) {
		Integer count = countBigramTable.get(w1).get(w0);
		if (count == null)
			System.err.println("No count for bigram " + w0 + " " + w1);
		return count;
	}
	
	public double getBigramProbability(String w0, String w1) {
		HashMap<String, Double> hashMapUnigram = probabilityBigramTable.get(w0);
		if (hashMapUnigram == null) return 0;
		
		Double prob = hashMapUnigram.get(w1);
		if (prob == null) return 0;
		
		return prob;
	}

	public Set<Entry<String, Double>> getAllPossibleNextWords(String w0){
		return probabilityBigramTable.get(w0).entrySet();
	}
	
	public void printBigramCountTable() {

		for (Entry<String, HashMap<String, Integer>> eB : countBigramTable.entrySet()) {
			String w1 = eB.getKey();
			HashMap<String, Integer> countUnigramTable_forWord = eB.getValue();

			for (Entry<String, Integer> eU : countUnigramTable_forWord.entrySet()) {
				String w0 = eU.getKey();
				Integer unigramCount_forWord = eU.getValue();

				System.out.println(w0 + " " + w1 + " " + " " + unigramCount_forWord);
			}
		}
	}

	public void printBigramProbabilityTable() {

		for (Entry<String, HashMap<String, Double>> eB : probabilityBigramTable.entrySet()) {
			String w0 = eB.getKey();
			HashMap<String, Double> hashMapUnigram = eB.getValue();

			for (Entry<String, Double> eU : hashMapUnigram.entrySet()) {
				String w1 = eU.getKey();
				Double unigramProbability = eU.getValue();

				System.out.println(w0 + " " + w1 + " " + unigramProbability);
			}
		}

	}
}
