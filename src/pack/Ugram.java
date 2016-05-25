package pack;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class Ugram {
	
	HashMap<String, Integer> countUnigramTable;
	HashMap<String, Double> probabilityUnigramTable;

	public Ugram() {
		countUnigramTable = new HashMap<String, Integer>();
	}

	// This function creates a unigram count for each w1 by summing up all
	// bigram counts that end in w1
	// Count(w1) = Sum Count(w0w1); where w0 E vocabulary
	public void InitFromBgram(Bgram bgram) {

		for (Entry<String, HashMap<String, Integer>> eB : bgram.countBigramTable.entrySet()) {
			String w1 = eB.getKey();
			HashMap<String, Integer> countUnigramTable_forWord_fromBigram = eB.getValue();

			Integer unigramCount = new Integer(0);
			countUnigramTable.put(w1, unigramCount);

			for (Entry<String, Integer> eU : countUnigramTable_forWord_fromBigram.entrySet()) {
				String w0 = eU.getKey();
				Integer unigramCount_forWord_fromBigram = eU.getValue();

				unigramCount += unigramCount_forWord_fromBigram;
			}

			countUnigramTable.put(w1, unigramCount);
		}
	}

	public void calculateProbabilityFromNumberOfWords(double number_of_words) {
		probabilityUnigramTable = new HashMap<String, Double>();

		for (Entry<String, Integer> eU : countUnigramTable.entrySet()) {
			String w0 = eU.getKey();
			Integer unigramCount = eU.getValue();

			// Get count of w0
			double w0_count = (double) unigramCount;

			// Calculate and store probability of a word w0
			double prob = w0_count / number_of_words;
			if (Summarizer.USE_LOG_PROB)
				prob = Math.log(prob);
			
			probabilityUnigramTable.put(w0, prob);
		}

	}

	public void incUnigram(String w0) {
		Integer unigramCount_forWord;

		// Get count of w0 and increment it
		// If not existent then create it
		unigramCount_forWord = countUnigramTable.get(w0);
		if (unigramCount_forWord == null) {
			unigramCount_forWord = new Integer(0);
			countUnigramTable.put(w0, unigramCount_forWord);
		}

		unigramCount_forWord++;
		countUnigramTable.put(w0, unigramCount_forWord);
	}

	public int getUnigramCount(String w0) {
		return countUnigramTable.get(w0);
	}
	
	public double getUnigramProbability(String w0) {
		return probabilityUnigramTable.get(w0);
	}

	public void printUnigramCountTable() {

		for (Entry<String, Integer> eU : countUnigramTable.entrySet()) {
			String w0 = eU.getKey();
			Integer unigramCount = eU.getValue();

			System.out.println(w0 + " " + " " + " " + unigramCount);
		}

	}

	public void printUnigramProbabilityTable() {

		for (Entry<String, Double> eU : probabilityUnigramTable.entrySet()) {
			String w0 = eU.getKey();
			Double unigramProbability = eU.getValue();

			System.out.println(w0 + " " + unigramProbability);
		}
	}
	
	public int getNumberOfIndividualWords() {
		return countUnigramTable.size();
	}
	
	public Set<Entry<String, Double>> getAllPossibleNextWords(){
		return probabilityUnigramTable.entrySet();
	}
}
