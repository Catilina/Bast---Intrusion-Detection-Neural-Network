
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.ml.neuralnet.Network;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.jblas.DoubleMatrix;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import junit.framework.Assert;


/**NetSentry Traffic anomaly detection
 * 
 * @author people

 */
public class TrafficTrainer {
			
		///INSTANCE VARIABLES
	//Step 3 variables
			int packetLength;					//How long each packet or line should be that the NN reads in at once (unit)
			String generationInitialization;	//what it starts with to learn
			int nCharactersToSample; 			//Byte read in at one time from the packet into the neural network BEFORE it is parsed.
			int numHiddenLayers;				//How many layers used (not including input)

			//Step 4 variables
			//Train a network 
			int numEpochs;						//Total number of training + sample generation epochs
			double epochToTotal_ratio ;	//how many sections of the total document are going to be split into to epochs for training sessions
			double packetsToEpoch_ratio;	//ratio of number of packets to the epochs
			int numberOfMiniBatches;			//How many sets of packets it does during an Epoch
			int numberOfPackets;				//number of packets the NN grabs to train with at one time
			int examplesPerEpoch;				//number of examples that the NN spits out between training sessions
			int nSamplesTo;					//Number of samples to  after each training epoch
			int lstmLayerSize;					//Number of bytes feed back into each layer, must be a set size regardless of amount of input given
			boolean isDoneTraining;
			int lengthOfTimeSeries;
			//Layer variables
			int seed;
			int nIterations; //Number of iterations during training
			double learningRate;
			String OptiAlgorithm;
			int inputNodes;
			String inputField1;
			String inputField2;
			String inputField3;
			String inputField4;
			String inputField5;
			int outputNodes;
			String outputField1;
			String outputField2;
			String outputField3;
			String outputField4;
			String outputField5;

			
			boolean danger;
	
	public void initializeTraining(int epochs, 
									int numBatches, 
									int numPackets, 
									int examplesEpoch,
									double epochToData_ratio_in,
									double packetsToEpoch_ration_in)
	{
		this.epochToTotal_ratio = epochToData_ratio_in;
		this.packetsToEpoch_ratio = packetsToEpoch_ration_in; 
		this.numEpochs =	epochs;
		this.numberOfMiniBatches = numBatches;
		this.numberOfPackets = numPackets;
		this.examplesPerEpoch = examplesEpoch;
	}
		
		
	
		
		//Training menu variable
		int nOut;
		Random rng;
		
		MultiLayerConfiguration conf = null;
		MultiLayerNetwork net = null;
				
		MultiLayerNetwork Test_net = null;
		//LOADing menu variables
		
		PacketIterator Test_iter;
		
		//PacketGrabber variables
		//PacketGrabber networkinterface = null;
		
  	    //MENU NAVIGATION VARIABLES	
		
		public TrafficTrainer()
		{
			
			//NN variables
		this.lstmLayerSize = 17;					//Number of units in each GravesLSTM layer
		this.numberOfPackets = 5; //Size of mini batch to use when  training
		this.numberOfMiniBatches = 50;
		this.examplesPerEpoch = this.numberOfMiniBatches * this.numberOfPackets;	//i.e., how many examples to learn on between generating samples	
		this.packetLength = 65;					//Length of each training example
		this.numEpochs = 300;						//Total number of training + sample generation epochs
		this.nSamplesTo = 20;					//Number of samples to  after each training epoch
		this.nCharactersToSample = 65; 				//Length of each sample to 
		this.nIterations = 2; //Number of iterations during training	
		this.learningRate = 0.01;
		this.isDoneTraining = false;
		this.danger = false;
		this.lengthOfTimeSeries = 1;
		}
		
