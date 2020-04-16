package bitmap_package2;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class EmployeeIndexFileWriter implements EmployeeIndexFile {
	
	private FileOutputStream mOutputStream=null;
	private BufferedOutputStream mBuffOutputStream=null;
	private Variant mVariant;
	private int mRecordWritten;
	
	public EmployeeIndexFileWriter(File aFile, Variant aVariant) throws FileNotFoundException
	{
		mOutputStream=new FileOutputStream(aFile, false);
		mBuffOutputStream=new BufferedOutputStream(mOutputStream, BLOCK_SIZE);
		mVariant = aVariant;
	}

	public void WriteRecord(EmployeeIndex aEmployeeINdex) throws IOException 
	{
		int index_size = INDEX_SIZE;
		int bitmapindex_size = BITMAPINDEX_SIZE;
		if(mVariant.IsDenseIndex())
		{
			bitmapindex_size = mVariant.GetBitMapSize();
			index_size= INDEXPROCESS_SIZE + EMPID_SIZE + bitmapindex_size + EOR_SIZE;
		}
		
		ByteArrayOutputStream lOutByteStream=null;
		PrintStream lOutPrintStream=null;
		byte[] lOutput=null;
		
		try
		{
			lOutByteStream=new ByteArrayOutputStream(index_size);
			lOutPrintStream=new PrintStream(lOutByteStream);			
			
			lOutPrintStream.format("%0"+INDEXPROCESS_SIZE+"d", aEmployeeINdex.GetIdProcessed());
			lOutPrintStream.format("%0"+EMPID_SIZE+"d", aEmployeeINdex.GetId());
			lOutPrintStream.format("%-"+bitmapindex_size+"."+bitmapindex_size+"s", aEmployeeINdex.GetBitSet());
			lOutPrintStream.print('\n');			
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
	
	public int GetRecordsWritten()
	{
		return mRecordWritten;
	}
	public int GetBlocksWritten()
	{
		return mRecordWritten * INDEX_SIZE / USED_BLOCK_SIZE;
	}
	public int GetDiskWrite()
	{
		return GetBlocksWritten();
	}	
}
