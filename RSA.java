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
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder m = new StringBuilder();
		
		pcl(args, bitSizeStr, nStr, dStr, eStr,m);
		
		if(!bitSizeStr.toString().equalsIgnoreCase("")){
			//This means you want to create a new key
			genRSAkey(bitSizeStr);
		}
		
		if(!eStr.toString().equalsIgnoreCase("")){
			RSAencrypt(m, nStr, eStr);
		}
		
		if(!dStr.toString().equalsIgnoreCase("")){
			RSAdecrypt(m, nStr, dStr);
		}
			
	}

	private static void RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {
		BigInteger e, n, M, C; 			//M = Message, C = Ciphertext
		//System.out.println("m = " + m.toString() + "\nkeyStr = " + keyStr.toString());
		String cipher = null;
		
		e = new BigInteger(eStr.toString(), 16); 	//Get e, should be first entry in the file
		n = new BigInteger(nStr.toString(), 16);	//Get n, should be second entry
		M = new BigInteger(m.toString(), 16);
		C = M.modPow(e, n);				//From slide, C = M^e mod n			
		//System.out.println("e = " + e.toString() + "\nn = " + n.toString() + "\nMessage = " + M.toString() + "\nC = " + C.toString());
		
		cipher = C.toString(16);		//Convert from BigInteger to string in hex
		System.out.println("\nCipher = " + cipher);
	}

	private static void RSAdecrypt(StringBuilder cStr, StringBuilder nStr, StringBuilder dStr){
		BigInteger d, n, C, M;
		//System.out.println("c = " + c.toString() + "\nkeyStr = " + keyStr.toString());
		String message = null;
		
		d = new BigInteger(dStr.toString(), 16);		//Pretty much the same as encrypt
		n = new BigInteger(nStr.toString(), 16);
		C = new BigInteger(cStr.toString(), 16);
		M = C.modPow(d, n);					//From slide, M = C^d mod n
		//System.out.println("d = " + d.toString() + "\nn = " + n.toString() + "\nC = " + C.toString() + "\nM = " + M.toString());
		
		message = M.toString(16);
		if (message.length() != 16)
			message = "0"+message; 			//I guess BigInteger.toString deletes leading zeroes. Makes sense. Put them back in. 
		System.out.println("\nMessage = " + message);	
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
		BigInteger p, q, n, phi, e, d;	//Using naming conventions from the slides because I'm unoriginal.
		String s = bitSizeStr.toString();
		String publicKey, privateKey;
				
		try{
			SecureRandom r = SecureRandom.getInstance("SHA1PRNG"); //Get a securely random number for seeding the prime method.
			int bitSize = Integer.valueOf(s);				//Do stuff to turn StringBuilder into an int.
			p = BigInteger.probablePrime(bitSize/2, r); 	//Generates numbers with the 2^-100 chance of not being prime. Close enough for me. 
			q = BigInteger.probablePrime(bitSize/2, r);		//bitSize/2 because after being multiplied together it'll be bitSize.
			n = p.multiply(q);
			phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
			e = new BigInteger("17"); 						//One example from slides, I don't see why not.
			while (phi.gcd(e).intValue() > 1) {				//Just in case e is not relatively prime to phi.
				e = e.add(BigInteger.ONE);					//Add one until it IS relatively prime.
			}
			d = e.modInverse(phi); 							//Thank god BigInteger has methods for literally everything, right?
			
			System.out.println("\n	Public Key:  (e, n) \n	Private Key: (d, n)");
			//Specifying 16 when using toString on a BigInteger encodes it in hex. BigIntegers are magic.
			System.out.println("\ne = " + e.toString(16) + "\n\nd = " + d.toString(16) + "\n\nn = " + n.toString(16));
						
		} catch (Exception ex){
    		System.out.println("Invalid algorithm given to SecureRandom.getInstance");
    		return;
    	} 
	}
	
	/**
	 * This function Processes the Command Line Arguments.
	 */
	private static void pcl(String[] args, StringBuilder bitSizeStr,
							StringBuilder nStr, StringBuilder dStr, StringBuilder eStr,
							StringBuilder m) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:b:n:i:");
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
		     	  case 'n':
		        	  arg = g.getOptarg();
		        	  nStr.append(arg);
		        	  break;
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
		useage += "-e   Usage: -e <public key> -n <modulus> -i <plaintext value>\n";
		useage += "       Encrypt the integer <plaintext value> (encoded in hex) using the public key pair (e, n).\n\n";
		useage += "-d   Usage: -d <private key> -n <modulus> -i Cipher.txt\n";
		useage += "       Decrypt the ciphertext value (encoded in hex) in the file using the private key pair (d, n).\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
