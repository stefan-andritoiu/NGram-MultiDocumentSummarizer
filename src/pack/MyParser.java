package pack;

import java.util.ArrayList;

public class MyParser {
	public static String replaceDelimiters(String text){
        return text.replace(".", " <E> <S> ");
	}

	public static String removeCharacters(String text) {
	    return text.replaceAll("(\\r|\\n|,| the |\"|'[a-z]* |‘)", " ");
	}

	public static ArrayList<String> split(String text){
		ArrayList<String> list = new ArrayList<String>();
		list.add("<S>");
		
        int pos = 0, end;
        while ((end = text.indexOf(" ", pos)) >= 0) {
        	if(end != pos)
        		list.add(text.substring(pos, end));
            pos = end + 1;
        }
        
        //make sure list ends in <E>
        if (list.get(list.size()-1).equals("<S>"))
        	list.remove(list.size()-1);
        else
        	list.add("<E>");
        
        return list;
	}
}
