package bitmap_package2;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;

public class ExtractFieldFileReader implements ExtractFieldFile
{
	private FileInputStream mInputStream=null;
	private BufferedInputStream mBuffInputStream=null;
	private Charset mDataCharset=null;
	private long mBytesRead;
	private int mRecordsRead;
	private long mRecordsInFile;

	
	public ExtractFieldFileReader(String aPath) throws FileNotFoundException
	{
		mInputStream=new FileInputStream(aPath);
		mBuffInputStream=new BufferedInputStream(mInputStream, BLOCK_SIZE); 
		mDataCharset=Charset.forName("US-ASCII");
		
		File lFile=new File(aPath);
		mRecordsInFile=lFile.length()/INDEX_SIZE;
	}

	public ExtractField ReadRecord() throws IOException, ParseException 
	{		
		
		byte[] lBuffer=new byte[INDEX_SIZE];
		
		int lResult=mBuffInputStream.read(lBuffer, 0, INDEX_SIZE);
		if(lResult==-1)
		{
			return null;
		}
		else if(lResult!=INDEX_SIZE)
		{
			throw new IOException();
		}
		else if(lBuffer[INDEX_SIZE-1]!='\n')
		{
			throw new ParseException("Invalid end-or-record roken", INDEX_SIZE-1); 
		}
		
		int lOffset=0;
		int lIdProcessed=Integer.parseInt(new String(lBuffer, lOffset, INDEXPROCESS_SIZE, mDataCharset));
		lOffset+=INDEXPROCESS_SIZE;
		int lId=Integer.parseInt(new String(lBuffer, lOffset, EMPID_SIZE, mDataCharset));
		lOffset+=EMPID_SIZE;
		mRecordsRead++;
		return new ExtractField(lIdProcessed, lId);
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
	
	public long GetRecordsInFile()
	{
		return mRecordsInFile;
	}
	
	public long GetBytesRead()
	{
		return mBytesRead;
	}
	public int GetRecordsRead()
	{
		return mRecordsRead;
	}
	public int GetBlocksRead()
	{
		return mRecordsRead * INDEX_SIZE / USED_BLOCK_SIZE;
	}
	public int GetDiskRead()
	{
		return GetBlocksRead();
	}
	
}
