package pack;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
	static final boolean SMALL_TEXT = false;

	public static void main(String[] args) {
		String text = null;
		if (SMALL_TEXT) {
			text = "mama are mere , mama are banane, mama n-are castane , "
					+ "radu are banane . radu n-are castane radu are banane, \n \n tata are chiftele si tata are chiftele\n "
					+ "tata n-are banane . ";
		} else {
			
			try {
				//text = new String(Files.readAllBytes(Paths.get("battery-life_amazon_kindle.txt.data")));
				text = new String(Files.readAllBytes(Paths.get("bathroom_bestwestern_hotel_sfo.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("room_holiday_inn_london.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("rooms_bestwestern_hotel_sfo.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("sound_ipod_nano_8gb.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("video_ipod_nano_8gb.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("voice_garmin_nuvi_255W_gps.txt.data")));
				//text = new String(Files.readAllBytes(Paths.get("Soviet_union")));
							
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		text = text.toLowerCase();
		text = MyParser.removeCharacters(text);
		text = MyParser.replaceDelimiters(text);

		ArrayList<String> list = MyParser.split(text);

		for (String str : list) {
			System.out.print("'" + str + "' ");
		}
		System.out.println("\n");

		Summarizer summarizer = new Summarizer(list);
		summarizer.printInfo();
		
		//Most prob sentence
		//summarizer.mostProbableSentence();
		
		//Probability tree
		ProbabilitySentenceTree sentenceTree = new ProbabilitySentenceTree(summarizer);
		
		//Monte Carlo approach
		//MonteCarloSentenceTree sentenceTree = new MonteCarloSentenceTree(summarizer);
		
		
		sentenceTree.buildTree();
		sentenceTree.printMostProbableSentences();
		
	}

}
