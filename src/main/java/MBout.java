
public class MBout {
	
	public static void output(String textin)
	{
		//Adds text to the textArea for the Message Board
		Bast.bastTextArea.append(textin + "\n");
	}
	public static void status(String textin)
	{
		//Adds text to the textArea for the Message Board
		Bast.lblStatus.setText(textin);
	}
	
	public static void packet(String input)
	{
		Bast.bastTextArea.append(input);
	}
	/*
	 * Prints out to the message board the current percentage accuarcy (vector?)
	 * 
	 */
	public void stats(int percentage)
	{
		output("Bast is " + percentage + "% effective.\n");
		output("Real Packet: \n");
		//spit out a packet - redirect feed of one packet character length
		output("Perdiction Packet: \n");
		//spit out a sample - redirect samples to generate
	}

}
