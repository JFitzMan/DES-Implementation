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

import java.util.ArrayList;

import gnu.getopt.Getopt;


public class DES {


	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyFileName = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, inputFile, outputFile, keyFileName, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			readKey(keyFileName.toString(), keyStr );
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			readKey(keyFileName.toString(), keyStr );
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

			String key = keyStr.toString();
			String keyBin = "";
			keyBin = new BigInteger(key, 16).toString(2); //Found code on StackExchange to convert a hex string to binary
			while (keyBin.length() < 64){ //Add leading zeroes back in
				keyBin = "0"+keyBin;
			}
			System.out.println("key in binary: \n" + keyBin);
			
			byte[][] subKeys = new byte[16][48]; 
			
			subKeys = makeSubKeys(keyBin); //Pass the binary key along
			
			//Just prints out the Subkeys, eventually won't be needed
			System.out.println("Subkeys:");
			for(int i=0; i<16; i++){
				System.out.print("K[" + (i+1) +"]: ");
				for(int k=0; k<48; k++){
					System.out.print(subKeys[i][k]);
				}
				System.out.println();
			}

			File file = new File(inputFile.toString());
			FileInputStream byteStream = new FileInputStream(file);

			byte [] toEncrypt = new byte [8];
			boolean toEncryptIsEmpty = true;
			byte nextByte;
			BitSet encryptedBits = new BitSet(64);
			byte [] encryptedBytes = new byte[8];
			int count = 0;
			String encryptedText;
			int amountOfBytesInToEncrypt = 0;

			while ( (nextByte = (byte) byteStream.read()) != -1){
				//if toEncrypt is full, encrypt them, print encrypted bytes,
				// store nextByte in toEncrypt[0], and leave with the count at 1
				if (count == 8){

					//get encrypted bitset
					encryptedBits = encrypt64Bits(BitSet.valueOf(toEncrypt));
					//get encrypted bytes
					encryptedBytes = encryptedBits.toByteArray();
					//get encryped string
					encryptedText = new String (encryptedBytes, "UTF-8");
					//write encryped string to the output file
					writer.write(encryptedText);

					//print the hex representation of he encrypted bits
					StringBuilder sb = new StringBuilder();
    				for (byte b : toEncrypt) {
        				sb.append(String.format("%02x", b));
    				}//end for
    				System.out.println(sb);

					count = 0;
					amountOfBytesInToEncrypt = 0;
					//System.out.println("After Block");

					toEncryptIsEmpty = true;
				}//end if

				toEncrypt[count] = nextByte;
				toEncryptIsEmpty = false;
				count ++;
				amountOfBytesInToEncrypt++;
			}//end while

			byteStream.close();

