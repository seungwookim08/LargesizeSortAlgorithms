package comp6521_la1;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.nio.charset.Charset;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public class EmployeeFileReader implements EmployeeFile
{
	private FileInputStream mInputStream=null;
	private BufferedInputStream mBuffInputStream=null;
	private Charset mDataCharset=null;
	private long mBytesRead;
	private int recordsRead;
	
	public EmployeeFileReader(String aPath) throws FileNotFoundException
	{
		recordsRead = 0;
		mInputStream=new FileInputStream(aPath);
		mBuffInputStream=new BufferedInputStream(mInputStream, BLOCK_SIZE); //RA: By using BufferedInputStream we simulate reading data in blocks of size BLOCK_SIZE
		//RA: Alternative approach might include utilizing java.nio.file.Files and java.nio.channels routines to read from disk
		mDataCharset=Charset.forName("US-ASCII");
		mBytesRead=0;
	}

	public Employee ReadRecord() throws IOException, ParseException 
	{
		byte[] lBuffer=new byte[RECORD_SIZE];
		
		int lResult=mBuffInputStream.read(lBuffer, 0, RECORD_SIZE);
		if(lResult==-1)
		{
			return null;
		}
		else if(lResult!=RECORD_SIZE)
		{
			throw new IOException();
		}
		else if(lBuffer[RECORD_SIZE-1]!='\n')
		{
			throw new ParseException("Invalid end-or-record roken", RECORD_SIZE-1); 
		}
		mBytesRead+=lResult;

		String recordData= new String(lBuffer, mDataCharset);
		
		int lOffset=0;
		int lId=Integer.parseInt(recordData.substring(lOffset, EMPID_SIZE));
		lOffset+=EMPID_SIZE;
		int lDate = Integer.parseInt(recordData.substring(lOffset, lOffset + DATE_SIZE).replace("-",""));

		recordsRead++;
		return new Employee(lId, lDate, recordData);
	}
	
	@Override
	public void Close()
	{
		if(mBuffInputStream!=null)
		{
			try
			{
				mBuffInputStream.close();
			}
			catch(IOException aEx)
			{
				System.err.println("Error closing buffered input stream: "+aEx.getMessage());
			}
		}
		if(mInputStream!=null)
		{
			try
			{
				mInputStream.close();
			}
			catch(IOException aEx)
			{
				System.err.println("Error closing input stream: "+aEx.getMessage());
			}
		}
	}
	
	public long GetBytesRead()
	{
		return mBytesRead;
	}
	public int GetRecordssRead()
	{
		return recordsRead;
	}
	public int GetBlocksRead()
	{
		return recordsRead * RECORD_SIZE / USED_BLOCK_SIZE;
	}
	public int GetDiskRead()
	{
		return GetBlocksRead();
	}
}