		public TrafficTrainer(int lstmLayerSize_in,
				int numberOfPackets_in,
				int numberOfMiniBatches_in,
				int numEpochs_in,
				int nSamplesTo_in,
				int nCharactersToSample_in,
				int nIterations_in, double learningRate_in)
		{
			
			//NN variables
		this.lstmLayerSize = lstmLayerSize_in;					//Number of units in each GravesLSTM layer
		this.numberOfPackets = numberOfPackets_in; //5; //Size of mini batch to use when  training
		this.numberOfMiniBatches = numberOfMiniBatches_in; //50;
		this.examplesPerEpoch = this.numberOfMiniBatches * this.numberOfPackets;	//i.e., how many examples to learn on between generating samples	
		this.packetLength = 65; //65;					//Length of each training example
		this.numEpochs = numEpochs_in; //300;						//Total number of training + sample generation epochs
		
		this.nSamplesTo = nSamplesTo_in; //20;					//Number of samples to  after each training epoch
		this.nCharactersToSample = 65; //65; 				//Length of each sample to 
		
		this.nIterations = nIterations_in; //2; //Number of iterations during training
			
		this.learningRate = learningRate_in;
			
		
		this.isDoneTraining = false;
		this.danger = false;
		this.lengthOfTimeSeries = 1;
		}
		
		public void createNewTT()
		{
		

			//Step 3 variables
			setPacketLength(65);					//How long each packet or line should be that the NN reads in at once (unit)
			setGenerationInitialization(null);
			setNumHiddenLayers(3);				//How many layers used (not including input)
			setCharactersToSample(60); 			//Byte read in at one time from the packet into the neural network BEFORE it is parsed.
					
			//Step 4 variables
			//Train a network 
			setNumEpochs(300);						//Total number of training + sample generation epochs
			setEpochToTotal_ratio(0.10);	//how many sections of the total document are going to be split into to epochs for training sessions
			setPacketsToEpoch_ratio(0.25);	//ratio of number of packets to the epochs
			setNumberOfMiniBatches(50);			//How many sets of packets it does during an Epoch
			setNumberOfPackets(5);				//number of packets the NN grabs to train with at one time
			setExamplesPerEpoch(getNumberOfMiniBatches()*getNumberOfPackets());				//number of examples that the NN spits out between training sessions
			setSamplesTo(20);		//Number of samples to  after each training epoch
			setLayerSize(17);					//Number of bytes feed back into each layer, must be a set size regardless of amount of input given
			
			
			//Layer variables
			setSeed(1);
			setnIterations(2); //Number of iterations during training
			setLearningRate(0.01);
			setOptimizationAlgorithm("LINE_GRADIENT_DESCENT");
			setinputNodes(3);
			setinputField1(null);
			setinputField2(null);
			setinputField3(null);
			setinputField4(null);
			setinputField5(null);
			setoutputNodes(3);
			setOutputField1(null);
			setOutputField2(null);
			setOutputField3(null);
			setOutputField4(null);
			setOutputField5(null);
			
			
		}
		
////PROGRAM ENTRY


		
public void Train(String fileName_in, int t_seriesLength_in){		
	///SETTING UP NEURAL NETWORK	
		// Above is Used to 'prime' the LSTM with a character sequence to continue/complete.
		// Initialization characters must all be in CharacterIterator.getMinimalCharacterSet() by default
		rng = new Random(7);
		
		//Get a DataSetIterator that handles vectorization of text into something we can use to train
		// our GravesLSTM network.
		PacketIterator iter = null;
		try {
			iter = getPacketIterator(this.numberOfPackets,this.packetLength,this.examplesPerEpoch,true,fileName_in);
		} catch (Exception e5) {
			// TODO Auto-d catch block
			e5.printStackTrace();
		}
	    nOut = iter.totalOutcomes();
		

	    
	    this.lengthOfTimeSeries = t_seriesLength_in;
	    
		conf = new NeuralNetConfiguration.Builder()
			.timeSeriesLength(t_seriesLength_in)
			.optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT).iterations(this.nIterations)//set to default for testing
			.learningRate(getLearningRate())//added
			.rmsDecay(0.95)
			.seed(7)//added
			.regularization(true)
			.l2(0.001)
			.list(3)//hard coded number of layers 
			.layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(this.lstmLayerSize)//input columns now gets input nodes
					.updater(Updater.RMSPROP)
					.activation("tanh").weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build())
			.layer(1, new GravesLSTM.Builder().nIn(this.lstmLayerSize).nOut(this.lstmLayerSize)
					.updater(Updater.RMSPROP)
					.activation("tanh").weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build())
			.layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation("softmax")        //MCXENT + softmax for classification
					.updater(Updater.RMSPROP)
					.nIn(this.lstmLayerSize).nOut(nOut).weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build())
			.pretrain(false).backprop(true)
			
			.build();
		
		net = new MultiLayerNetwork(conf);
		
		net.init();
		net.setListeners(new ScoreIterationListener(1));
		

	

		
		//Print the  number of parameters in the network (and for each layer)
		Layer[] layers = net.getLayers();
		int totalNumParams = 0;


		for( int i=0; i<layers.length; i++ ){
			int nParams = layers[i].numParams();
			MBout.output("Number of parameters in layer " + i + ": " + nParams);
			totalNumParams += nParams;
		}
		
		
		MBout.output("Total number of network parameters: " + totalNumParams);
		
		
		
		
		int i=0;
		
		//Do training, and then  and print samples from network
		while( iter.hasNext() && !isDoneTraining)
			{
			net.fit(iter);
			
			MBout.output("--------------------");
			MBout.output("Completed epoch " + i );
			MBout.output("Sampling characters from network given initialization \""+ (generationInitialization == null ? "" : generationInitialization) +"\"");
			String[] samples = sampleCharactersFromNetwork(generationInitialization,net,iter,rng,nCharactersToSample,nSamplesTo);
			
			for( int j=0; j<samples.length; j++ )
				{
				MBout.output("----- Sample " + j + " -----");
				MBout.output(samples[j]);
				}
			
			iter.reset();	//Reset iterator for another epoch
			i++;
		}
		
		
		
		
