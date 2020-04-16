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

public class EmployeeFileReader implements EmployeeFile
{
	private FileInputStream mInputStream=null;
	private BufferedInputStream mBuffInputStream=null;
	private Charset mDataCharset=null;
	private long mBytesRead;
	private int mRecordsRead;
	private long mRecordsInFile;
	
	public EmployeeFileReader(String aPath) throws FileNotFoundException
	{
		mRecordsRead=0;
		mInputStream=new FileInputStream(aPath);
		mBuffInputStream=new BufferedInputStream(mInputStream, BLOCK_SIZE); //RA: By using BufferedInputStream we simulate reading data in blocks of size BLOCK_SIZE
		//RA: Alternative approach might include utilizing java.nio.file.Files and java.nio.channels routines to read from disk
		mDataCharset=Charset.forName("US-ASCII");
		mBytesRead=0;
		
		File lFile=new File(aPath);
		mRecordsInFile=lFile.length()/RECORD_SIZE;
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
		
		int lOffset=0;
		int lId=Integer.parseInt(new String(lBuffer, lOffset, EMPID_SIZE, mDataCharset));
		lOffset+=EMPID_SIZE;
		int lDate = Integer.parseInt(new String(lBuffer, lOffset, DATE_SIZE, mDataCharset).replace("-",""));
		lOffset+=DATE_SIZE;
		String lName=("!"+new String(lBuffer, lOffset, NAME_SIZE, mDataCharset)).trim().substring(1);
		lOffset+=NAME_SIZE;
		Gender lGen=Gender.FromInt(Integer.parseInt(new String(lBuffer, lOffset, GEN_SIZE, mDataCharset)));
		lOffset+=GEN_SIZE;
		int lDept=Integer.parseInt(new String(lBuffer, lOffset, DEPT_SIZE, mDataCharset));
		lOffset+=DEPT_SIZE;
		BigInteger lSocNum=new BigInteger(new String(lBuffer, lOffset, SOCNUM_SIZE, mDataCharset));
		lOffset+=SOCNUM_SIZE;
		String lAddress=("!"+new String(lBuffer, lOffset, ADDRESS_SIZE, mDataCharset)).trim().substring(1);
		lOffset+=ADDRESS_SIZE;
		
		mRecordsRead++;
		return new Employee(lId, lDate, lName, lGen, lDept, lSocNum, lAddress);
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
	public int GetRecordsRead()
	{
		return mRecordsRead;
	}
	public int GetBlocksRead()
	{
		return mRecordsRead * RECORD_SIZE / USED_BLOCK_SIZE;
	}
	public int GetDiskRead()
	{
		return GetBlocksRead();
	}
	public long GetRecordsInFile()
	{
		return mRecordsInFile;
	}
}
