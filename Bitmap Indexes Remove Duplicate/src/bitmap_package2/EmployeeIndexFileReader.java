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

	public class EmployeeIndexFileReader implements EmployeeIndexFile
	{
		private FileInputStream mInputStream=null;
		private BufferedInputStream mBuffInputStream=null;
		private Charset mDataCharset=null;
		private long mBytesRead;
		private int mRecordsRead;
		private long mRecordsInFile;
		private Variant mVariant;
		
		public EmployeeIndexFileReader(String aPath, Variant aVariant) throws FileNotFoundException
		{
			mRecordsRead=0;
			mInputStream=new FileInputStream(aPath);
			mBuffInputStream=new BufferedInputStream(mInputStream, BLOCK_SIZE); 
			mDataCharset=Charset.forName("US-ASCII");
			mVariant = aVariant;
			
			File lFile=new File(aPath);
			mRecordsInFile=lFile.length()/INDEX_SIZE;
		}

		public EmployeeIndex ReadRecord() throws IOException, ParseException 
		{		
			int index_size = INDEX_SIZE;
			int bitmapindex_size = BITMAPINDEX_SIZE;				
			if(mVariant.IsDenseIndex())
			{
				bitmapindex_size = mVariant.GetBitMapSize();
				index_size= INDEXPROCESS_SIZE + EMPID_SIZE + bitmapindex_size + EOR_SIZE;
			}
			
			byte[] lBuffer=new byte[index_size];
			
			int lResult=mBuffInputStream.read(lBuffer, 0, index_size);
			if(lResult==-1)
			{
				return null;
			}
			else if(lResult!=index_size)
			{
				throw new IOException();
			}
			else if(lBuffer[index_size-1]!='\n')
			{
				throw new ParseException("Invalid end-or-record roken", index_size-1); 
			}
			mBytesRead+=lResult;
			
			int lOffset=0;
			int lIdProcessed=Integer.parseInt(new String(lBuffer, lOffset, INDEXPROCESS_SIZE, mDataCharset));
			lOffset+=INDEXPROCESS_SIZE;
			int lId=Integer.parseInt(new String(lBuffer, lOffset, EMPID_SIZE, mDataCharset));
			lOffset+=EMPID_SIZE;
			String lBitMapIndex=("!"+new String(lBuffer, lOffset, bitmapindex_size, mDataCharset)).trim().substring(1);
			lOffset+=bitmapindex_size;		
			mRecordsRead++;
			return new EmployeeIndex(lIdProcessed, lId, lBitMapIndex);
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
