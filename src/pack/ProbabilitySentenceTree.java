package pack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Map.Entry;

public class ProbabilitySentenceTree {
	static final int MAX_NUMBER_SENTENCES = 5000;

	Summarizer summarizer;
	ArrayList<SentenceProb> mostProbableSentences = new ArrayList<>();
	PriorityQueue<Node> openNodes =  new PriorityQueue<Node>(MAX_NUMBER_SENTENCES, new NodeComparator());

	public ProbabilitySentenceTree(Summarizer summarizer) {
		this.summarizer = summarizer;
	}

	public void buildTree() {
		int number_of_sentences=0;
		Node padding = new Node("<pad>", null, 0, (Summarizer.USE_LOG_PROB) ? 0 : 1);
		Node start = new Node("<S>", padding, 1, (Summarizer.USE_LOG_PROB) ? 0 : 1);
		openNodes.add(start);

		while (!openNodes.isEmpty() && number_of_sentences < MAX_NUMBER_SENTENCES) {
			Node n1 = openNodes.poll();
			Node n0 = n1.parent;

			for (Entry<String, Double> e : summarizer.bgram.getAllPossibleNextWords(n1.word)) {
				String w2_candidate = e.getKey();
				double prob_of_ngram = summarizer.getScore(n0.word, n1.word, w2_candidate);
				double prob_of_node = prob_of_ngram + n1.prob_of_node;
				
				Node n2 = new Node(w2_candidate, n1, n1.depth + 1, prob_of_node);

				if (n2.word.equals("<E>")) {
					addToMostProbableSentences(n2);
					number_of_sentences++;
				} else {
					if (n2.depth < Summarizer.MAX_SENTENCE_LEN){
						if(!isDumbCycle(n2)){
							openNodes.add(n2);
						}
					}
				}
			}
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
		
		mostProbableSentences.add(new SentenceProb(sentence.toString(), endNode.prob_of_node));
		System.out.println("Added:\n" + sentence.toString()+ "    " + endNode.prob_of_node);
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

		public SentenceProb(String sentence, double prob) {
			this.sentence = sentence;
			this.prob = prob;
		}
	}
	
	private class NodeComparator implements Comparator<Node> {

		@Override
		public int compare(Node node1, Node node2) {
			double prob_of_sentence1 = node1.prob_of_node/node1.depth;
			double prob_of_sentence2 = node2.prob_of_node/node2.depth;
			
			if(prob_of_sentence1 > prob_of_sentence2)
				return -1;
			
			if(prob_of_sentence1 < prob_of_sentence2)
				return 1;
			
			return 0;
		}
	}
}
