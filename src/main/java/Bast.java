import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;


public class Bast {

	private JFrame frame;
	private JPanel panelMessageBoard;
	private JButton btnLoad;

	static JTextArea bastTextArea;
	private JTextField textFieldLearningRate;
	private JTextField textFieldNN_LoadCFG;
	private JTextField textFieldNN_LoadCoefficients;
	private JTextField textFieldnPacketsInMiniBatch;
	private JTextField textFieldNmbrOfIterations;
	private JTextField textFieldSampleLength;
	private JTextField textFieldCapFile;
	private JTextField textFieldTimeSeriesLength;
	private JTextField textFieldNmbrOfSamples;
	private JTextField textFieldLayerSize;	
	private JTextField textFieldNmbrOfEpochs;
	private JTextField textFieldExmplInit;
	private JTextField textFieldChallenge;
	
	JLabel lblBast;
	private JLabel lblCoefficentsPath;
	private JLabel lblConfigurationFilePath;
	private JLabel lblStatusBoard;
	private JLabel lblEnterTheTraining;
	static JLabel lblStatus;
	private JLabel lblblankFieldsAre;

	private TrafficTrainer tt;//traffic trainer loaded

	String dataSet;
	int timeSeriesLength;
	static String exampleInitial;
	static int nmbrSamples;
	private JTextPane txtpnNotification;
	private JPanel imagePanel;
	private JLabel lblNewLabel;

	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() { 
				try {
					Bast window = new Bast();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Bast() 
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
	
		
		//This is the main window the application appears in
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 102, 153));
		frame.setBounds(100, 100, 1000, 750);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
				

		//Label of our program name at top-right corner
		JLabel lblBast = new JLabel("Bast");
		lblBast.setFont(new Font("HanziPen TC", Font.BOLD | Font.ITALIC, 35));
		lblBast.setToolTipText("This is the name of the program you are using.");
		lblBast.setBounds(16, 0, 100, 44);
		frame.getContentPane().add(lblBast);
		
		frame.getContentPane().add(new JLabel(new ImageIcon("Bast.png")));
		
		
		
		//--------------------------Message Board--------------------------//
		//--------------How Bast communicates with the User---------------//
		//Label for the status board
		lblStatusBoard = new JLabel("Status Board");
		lblStatusBoard.setForeground(Color.CYAN);
		lblStatusBoard.setFont(new Font("HanziPen TC", Font.BOLD, 20));
		lblStatusBoard.setBounds(16, 408, 250, 24);
		frame.getContentPane().add(lblStatusBoard);

		//Create the Panel
		panelMessageBoard = new JPanel();
		panelMessageBoard.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMessageBoard.setBackground(Color.DARK_GRAY);
		panelMessageBoard.setBounds(16, 436, 960, 286);
		frame.getContentPane().add(panelMessageBoard);
		panelMessageBoard.setLayout(null);
		
		//Create the scroll bar
		JScrollPane scroll_bar = new JScrollPane();
		scroll_bar.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scroll_bar.setBounds(6, 25, 948, 251);
		scroll_bar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		
		//Text area, redirects sys.out and sys.err to pane
		//create and setup Text Area
	    final ImageIcon imageIcon = new ImageIcon("emblem-danger.png");
		bastTextArea = new JTextArea(){
		      Image image = imageIcon.getImage();

		      Image grayImage = GrayFilter.createDisabledImage(image);
		      {
		        setOpaque(false);
		      }
		      public void paint(Graphics g) {
		        g.drawImage(grayImage, 0, 0, this);
		        super.paint(g);
		      }
		    };
		bastTextArea.setEditable(false);
		bastTextArea.setLineWrap(true);
		bastTextArea.setWrapStyleWord(true);
		bastTextArea.setTabSize(12);
		scroll_bar.setViewportView(bastTextArea);
		bastTextArea.setBackground(Color.WHITE);
		
		//Label, Shows users selection and provides short update messages
		lblStatus = new JLabel("Welcome to Bast!");
		lblStatus.setBounds(6, 6, 891, 21);
		lblStatus.setForeground(Color.CYAN);
		lblStatus.setBackground(Color.WHITE);
		lblStatus.setVerticalAlignment(SwingConstants.TOP);
		lblStatus.setToolTipText("This is Bast's way to communicate with you.");
		panelMessageBoard.add(lblStatus);
		panelMessageBoard.add(scroll_bar);

