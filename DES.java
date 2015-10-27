import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.util.BitSet;
import java.math.BigInteger;
import java.security.SecureRandom;

import gnu.getopt.Getopt;


public class DES {

	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyStr, inputFile, outputFile);
		}
		
		
	}
	

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset());
			String IVStr = lines.get(0);
			lines.remove(0);
			String encryptedText;
			
			for (String line : lines) {
				encryptedText = DES_decrypt(IVStr, line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {
		
		return null;
	}


	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			
			String encryptedText;
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(String line) {
		
		return null;
	}


	static void genDESkey() {
		try{
    		// Create a secure random number generator using the SHA1PRNG algorithm
    		SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG");
    		// Get 128 random byte
    		byte[] randomBytes = new byte[128];
    		secureRandomGenerator.nextBytes(randomBytes);
    		// Create two secure number generators with the same seed
    		int seedByteCount = 5;
    		byte[] seed = secureRandomGenerator.generateSeed(seedByteCount);
    		SecureRandom secureRandom1 = SecureRandom.getInstance("SHA1PRNG");
    		secureRandom1.setSeed(seed);
    		SecureRandom secureRandom2 = SecureRandom.getInstance("SHA1PRNG");
    		secureRandom2.setSeed(seed);
    	} catch (Exception e){
    	}

		System.out.println("New key goes here");
		return;
	}


	/**
	 * This function Processes the Command Line Arguments.
	 * -p for the port number you are using
	 * -h for the host name of system
	 */
	private static void pcl(String[] args, StringBuilder inputFile,
							StringBuilder outputFile, StringBuilder keyString,
							StringBuilder encrypt) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:i:o:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'o':
		        	  arg = g.getOptarg();
		        	  outputFile.append(arg);
		        	  break;
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  inputFile.append(arg);
		        	  break;
	     	  	  case 'e':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("e");
		        	  break;
	     	  	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("d");
		        	  break;
		          case 'k':
		        	  genDESkey();
		        	  break;
		          case 'h':
		        	  callUseage(0);
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		              break;
		       }
		   }
		
	}
	
	private static void callUseage(int exitStatus) {
		
		String useage = "";
		useage += "-h   No args\n";
		useage += "       Usage information.\n\n";
		useage += "-k   No args\n";
		useage += "       Generate a DES key, encoded in hex, printed on the command line.\n\n";
		useage += "-e   Usage: -e <64 bit key in hex> -i <input file> -o <output file>\n";
		useage += "       Encrypt the file <input file> using <64 bit key in hex>\n";
		useage += "       and store the encrypted file in <output file>.\n\n";
		useage += "-d   Usage: -d <64 bit key in hex> -i <input file> -o <output file>\n";
		useage += "       Decrypt the file <input file> using <64 bit key in hex>\n";
		useage += "       and store the plain text <output file>.\n";

		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
}
