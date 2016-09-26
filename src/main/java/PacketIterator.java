

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

/** 
 * 
 * This class will handle producing a dataset by reading in
 * a .csv file for a packet capture and produce each field 
 * as a 
 * 
 * 
 * @author People
 */
public class PacketIterator implements DataSetIterator {
	private static final long serialVersionUID = -7287833919126626356L;
	private static final int MAX_SCAN_LENGTH = 200; 
	private char[] validCharacters;
	static Map<Character,Integer> charToIdxMap;
	private char[] fileCharacters;
	private int packetLength;
	private int numberOfPackets;
	private int numExamplesToFetch;
	private int examplesSoFar = 0;
	private int currentPacketIndex = 1;
	
	private Random rng;
	private final int numCharacters;//number of input nodes
	private final boolean alwaysStartAtNewLine;
	
	private int numberOfMiniSessions = 0;
	CSVParser filehandler;
	
	int number_of_packets;
	
	
	public PacketIterator(String path, int miniBatchSize, int exampleSize, int numExamplesToFetch ) throws IOException {
		this(path,Charset.defaultCharset(),miniBatchSize,exampleSize,numExamplesToFetch,getCharacterSet(), new Random(),true);
	}
	
	/**
	 * @param textFilePath Path to text file to use for generating samples
	 * @param textFileEncoding Encoding of the text file. Can try Charset.defaultCharset()
	 * @param miniBatchSize Number of examples per mini-batch
	 * @param exampleLength Number of characters in each input/output vector
	 * @param numExamplesToFetch Total number of examples to fetch (must be multiple of miniBatchSize). Used in hasNext() etc methods
	 * @param validCharacters Character array of valid characters. Characters not present in this array will be removed
	 * @param rng Random number generator, for repeatability if required
	 * @param alwaysStartAtNewLine if true, scan backwards until we find a new line character (up to MAX_SCAN_LENGTH in case
	 *  of no new line characters, to avoid scanning entire file)
	 * @throws IOException If text file cannot  be loaded
	 */
	public PacketIterator(String textFilePath, Charset textFileEncoding, int miniBatchSize, int packetLength_in,
			int numExamplesToFetch, char[] validCharacters, Random rng, boolean alwaysStartAtNewLine ) throws IOException {
		
		if(textFilePath != null)
			if( !new File(textFilePath).exists()) throw new IOException("Could not access file (does not exist): " + textFilePath);

		if( miniBatchSize <= 0 ) throw new IllegalArgumentException("Invalid miniBatchSize (must be >0)");
		this.validCharacters = validCharacters;
		
		//Controls Packet Iterator
		this.packetLength = packetLength_in;
		this.numberOfPackets = miniBatchSize;
		this.numExamplesToFetch = numExamplesToFetch;
		
		this.rng = rng;
		this.alwaysStartAtNewLine = true;
		
		//Store valid characters is a map for later use in vectorization
		
		charToIdxMap = new HashMap<>();
		
		for( int i=0; i<validCharacters.length; i++ ) charToIdxMap.put(validCharacters[i], i);
		numCharacters = validCharacters.length;
		
		//Load file and convert contents to a char[] 
		//CSV file
		System.out.println("Loading packet capture.");
		if(textFilePath !=null)
			{
			this.filehandler = new CSVParser(textFilePath);
			System.out.println("Packet Capture file loaded.");
			this.number_of_packets = this.filehandler.numberOfPackets;
			System.out.println("There are " + this.number_of_packets +" Packets in the file.");
			}
		else{
			System.out.println("Failed to Load File.");
			}
	
		
	}
	
	/** A minimal character set, with a-z, A-Z, 0-9 and common punctuation etc */
	public static char[] getCharacterSet(){
		List<Character> validChars = new LinkedList<>();
		for(char c='a'; c<='z'; c++) validChars.add(c);
		for(char c='A'; c<='Z'; c++) validChars.add(c);
		for(char c='0'; c<='9'; c++) validChars.add(c);
		char[] temp = {':', '.',',','_','"','/'};
		for( char c : temp ) validChars.add(c);
		char[] out = new char[validChars.size()];
		int i=0;
		for( Character c : validChars ) out[i++] = c;
		return out;
	}
	
	
	
	public char convertIndexToCharacter( int idx ){
		return validCharacters[idx];
	}
	
