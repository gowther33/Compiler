import java.io.BufferedReader;
// import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Arrays;
import java.util.List;
// import java.io.FileWriter;

// . = ref (Object reference)

public class LexicalAnalyzer {

	public static void main(String[] args) {
		LexicalAnalyzer LA = new LexicalAnalyzer();
		LA.generateTokens();
	}
	// ArrayList to store tokens
	public ArrayList<Token> tokenList = new ArrayList<>();
	public ArrayList<String> listOfLines = new ArrayList<>();
	private static Character[] breakers1 = {'~','+','-','*','/','%','^','|','&','=','<','>'};
	private static List<Character> breakersOp = Arrays.asList(breakers1);
	private static Character[] breakers2 = {' ',';','{','}','(',')','[',']',',','.'};
	private static List<Character> breakersPunc = Arrays.asList(breakers2); 

	// For variables
//	public static String varstr = "[A-Za-z]([A-Za-z_]*|[0-9]|)*|_[A-Za-z]|[0-9]([A-Za-z]|[0-9]|_)*";
	
	public static String varstr = "_[A-Z a-z 0-9][A-Z a-z 0-9_]*|[A-Z a-z][A-Z a-z 0-9 _]*";
	
	public static Pattern varPattern = Pattern.compile(varstr);
	// Matcher
	public static Matcher matcher;
	public static int lineNo = 1;
	
	
	// List of punctuators, operators, and keywords Can also store in hashmap
	//	public static String[][] specialSym = {{"access modifier","$"},{"access modifier","#"},{"access modifier","##"},{"inheritance",":"}}; 
	public static String[][] punc = {{"semicolon",";"},{"lp","("},{"rp",")"},{"lcb","{"},{"rcb","}"},{"comma",","},{"[","["},{"]","]"}};
	public static String[][] Arithematic = {{"PM","+"},{"PM","-"},{"MMD","*"},{"MMD","/"},{"MMD","%"},{"Pow","^"}};
	public static String[][] KW= {{"DT", "int"},{"DT", "float"},{"DT", "chr"},{"DT", "str"},{"DT", "bool"},{"if","if"},{"else","else"},{"while","repeat"},{"for","iterate"},{"break","quit"},{"continue","ignore"},{"func","func"},{"return","return"},{"class","model"},{"try","run"},{"grab","grab"},{"my","my"},{"base","base"},{"new","obj"},{"finally","finally"},{"abstract","hide"}};
	
	// Method to check Keywords and IDs
	public static String isKeyword_ID(String word) {
		// Check KW
	    for(short row = 0; row < KW.length; row++){
	    	if((KW[row][1]).equals(word)) {
            	// It will match a specific keyword and we will return the class name 
	    		return KW[row][0];
	    	}
	    }
		// If not ID then keyword
		if (varPattern.matcher(word).matches()) {
			return "ID";
		}
	    return "";
	}

