import java.io.IOException;
import java.io.PrintWriter;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.BitSet;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import gnu.getopt.Getopt;

public class RSA {

	public static void main(String[] args){
		
		StringBuilder bitSizeStr = new StringBuilder();
		//StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder m = new StringBuilder();
		
		pcl(args, bitSizeStr, dStr, eStr,m);
		
		if(!bitSizeStr.toString().equalsIgnoreCase("")){
			//This means you want to create a new key
			genRSAkey(bitSizeStr);
		}
		
		if(!eStr.toString().equalsIgnoreCase("")){
			RSAencrypt(m, eStr);
		}
		
		if(!dStr.toString().equalsIgnoreCase("")){
			RSAdecrypt(m, dStr);
		}
		
		
	}

	private static void RSAencrypt(StringBuilder m, StringBuilder keyStr) {
		BigInteger e, n, M, C; 			//M = Message, C = Ciphertext
		//System.out.println("m = " + m.toString() + "\nkeyStr = " + keyStr.toString());
		String[] keys = getKeys(keyStr);
		String cipher = null;
		
		e = new BigInteger(keys[0]); 	//Get e, should be first entry in the file
		n = new BigInteger(keys[1]);	//Get n, should be second entry
		M = new BigInteger(m.toString(), 16);
		C = M.modPow(e, n);				//From slide, C = M^e mod n			
		//System.out.println("e = " + e.toString() + "\nn = " + n.toString() + "\nMessage = " + M.toString() + "\nC = " + C.toString());
		
		cipher = C.toString(16);		//Convert from BigInteger to string in hex
		System.out.println("\nCipher = " + cipher);
		
		try (Writer cWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Cipher.txt"), "utf-8"))) {
				cWriter.write(cipher);
			} catch (Exception ex) {
				System.out.println("File could not be written to.");
			}
		System.out.println("\nCipher has been written to Cipher.txt");
	}

	private static void RSAdecrypt(StringBuilder c, StringBuilder keyStr){
		BigInteger d, n, C, M;
		//System.out.println("c = " + c.toString() + "\nkeyStr = " + keyStr.toString());
		String[] keys = getKeys(keyStr);
		String cipher = null;
		String message = null;
		
		try{ 			//Get the ciphertext from the file it was written to
			BufferedReader buf = new BufferedReader(new FileReader(c.toString()));
			StringBuffer stringBuf = new StringBuffer();
			cipher = buf.readLine();
		} catch (Exception ex) {
			System.err.println("File " + keyStr.toString() + " could not be opened!");
		}
		
		d = new BigInteger(keys[0]);		//Pretty much the same as encrypt
		n = new BigInteger(keys[1]);
		C = new BigInteger(cipher, 16);
		M = C.modPow(d, n);					//From slide, M = C^d mod n
		//System.out.println("d = " + d.toString() + "\nn = " + n.toString() + "\nC = " + C.toString() + "\nM = " + M.toString());
		
		message = M.toString(16);
		System.out.println("\nMessage = " + message);
		
		try (Writer mWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Message.txt"), "utf-8"))) {
				mWriter.write(message);
			} catch (Exception ex) {
				System.out.println("File could not be written to.");
			}
		System.out.println("\nMessage has been written to Message.txt");
		
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
		BigInteger p, q, n, phi, e, d;	//Using naming conventions from the slides because I'm unoriginal.
		String s = bitSizeStr.toString();
		String publicKey, privateKey;
				
		try{
			SecureRandom r = SecureRandom.getInstance("SHA1PRNG"); //Get a securely random number for seeding the prime method.
			int bitSize = Integer.valueOf(s);			//Do stuff to turn StringBuilder into an int.
			p = BigInteger.probablePrime(bitSize/2, r); 	//Generates numbers with the 2^-100 chance of not being prime.
			q = BigInteger.probablePrime(bitSize/2, r);		//Close enough for me.
			n = p.multiply(q);
			phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
			e = new BigInteger("17"); 					//One example from slides, I don't see why not.
			while (phi.gcd(e).intValue() > 1) {
				e = e.add(BigInteger.ONE);				//Just in case e is not relatively prime to phi.
			}
			d = e.modInverse(phi); //Thank god BigInteger has methods for literally everything, right?
			
			System.out.println("\nPublic key = (" + e.toString() + " , " + n.toString() + ")");
			System.out.println("\nPrivate key = (" + d.toString() + " , " + n.toString() + ")");
			
			try (Writer pubWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("publicKey.txt"), "utf-8"))) {
				pubWriter.write(e.toString() + " " + n.toString());
			} 
			try (Writer privWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("privateKey.txt"), "utf-8"))) {
				privWriter.write(d.toString() + " " + n.toString());
			}
			
			System.out.println("\nKeys have been written to publicKey.txt and privateKey.txt");
				
		} catch (Exception ex){
    		System.out.println("Invalid algorithm given to SecureRandom.getInstance (I think)");
    		return;
    	} 
	}

	private static String[] getKeys(StringBuilder keyStr){
		String[] keys = { " ", " " };
		try{
			BufferedReader buf = new BufferedReader(new FileReader(keyStr.toString()));
			StringBuffer stringBuf = new StringBuffer();
			String line = null;
			while ((line = buf.readLine())!=null){
				stringBuf.append(line);
			}
			keys = stringBuf.toString().split(" ");
		} catch (Exception ex) {
			System.err.println("File " + keyStr.toString() + " could not be opened!");
		}
		return keys;
	}
	
	/**
	 * This function Processes the Command Line Arguments.
	 */
	private static void pcl(String[] args, StringBuilder bitSizeStr,
							StringBuilder dStr, StringBuilder eStr,
							StringBuilder m) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:b:i:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  m.append(arg);
		        	  break;
		          case 'e':
		        	  arg = g.getOptarg();
		        	  eStr.append(arg);
		        	  break;
		     	  /*case 'n':
		        	  arg = g.getOptarg();
		        	  nStr.append(arg);
		        	  break; */
		     	  case 'd':
		        	  arg = g.getOptarg();
		        	  dStr.append(arg);
		        	  break;
		          case 'k':
		        	  break;
		     	  case 'b':
		        	  arg = g.getOptarg();
		        	  bitSizeStr.append(arg);
		        	  break;
		          case 'h':
		        	  callUsage(0);
		          case '?':
		            break; // getopt() already printed an error
		          default:
		              break;
		       }
		   }
	}
	
	private static void callUsage(int exitStatus) {

		String useage = "";
		useage += "-h   No args\n";
		useage += "       Usage information.\n\n";
		useage += "-k   Usage: -k -b <bit size>\n";
		useage += "       Generate a private/public key pair, encoded in hex, printed on the command line.\n";
		useage += "       Key will be <bit size> big. If not specified, default is 1024. \n\n";
		useage += "-e   Usage: -e <public key> -i <plaintext value>\n";
		useage += "       Encrypt the integer <plaintext value> (encoded in hex) using <public key>\n\n";
		useage += "-d   Usage: -d <private key> -i <cipertext value>\n";
		useage += "       Decrypt the <ciphertext value> using <private key>\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
