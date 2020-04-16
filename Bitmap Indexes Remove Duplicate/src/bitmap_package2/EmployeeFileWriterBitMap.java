package bitmap_package2;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class EmployeeFileWriterBitMap implements EmployeeFile
{
	
	private BufferedWriter mBuffWriter=null;
	//private int mDiskWrite;
	
	public EmployeeFileWriterBitMap(File aFile) throws FileNotFoundException
	{
		try {
			mBuffWriter=new BufferedWriter(new FileWriter(aFile), 327680);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void WriteRecord(HashMap<Integer,IndexBitSet> aRecord) throws IOException 
	{
		for(Entry<Integer, IndexBitSet> c : aRecord.entrySet())
		{
			mBuffWriter.write(c.getKey() + " : " + c.getValue());
			mBuffWriter.newLine();
		}
		
	}
	@Override
	public void Close()
	{
		if(mBuffWriter!=null)
		{
			try
			{
				mBuffWriter.close();
			}
			catch(IOException aEx)
			{
				System.err.println("Error closing buffered output stream: "+aEx.getMessage());
			}
		}
	}
}