	// Method to check if character is alphanumeric
    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]*$");
    }
	
	// Method to tokenize value in temp wherever this funtion is called immediately after its call temp should be empty
	public int tokenizeTemp(String temp, ArrayList<Token> tokenlist, int lineNo){
		// Check if keyword or ID
		String cp = isKeyword_ID(temp);
		if (cp.length()!=0) { 
			if (cp.equals(temp)) { 
				tokenList.add(new Token(cp, cp, lineNo));
				return 0;
			}	
			else {
				tokenList.add(new Token(cp, temp, lineNo));
				return 0;
			}
		}
		// Check if temp has a constant
		else if (CheckingRE(temp, tokenList, lineNo)){
			return 0;
		}
		// IF not then invalid lexeme
		else {
			tokenList.add(new Token("InvalidLexeme",temp, lineNo));
			return 0;
		}
	}

	// Method to check constants
	public static boolean CheckingRE(String temp,ArrayList<Token> tokenlist, int lineNo) {
		boolean isTrue = false; // This flag checks whether any RE matched otherwise invalid lexeme
		// boolean flag = false;
		String [][] RE = {{"[+|-]?[0-9]*[.][0-9]+","float"},{"[+-]?[0-9]+","int"},{"true|false","bool"},
				{"\"(.|\\w)*\"","str"},
				{"\'[\\[\r|\n|\'|\"|\b|\t|\f]]|[a-zA-Z]|[0-9]|[`|~|!|@|#|$|%|^|&|*|(|)|_|_|+|=|{|}|\\[|\\]|<|>|?|.|,]\'","char"}};
		for(int i=0;i<RE.length;i++) {
			boolean bool = true;
			boolean bool1 = true;
			while(temp.length()!=0 && bool){
				Pattern pattern = Pattern.compile(RE[i][0]);
			    Matcher matcher = pattern.matcher(temp);
			    boolean match = matcher.find();
			    if (match) {
					isTrue = true;
			    	// flag = false;
			    	
			    	if(bool==false) {
			    		break;
			    	}

			    	if(i==4 && matcher.start()+matcher.end()==temp.length()) {
			    		// tokenlist.add("ClassName : " + RE[i][1] + " ValueName : " + temp.substring(matcher.start(),matcher.end()));
						tokenlist.add( new Token(RE[i][1], temp.substring(matcher.start(),matcher.end()), lineNo) );
			    		// tokenlist.add("\n");
			    		break;
			    	}
			    	else if(i==4 && matcher.start()+matcher.end()!=temp.length()) {
			    		// tokenlist.add("ClassName : Invalid Lexeme " + " ValueName : " + temp.substring(0,temp.length()));
						tokenlist.add( new Token("InvalidLexeme", temp.substring(matcher.start(),matcher.end()), lineNo) );
			    		// tokenlist.add("\n");
			    		bool=false;
			    		break;	
			    	}
			    	else {
			    		if(matcher.start()+matcher.end()==temp.length()){
			        // System.out.println("Match Found");
			        // System.out.println("\n");
			        
			        //System.out.println(matcher.start());
			        //System.out.println(matcher.end());
			        // tokenlist.add("ClassName : " + RE[i][1] + " ValueName : " + temp.substring(matcher.start(),matcher.end()));
					tokenlist.add( new Token(RE[i][1], temp.substring(matcher.start(),matcher.end()), lineNo) );
			        //tokenlist.add("\n");
			        temp = temp.substring(matcher.end(),temp.length());
			    		}
			    	}
			        }
			    else {
			    	//tokenlist.add("ClassName : Invalid Lexeme " + " ValueName : " + temp.substring(0,temp.length()));
			        bool =false;
			        // flag=true;
			        //bool1 = false;
			        break;
			    }
			}
	        if(bool1==false){
	        	break;
	        }
	        
		}
		return isTrue;
		// System.out.println(tokenlist);
	}

	
	// Function to check dot when temp is not empty
	public int checkDotN(String temp, char ch, ArrayList<Token> tokenlist, int lineNum, int i, int j){
		String temp2 = "";
		int x=i+1; // Index variable to check right side of dot
		// Check if temp is ID 
		if ( varPattern.matcher(temp).matches() )
		{
			// If matches then . is a brekaer so make token of temp as ID
			tokenList.add(new Token("ID", temp, lineNo));
			// temp = "";
			// Check right side of . till a breaker occurs
			// Iterate over the current jth line from x = i+1th index
			for (;x < listOfLines.get(j).length(); x++){
				// If character is not breaker then concat in temp2
				if ( (breakersOp.contains(listOfLines.get(j).charAt(x)) || breakersPunc.contains(listOfLines.get(j).charAt(x))) )
				{
					if ( temp2.length() == 0 )
					{	
						// Case a.+;
						// Make token of Reference
						tokenList.add(new Token("ref", Character.toString(ch), lineNo));
						// Return value of x
						return x;
					}
					// If temp2 is not empty
					else
					{
						// Check if ID then tokenize ID and dot also
						if ( varPattern.matcher(temp2).matches() )
						{
							// Tokenize dot
							tokenList.add(new Token("ref", Character.toString(ch), lineNo));
							// Tokenize ID
							tokenList.add(new Token("ID", temp2, lineNo));
							temp2 = "";
							// Start checking char at index after this index
							return x;
						}

						// Check if temp2 has all digits then concat with . 
						else if ( temp2.matches("[0-9]+") )
						{
							temp2 = ch + temp2;
							// Make token of float
							tokenList.add(new Token("float", temp2, lineNo));
							temp2 = "";
							return x;
						}
						// Otherwise make invalid lexeme of temp2 and tokenize dot
						else
						{   
							// Case a.555g.
							// Tokenize dot
							tokenList.add(new Token("ref", Character.toString(ch), lineNo));
							// Invalid lexeme
							tokenList.add(new Token("InvalidLexeme", temp2, lineNo));
							temp2 = "";
							return x;
						}
					}
				}
				else
				{
					temp2 += Character.toString(listOfLines.get(j).charAt(x));
				}

			}

		}
		// Check if temp is all digits then concat . with it
		else if ( temp.matches("[0-9]+") )
		{
			temp += ch; // Here we use temp
			// Check right side of .
			x = i+1; // index to check line characters after .
			// Iterate over the current jth line from x = i+1th index
			for (;x < listOfLines.get(j).length(); x++)
			{
				if ( (breakersOp.contains(listOfLines.get(j).charAt(x)) || breakersPunc.contains(listOfLines.get(j).charAt(x))) )
				{
					if ( temp2.length() == 0 )
					{
						// Case 55.+;
						// Make token of float
						tokenList.add(new Token("float", temp, lineNo));
						temp = "";
						// Start checking char at x
						return x;
					}
					else
					{
						// If characters after . are all digits
						if ( temp2.matches("[0-9]+") )
						{
							// Case 55.55+
							temp += temp2; 
							tokenList.add(new Token("float", temp, lineNo));
							temp2 = "";
							// Start checking char at x
							return x;			
						}
						// Case 55.aap.
						else if ( varPattern.matcher(temp2).matches() )
						{
							// Make token of float and ID
							tokenList.add(new Token("float", temp, lineNo));
							tokenList.add(new Token("ID", temp2, lineNo));
							temp2 = "";
							return x;
						}
						// Case 55.55pt.
						// Invalid lexeme
						else if ( isAlphaNumeric(temp2) )
						{
							temp += temp2;
							tokenList.add(new Token("InvalidLexeme", temp, lineNo));
							temp2 = "";
							// Start checking char at x
							return x;
						}
					}
				}
				else
				{
					temp2 += Character.toString(listOfLines.get(j).charAt(x));

				}
			}
		}
		// Invalid lexeme by default
		// Case 1356hg.a
		tokenList.add(new Token("InvalidLexeme", temp, lineNo));
		// Tokenize dot
		tokenList.add(new Token("ref", Character.toString(ch), lineNo));
		return i+1; // Check next char in line
	}


	// Function to check dot when temp is empty
	public int checkDotE(char ch, ArrayList<Token> tokenlist, int lineNum, int i, int j){
		String temp2 = "";
		int x = i+1;
		// Iterate over the current jth line from x = i+1th index
		for (;x < listOfLines.get(j).length(); x++)		
		{
			if ( (breakersOp.contains(listOfLines.get(j).charAt(x)) || breakersPunc.contains(listOfLines.get(j).charAt(x))) )
			{
				if ( temp2.length() == 0 )
				{
					// Case .+;
					// Make token of dot and check character at x
					tokenList.add(new Token("ref", Character.toString(ch), lineNo));
					// Start checking char at x
					return x;
				}
				else
				{
					// If characters after . are all digits
					// .555+
					if ( temp2.matches("[0-9]+") )
					{
						temp2 = ch + temp2; 
						tokenList.add(new Token("float", temp2, lineNo));
						temp2 = "";
						// Start checking char at x
						return x;			
					}

					// If ID
					// Case .a.
					else if ( varPattern.matcher(temp2).matches() ){
						// Make token of ref and ID
						tokenList.add(new Token("ref", Character.toString(ch), lineNo));
						tokenList.add(new Token("ID", temp2, lineNo));
						temp2 = "";
						return x;
					}

					// Case .55pt., .kp35p3p, .drttpp
					// Invalid lexeme
					else {
						temp2 = ch + temp2;
						tokenList.add(new Token("InvalidLexeme", temp2, lineNo));
						temp2 = "";
						return x;
					}
				}
			}
			else
			{
				temp2 += Character.toString(listOfLines.get(j).charAt(x));
			}
		}
		return x;
	}
	
	// Method to check arithematic operators
	public static String isArithematic(String word) {
	    for(short row = 0; row < Arithematic.length; row++){
	    	if((Arithematic[row][1]).equals(word)) {
            	// It will match a specific keyword and we will enter the classpart and value 
	    		return Arithematic[row][0];
	    	}
	    }
		return "";
	}

	// // Method to check Punctuators
	public static String isPunctuator(String word) {
	    for(short row = 0; row < punc.length; row++) {
	    	if((punc[row][1]).equals(word)) {
            	// It will match a specific keyword and we will enter the classpart and value 
	    		return punc[row][0];
	    	}
	    }
		return "";
	}

	// Syntax Check
	public ArrayList<Token> getTokens(){
		return this.tokenList;
	}

	// Print the tokens
	public  void printList() {
		for (int i = 0; i < tokenList.size(); i++){
			System.out.println(String.format("Token %d",i) +" "+ tokenList.get(i).toString());			
		}
	}
	
	public void generateTokens()
	{		
		// Reads code line by line and adds to the arraylist
		BufferedReader bufReader;
		try {
			bufReader = new BufferedReader(new FileReader("run.txt"));
			String line = bufReader.readLine(); 
			while (line != null) { 
				listOfLines.add(line);
				line = bufReader.readLine(); 
			} 
			bufReader.close();
		}
		
		catch (IOException e) {
//			System.out.println("File does not exists.");
			e.printStackTrace();
		}
		
		// File to write tokens
		// FileWriter fileWrite = new FileWriter("test.txt"); 
		

		// Read code line by line
		int j = 0;
		for (; j < listOfLines.size(); j++) {
			String temp = "";
			String cls;
			char ch;
			boolean flag = false; // Flag for two character operators
			// boolean flag3 = false; // Flag for occurence of floating point
			// Read code char by char
			int i = 0;
			for (; i < listOfLines.get(j).length(); i++) {
				// Read char by char 
				ch = listOfLines.get(j).charAt(i);
//				System.out.println(ch);
	        	// Check if flag is true it is true when we have two character operators
	        	if (flag) {
	        		flag = false;
	        		continue;
	        	}
				// Check for comment
				if (ch == '!') {
					// Catch multiline comments
					boolean lineCheck = false; // To check whether EOL is reached while checking for multiline comment
					if (listOfLines.get(j).charAt(i+1) == '!'){
						String val = "";
						// boolean isMultiline = true; // Flag to know whether !! is not found 
						boolean found = false;
						int k = i+2; // Since i+1th char is ! 
						// Iterate on all the rest of the lines
						for (int l = j; l < listOfLines.size(); l++){
							// Iterate over each line string till !! is found
							for (; k < listOfLines.get(l).length(); k++){
								if (listOfLines.get(l).charAt(k) == '!' && listOfLines.get(l).charAt(k+1) == '!'){
									// skip lth line all the way to kth index and start checking from k+1 index of that line
									found = true;
									int n = k+2;

									// Check if character after k+1 is anything but space
									for (; n<listOfLines.get(l).length();n++){
										
										if ( n == listOfLines.get(l).length()-1 )
										{lineCheck = true;}
										
										if ( listOfLines.get(l).charAt(n) != ' ' )
										{	break; }
										
									}
									// If EOL is reached
									if ( lineCheck ){
										break;
									}
									else{
										j = l;
										i = n;										
										break;
									}
								}
								else {
									val = val + listOfLines.get(l).charAt(k);
								}
							}
							// If !! is found break from outer loop
							if (found) {
								break;
							}
							else {
								lineNo++;
							}						
						}
						// If another !! is not found
						if (!found){
							// Invalid lexeme
							tokenList.add(new Token("InvalidLexeme", val));
							// All lines exhausted so no further checking
							j = listOfLines.size() - 1;
						}
						// IF multiline is found and EOL is not reached
						if ( found && !lineCheck){
							// If we donot put this check then ! would be concatenated in temp at the end of main inner loop
							ch = listOfLines.get(j).charAt(i);
						}
					}

					else{
						// Break the inner loop and go to next line in case of single line comment
						break;
					}
				}
				// Check for comment

				// Check for Dot
				// Check for floating point when temp is not empty
				if ( ch == '.' && temp.length() != 0 )
				{
					// Check for my and super
					if ( temp.equals("my") || temp.equals("base"))
					{
						// Make token of my or base and empty temp then call checkDotE
						tokenList.add(new Token(temp, temp, lineNo));
						temp = "";
						i = checkDotE(ch, tokenList, lineNo, i, j);
						ch = listOfLines.get(j).charAt(i);
						// While ch is not dot
						while ( ch == '.'){
							i = checkDotE(ch, tokenList, lineNo, i, j);
							ch = listOfLines.get(j).charAt(i);
						}						
					}
					else{
						// Call checkDotN
						// It returns index of next character to look for
						i = checkDotN(temp, ch, tokenList, lineNo, i, j);
						temp = "";
						ch = listOfLines.get(j).charAt(i); // Check if the next ch is not . if it is then again call checkDot
						// While ch is not dot
						while ( ch == '.'){
							i = checkDotE(ch, tokenList, lineNo, i, j);
							ch = listOfLines.get(j).charAt(i);
						}
					}
				}

				// FLoating point when temp is empty
				if ( ch == '.' && temp.length() == 0 )
				{
					i = checkDotE(ch, tokenList, lineNo, i, j);
					ch = listOfLines.get(j).charAt(i); // Check if the next ch is not . if it is then again call checkDot
					// While ch is not dot
					while ( ch == '.'){
						i = checkDotE(ch, tokenList, lineNo, i, j);
						ch = listOfLines.get(j).charAt(i);
					}					
				}

				// Breakers Punc except .
				if (breakersPunc.contains(ch) && ch != '.') 
				{
					// Check if blank space
					if (ch == ' ' && temp.length() != 0) {
						// Tokenize temp
						tokenizeTemp(temp, tokenList, lineNo);
						temp = "";
						continue; // Continue to next character
					}

					// To handle extra spaces
					else if (ch == ' ' && temp.length() == 0){
						continue;
					}

					else {
						cls = isPunctuator(Character.toString(ch));
						if (cls.length() != 0) {
							String punCLS = cls;
							// Run only when temp is not empty
							if (temp.length() != 0) {
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";
							}
							// After tokenizing temp tokenize the punctuator at ch
							tokenList.add(new Token(punCLS, Character.toString(ch), lineNo));
							continue;
						}
						// Else invalid punctuator
						else{
							// Run only when temp is not empty
							if (temp.length() != 0) {		
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";								
							}
							tokenList.add(new Token("InvalidLexeme",Character.toString(ch), lineNo));
							continue;
						}
					}	
				}
				
				// Breakers Op
				else if (breakersOp.contains(ch)) {
						cls = isArithematic(Character.toString(ch));
						// Check for arithematic
						if (cls.length() != 0) {
							String arCLS = cls; // Stores value of arithematic class
							// Tokenize temp first if not empty
							if (temp.length() != 0) {
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";
							}
							// Check for inc-dec
							if (listOfLines.get(j).charAt(i+1) == ch) {
								if (ch == '+' || ch == '-') {
									// check temp
									// Class increment decrement
									Token word = new Token("IncDec", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo); 
									tokenList.add(word);
									flag = true; // This flag is used to skip iteration of next ch when len(operators) == 2
									continue;
								}
							}
							// Check for compound assignment
							else if (listOfLines.get(j).charAt(i+1) == '~') {
								// Class compound assignment
								Token word = new Token("CompAsgn", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo); 
								tokenList.add(word);
								flag = true;
								continue;
							}
							// Logical not class ^^
							else if (ch == '^' && listOfLines.get(j).charAt(i+1) == '^') {
								Token word = new Token("LogNot", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo); 
								tokenList.add(word);
								flag = true;
								continue;
							}
							// Otherwise simple arithmetic op
							else {
								// arithematic class
								Token word = new Token(arCLS, Character.toString(ch), lineNo); 
								tokenList.add(word);
								continue;
							}
						}

						// If assignment op
						else if (ch == '~') {
							// Tokenize temp first if its not null
							if (temp.length() != 0){
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";
							}
							// Tokenize the assign operator
							Token word = new Token("AsgnOp", Character.toString(ch), lineNo); 
							tokenList.add(word);
							continue;
						}
						
						// Logical ops and or
						else if (ch == '&' || ch == '|') {
							// Tokenize temp first if its not null
							if (temp.length() != 0){
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";
							}
							Token word = new Token("AndOR", Character.toString(ch), lineNo); 
							tokenList.add(word);
							// Check only when temp is empty
							continue;
						}
						// Check relational
						else if (ch == '=' || ch == '<' || ch == '>') {
							// Tokenize temp first if its not null
							if (temp.length() != 0){
								// Tokenize temp
								tokenizeTemp(temp, tokenList, lineNo);
								temp = "";
							}
							if (listOfLines.get(j).charAt(i+1) == '=' || listOfLines.get(j).charAt(i+1) == '>') {
								// equals to
								Token word = new Token("ROP", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo); 
								tokenList.add(word);
								flag = true;
								continue;	
							}
							// Incase of < or > 
							Token word = new Token("ROP", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo); 
							tokenList.add(word);
							continue;
						}
						// Invalid lexeme
						else {
							tokenList.add(new Token("InvalidLexeme",Character.toString(ch), lineNo));
							continue;
						}
					}
				
				// check for special symbs also breakers
				// Check for Access Modifiers
				else if (ch == '#' || ch == '$') {
					// Check if ##
					if (listOfLines.get(j).charAt(i+1) == '#') {
						tokenList.add(new Token("AM", Character.toString(ch)+Character.toString(listOfLines.get(j).charAt(i+1)), lineNo));
						flag = true;
						continue;
					}
					// For #, $
					tokenList.add(new Token("AM", Character.toString(ch), lineNo));
					continue;
				}

				// Check inheritance
				else if (ch == ':') {
					// Tokenize temp first if its not null
					if (temp.length() != 0){
						// Tokenize temp
						tokenizeTemp(temp, tokenList, lineNo);
						temp = "";
					}
					tokenList.add(new Token("INH", Character.toString(ch), lineNo));
					continue;
				}
				// Concatenate character if nothing matched
				temp += ch;						
			}
			// If multiline comment is not there
			lineNo++;
		}
		tokenList.add(new Token("?", "?", -1));
		// checkSyntax(tokenList);
		System.out.println("Token list");
		printList();
	}
	// Tokenization
	// public static void main(String[] args) throws IOException {}
}
//Class for token
class Token {
	public String CP;
	public String value;
	public int lineNo;
	
	public Token() {}
	
	public Token(String className, String value, int lineNo) {
		this.CP = className;
		this.value = value;
		this.lineNo = lineNo;
	}
	
	public Token(String className, int lineNo) {
		this.CP = className;
		this.lineNo = lineNo;
	}

	public Token(String className, String value) {
		this.CP = className;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "ClassName:"+this.CP+", Value:"+this.value+", LineNo:"+this.lineNo;
	}
}