//Write model to file
		
		
		MBout.output("\n\nTraining complete, writing training session to file");
		
		OutputStream fos = null;
		try {
			fos = Files.newOutputStream(Paths.get("coefficients.bin"));
		} catch (IOException e4) {
			// TODO Auto-d catch block
			e4.printStackTrace();
		}
        DataOutputStream dos = new DataOutputStream(fos);
        try {
			Nd4j.write(net.params(), dos);
		} catch (IOException e3) {
			// TODO Auto-d catch block
			e3.printStackTrace();
		}
        try {
			dos.flush();
		} catch (IOException e2) {
			// TODO Auto-d catch block
			e2.printStackTrace();
		}
        try {
			dos.close();
		} catch (IOException e1) {
			// TODO Auto-d catch block
			e1.printStackTrace();
		}
        try {
			FileUtils.writeStringToFile(new File("conf.json"), net.getLayerWiseConfigurations().toJson());
		} catch (IOException e) {
			// TODO Auto-d catch block
			e.printStackTrace();
		}

      
        MBout.output("Finished writing session to file");
		if(this.isDoneTraining == true)
			this.setDoneTraining();
}
		
public void setDoneTraining()
	{
	if(this.isDoneTraining == false)
		this.isDoneTraining = true;
	else
		this.isDoneTraining = false;
	}


//LOADING MENU
//gives user the option of loading a NN from a specific location
public void LoadNeuralNet(String cfgFileLocation, String coeffFileLocation)
	{
	
	try{
	 conf = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(cfgFileLocation)));
     
     DataInputStream dis = new DataInputStream(new FileInputStream(coeffFileLocation));
     INDArray newParams = Nd4j.read(dis);
     dis.close();

     net = new MultiLayerNetwork(conf);
     net.init();
     net.setParameters(newParams);
     System.out.println(net.params());
     
	}
	catch(IOException e)
	{
		MBout.output("Network or Config file not found. Train a network first.");
		MBout.output(e.getMessage());

	}
	
     
     
     
     
     
	}