			//DO STUFF WITH THE EXTRA LEFT OVER IN TO ENCRYPT BEFORE CLOSING WRITER
			if (!toEncryptIsEmpty){

				//get encrypted bitset
				encryptedBits = encrypt64Bits(BitSet.valueOf(toEncrypt));
				//get encrypted bytes
				encryptedBits.clear(amountOfBytesInToEncrypt*8, 64);
				byte [] someEncryptedBytes = encryptedBits.toByteArray();
				for (int i = 0; i < amountOfBytesInToEncrypt; i++){
					encryptedBytes[i] = someEncryptedBytes[i];
				}
				for (int i = amountOfBytesInToEncrypt; i < 8; i ++){
					encryptedBytes[i] = 0;
				}
				//get encryped string
				encryptedText = new String (encryptedBytes, "UTF-8");
				//write encryped string to the output file
				writer.write(encryptedText);

				//print the hex representation of he encrypted bits
				StringBuilder sb = new StringBuilder();
    			for ( int i = 0; i < 8; i++) {
        			sb.append(String.format("%02x", encryptedBytes[i]));
    			}
    			System.out.println(sb);
    		}//end if
			

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}


		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * This is going to collect bytes until it forms a new line
	 *
	 * At that point, its going to call encrypt64bits, get the enrypted bits, and return them.
	 *
	 * When the input arg line == null, it will need to prepare a buffer before sending whats
	 * left to the encrypt64bits function
	 * @param line
	 */
	private static String DES_encrypt(String line) {
		//check to see if this is the last line
		if (line == null){
			//pad toEncrpt with 00s and encrypt it
		}
		//process line into toEncrypt String
		else{
			byte [] newLine = line.getBytes(); 

		}
		return "";
	}

	//before coming here, the bits MUST be padded. 64 bits are expected as input
	private static BitSet encrypt64Bits(BitSet input){
		return input;
	}

	/*
	* given an array of bytes, this function return a bitset that represents the array.
	*
	*/
	private static BitSet bytesToBitSet(byte[] input){

		//each byte has 8 bits in the bitset
		BitSet toReturn = new BitSet(input.length * 8);
		int curIndex = 0;

		//this loop iterates over the bytes
		for (int i = 0; i < input.length; i++){
			//working byte
			byte b = input[i];

			//this loop iterates over the byte
			for ( int k=0; k<8; k++) {
				//flip bits
				if ((b & (1 << k)) > 0){
					//add curIndex to account for multiple bytes
        			toReturn.set(curIndex + k);
   			 	}
			}
			//increment curIndex by 8 to move to next byte position
			curIndex += 8;
		}
		//return new bitset
		return toReturn;
	}//bytesToBitSet

	static byte[][] makeSubKeys(String keyBin){
	
		byte[] C0 = new byte[28]; //Bytes for first permutation
		byte[] D0 = new byte[28];
		byte[][] C = new byte[16][28]; //Arrays for two halves of eventual subkey generation
		byte[][] D = new byte[16][28];
		byte[][] CD = new byte[16][56]; //Array that puts the halves together
		byte[][] subKeys = new byte[16][48]; //Array that holds permuted subkeys
		
		for(int i=0; i<28; i++){
			C0[i]=1;
			D0[i]=0;
		}		
		
		//Make the first permutation 
		System.out.println("First permutation (C0 + D0): ");
		int i=0;
		for(i=0; i<28; i++){
			if(keyBin.charAt(PC1[i]-1) == '1'){ //PC1[i]-1 because of array math :P
				C0[i]=1; 						//Can't actually just copy the contents of the character because chars can't into bytes
				System.out.print(C0[i]);		//This is the next best thing
			} else {
				C0[i]=0;
				System.out.print(C0[i]);
			}
		}
		
		//Same thing down here, but for the second half of the permutation. That's why i starts at 28.
		for(i=28; i<56; i++){				
			if(keyBin.charAt(PC1[i]-1) == '1'){
				D0[i-28]=1;
				System.out.print(D0[i-28]);
			} else {
				D0[i-28]=0;
				System.out.print(D0[i-28]);
			}
		}
		
		System.out.println("\nShifts: ");
		C[0] = leftShift(C0, rotations[0]); 	//Do the first left shifts
		D[0] = leftShift(D0, rotations[0]);
		
		for(i=1; i<16; i++){ 					//Do the rest of the left shifts
			C[i] = leftShift(C[i-1], rotations[i]);
			D[i] = leftShift(D[i-1], rotations[i]);
		}
		
		//Just printing out the left shifted sub-subkeys, won't need eventually
		for(i=0; i<16; i++){
			System.out.print("C[" + (i+1) +"]: ");
			for(int k=0; k<28; k++){
				System.out.print(C[i][k]);
			}
			System.out.print(" + D[" + (i+1) +"]: ");
			for(int j=0; j<28; j++){
				System.out.print(D[i][j]);
			}
			System.out.println();
		}
	
		//Merge the sub-subkey arrays together, one by one
		for(i=0; i<16; i++){
			System.arraycopy(C[i], 0, CD[i], 0, 28); 	//First half of each array, spots [0-28] are the C half
			System.arraycopy(D[i], 0, CD[i], 28, 28);	//Spots [29-56] are the D half
			for(int k=0; k<48; k++){
				subKeys[i][k] = CD[i][PC2[k]-1]; 		//Permute with PC2 and copy over to subkey array. Once again, PC[k]-1 because array math
			}
		}
		
		return subKeys; 
	}
	
	static byte[] leftShift(byte[] toShift, int numShifts){ //Made this function's parameters basic so we can use again if necessary
		byte shifted[] = new byte[toShift.length]; 			//Make a new byte, the one that's going to be shifted
		//Copy the provided byte to it (Had to look up how to copy arrays -- surprisingly easy!
		for(int i=0; i<numShifts; i++){
					//Move everything over one! Hold the first position temporarily.
			for(int k=0; k<toShift.length-1; k++)
				shifted[k] = toShift[k+1];
			shifted[toShift.length-1]=toShift[0];					//Put that temporarily held bit back, in the last position
			System.arraycopy(shifted, 0, toShift, 0, shifted.length);
		}
		return shifted; 									//Send the fancy new shifted byte back over
	}
	
	/*
	* Generates a random key. Saves the hex representation as a string to keyStr
	* Writes the bytes to the file key.k
	*/
	static void genDESkey() {

		try{

    		SecureRandom randomGen = SecureRandom.getInstance("SHA1PRNG");

    		//array of bytes to store the random numbers in 
    		byte[] bytes = new byte[8]; 
    		//fill it with random bytes
    		randomGen.nextBytes(bytes);

    		StringBuilder sb = new StringBuilder();
    		for (byte b : bytes) {
        		sb.append(String.format("%02x", b));
    		}

    		System.out.println(sb);

    		//also write key to file key.k
    		FileOutputStream outputStream = new FileOutputStream("key.k");
    		outputStream.write(bytes);
    		outputStream.close();


			return;
    	} catch (Exception e){
    		System.out.println("Invalid algorithm given to SecureRandom.getInstance (I think)");
    		return;
    	} 

		
	}
	/**
	 * This function Processes the Command Line Arguments.
	 * -p for the port number you are using
	 * -h for the host name of system
	 */
	private static void pcl(String[] args, StringBuilder inputFile,
							StringBuilder outputFile, StringBuilder keyFileName,
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
		        	  keyFileName.append(arg);
		        	  encrypt.append("e");
		        	  break;
	     	  	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyFileName.append(arg);
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

	/*
	* Reads key in from file key.k
	* saves hex representation of it in keyStr
	*/
	private static void readKey(String fileName, StringBuilder keyStr) {

		try {

			//buffer to read key into
			byte [] buf = new byte [8];
			//reads data into buffer
			FileInputStream input = new FileInputStream(fileName);

			int bytesRead = 0;
			//read all of the bytes
			while ( bytesRead != -1 ){
				bytesRead = input.read(buf);
			}
			//close file after reading
			input.close();

			StringBuilder sb = new StringBuilder();
    		for (byte b : buf) {
        		sb.append(String.format("%02x", b));
    		}
			//append the bytes read to keyStr
			keyStr.append(sb);
			System.out.println("Key read: " + sb);

		} catch (FileNotFoundException e) {
			System.err.println("File " + fileName + " could not be opened!");
		}catch (IOException e){
			System.err.println("File " + fileName + " could not be read!");
		}

	}

	public static int[]  PC1 = {
		57, 49, 41, 33, 25, 17, 9 ,
		1 , 58, 50, 42, 34, 26, 18,
		10, 2 , 59, 51, 43, 35, 27,
		19, 11, 3 , 60, 52, 44, 36,
		63, 55, 47, 39, 31, 23, 15,
		7 , 62, 54, 46, 38, 30, 22,
		14, 6 , 61, 53, 45, 37, 29,
		21, 13, 5 , 28, 20, 12, 4 };
	
	
	public static int[] PC2 = {
		14, 17, 11, 24, 1 , 5 ,
		3 , 28, 15, 6 , 21, 10,
		23, 19, 12, 4 , 26, 8 ,
		16, 7 , 27, 20, 13, 2 ,
		41, 52, 31, 37, 47, 55,
		30, 40, 51, 45, 33, 48,
		44, 49, 39, 56, 34, 53,
		46, 42, 50, 36, 29, 32 };
	
	public static int[] rotations = {
		1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
	};
	
	public static int[] IP = {
		58, 50, 42, 34, 26, 18, 10, 2
		, 60, 52, 44, 36, 28, 20, 12, 4
		, 62, 54, 46, 38, 30, 22, 14, 6
		, 64, 56, 48, 40, 32, 24, 16, 8
		, 57, 49, 41, 33, 25, 17, 9, 1
		, 59, 51, 43, 35, 27, 19, 11, 3
		, 61, 53, 45, 37, 29, 21, 13, 5
		, 63, 55, 47, 39, 31, 23, 15, 7
	};
	public static int[] E = {	
		32, 1, 2, 3, 4, 5
		, 4, 5, 6, 7, 8, 9
		, 8, 9, 10, 11, 12, 13
		, 12, 13, 14, 15, 16, 17
		, 16, 17, 18, 19, 20, 21
		, 20, 21, 22, 23, 24, 25
		, 24, 25, 26, 27, 28, 29
		, 28, 29, 30, 31, 32, 1
	};
	
//	http://cafbit.com/resource/DES.java
	public static final byte[][] S = { {
        14, 4,  13, 1,  2,  15, 11, 8,  3,  10, 6,  12, 5,  9,  0,  7,
        0,  15, 7,  4,  14, 2,  13, 1,  10, 6,  12, 11, 9,  5,  3,  8,
        4,  1,  14, 8,  13, 6,  2,  11, 15, 12, 9,  7,  3,  10, 5,  0,
        15, 12, 8,  2,  4,  9,  1,  7,  5,  11, 3,  14, 10, 0,  6,  13
    }, {
        15, 1,  8,  14, 6,  11, 3,  4,  9,  7,  2,  13, 12, 0,  5,  10,
        3,  13, 4,  7,  15, 2,  8,  14, 12, 0,  1,  10, 6,  9,  11, 5,
        0,  14, 7,  11, 10, 4,  13, 1,  5,  8,  12, 6,  9,  3,  2,  15,
        13, 8,  10, 1,  3,  15, 4,  2,  11, 6,  7,  12, 0,  5,  14, 9
    }, {
        10, 0,  9,  14, 6,  3,  15, 5,  1,  13, 12, 7,  11, 4,  2,  8,
        13, 7,  0,  9,  3,  4,  6,  10, 2,  8,  5,  14, 12, 11, 15, 1,
        13, 6,  4,  9,  8,  15, 3,  0,  11, 1,  2,  12, 5,  10, 14, 7,
        1,  10, 13, 0,  6,  9,  8,  7,  4,  15, 14, 3,  11, 5,  2,  12
    }, {
        7,  13, 14, 3,  0,  6,  9,  10, 1,  2,  8,  5,  11, 12, 4,  15,
        13, 8,  11, 5,  6,  15, 0,  3,  4,  7,  2,  12, 1,  10, 14, 9,
        10, 6,  9,  0,  12, 11, 7,  13, 15, 1,  3,  14, 5,  2,  8,  4,
        3,  15, 0,  6,  10, 1,  13, 8,  9,  4,  5,  11, 12, 7,  2,  14
    }, {
        2,  12, 4,  1,  7,  10, 11, 6,  8,  5,  3,  15, 13, 0,  14, 9,
        14, 11, 2,  12, 4,  7,  13, 1,  5,  0,  15, 10, 3,  9,  8,  6,
        4,  2,  1,  11, 10, 13, 7,  8,  15, 9,  12, 5,  6,  3,  0,  14,
        11, 8,  12, 7,  1,  14, 2,  13, 6,  15, 0,  9,  10, 4,  5,  3
    }, {
        12, 1,  10, 15, 9,  2,  6,  8,  0,  13, 3,  4,  14, 7,  5,  11,
        10, 15, 4,  2,  7,  12, 9,  5,  6,  1,  13, 14, 0,  11, 3,  8,
        9,  14, 15, 5,  2,  8,  12, 3,  7,  0,  4,  10, 1,  13, 11, 6,
        4,  3,  2,  12, 9,  5,  15, 10, 11, 14, 1,  7,  6,  0,  8,  13
    }, {
        4,  11, 2,  14, 15, 0,  8,  13, 3,  12, 9,  7,  5,  10, 6,  1,
        13, 0,  11, 7,  4,  9,  1,  10, 14, 3,  5,  12, 2,  15, 8,  6,
        1,  4,  11, 13, 12, 3,  7,  14, 10, 15, 6,  8,  0,  5,  9,  2,
        6,  11, 13, 8,  1,  4,  10, 7,  9,  5,  0,  15, 14, 2,  3,  12
    }, {
        13, 2,  8,  4,  6,  15, 11, 1,  10, 9,  3,  14, 5,  0,  12, 7,
        1,  15, 13, 8,  10, 3,  7,  4,  12, 5,  6,  11, 0,  14, 9,  2,
        7,  11, 4,  1,  9,  12, 14, 2,  0,  6,  10, 13, 15, 3,  5,  8,
        2,  1,  14, 7,  4,  10, 8,  13, 15, 12, 9,  0,  3,  5,  6,  11
    } };
	
	public static int[] P = {
		16, 7, 20, 21,
		29, 12, 28, 17,
		1, 15, 23, 26,
		5, 18, 31, 10,
		2, 8, 24, 14,
		32, 27, 3, 9,
		19, 13, 30, 6,
		22, 11, 4, 25};
	
	public static int[] FP = {
		40, 8, 48, 16, 56, 24, 64, 32
		, 39, 7, 47, 15, 55, 23, 63, 31
		, 38, 6, 46, 14, 54, 22, 62, 30
		, 37, 5, 45, 13, 53, 21, 61, 29
		, 36, 4, 44, 12, 52, 20, 60, 28
		, 35, 3, 43, 11, 51, 19, 59, 27
		, 34, 2, 42, 10, 50, 18, 58, 26
		, 33, 1, 41, 9, 49, 17, 57, 25
	};
	
}
