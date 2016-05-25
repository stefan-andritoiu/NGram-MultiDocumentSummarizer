package pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class Summarizer {
	static final boolean USE_LOG_PROB = true;
	static final double INF = Double.MAX_VALUE;
	static final double UNSEEN_WORD_PROB = 0;
	static final int MAX_SENTENCE_LEN = 20;
	static double A=1, B=1, C=1;

	Tgram tgram;
	Bgram bgram;
	Ugram ugram;
	int total_number_of_words;
	long duration;
	
	public Summarizer(ArrayList<String> list) {
		// -------------------------- start timer-----------------------
		long startTime = System.nanoTime();

		total_number_of_words = list.size();

		// Trigram count
		tgram = new Tgram();
		tgram.InitFromWordList(list);

		// Bigram count
		bgram = new Bgram();
		bgram.InitFromTgram(tgram);
		// compensate for 1st bigram
		bgram.incBigram(list.get(0), list.get(1));

		// Unigram count
		ugram = new Ugram();
		ugram.InitFromBgram(bgram);
		// compensate for 1st unigram
		ugram.incUnigram(list.get(0));

		// Trigram prob
		tgram.calculateProbabilityFromBgram(bgram);

		// Bigram prob
		bgram.calculateProbabilityFromUgram(ugram);

		// Trigram prob
		ugram.calculateProbabilityFromNumberOfWords(total_number_of_words);

		// -------------------------- end timer-----------------------
		long endTime = System.nanoTime();
		duration = (endTime - startTime);
	}

	public void mostProbableSentence() {
		ArrayList<String> sentence = new ArrayList<>();
		sentence.add("<S>");
		sentence.add("<S>");

		int index = 0;
		while (true) {
			String w0 = sentence.get(index);
			String w1 = sentence.get(index + 1);
			String w2 = null;
			double best_score = -INF;

			for (Entry<String, Double> e : bgram.getAllPossibleNextWords(w1)) {
				String w2_candidate = e.getKey();
				if (sentence.contains(w2_candidate)) 
					continue;
				
				double score = getScore(w0, w1, w2_candidate);
				System.out.println(w2_candidate + "    " + score);
				if (best_score < score) {
					best_score = score;
					w2 = w2_candidate;
				}
			}
			//System.out.println(w2);
			sentence.add(new String(w2));

			index++;
			if (index > MAX_SENTENCE_LEN || w2.equals("<E>"))
				break;
		}
		for (String word : sentence) 
			System.out.print(word + " ");	
	}

	public double getScore(String w0, String w1, String w2) {
		//System.out.println(w0 + " " + w1 + " " + w2);
		double score = A * tgram.getTrigramProbability(w0, w1, w2) + B * bgram.getBigramProbability(w1, w2)
				+ C * ugram.getUnigramProbability(w2);

		return score;
	}

	public void printInfo() {
		System.out.println("Trigram count:");
		tgram.printTrigramCountTable();
		System.out.println();

		System.out.println("Bigram count:");
		bgram.printBigramCountTable();
		System.out.println();

		System.out.println("Unigram count:");
		ugram.printUnigramCountTable();
		System.out.println();

		System.out.println("Trigram probability:");
		tgram.printTrigramProbabilityTable();
		System.out.println();

		System.out.println("Bigram probability:");
		bgram.printBigramProbabilityTable();
		System.out.println();

		System.out.println("Unigram probability:");
		ugram.printUnigramProbabilityTable();
		System.out.println();

		System.out.println("Total words: " + total_number_of_words);
		System.out.println("Number of words in vocabulary: " + ugram.getNumberOfIndividualWords());

		System.out.println("Duration of n-gram creation: " + duration / 1000000 + " ms");
	}
}