public void Examples(int howMany,String initString) throws Exception
{
	if(net == null)
	{
		MBout.output("You first Need to train or load a Neural Network!!!");

	}
	else
	{
		int nmbrOfExamples = howMany;
		
		Random rnd = new Random(13);
		MBout.output("Getting packet itterator");
		
		PacketIterator example_iterator = getPacketIterator(numberOfPackets,packetLength,examplesPerEpoch,false,null);
		
		MBout.output("Sampling characters from network given initialization:" + (generationInitialization == null ? "" : generationInitialization) +"\"");
	
		
		String[] samples = sampleCharactersFromNetwork(initString,net,example_iterator,rnd,nCharactersToSample,nmbrOfExamples);
		for( int j=0; j<samples.length; j++ ){
			MBout.packet("\n----- Sample " + j + " -----\n");
			MBout.output(samples[j]);
			
		}

	}
	
	
}


//TEST MENU
//Sets an interface to monitor mode and and begins packet capture and comparison
//This is where the voodoo happens
//should check that valid network is loaded
public void ChallengeNeuralNet(String challengeFile_in)
	{
	int counter = 0;
	//compare
	if(net == null)
	{
		MBout.output("You first Need to train or load a Neural Network!!!");
		
	}
	else
	{
	
		
		MBout.output("Testing Input Packets against Network.");
		
		PacketIterator iter = null;
		
		try {
			iter = getPacketIterator(this.numberOfPackets,this.packetLength,this.examplesPerEpoch,true,challengeFile_in);
		
			
			//Create Evaluation Object 
			Evaluation evaluator = new Evaluation();
		    
	
			//test each packet in in iterator
		    while(iter.hasNext()) {
	
		   
		    	
		    	 DataSet dsTest = iter.next();
		    	
		
		    	 
		    	 
		    	 
		    	 //generate predicted features using challenge set as input
		            INDArray predicted = net.output(dsTest.getFeatureMatrix(), true);
		         //get dataset of actual features
		            INDArray actual = dsTest.getLabels();
		        
		      

		          
		          
		          //evaluate predicted against actual results
		         evaluator.eval(actual.norm1(2) ,predicted.norm1(2));
		         
		         if(evaluator.f1() < 0.420)
		         	{
		        	 this.danger = true; 
		        	 MBout.output("ABNORMAL TRAFFIC!!");
		         	}
		         else{ 
		        	 this.danger = false;
		        	 MBout.output("Everything is normal!!");
		         		}
		         counter++;            
		    if(counter == 10){     
		       MBout.output(evaluator.stats());
		       counter=0;
		    	}
		       
		    }
			
			
			
			
			
			
		} catch (Exception e5) {
			// TODO Auto-d catch block
			e5.printStackTrace();
		
		}	
		
		
		
		}	
	
	}

	


	/** 
	 *@param int size of the sub-strings being fed back into model
	 *@param length of examples to output <length of packet>
	 *@param Number of examples to  during training for each epoch
	 *
	 *@returns a configured packet iterator object.
	 *
	 *ask 
	 *
	 */
	private static PacketIterator getPacketIterator(int miniBatchSize, 
			int packetLength, 
			int examplesPerEpoch, 
			boolean loadFile, 
			String file_in) 
					throws Exception{
		
		
		
		
		
		
		if(loadFile){
		
		
		
		
	
		
		
		
			//Storage location from downloaded file
		File f = new File(file_in);
		if( !f.exists() ){
			
			MBout.output("The file " + file_in + " does not exsist.");
		} else {
			MBout.output("Opening " + f.getAbsolutePath());
		}
		
		if(!f.exists()) throw new IOException("File does not exist: " + file_in);	//Download problem?
		}
		
		char[] validCharacters = PacketIterator.getCharacterSet();	//Which characters are allowed? Others will be removed
		
		return new PacketIterator(file_in, Charset.forName("UTF-8"),
				miniBatchSize, packetLength, examplesPerEpoch, validCharacters, new Random(31337),true);
	}
	

	
	
	
	/**  a sample from the network, given an (optional, possibly null) initialization. Initialization
	 * can be used to 'prime' the RNN with a sequence you want to extend/continue.<br>
	 * Note that the initalization is used for all samples
	 * @param initialization String, may be null. If null, select a random character as initialization for all samples
	 * @param charactersToSample Number of characters to sample from network (excluding initialization)
	 * @param net MultiLayerNetwork with one or more GravesLSTM/RNN layers and a softmax output layer
	 * @param iter CharacterIterator. Used for going from indexes back to characters
	 */
	private static String[] sampleCharactersFromNetwork( String initialization, MultiLayerNetwork net,
			PacketIterator iter, Random rng, int charactersToSample, int numSamples ){
		
		
		if( initialization == null )
		{
			initialization = String.valueOf("10.0.1.29,");
			
			
		}
		//	
		
		
		//Create input for initialization
		INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), initialization.length());
		
		char[] init = initialization.toCharArray();
		
		for(int d=0;d<init.length;d++)
			
		
	
		for( int i=0; i<init.length; i++ ){
			int idx = iter.convertCharacterToIndex(init[i]);
			
	
			for( int j=0; j<numSamples; j++ ){
				
				initializationInput.putScalar(new int[]{j,idx,i}, 1.0f);
			}
		}
	
		
		StringBuilder[] sb = new StringBuilder[numSamples];
		for( int i=0; i<numSamples; i++ ) sb[i] = new StringBuilder(initialization);
		
		//Sample from network (and feed samples back into input) one character at a time (for all samples)
		//Sampling is done in parallel here
		
		net.rnnClearPreviousState();
		INDArray output = net.rnnTimeStep(initializationInput);
		output = output.tensorAlongDimension(output.size(2)-1,1,0);	//Gets the last time step output
		
		for( int i=0; i<charactersToSample; i++ ){
			//Set up next input (single time step) by sampling from previous output
			INDArray nextInput = Nd4j.zeros(numSamples,iter.inputColumns());
			//Output is a probability distribution. Sample from this for each example we want to , and add it to the new input
			for( int s=0; s<numSamples; s++ ){
				double[] outputProbDistribution = new double[iter.totalOutcomes()];
				for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);
				int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution,rng);
				
				nextInput.putScalar(new int[]{s,sampledCharacterIdx}, 1.0f);		//Prepare next time step input
				sb[s].append(iter.convertIndexToCharacter(sampledCharacterIdx));	//Add sampled character to StringBuilder (human readable output)
			}
			
			output = net.rnnTimeStep(nextInput);	//Do one time step of forward pass
		}
		MBout.output("done generating samples");
		
		String[] out = new String[numSamples];
		for( int i=0; i<numSamples; i++ ) out[i] = sb[i].toString();
		return out;
	}
	
	


	
	
	
	/*
	 * Getters and Setters for STEP 3 of BAST	
	 */	
	//Getters	
	public int getPacketLength()
	{
		return packetLength;
	}
	public String getGenerationInitialization()
	{
		return generationInitialization;
	}
	public int getnCharactersToSample()
	{
		return nCharactersToSample;
	}
	public int getNumHiddenLayers()
	{
		return numHiddenLayers;
	}
	//Setters
	public void setPacketLength(int num)
	{
		packetLength = num;
	}
	public void setGenerationInitialization(String a)
	{
		generationInitialization = a;
	}
	public void setCharactersToSample(int num)
	{
		nCharactersToSample = num;
	}
	public void setNumHiddenLayers(int num)
	{
		numHiddenLayers = num;
	}
	
	
	/*
	 * Training
	 */
	public int getNumEpochs()
	{
		return numEpochs;
	}
	public double getEpochToTotal_ratio()
	{
		return epochToTotal_ratio;
	}
	public double getPacketsToEpoch_ratio()
	{
		return packetsToEpoch_ratio;
	}
	public int getNumberOfMiniBatches()
	{
		return numberOfMiniBatches;
	}
	public int getNumberOfPackets()
	{
		return numberOfPackets;
	}
	public int getSamplesTo()
	{
		return nSamplesTo;
	}
	public int getLayerSize()
	{
		return lstmLayerSize;
	}
	//Setters
	public void setNumberOfMiniBatches(int num)
	{
		numberOfMiniBatches = num;
	}
	public void setNumberOfPackets(int num)
	{
		numberOfPackets = num;
	}
	public void setExamplesPerEpoch(int num)
	{
		examplesPerEpoch = num;
	}
	public void setSamplesTo(int num)
	{
		nSamplesTo = num;
	}
	public void setLayerSize(int num)
	{
		lstmLayerSize = num;
	}
	public void setNumEpochs(int num)
	{
		numEpochs = num;
	}
	public void setEpochToTotal_ratio(double num)
	{
		epochToTotal_ratio = num;
	}	
	public void setPacketsToEpoch_ratio(double num)
	{
		packetsToEpoch_ratio = num;
	}

	
	/*
 * Getters and Setters for Layers
 */
