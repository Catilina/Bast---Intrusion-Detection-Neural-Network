import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class CSVParser 
{

	
	
	
	
	
	public List<String> toAddress;
	public List<String> toPort;
	
	public List<String> fromAddress;
	public List<String> fromPort;
	
	public List<String> packetLength;
	public List<String> protocol;
	
	static int numberOfPackets = 0;
	
	
	public CSVParser(String fileNameIn)
	{
		
		toAddress  = new ArrayList<String>();;
		toPort = new ArrayList<String>();
		
		fromAddress  = new ArrayList<String>();;
		fromPort  = new ArrayList<String>();
		
		
		protocol  = new ArrayList<String>();;
		packetLength = new ArrayList<String>();
		
		this.readInFile(fileNameIn);
		
	}
	
public void readInFile(String PathIn)
{
	BufferedReader br = null;
	String line = "";
	String csvSplitBy = ",";
	
	try
	{
		br = new BufferedReader(new FileReader(PathIn));
		
		while( (line = br.readLine()) != null) 
			{
			
			String[] parsed_line = line.split(csvSplitBy);
		
			
			
			
			toAddress.add(parsed_line[0]);
			toPort.add(parsed_line[1]);
			
			fromAddress.add(parsed_line[2]);
			fromPort.add(parsed_line[3]);
			
			
			protocol.add(parsed_line[4]);
			packetLength.add(parsed_line[5]);
			numberOfPackets++;
			}
		
	}
	catch(FileNotFoundException e)
	{
		System.out.println(e.getLocalizedMessage());
	} catch (IOException e) {
	
		e.printStackTrace();
	}
	

	
}	
	
	
	public List<String> getToAddressColumn()
	{
		return toAddress;
	}
	
	public List<String> getToPortColumn()
	{
		return toPort;
	}
	
	public  int getNumberOfPackets()
	{
	return this.getNumberOfPackets();	
	}
	
	public List<String> getFromAddressColumn()
	{
		return fromAddress;
	}
	
	public List<String> getFromPortColumn()
	{
		return fromPort;
	}
	
	
	
	public List<String> getProtocolColumn()
	{
		return protocol;
	}
	
	public List<String> getPacketLengthColumn()
	{
		return packetLength;
	}
}

