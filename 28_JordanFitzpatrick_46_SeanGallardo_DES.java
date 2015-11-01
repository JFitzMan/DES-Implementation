//28_Jordan_Fitzpatrick
//46_Sean_Gallado

//partners

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


public class DES {


	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyFileName = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			//readKey(keyFileName.toString(), keyStr );
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			//readKey(keyFileName.toString(), keyStr );
			decrypt(keyStr, inputFile, outputFile);
		}
		
		
	}
	

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		/*
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
		*/
		try {
			//PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			FileOutputStream writer = new FileOutputStream(outputFile.toString());

			String key = keyStr.toString();
			String keyBin = "";
			keyBin = new BigInteger(key, 16).toString(2); //Found code on StackExchange to convert a hex string to binary
			while (keyBin.length() < 64){ //Add leading zeroes back in
				keyBin = "0"+keyBin;
			}
			//System.out.println("key in binary: \n" + keyBin);
			
			byte[][] subKeys = new byte[16][48]; 
			BitSet[] subKeyBits = new BitSet[16];
			for(int i=0; i<16; i++)
				subKeyBits[i] = new BitSet(48);
			
			subKeys = makeSubKeys(keyBin); //Pass the binary key along
			/*
			
			System.out.println("Subkeys:");
			for(int i=0; i<16; i++){
				System.out.print("K[");
				if(i<9) System.out.print("0"+(i+1));
				else System.out.print(i+1);
				System.out.print("]: ");
				for(int k=0; k<48; k++){
					System.out.print(subKeys[i][k]);
				}
				System.out.println();
			}		*/
			
			
			subKeyBits = keysToBits(subKeys);		
			/*
			System.out.println();
			for(int i=0; i<16; i++){
				System.out.print("K[");
				if(i<9) System.out.print("0"+(i+1));
				else System.out.print(i+1);
				System.out.print("]: ");
				System.out.println(getBitSetString(subKeyBits[i]) + "\tLength: " + subKeyBits[i].length());
			}
			*/

			File file = new File(inputFile.toString());
			FileInputStream byteStream = new FileInputStream(file);

			byte [] toDecrypt = new byte [8];
			byte nextByte;
			BitSet decryptedBits = new BitSet(64);
			byte [] decryptedBytes = new byte[8];
			int count = 0;
			String decryptedText;
			boolean isIV = true;
			BitSet nextBlock;
			BitSet previousBlock= new BitSet(64);



			while ( (nextByte = (byte) byteStream.read()) != -1){
				//if toEncrypt is full, encrypt them, print encrypted bytes,
				// store nextByte in toEncrypt[0], and leave with the count at 1
				if (count == 8){
					
					if (isIV){
						previousBlock = bytesToBitSet(toDecrypt);
						previousBlock.set(64);
						isIV = false;
						//System.out.println("Got IV: ");
						StringBuilder temp = new StringBuilder();
    					for (byte b : toDecrypt) {
        					temp.append(String.format("%02x", b));
    					}//end for
    					//System.out.println("IV:           " + temp);
    					//System.out.println("IV from bits: " + getBitSetString(previousBlock));
					}
					else{
						BitSet bitsToDecrypt = bytesToBitSet(toDecrypt);

						//System.out.println("nextBitsToDecrypt: " + getBitSetString(bitsToDecrypt));
						//bitsToDecrypt.set(64);
						//get encrypted bitset
						nextBlock = bitsToDecrypt;
						decryptedBits.clear();
						decryptedBits = decrypt64Bits(bitsToDecrypt, subKeyBits);
						//get encrypted bytes
						//System.out.println(encryptedBits.length());
						//decryptedBits.set(64, false);

						decryptedBits.xor(previousBlock);
						//decryptedBits.set(64);
						previousBlock = nextBlock;
						decryptedBytes = decryptedBits.toByteArray();

						if (decryptedBytes.length != 8){
						byte [] newDecryptedBytes = new byte[8];
						for (int i = 0; i < decryptedBytes.length; i++) {
							newDecryptedBytes[i] = decryptedBytes[i];
						}
						for (int i = decryptedBytes.length; i < 8; i++) {
							newDecryptedBytes[i] = 0;
						}
						decryptedBytes = newDecryptedBytes;
						}
						//System.out.println(encryptedBytes.length + encryptedBytes.toString());
						//get encryped string
						decryptedText = new String (decryptedBytes, "UTF-8");
						//write encryped string to the output file
						//decryptedBits.set(64, false);

						writer.write(decryptedBytes);
						//System.out.println("decryptedBits: " + getBitSetString(decryptedBits));

						//previousBlock = decryptedBits;
						//System.out.println("previousBlock: " + getBitSetString(previousBlock));
						/*
						//print the hex representation of he encrypted bits
						StringBuilder sb = new StringBuilder();
    					for (int i = 0; i < 8; i++) {
        					sb.append(String.format("%02x", decryptedBytes[i]));
    					}//end for
    					System.out.println(sb);*/
    			    }

					count = 0;
					//System.out.println("After Block");

				}//end if

				toDecrypt[count] = nextByte;
				count ++;
			}//end while

			byteStream.close();	

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static BitSet decrypt64Bits(BitSet input, BitSet[] subkeys){
		//System.out.println("Initial bits to encrypt: " + getBitSetString(input));
		//System.out.println("Initial size: " + input.length());


		//permutate all inpute through table IP
		BitSet permutedBits = permute (input, IP);
		//System.out.println("Bits after IP:           " + getBitSetString(permutedBits));

		//get left and right halves
		//BitSet left = permutedBits.get(0, permutedBits.length()/2);
		//BitSet right = permutedBits.get(permutedBits.length()/2, permutedBits.length());
		BitSet left = new BitSet(33);
		BitSet right = new BitSet(33);
		for(int i = 0; i < 32; i ++){
			if (permutedBits.get(i)){
				left.set(i);
			}
			if(permutedBits.get(i+32)){
				right.set(i);
			}
		}
		right.set(32);
		left.set(32);
		//System.out.println("Left:  " + getBitSetString(left));
		//System.out.println("Right: " + getBitSetString(right));
		

		//16 iterations using function f that operates on two blocks
		for (int i = 0; i < 16; i++){
			//System.out.println("Round " + (i+1) + " ----------------");
			BitSet rightTemp = right;
			//System.out.println("right temp: " + getBitSetString(rightTemp));
			//f input: data of 32 bits and a key of 48 bits
			//f output: block of 32 bits
			BitSet newRight = roundFunction(right, subkeys[i]);
			//System.out.println("new right: " + getBitSetString(newRight));
			right = newRight;
			//System.out.println("right after rounds " + getBitSetString(right));
			//System.out.println("new right: " + getBitSetString(right));

			//new r = xor(L, f(R, subkey[i]))
			//System.out.println("eft before xor with right " + getBitSetString(left));
			right.xor(left);
			right.set(32);
			//System.out.println("right after xor " + getBitSetString(right));

			//new L = R before xor
			left = rightTemp;
			//System.out.println("new left: " + getBitSetString(left));
		}
		//System.out.println("After Rounds");
		//System.out.println("Left:  " + getBitSetString(left));
		//System.out.println("Right: " + getBitSetString(right));

		//reverse halves after rounds
		BitSet bitsAfterRounds = new BitSet(64);//+1 to account for zeros not printing
		for(int i = 0; i < 65; i++){
			//first half to be concatenated, right (reversed on purpose)
			if (i < 32){
				if(right.get(i))
					bitsAfterRounds.set(i);
			}
			//second half, left
			else{
				if(left.get(i-32))
					bitsAfterRounds.set(i);
			}
		}
		bitsAfterRounds.set(64);
		//System.out.println("Bits after 16 rounds: " + getBitSetString(bitsAfterRounds));



		//apply FP table to output
		BitSet bitsToReturn = permute(bitsAfterRounds, FP);
		//System.out.println("Bits after FP: " + getBitSetString(bitsToReturn));

		//return encrypted bits

		return bitsToReturn;
	}

	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		try {
			//PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			FileOutputStream writer = new FileOutputStream(outputFile.toString());

			String key = keyStr.toString();
			String keyBin = "";
			keyBin = new BigInteger(key, 16).toString(2); //Found code on StackExchange to convert a hex string to binary
			while (keyBin.length() < 64){ //Add leading zeroes back in
				keyBin = "0"+keyBin;
			}
			//System.out.println("key in binary: \n" + keyBin);
			
			byte[][] subKeys = new byte[16][48]; 
			BitSet[] subKeyBits = new BitSet[16];
			for(int i=0; i<16; i++)
				subKeyBits[i] = new BitSet(48);
			
			subKeys = makeSubKeys(keyBin); //Pass the binary key along
			/*
			System.out.println("Subkeys:");
			for(int i=0; i<16; i++){
				System.out.print("K[");
				if(i<9) System.out.print("0"+(i+1));
				else System.out.print(i+1);
				System.out.print("]: ");
				for(int k=0; k<48; k++){
					System.out.print(subKeys[i][k]);
				}
				System.out.println();
			}		*/
			
			subKeyBits = keysToBits(subKeys);	
					/*
			System.out.println();
			for(int i=0; i<16; i++){
				System.out.print("K[");
				if(i<9) System.out.print("0"+(i+1));
				else System.out.print(i+1);
				System.out.print("]: ");
				System.out.println(getBitSetString(subKeyBits[i]) + "\tLength: " + subKeyBits[i].length());
			}
			
*/
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
			boolean useIV = true;

			//get IV set up first
			byte[] iv = new byte[8]; 
    		//fill it with random bytes
			try{
				SecureRandom randomGen = SecureRandom.getInstance("SHA1PRNG");
				randomGen.nextBytes(iv);
			}
			catch( NoSuchAlgorithmException e) {
				System.out.println("No such algorithm!");
			}

			//random iv has been generated
			String ivString = new String (iv, "UTF-8");
			writer.write(iv);
			BitSet ivBits = bytesToBitSet(iv);
			ivBits.set(8);

			StringBuilder temp = new StringBuilder();
    		for (byte b : iv) {
        		temp.append(String.format("%02x", b));
    		}//end for
    		//System.out.println("IV: " + temp);



			while ( (nextByte = (byte) byteStream.read()) != -1){
				//if toEncrypt is full, encrypt them, print encrypted bytes,
				// store nextByte in toEncrypt[0], and leave with the count at 1
				if (count == 8){
					//xor with previous block
					BitSet bitsToEncrypt = bytesToBitSet(toEncrypt);
					//bitsToEncrypt.set(64);
					//System.out.println("First block: " + getBitSetString(bitsToEncrypt));
					
					if (useIV){
						useIV = false;
						//System.out.println(" IV bits: " + getBitSetString(ivBits));
						bitsToEncrypt.xor(ivBits);
						bitsToEncrypt.set(64);
						//System.out.println(" after xor: " + getBitSetString(bitsToEncrypt));
					}
					else{
						bitsToEncrypt.xor(encryptedBits);
						bitsToEncrypt.set(64);
						//System.out.println(" after xor: " + getBitSetString(bitsToEncrypt));
					}
					
					//get encrypted bitset
					encryptedBits = encrypt64Bits(bitsToEncrypt, subKeyBits);
					//get encrypted bytes
					//System.out.println(encryptedBits.length());
					encryptedBits.set(64, false);
					encryptedBytes = encryptedBits.toByteArray();

					if (encryptedBytes.length != 8){
						byte [] newEncryptedBytes = new byte[8];
						for (int i = 0; i < encryptedBytes.length; i++) {
							newEncryptedBytes[i] = encryptedBytes[i];
						}
						for (int i = encryptedBytes.length; i < 8; i++) {
							newEncryptedBytes[i] = 0;
						}
						encryptedBytes = newEncryptedBytes;
					}

					//System.out.println(encryptedBytes.length + encryptedBytes.toString());
					//get encryped string
					encryptedText = new String (encryptedBytes, "UTF-8");
					//write encryped string to the output file
					writer.write(encryptedBytes);

					//print the hex representation of he encrypted bits
					StringBuilder sb = new StringBuilder();
    				for (int i = 0; i < 8; i++) {
        				sb.append(String.format("%02x", encryptedBytes[i]));
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

				//xor with previous block
					BitSet bitsToEncrypt = bytesToBitSet(toEncrypt);
					//bitsToEncrypt.set(64);
					//System.out.println("last block: " + getBitSetString(bitsToEncrypt));
					//System.out.println(" Size:  " + bitsToEncrypt.length());

					
					if (useIV){
						useIV = false;
						//System.out.println(" IV bits: " + getBitSetString(ivBits));
						bitsToEncrypt.xor(ivBits);
						bitsToEncrypt.set(64);
						//System.out.println(" after xor: " + getBitSetString(bitsToEncrypt));
					}
					else{
						bitsToEncrypt.xor(encryptedBits);
						bitsToEncrypt.set(64);
						//System.out.println(" after xor: " + getBitSetString(bitsToEncrypt));
					}

				//get encrypted bitset
				encryptedBits = encrypt64Bits(bitsToEncrypt, subKeyBits);
				//encryptedBits.set(64, false);
				//get encrypted bytes
				encryptedBytes = encryptedBits.toByteArray();
				//get encryped string
				encryptedText = new String (encryptedBytes, "UTF-8");
				//write encryped string to the output file
				//System.out.println("about to write" + getBitSetString(encryptedBits));

				writer.write(encryptedBytes);

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
	/*
	public static int[] IP = {
		58, 50, 42, 34, 26, 18, 10, 2
		, 60, 52, 44, 36, 28, 20, 12, 4
		, 62, 54, 46, 38, 30, 22, 14, 6
		, 64, 56, 48, 40, 32, 24, 16, 8
		, 57, 49, 41, 33, 25, 17, 9, 1
		, 59, 51, 43, 35, 27, 19, 11, 3
		, 61, 53, 45, 37, 29, 21, 13, 5
		, 63, 55, 47, 39, 31, 23, 15, 7
	};*/

	
	/*
	 * TODO: You need to write the DES encryption here.
	 * This is going to collect bytes until it forms a new line
	 *
	 * At that point, its going to call encrypt64bits, get the enrypted bits, and return them.
	 *
	 * When the input arg line == null, it will need to prepare a buffer before sending whats
	 * left to the encrypt64bits function
	 * @param line
	*/
	
	//before coming here, the bits MUST be padded. 64 bits are expected as input
	private static BitSet encrypt64Bits(BitSet input, BitSet[] subkeys){
		//System.out.println("Initial bits to encrypt: " + getBitSetString(input));
		//System.out.println("Initial size: " + input.length());


		//permutate all inpute through table IP
		BitSet permutedBits = permute (input, IP);
		//System.out.println("Bits after IP:           " + getBitSetString(permutedBits));

		//58th bit of input should euqal the 1st bit ouf output
		if ( (permutedBits.get(0) && input.get(57)) && (!permutedBits.get(0) && !input.get(57))  ){
			System.out.println("problem");
		}

		//get left and right halves
		//BitSet left = permutedBits.get(0, permutedBits.length()/2);
		//BitSet right = permutedBits.get(permutedBits.length()/2, permutedBits.length());
		BitSet left = new BitSet(33);
		BitSet right = new BitSet(33);
		for(int i = 0; i < 32; i ++){
			if (permutedBits.get(i)){
				left.set(i);
			}
			if(permutedBits.get(i+32)){
				right.set(i);
			}
		}
		right.set(32);
		left.set(32);
		//System.out.println("Left:  " + getBitSetString(left));
		//System.out.println("Right: " + getBitSetString(right));
		

		//16 iterations using function f that operates on two blocks
		for (int i = 0; i < 16; i++){
			//System.out.println("Round " + (i+1) + " ----------------------------------------------");
			BitSet rightTemp = right;
			//System.out.println("right temp:                 " + getBitSetString(rightTemp));
			//f input: data of 32 bits and a key of 48 bits
			//f output: block of 32 bits
			BitSet newRight = roundFunction(right, subkeys[i]);
			//System.out.println("new right: " + getBitSetString(newRight));
			right = newRight;
			//System.out.println("right after rounds:         " + getBitSetString(right));
			//System.out.println("new right: " + getBitSetString(right));

			//new r = xor(L, f(R, subkey[i]))
			//System.out.println("Left before xor with right: " + getBitSetString(left));
			right.xor(left);
			right.set(32);
			//System.out.println("right after xor             " + getBitSetString(right));

			//new L = R before xor
			left = rightTemp;
			//System.out.println("new left:                   " + getBitSetString(left));
		}
		//System.out.println("After Rounds");
		//System.out.println("Left:  " + getBitSetString(left));
		//System.out.println("Right: " + getBitSetString(right));

		//reverse halves after rounds
		BitSet bitsAfterRounds = new BitSet(64);//+1 to account for zeros not printing
		for(int i = 0; i < 65; i++){
			//first half to be concatenated, right (reversed on purpose)
			if (i < 32){
				if(right.get(i))
					bitsAfterRounds.set(i);
			}
			//second half, left
			else{
				if(left.get(i-32))
					bitsAfterRounds.set(i);
			}
		}
		bitsAfterRounds.set(64);
		//System.out.println("Bits after 16 rounds: " + getBitSetString(bitsAfterRounds));



		//apply FP table to output
		BitSet bitsToReturn = permute(bitsAfterRounds, FP);
		//System.out.println("Bits after FP: " + getBitSetString(bitsToReturn));

		//return encrypted bits

		return bitsToReturn;
	}

	private static BitSet roundFunction(BitSet input, BitSet key){

		//expand input from 32 to 48 bits with selection table E
		//System.out.println("    Bits before expansion:       " + getBitSetString(input));
		BitSet afterExpansion = permute(input, E);
		//System.out.println("    Bits after expansion:        " + getBitSetString(afterExpansion));

		//xor the output with the key
		//afterExpansion.xor(key);
		afterExpansion.set(48);
		//System.out.println("    Bits after xor with the key: " + getBitSetString(afterExpansion));

		BitSet afterSboxReduction = new BitSet(32);
		afterSboxReduction.clear();

		//do sbox reduction from 48 bits to 36
		int firstBit = 0;
		int firstBitToSet = 0;

		for (int i = 0; i < 8; i++){

			//find sbox row using six bits starting with firstBit
			int row = 0;
			//if the first bit is one, add 2 to row
			if (afterExpansion.get(firstBit))
				row += 2;
			//if the last bit is one, add 1 to row
			if (afterExpansion.get(firstBit+5))
				row += 1;

			//sfind sbox col using six bits starting with firstBit
			int col = 0;
			if (afterExpansion.get(firstBit+1))
				col += 8;
			if (afterExpansion.get(firstBit+2))
				col +=4;
			if (afterExpansion.get(firstBit+3))
				col+=2;
			if (afterExpansion.get(firstBit+4))
				col+=1;

			int sBoxVal = S[row][col];


			//System.out.println("number to represent in binary: " + sBoxVal);
			String bits = Integer.toBinaryString(sBoxVal);
			//System.out.println(bits);

			//set the proper bits
			if(bits.length() == 1){
				if (bits.charAt(0) == '1')
					afterSboxReduction.set(firstBitToSet+3);
			}
			if(bits.length() == 2){
				if (bits.charAt(0) == '1')
					afterSboxReduction.set(firstBitToSet+2);
				if (bits.charAt(1) == '1')
					afterSboxReduction.set(firstBitToSet+3);

			}
			if(bits.length() == 3){
				if (bits.charAt(0) == '1')
					afterSboxReduction.set(firstBitToSet+1);
				if (bits.charAt(1) == '1')
					afterSboxReduction.set(firstBitToSet+2);
				if (bits.charAt(2) == '1')
					afterSboxReduction.set(firstBitToSet+3);

			}
			if(bits.length() == 4){
				if (bits.charAt(0) == '1')
					afterSboxReduction.set(firstBitToSet);
				if (bits.charAt(1) == '1')
					afterSboxReduction.set(firstBitToSet+1);
				if (bits.charAt(2) == '1')
					afterSboxReduction.set(firstBitToSet+2);
				if (bits.charAt(3) == '1'){
					afterSboxReduction.set(firstBitToSet+3);
				}
			}	
			/*

			if (i < 7){
				afterSboxReduction.set(firstBitToSet+4);
				//System.out.println("Binary Representation: " + getBitSetString(afterSboxReduction.get(firstBitToSet, firstBitToSet+5)));
				afterSboxReduction.set(firstBitToSet+4, false);
			}*/

			firstBit = firstBit+6;
			firstBitToSet = firstBitToSet+4;

		}

		afterSboxReduction.set(32);
		//System.out.println("    After SBox replacements:     " + getBitSetString(afterSboxReduction));

		//do final permutation P
		BitSet toReturn = permute(afterSboxReduction, P);
		toReturn.set(32);
		//System.out.println("    After final P :              " + getBitSetString(toReturn));
		//System.out.println("After final permute: " + getBitSetString(toReturn));



		//return encrypted block
		return toReturn;
	}

	private static BitSet permute(BitSet in, int [] permuteTable){

		int numberOfBits = permuteTable.length;
		BitSet output = new BitSet(numberOfBits);

		for (int i = 0; i < permuteTable.length; i++){
			//get the value of the first bit
			boolean bitValue = in.get(permuteTable[i]-1);
			//set the permuteTable[i] bit of output to that value.
			output.set(i, bitValue);
		}
		output.set(permuteTable.length);
		return output;

	}

	private static String getBitSetString(BitSet input){

		StringBuilder output = new StringBuilder();
		int count = 0;

		for (int i = 0; i < input.length()-1; i++){
			if ( input.get(i) == true ) {
				output.append("1");
			} else if (input.get(i) == false ){
				output.append("0");
			}
			count ++;
			if (count == 4){
				output.append(" ");
				count = 0;
			}
		}
		return output.toString();
	}

	/*
	* given an array of bytes, this function return a bitset that represents the array.
	*
	*/
	private static BitSet bytesToBitSet(byte[] input){

		//each byte has 8 bits in the bitset
		BitSet toReturn = new BitSet(input.length * 8 + 1);
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
		toReturn.set(input.length * 8);
		return toReturn;
	}//bytesToBitSet

	static byte[][] makeSubKeys(String keyBin){
	
		byte[] C0 = new byte[28];
		byte[] D0 = new byte[28];
		byte[][] C = new byte[16][28]; //Arrays for two halves of eventual subkey generation
		byte[][] D = new byte[16][28];
		byte[][] CD = new byte[16][56]; //Array that puts the halves together
		byte[][] subKeys = new byte[16][48]; //Array that holds permuted subkeys
		
		//Make the first permutation 
		//System.out.println("First permutation (C0 + D0): ");
		int i=0;
		for(i=0; i<28; i++){
			if(keyBin.charAt(PC1[i]-1) == '1'){ //PC1[i]-1 because of array math :P
				C0[i]=1; 						//Can't actually just copy the contents of the character because chars can't into bytes	
			} else 	C0[i]=0;
		}
		
		//Same thing down here, but for the second half of the permutation. That's why i starts at 28.
		for(i=28; i<56; i++){				
			if(keyBin.charAt(PC1[i]-1) == '1'){
				D0[i-28]=1;
				} else	D0[i-28]=0;
		}
		
		//Print out permutation halves
		/*
		System.out.print("C0: ");
		for(i=0; i<28; i++)
			System.out.print(C0[i]);
		
		System.out.print(" D0: ");
		for(i=0; i<28; i++)
			System.out.print(D0[i]);
		*/
			
		//System.out.println("\nShifts: ");
		
		C[0] = leftShift(C0, rotations[0]);
		D[0] = leftShift(D0, rotations[0]);
		
		for(i=1; i<16; i++){ 					//Do left shifts
			C[i] = leftShift(C[i-1], rotations[i]);
			D[i] = leftShift(D[i-1], rotations[i]);
		}
		
		//Just printing out the left shifted sub-subkeys, won't need eventually
		/*
		for(i=0; i<16; i++){
			System.out.print("C[");
			if(i<10) System.out.print("0"+(i));
			else System.out.print(i);
			System.out.print("]: ");
			for(int k=0; k<28; k++){
				System.out.print(C[i][k]);
			}
			System.out.print(" D[");
			if(i<10) System.out.print("0"+(i));
			else System.out.print(i);
			System.out.print("]: ");
			for(int j=0; j<28; j++){
				System.out.print(D[i][j]);
			}
			System.out.println();
		}
		*/
	
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
		System.arraycopy(toShift, 0, shifted, 0, toShift.length);								//Copy the provided byte to it (Had to look up how to copy arrays -- surprisingly easy!
		for(int i=0; i<numShifts; i++){
			byte temp = shifted[0];							//Move everything over one! Hold the first position temporarily.
			for(int k=0; k<toShift.length-1; k++)
				shifted[k] = shifted[k+1];
			shifted[toShift.length-1] = temp;				//Put that temporarily held bit back, in the last position
		}
		return shifted; 									//Send the fancy new shifted byte back over
	}
	
	static BitSet[] keysToBits(byte[][] subKeys){
		BitSet[] subKeyBits = new BitSet[16];
		for(int i=0; i<16; i++){
			subKeyBits[i] = new BitSet(49);
			subKeyBits[i].set(48, true);
		}
		
		for(int i=0; i<subKeys.length; i++){
			for(int k=0; k<48; k++){
				if(subKeys[i][k] == 1)
					subKeyBits[i].set(k, true);
				else if(subKeys[i][k] ==0) 
					subKeyBits[i].set(k, false);
			}
		}
		
		return subKeyBits;	
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
    		//FileOutputStream outputStream = new FileOutputStream("key.k");
    		//outputStream.write(bytes);
    		//outputStream.close();


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
							StringBuilder outputFile, StringBuilder keyStr,
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
		        	  keyStr.append(arg);
		        	  encrypt.append("e");
		        	  break;
	     	  	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyStr.append(arg);
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