//Getters
public int getSeed()
{
	return seed;
}
public int getnIterations()
{
	return nIterations;
}
public double getLearningRate()
{
	return learningRate;
}
public OptimizationAlgorithm getOptimizationAlgorithm()
{
	//Method takes a string and compares to the enum array of optimization algorithms 
	//returns the enumerated algorithm that matches
	return (OptimizationAlgorithm.valueOf(OptiAlgorithm));
}
public int getinputNodes()
{
	return inputNodes;
}
public int getoutputNodes()
{
	return outputNodes;
}
public String getinputField1()
{
	return inputField1;
}
public String getinputField2()
{
	return inputField2;
}
public String getinputField3()
{
	return inputField3;
}
public String getinputField4()
{
	return inputField4;
}
public String getinputField5()
{
	return inputField5;
}
public String getOutputField1()
{
	return outputField1;
}
public String getOutputField2()
{
	return outputField2;
}
public String getOutputField3()
{
	return outputField3;
}
public String getOutputField4()
{
	return outputField4;
}
public String getOutputField5()
{
	return outputField5;
}
//Setters
public void setSeed(int num)
{
	seed = num;
}
public void setnIterations(int num)
{
	nIterations = num;
}
public void setLearningRate(double d)
{
	learningRate = d;
}
public void setOptimizationAlgorithm(String newAlgorithm)
{
	OptiAlgorithm = newAlgorithm;
}
public void setinputNodes(int num)
{
	inputNodes = num;
}
public void setoutputNodes(int num)
{
	outputNodes = num;
}
public void setinputField1(String field)
{
	inputField1 = field;
}
public void setinputField2(String field)
{
	inputField2 = field;
}
public void setinputField3(String field)
{
	inputField3 = field;
}
public void setinputField4(String field)
{
	inputField4 = field;
}
public void setinputField5(String field)
{
	inputField5 = field;
}
public void setOutputField1(String field)
{
	outputField1 = field;
}
public void setOutputField2(String field)
{
	outputField2 = field;
}
public void setOutputField3(String field)
{
	outputField3 = field;
}
public void setOutputField4(String field)
{
	outputField4 = field;
}
public void setOutputField5(String field)
{
	outputField5 = field;
}




	/*
	  public Evaluation eval(MultiLayerNetwork network) {
	        Evaluation ev = new Evaluation(nIn);
	        
	        INDArray predict = network.  ;
	        
	        ev.eval(data, predict);
	        
	        
	        
	        return ev;
	    }
	*/
	
	private static void PrintMainMenu()
	{
		MBout.output("MAIN MENU");
		MBout.output("1) Training Menu");
		MBout.output("2) Loading Menu");
		MBout.output("3)  Examples from Neural Net");
		MBout.output("4) Test Menu");
		MBout.output("5) EXIT");
		MBout.output("Select a menu option:");
		
	}
	
	
	/** Given a probability distribution over discrete classes, sample from the distribution
	 * and return the d class index.
	 * @param distribution Probability distribution over classes. Must sum to 1.0
	 */
	private static int sampleFromDistribution( double[] distribution, Random rng ){
		double d = rng.nextDouble();
		
		double sum = 0.0;
		
		for( int i=0; i<distribution.length; i++ ){
			sum += distribution[i];
			if( d <= sum ) return i;
		}
		//Should never happen if distribution is a valid probability distribution
		throw new IllegalArgumentException("Distribution is invalid? d="+d+", sum="+sum);
	}
}