		/////////MESSAGE BOARD OUTPUT//////////////
		//creates redirect object to pipe stdout and stderr to textArea
				
		
		
		//-----------------------------Step Tabs-------------------------------//
		//---------------------Load/create Neural Network----------------------//
		
		//NEURAL NETWORK TAB #1
		JTabbedPane tabbedPaneSteps = new JTabbedPane();
		
		tabbedPaneSteps.setTabPlacement(JTabbedPane.BOTTOM);
		tabbedPaneSteps.setBounds(16, 41, 660, 242);
		frame.getContentPane().add(tabbedPaneSteps);
		
		//Panel that holds all the content for Step 1 Tab
		JPanel panel = new JPanel();
		tabbedPaneSteps.addTab("Load", null, panel, "Load DeepLearning4Java Neural Network files.");
		panel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.setBackground(Color.DARK_GRAY);
		tabbedPaneSteps.setMnemonicAt(0, KeyEvent.VK_1);
		panel.setLayout(null);
		
		//Load Button
		btnLoad = new JButton("Load Bast");
		btnLoad.setToolTipText("Enter the filepaths or click to use default filepaths.");
		btnLoad.setBounds(336, 69, 250, 75);
		panel.add(btnLoad);
		btnLoad.setBackground(Color.GRAY);
		btnLoad.setForeground(Color.CYAN);
		btnLoad.setFont(new Font("HanziPen TC", Font.BOLD, 25));
		btnLoad.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				
				MBout.status("Loading...");
				Executors.newSingleThreadExecutor().execute(new Runnable() 
				{
				    @Override 
				    public void run() 
				    {
				        // code in here
				    	String cfgFilePath = textFieldNN_LoadCFG.getText();
						String coeffFilePath = textFieldNN_LoadCoefficients.getText();
						
						if (cfgFilePath.length() == 0)
						{
							cfgFilePath = "conf.json";
							textFieldNN_LoadCFG.setText(cfgFilePath);
						}
						if (coeffFilePath.length() == 0)
						{
							coeffFilePath = "coefficients.bin";
							textFieldNN_LoadCoefficients.setText(coeffFilePath);
						}
						MBout.output("Creating Neural Network from " + cfgFilePath + " and " + coeffFilePath + " files...");
						tt = new TrafficTrainer();
						tt.LoadNeuralNet(cfgFilePath, coeffFilePath);

						//load files for neural net
						//String cfgFilePath = textFieldNN_LoadCFG.getText();
						//String coeffFilePath = textFieldNN_LoadCoefficients.getText();
						////Trying this out...
						MBout.output("Getting data from Bast...");
						
						//Column A
						textFieldLayerSize.setText(Integer.toString(tt.getLayerSize())); 
						textFieldnPacketsInMiniBatch.setText(Integer.toString(tt.getNumberOfMiniBatches()));
						textFieldNmbrOfIterations.setText(Integer.toString(tt.getnIterations()));				
						textFieldNmbrOfEpochs.setText(Integer.toString(tt.getNumEpochs()));
						
						//Column B
						textFieldLearningRate.setText(Double.toString(tt.getLearningRate()));
						textFieldNmbrOfSamples.setText(Integer.toString(tt.getSamplesTo()));					
						textFieldSampleLength.setText(Integer.toString(tt.getnCharactersToSample()));
						textFieldTimeSeriesLength.setText(Integer.toString(tt.lengthOfTimeSeries));
						textFieldChallenge.setText("NewPacketCapture.csv");
						textFieldCapFile.setText("ChallengePacCapGood1.csv");
						textFieldExmplInit.setText("10.0.1.29,");
						MBout.output("All training fields are populated with Bast's current configurations");
						MBout.status("Bast is fully loaded and ready to train.");
					}
				}); 
			};
		});
		
		/*Text fields for neural network
		 * Must take two file paths one for the coefficients file and one for the config file
		 */
		
		textFieldNN_LoadCFG  = new JTextField();
		textFieldNN_LoadCFG.setBounds(34, 60, 173, 33);
		panel.add(textFieldNN_LoadCFG);
		textFieldNN_LoadCFG.setColumns(35);
		
		textFieldNN_LoadCoefficients  = new JTextField();
		textFieldNN_LoadCoefficients.setBounds(34, 128, 173, 33);
		panel.add(textFieldNN_LoadCoefficients);
		textFieldNN_LoadCoefficients.setColumns(35);
		
		
		//Labels for the LOAD tab
		JLabel lblChooseOne = new JLabel("Enter the two filenames :");
		lblChooseOne.setFont(new Font("HanziPen TC", Font.BOLD, 18));
		lblChooseOne.setForeground(Color.CYAN);
		lblChooseOne.setBounds(6, 6, 350, 27);
		panel.add(lblChooseOne);
		
		lblConfigurationFilePath = new JLabel("Configuration File Path:");
		lblConfigurationFilePath.setToolTipText(".json filetype");
		lblConfigurationFilePath.setForeground(Color.WHITE);
		lblConfigurationFilePath.setBounds(34, 45, 155, 16);
		panel.add(lblConfigurationFilePath);
		
		lblCoefficentsPath = new JLabel("CoEfficents Path:");
		lblCoefficentsPath.setToolTipText(".bin filetype");
		lblCoefficentsPath.setForeground(Color.WHITE);
		lblCoefficentsPath.setBounds(34, 112, 135, 16);
		panel.add(lblCoefficentsPath);
	

		
		/////TRAINING DATA FOR NEURAL NET//////////////
		////Setup labels and text entry for all input needed to train Neural Net
		////Arranged into columns A and B

		//Panel that holds all the content for the TRAIN tab
		JPanel panel3 = new JPanel();
		tabbedPaneSteps.addTab("Train", null, panel3, "Input values to train the Neural Net");
		panel3.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel3.setBackground(Color.DARK_GRAY);
		panel3.setLayout(null);
		
		lblEnterTheTraining = new JLabel("Enter the training data:");
		lblEnterTheTraining.setBounds(16, 6, 206, 30);
		lblEnterTheTraining.setForeground(Color.CYAN);
		lblEnterTheTraining.setFont(new Font("HanziPen TC", Font.BOLD, 18));
		panel3.add(lblEnterTheTraining);
		
		
		////setup configuration from text fields
		///Commented out sections not yet in use
		//some text boxes were using the same names and throwing errors 
		//trying to pull from empty boxes in other tabs.
		//Button launches Bast in Test mode, this is the first phase
		//Button launches Bast in Test mode, this is the first phase
		JButton btnTrain = new JButton("Train");
		btnTrain.setFont(new Font("HanziPen TC", Font.PLAIN, 15));
		btnTrain.setBounds(400, 128, 90, 54);
		panel3.add(btnTrain);
		btnTrain.setToolTipText("Click to begin training. Ensure all fields are correct.");
		btnTrain.setForeground(Color.CYAN);
		btnTrain.setBackground(Color.GRAY);
		btnTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
				{
					Executors.newSingleThreadExecutor().execute(new Runnable() 
					{
					    @Override 
					    public void run() 
					    {
							MBout.status("Generating the examples...");
							
							try
							{
							exampleInitial = textFieldExmplInit.getText();
							nmbrSamples = Integer.parseInt(textFieldNmbrOfSamples.getText());
							}
							catch(Exception e)
							{
								MBout.output("Using default values for Example Initial and number of samples.");
								exampleInitial = "192.";
								nmbrSamples = 5;
							}
							if (exampleInitial.length() == 0)
							{
								exampleInitial = null;
							}
							//Load Samples Generated							
							/*
							 * Initializes Bast and sets the examples
							 * message passed populates on the message board
							 * insert a description of what is happening.
							 */ 
							try 
							{		
								tt.Examples(nmbrSamples, exampleInitial);
								MBout.output("Examples are set.");
							}
						    catch (Exception e1) 
							{
						    	 // TODO Auto-generated catch block
						    	 e1.printStackTrace();
							}
							
							
							MBout.output("Examples generated.");			
							
							
							////////////Gather Input/////////
							////////////////Column A + B ///////////// not including # of samples or sample initialization (gathered above)
							int newLayerSize = Integer.parseInt(textFieldLayerSize.getText());			
							int newBatchSize = Integer.parseInt(textFieldnPacketsInMiniBatch.getText());				
							int nmbrIter = Integer.parseInt(textFieldNmbrOfIterations.getText());
							int newNmbrEpochs = Integer.parseInt(textFieldNmbrOfEpochs.getText());				
							double lrngRate = Double.parseDouble(textFieldLearningRate.getText());			
							int sampleLength = Integer.parseInt(textFieldSampleLength.getText());
							////////////////Column C////////////////
							timeSeriesLength = Integer.parseInt(textFieldTimeSeriesLength.getText());
			
							//Load Data Set
							dataSet = textFieldCapFile.getText();
							if (dataSet.length() == 0)
							{
								dataSet = "NewPacketCapture.csv";
								textFieldCapFile.setText("NewPacketCapture.csv");
							}
							MBout.output("Useing the following Data Set... " + dataSet);
								    
							tt.Train(dataSet, timeSeriesLength);
								
							///////////////Pass Input Gathered//////////
							lblStatus.setText("Training...");
							
							tt = new TrafficTrainer(newLayerSize,
									newBatchSize,
									50, //set as static until little more rearranging can be done
									newNmbrEpochs,
									nmbrSamples,
									sampleLength,
									nmbrIter,
									lrngRate);
							
			
							MBout.output("Running training thread...");
				
							lblStatus.setText("Training Complete.");
						}
					}); 
				};
			});
		
		//Sets the network as if it is done training.
		JButton btnStopTrain = new JButton("Stop");
		btnStopTrain.setFont(new Font("HanziPen TC", Font.PLAIN, 15));
		btnStopTrain.setBounds(520, 128, 90, 54);
		panel3.add(btnStopTrain);
		btnStopTrain.setToolTipText("Click to begin training. Ensure all fields are correct.");
		btnStopTrain.setForeground(Color.CYAN);
		btnStopTrain.setBackground(Color.GRAY);
		btnStopTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
				{
					tt.setDoneTraining();	
				};
			});
		/////////////////////////COLUMN A/////////
		////////////////////////////Layer Size/////////////////////////////////////
		JLabel lblLayerSize = new JLabel("Layer Size:");
		lblLayerSize.setToolTipText("Size of hidden layer in packet.");
		lblLayerSize.setForeground(Color.WHITE);
		lblLayerSize.setBounds(16, 43, 87, 16);
		panel3.add(lblLayerSize);
		
		textFieldLayerSize = new JTextField();
		textFieldLayerSize.setBounds(110, 38, 64, 26);
		textFieldLayerSize.setColumns(10);
		panel3.add(textFieldLayerSize);
		
		////////////////////////////Nmbr Of Packets in Mini Batch//////////////////////////
		JLabel lblPacketsInMiniBatch= new JLabel("Batch Size:");
		lblPacketsInMiniBatch.setToolTipText("Number of packets used in each mini training session");
		lblPacketsInMiniBatch.setForeground(Color.WHITE);
		lblPacketsInMiniBatch.setBounds(16, 80, 87, 16);
		panel3.add(lblPacketsInMiniBatch);
		
		textFieldnPacketsInMiniBatch = new JTextField();
		textFieldnPacketsInMiniBatch.setColumns(10);
		textFieldnPacketsInMiniBatch.setBounds(110, 76, 64, 26);
		panel3.add(textFieldnPacketsInMiniBatch);
		
		
		/////////////////////////////////////Number Of Iterations/////////////////////////////
		JLabel lblNmbrOfIterations = new JLabel("# Iterations:");
		lblNmbrOfIterations.setToolTipText("Number of times Bast repeats mini training sesstions");
		lblNmbrOfIterations.setForeground(Color.WHITE);
		lblNmbrOfIterations.setBounds(16, 118, 100, 16);
		panel3.add(lblNmbrOfIterations);
		
		textFieldNmbrOfIterations = new JTextField();
		textFieldNmbrOfIterations.setBounds(110, 114, 64, 26);
		textFieldNmbrOfIterations.setColumns(10);
		panel3.add(textFieldNmbrOfIterations);
		///////////////////////Number Of Epochs////////////////////////////
		JLabel lblNmbrOfEpochs = new JLabel("# Epochs:");
		lblNmbrOfEpochs.setToolTipText("Number of packets in a group used to train");
		lblNmbrOfEpochs.setForeground(Color.WHITE);
		lblNmbrOfEpochs.setBounds(16, 158, 87, 16);
		panel3.add(lblNmbrOfEpochs);
		
		textFieldNmbrOfEpochs = new JTextField();
		textFieldNmbrOfEpochs.setBounds(110, 154, 64, 26);
		panel3.add(textFieldNmbrOfEpochs);
		textFieldNmbrOfEpochs.setColumns(10);

		
		
		////////////COLUMN B/////////////
