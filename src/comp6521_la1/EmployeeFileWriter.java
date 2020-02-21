package comp6521_la1;

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class EmployeeFileWriter implements EmployeeFile
{
	
	private FileOutputStream mOutputStream=null;
	private BufferedOutputStream mBuffOutputStream=null;
	private long mBytesWritten;
	private int mRecordWritten;
	private int mDiskWrite;
	
	public EmployeeFileWriter(File aFile) throws FileNotFoundException
	{
		mOutputStream=new FileOutputStream(aFile, false);
		mBuffOutputStream=new BufferedOutputStream(mOutputStream, BLOCK_SIZE);//RA: By using BufferedOutputStream we simulate writing data in blocks of size BLOCK_SIZE
		//RA: Alternative approach might include utilizing java.nio.file.Files and java.nio.channels routines to read from disk
		mBytesWritten=0;
		mRecordWritten=0;
		mDiskWrite=0;
	}
	
	public void WriteRecord(Employee aEmployee) throws IOException 
	{
		ByteArrayOutputStream lOutByteStream=null;
		PrintStream lOutPrintStream=null;
		byte[] lOutput=null;
		
		try
		{
			lOutByteStream=new ByteArrayOutputStream(RECORD_SIZE);
			lOutPrintStream=new PrintStream(lOutByteStream);						
			lOutPrintStream.print(aEmployee.GetData());	
			lOutPrintStream.flush();
			lOutput=lOutByteStream.toByteArray();
			mRecordWritten++;
		}
		finally
		{
			if(lOutPrintStream!=null)
			{
				lOutPrintStream.close();
			}
			if(lOutByteStream!=null)
			{
				lOutByteStream.close();
			}
		}
		if((lOutput!=null)&&(lOutput.length>0))
		{
			mBuffOutputStream.write(lOutput);
			mBytesWritten+=lOutput.length;
		}
	}
	
	@Override
	public void Close()
	{
		if(mBuffOutputStream!=null)
		{
			try
			{
				mBuffOutputStream.close();
			}
			catch(IOException aEx)
			{
				System.err.println("Error closing buffered output stream: "+aEx.getMessage());
			}
		}
		if(mOutputStream!=null)
		{
			try
			{
				mOutputStream.close();
			}
			catch(IOException aEx)
			{
				System.err.println("Error closing output stream: "+aEx.getMessage());
			}
		}
	}
	
	public long GetBytesWritten()
	{
		return mBytesWritten;
	}
	public int GetRecordsWritten()
	{
		return mRecordWritten;
	}
	public int GetBlocksWritten()
	{
		return mRecordWritten * RECORD_SIZE / USED_BLOCK_SIZE;
	}
	public int GetDiskWrite()
	{
		return GetBlocksWritten();
	}	
}
