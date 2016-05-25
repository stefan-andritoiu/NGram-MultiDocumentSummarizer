package pack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

public class MonteCarloSentenceTree {
	static final int MAX_NUMBER_SENTENCES = 50;

	Summarizer summarizer;
	ArrayList<SentenceProb> mostProbableSentences = new ArrayList<>();
	ArrayList<Node> openNodes =  new ArrayList<Node>();

	public MonteCarloSentenceTree(Summarizer summarizer) {
		this.summarizer = summarizer;
	}

	public void buildTree() {
		Node padding = new Node("<pad>", null, 0, (Summarizer.USE_LOG_PROB) ? 0 : 1);
		Node start = new Node("<S>", padding, 1, (Summarizer.USE_LOG_PROB) ? 0 : 1);
		openNodes.add(start);

		while (!openNodes.isEmpty() && openNodes.size() < MAX_NUMBER_SENTENCES) {
			Node n1 = openNodes.remove(0);
			Node n0 = n1.parent;

			for (Entry<String, Double> e : summarizer.bgram.getAllPossibleNextWords(n1.word)) {
				String w2_candidate = e.getKey();
				double prob_of_ngram = summarizer.getScore(n0.word, n1.word, w2_candidate);
				double prob_of_node = prob_of_ngram + n1.prob_of_node;
				
				Node n2 = new Node(w2_candidate, n1, n1.depth + 1, prob_of_node);

				if (n2.word.equals("<E>")) {
					addToMostProbableSentences(n2);
				} else {
					if (n2.depth < Summarizer.MAX_SENTENCE_LEN){
						if(!isDumbCycle(n2)){
							openNodes.add(n2);
						}
					}
				}
			}
		}
		
		//unravel all frontier nodes
		while (!openNodes.isEmpty()) {
			Node n1 = openNodes.remove(openNodes.size()-1);
			Node n0 = n1.parent;

			double best_prob = -Summarizer.INF;
			Node best_n2 = null;
			for (Entry<String, Double> e : summarizer.bgram.getAllPossibleNextWords(n1.word)) {
				String w2_candidate = e.getKey();
				double prob_of_ngram = summarizer.getScore(n0.word, n1.word, w2_candidate);
				double prob_of_node = prob_of_ngram + n1.prob_of_node;
				
				Node n2 = new Node(w2_candidate, n1, n1.depth + 1, prob_of_node);
			
				if (n2.word.equals("<E>")) {
					addToMostProbableSentences(n2);
				} else {
					if(!isDumbCycle(n2)){//if it's not a cycle, consider it for the best node
						if (best_prob < n2.prob_of_node){
							best_prob = n2.prob_of_node;
							best_n2 = n2;
						}
					}
				}
			}
			if(best_n2 != null)
				openNodes.add(best_n2);
		}
	}

	public void addToMostProbableSentences(Node endNode) {
		StringBuilder sentence = new StringBuilder("");
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node node = endNode;

		while (node.parent != null) {
			nodeList.add(node);
			node = node.parent;
		}
		for (int i = nodeList.size() - 1; i >= 0; i--) {
			sentence.append(' ');
			sentence.append(nodeList.get(i).word);
		}
		
		mostProbableSentences.add(new SentenceProb(sentence.toString(), endNode.prob_of_node, endNode.depth));
		//System.out.println("Added:\n" + sentence.toString()+ "    " + endNode.prob_of_node);
	}
	
	public boolean isDumbCycle(Node nodeI) {
		Node nodeJ = nodeI.parent;
		
		while (nodeJ != null){
			if (nodeJ.word.equals(nodeI.word))
				return true;
			nodeJ = nodeJ.parent;
		}
		
		return false;
	}
	
	public boolean isCycle(Node nodeI){
		Node nodeJ = nodeI.parent;
		
		while(nodeJ != null){
			if(nodeI.word.equals(nodeJ.word)){
				System.out.println("\t'" + nodeJ.word + "'  equals  '" + nodeI.word + "' |  testing eerything in between");
				
				Node nodeIK = nodeI.parent;
				Node nodeJK = nodeJ.parent;
				
				while(nodeJK != null){
					System.out.println("\t\t\t'" + nodeJK.word + "' against '" + nodeIK.word + "'");
					
					if(!nodeIK.word.equals(nodeJK.word)) break;
					if(nodeIK == nodeJ) return true;
					
					nodeIK = nodeIK.parent;
					nodeJK = nodeJK.parent;
				}
			}
			nodeJ = nodeJ.parent;
		}
		
		return false;
	}
	
//------------------------printers-------------------------------------
	public void printMostProbableSentences(){
		
		Collections.sort(mostProbableSentences, new SentenceComparator());
			
		for (SentenceProb sentenceProb : mostProbableSentences) {
			System.out.println(sentenceProb.sentence + "    " + sentenceProb.prob);
		}
	}
	
//-------------------------internal classes-------------------------------
	private class Node {
		String word;
		Node parent;
		double prob_of_node;
		int depth;

		public Node(String word, Node parent, int depth, double prob_of_node) {
			this.word = word;
			this.parent = parent;
			this.depth = depth;
			this.prob_of_node = prob_of_node;
		}
	}

	private class SentenceProb {
		String sentence;
		double prob;
		int length;

		public SentenceProb(String sentence, double prob, int length) {
			this.sentence = sentence;
			this.prob = prob;
			this.length = length;
		}
	}
	
	private class SentenceComparator implements Comparator<SentenceProb> {

		@Override
		public int compare(SentenceProb sentence1, SentenceProb sentence2) {
			double prob_of_sentence1 = sentence1.prob/sentence1.length;
			double prob_of_sentence2 = sentence2.prob/sentence2.length;
			
			if(prob_of_sentence1 > prob_of_sentence2)
				return -1;
			
			if(prob_of_sentence1 < prob_of_sentence2)
				return 1;
			
			return 0;
		}
	}
}