//////////////////////////Learning Rate///////////////////////////////////////////
	
		JLabel lblTrainingRate = new JLabel("Learning Rate:");
		lblTrainingRate.setToolTipText("initial rate of learning");
		lblTrainingRate.setForeground(Color.WHITE);
		lblTrainingRate.setBounds(195, 43, 107, 16);
		panel3.add(lblTrainingRate);
		
		textFieldLearningRate = new JTextField();
		textFieldLearningRate.setBounds(298, 38, 64, 26);
		textFieldLearningRate.setColumns(10);
		panel3.add(textFieldLearningRate);
		
		// 12 42 75 108
		///////////////////////Numbers Of Samples//////////////////////
		JLabel lblNmbrOfSamples = new JLabel("# Samples:");
		lblNmbrOfSamples.setToolTipText("How many samples Bast provides on the status board.");
		lblNmbrOfSamples.setForeground(Color.WHITE);
		lblNmbrOfSamples.setBounds(192, 80, 107, 16);
		panel3.add(lblNmbrOfSamples);
		
		textFieldNmbrOfSamples = new JTextField();
		textFieldNmbrOfSamples.setBounds(298, 76, 64, 26);
		panel3.add(textFieldNmbrOfSamples);
		textFieldNmbrOfSamples.setColumns(10);
		
		
		///////////////////////////Sample SampleLength//////////////////
		JLabel lblSampleLength = new JLabel("Sample Size:");
		lblSampleLength.setToolTipText("Size of the sample.");
		lblSampleLength.setForeground(Color.WHITE);
		lblSampleLength.setBounds(195, 118, 107, 16);
		panel3.add(lblSampleLength);
		
		textFieldSampleLength = new JTextField();
		textFieldSampleLength.setBounds(298, 114, 64, 26);
		panel3.add(textFieldSampleLength);
		textFieldSampleLength.setColumns(10);
		
		/////////////////////////Sample Initialization
		JLabel lblgenerationInitialization = new JLabel("Sample Init:");
		lblgenerationInitialization.setToolTipText("What samples generated during training are initialized with.");
		lblgenerationInitialization.setForeground(Color.WHITE);
		lblgenerationInitialization.setBounds(199, 158, 100, 16);
		panel3.add(lblgenerationInitialization);		
		textFieldExmplInit = new JTextField();
		textFieldExmplInit.setBounds(298, 154, 64, 26);
		panel3.add(textFieldExmplInit);
		textFieldExmplInit.setColumns(10);
		
		
		
		////////////COLUMN C/////////////
		
		//////////Time Series - how many 
		JLabel lbltimeSeriesLength = new JLabel("Time Series Length:");
		lbltimeSeriesLength.setToolTipText("How Bast tells time, useing the amount of next() calls to decide time length.");
		lbltimeSeriesLength.setForeground(Color.WHITE);
		lbltimeSeriesLength.setBounds(380, 36, 150, 30);
		panel3.add(lbltimeSeriesLength);
		textFieldTimeSeriesLength = new JTextField();
		textFieldTimeSeriesLength.setBounds(534, 39, 64, 26);
		textFieldTimeSeriesLength.setColumns(10);
		panel3.add(textFieldTimeSeriesLength);

		
		JLabel lblCapFile = new JLabel("Data Set:");
		lblCapFile.setToolTipText("The datafile the training session is conducted on.");
		lblCapFile.setForeground(Color.WHITE);
		lblCapFile.setBounds(380, 80, 71, 16);
		panel3.add(lblCapFile);
		textFieldChallenge = new JTextField();
		textFieldChallenge.setBounds(456, 76, 142, 26);
		panel3.add(textFieldChallenge);
		textFieldChallenge.setColumns(10);
		
		lblblankFieldsAre = new JLabel("***Blank fields are optional***");
		lblblankFieldsAre.setHorizontalAlignment(SwingConstants.CENTER);
		lblblankFieldsAre.setFont(new Font("HanziPen TC", Font.BOLD, 15));
		lblblankFieldsAre.setForeground(Color.CYAN);
		lblblankFieldsAre.setBounds(300, 16, 244, 15);
		panel3.add(lblblankFieldsAre);
		
	

		//VALIDATE TAB 
		
		//Panel that holds all the content
		JPanel panel2 = new JPanel();
		tabbedPaneSteps.addTab("Validate", null, panel2, "Challenge Neural Network");
		panel2.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel2.setBackground(Color.DARK_GRAY);
		panel2.setLayout(null);
		tabbedPaneSteps.setMnemonicAt(1, KeyEvent.VK_2);
		
		//labels for VALIDATE tab
		JLabel lblValidate = new JLabel("Challenge Bast");
		lblValidate.setBounds(6, 6, 299, 29);
		panel2.add(lblValidate);
		lblValidate.setFont(new Font("HanziPen TC", Font.BOLD, 18));
		lblValidate.setForeground(Color.CYAN);
		
		
		JLabel lblChlngFile = new JLabel("Data Set:");
		lblChlngFile.setToolTipText("The datafile the training session is conducted on.");
		lblChlngFile.setForeground(Color.WHITE);
		lblChlngFile.setBounds(128, 81, 71, 16);
		panel2.add(lblChlngFile);
		textFieldCapFile = new JTextField();
		textFieldCapFile.setBounds(206, 77, 188, 26);
		panel2.add(textFieldCapFile);
		textFieldCapFile.setColumns(10);
		
	
		JButton btnChallenge = new JButton("Challenge Accepted");
		btnChallenge.setFont(new Font("HanziPen TC", Font.PLAIN, 15));
		btnChallenge.setBounds(213, 114, 220, 54);
		panel2.add(btnChallenge);
		btnChallenge.setToolTipText("Click to begin training. Ensure all fields are correct.");
		btnChallenge.setForeground(Color.CYAN);
		btnChallenge.setBackground(Color.GRAY);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel.setIcon(new ImageIcon("/Users/Babs/Desktop/Bast2/Bast.png"));
		lblNewLabel.setBounds(658, 51, 336, 353);
		frame.getContentPane().add(lblNewLabel);
		btnChallenge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
				{
				MBout.output("Running challenge file against Neural Net...");

					Executors.newSingleThreadExecutor().execute(new Runnable() 
					{
					    @Override 
					    public void run() 
					    {
							//textFieldChallenge.getText()
							tt.ChallengeNeuralNet(textFieldCapFile.getText());
							lblStatus.setText("Challenge complete.");
						}

					}); 
				};
			});
	if(tt != null)	
		if(tt.danger)
		{
			///Notification area when a packet is thrown back over a certain percentage. 
			txtpnNotification = new JTextPane();
			txtpnNotification.setToolTipText("DANGER WILL ROBINSON!!! DANGER!!");
			txtpnNotification.setForeground(new Color(0, 255, 255));
			txtpnNotification.setFont(new Font("HanziPen TC", Font.PLAIN, 16));
			txtpnNotification.setEditable(false);
			txtpnNotification.setBackground(new Color(0, 102, 153));
			txtpnNotification.setText("DANGER WILL ROBINSON!!! DANGER!!");
			txtpnNotification.setBounds(16, 306, 628, 90);
			frame.getContentPane().add(txtpnNotification);
			
		}
		
		

	}
}