	public int convertCharacterToIndex( char c ){
		return charToIdxMap.get(c);
	}
	
	public char getRandomCharacter(){
		return validCharacters[(int) (rng.nextDouble()*validCharacters.length)];
	}

	public boolean hasNext() {
		return examplesSoFar + numberOfPackets <= numExamplesToFetch;
	}


	
	public DataSet next()
		{	
		return next(this.numberOfPackets);
		}

	public char[] getPacket(CSVParser filehandler_in)
	{
		String newPacket =  
							filehandler_in.toAddress.get(currentPacketIndex).substring(1, filehandler_in.toAddress.get(currentPacketIndex).length() - 1)
							+","+filehandler_in.toPort.get(currentPacketIndex).substring(1, filehandler_in.toPort.get(currentPacketIndex).length() - 1)
							
							+","+filehandler_in.fromAddress.get(currentPacketIndex).substring(1, filehandler_in.fromAddress.get(currentPacketIndex).length() - 1)
							+","+filehandler_in.fromPort.get(currentPacketIndex).substring(1, filehandler_in.fromPort.get(currentPacketIndex).length() - 1)
							
							+","+filehandler_in.protocol.get(currentPacketIndex).substring(1, filehandler_in.protocol.get(currentPacketIndex).length() - 1)
							+","+filehandler_in.packetLength.get(currentPacketIndex).substring(1, filehandler_in.packetLength.get(currentPacketIndex).length() - 1);
		
		
		
		return newPacket.toCharArray();
	
	}
	
	
	//READ IN SPECIFIED NUMBER OF PACKETS AND FEEDING THEIR CHARACTER MAPPING INTO A DataSet
	//
	public DataSet next(int numberOfPackets_In) {
		
		numberOfMiniSessions++;
	
		  
		
		
		//Allocate space:
		INDArray input = Nd4j.zeros(new int[]{numberOfPackets_In,numCharacters,packetLength});
		
		INDArray labels = Nd4j.zeros(new int[]{numberOfPackets_In,numCharacters,packetLength});
		
		
		char[] currentPacket;
		
		int currCharIdx;
		int nextCharIdx;
		//select a subset of the file.
		// Pick out each field and add to dataset
		
	//	System.err.println();
	//	System.err.println("Input Dataset number "+ numberOfMiniSessions );

		for(int x=0;x<numberOfPackets && x<CSVParser.numberOfPackets;x++)
			{	
			//loop through required number of packets
			//place each field from current global index in single string seperated by commas
			//feed string into DataSet
			
			
			currentPacket = getPacket(this.filehandler);
		
			
			currCharIdx = charToIdxMap.get(currentPacket[0] );	//Current input
		
		
	//		System.err.print(currentPacket[0]);
				//Feed in Time
				int c =0;
					for(int j=1;j < currentPacket.length;j++,c++)
					{
						nextCharIdx = charToIdxMap.get(currentPacket[j] );
						//System.err.print(currentPacket[j]);
						
						input.putScalar(new int[]{0,currCharIdx,c}, 1.0);
						
						labels.putScalar(new int[]{0,nextCharIdx ,c }, 1.0);
						
						float temp = input.getFloat(new int[]{0,currCharIdx,c});
						float temp2 = labels.getFloat(new int[]{0,nextCharIdx,c});
						
						if(temp != 1.0f)
							{
							throw new RuntimeException();
							}
						
						if(temp2 != 1.0f)
							{
							throw new RuntimeException();
							}
						currCharIdx = nextCharIdx;
						
					}
		
					currentPacketIndex++;
			
		
		
			}
		///LOOP THROUGH
	
		
		
		examplesSoFar += numberOfPackets_In;
		
		
		return new DataSet(input,labels);
		
	}

	public int totalExamples() {
		return numExamplesToFetch;
	}

	public int inputColumns() {
		//return numCharacters;
		return numCharacters;
	}

	public int totalOutcomes() {
		//return numCharacters;
		return numCharacters;
	}

	public void reset() {
		examplesSoFar = 0;
	}

	public int batch() {
		return numberOfPackets;
	}

	public int cursor() {
		return examplesSoFar;
	}

	public int numExamples() {
		return numExamplesToFetch;
	}

	public void setPreProcessor(DataSetPreProcessor preProcessor) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	

	
	
}