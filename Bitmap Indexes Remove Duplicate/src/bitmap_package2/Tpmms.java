package bitmap_package2;

import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;


public class Tpmms
{
	private final long mMemorySize;
	private final String[] mDataFiles;
	private final String mResultPath;
	private final long mMaxRecordsPerList;
	private TreeMap<Integer, ArrayList<Employee>> mList;
	private ArrayList<File> mListStore;

	private int mP1BlocksRead;
	private int mP1DiskRead;
	private int mP1RecordsWrite;
	private int mP1BlocksWrite;
	private int mP1DiskWrite;
	
	private int mP2BlocksRead;
	private int mP2DiskRead;
	private int mP2RecordsWrite;
	private int mP2BlocksWrite;
	private int mP2DiskWrite;

	private long mTotalTime;
	
	private int mAdjustmentFactorReadWrite;
	
	private class EmployeeQueueContext
	{
		public final EmployeeFileReaderFast mEmployeeFileReaderFast;
		public Employee mEmployee;
		
		public EmployeeQueueContext(EmployeeFileReaderFast aEmployeeFileReaderFast, Employee aEmployee)
		{
			mEmployeeFileReaderFast=aEmployeeFileReaderFast;
			mEmployee=aEmployee;
		}
	}
	
	private class EmployeeComparator implements Comparator<EmployeeQueueContext>
	{		 
	    @Override
	    public int compare(EmployeeQueueContext aEmployee1, EmployeeQueueContext aEmployee2)
	    {
	        if(aEmployee1.mEmployee.GetId()<aEmployee2.mEmployee.GetId())
	        {
	        	return -1;
	        }
	        else if(aEmployee1.mEmployee.GetId()>aEmployee2.mEmployee.GetId())
	        {
	        	return 1;
	        }
	        else
	        {
	        	if(aEmployee1.mEmployee.GetDate()<aEmployee2.mEmployee.GetDate()){
	        		return -1;
	        	} else
	        	{
	        		return 1;
	        	}
	        }
	    }
	}   
	
	
	public Tpmms(long aMemorySize, String[] aDataFiles, String aResultFile)
	{
		mMemorySize=aMemorySize;
		mDataFiles=aDataFiles;
		mResultPath=aResultFile;
		mMaxRecordsPerList=(mMemorySize/EmployeeFile.BLOCK_SIZE)*EmployeeFile.RECORDS_PER_BLOCK;
		mList=new TreeMap<Integer, ArrayList<Employee>>();
		mListStore=new ArrayList<File>();
		mP1BlocksRead = 0;
		mP1DiskRead = 0;
		mP1RecordsWrite = 0;
		mP1BlocksWrite = 0;
		mP1DiskWrite = 0;
		mP2BlocksRead = 0;
		mP2DiskRead = 0;
		mP2RecordsWrite = 0;
		mP2BlocksWrite = 0;
		mP2DiskWrite = 0;
		mTotalTime = 0;
	}
	

	private EmployeeFileWriter WriteListFile() throws IOException
	{
		if(mList.isEmpty())
		{
			return null;
		}
		EmployeeFileWriter lEmployeeFileWriter=null;		
		try
		{
			File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
			lEmployeeFileWriter=new EmployeeFileWriter(lTempOutFile);
			for(Map.Entry<Integer, ArrayList<Employee>> lIdEntry: mList.entrySet())
			{
				for(Employee lEmployee: lIdEntry.getValue())
				{
					lEmployeeFileWriter.WriteRecord(lEmployee);					
				}
				lIdEntry.getValue().clear();
			}
			mList.clear();
			mListStore.add(lTempOutFile);
		}
		finally
		{
			if(lEmployeeFileWriter!=null)
			{
				lEmployeeFileWriter.Close();
			}
		}
		return lEmployeeFileWriter;
	}
	
	public void Process() throws IOException, ParseException
	{
		System.out.println("TPMMS, Factor Buffer Read/Write= " + mAdjustmentFactorReadWrite);
		System.out.println("");
		System.out.println("TPMMS, Phase 1: START");
		ExecTimer lTimer=new ExecTimer();
		lTimer.Start();
		for(String lPath: mDataFiles)
        {
			EmployeeFileReaderFast lEmployeeFileReaderFast=null;
			try
			{
				lEmployeeFileReaderFast=new EmployeeFileReaderFast(lPath);
				long lRecordCounter=0;				
				Employee lEmployee=null;
				while((lEmployee=lEmployeeFileReaderFast.ReadRecord())!=null)
				{
					int lCurrentId=lEmployee.GetId();
					if(mList.get(lCurrentId)==null)
					{
						mList.put(lCurrentId, new ArrayList<Employee>());
					}
					mList.get(lCurrentId).add(lEmployee);
					lRecordCounter++;
					if(lRecordCounter==mMaxRecordsPerList)
					{
						lRecordCounter=0;
						EmployeeFileWriter fileWriter = WriteListFile();
						mP1RecordsWrite+=fileWriter.GetRecordsWritten();
						mP1BlocksWrite +=fileWriter.GetBlocksWritten();
						mP1DiskWrite += fileWriter.GetDiskWrite();
					}
				}
				if(lRecordCounter>0)
				{
					lRecordCounter=0;
					EmployeeFileWriter fileWriter = WriteListFile();
					mP1RecordsWrite+=fileWriter.GetRecordsWritten();
					mP1BlocksWrite +=fileWriter.GetBlocksWritten();
					mP1DiskWrite += fileWriter.GetDiskWrite();
				}
				mP1BlocksRead +=lEmployeeFileReaderFast.GetBlocksRead();
				mP1DiskRead += lEmployeeFileReaderFast.GetDiskRead();
				
			}
			finally
			{
				if(lEmployeeFileReaderFast!=null)
				{
					lEmployeeFileReaderFast.Close();
				}
			}
        }
		
		long p1Time = lTimer.GetTime();
		mTotalTime += p1Time;
				
		System.out.println("TPMMS, Phase 1: Blocks Read= " + mP1BlocksRead);
		System.out.println("TPMMS, Phase 1: Disk Read= " + mP1DiskRead);
		System.out.println("TPMMS, Phase 1: Records Written= " + mP1RecordsWrite);
		System.out.println("TPMMS, Phase 1: Blocks Written= " + mP1BlocksWrite);
		System.out.println("TPMMS, Phase 1: Disk Write= " + mP1DiskWrite);
		System.out.println("TPMMS, Phase 1: FINISH, duration="+p1Time+"ms");
		System.out.println("");
		
		EmployeeFileWriter lEmployeeFileWriter=null;
		ArrayList<EmployeeFileReaderFast> lFileReaders=new ArrayList<EmployeeFileReaderFast>();
		PriorityQueue<EmployeeQueueContext> lListValues=new PriorityQueue<EmployeeQueueContext>(new EmployeeComparator());
		System.out.println("TPMMS, Phase 2: START");
		lTimer.Start();
		try
		{
			lEmployeeFileWriter=new EmployeeFileWriter(new File(mResultPath));
			for(File lFile:  mListStore)
			{
				EmployeeFileReaderFast lFileReader=new EmployeeFileReaderFast(lFile.getPath());
				lFileReaders.add(lFileReader);				
				Employee lEmployee=lFileReader.ReadRecord();
				if(lEmployee!=null)
				{
					lListValues.add(new EmployeeQueueContext(lFileReader, lEmployee));
				}				
			}
			EmployeeQueueContext lLastTopValue=null;
			while(true)
			{
				EmployeeQueueContext lTopValue=lListValues.poll();
				if(lTopValue==null)
				{
					if(lLastTopValue!=null)
					{
						lEmployeeFileWriter.WriteRecord(lLastTopValue.mEmployee);
					}
					break;
				}
				if(lLastTopValue!=null)
				{
					if(lLastTopValue.mEmployee.GetId()<lTopValue.mEmployee.GetId())
					{
						lEmployeeFileWriter.WriteRecord(lLastTopValue.mEmployee);
						lLastTopValue=lTopValue;
					}
				}
				else
				{
					lLastTopValue=lTopValue;
				}
				Employee lEmployee=lTopValue.mEmployeeFileReaderFast.ReadRecord();
				if(lEmployee!=null)
				{
					lListValues.add(new EmployeeQueueContext(lTopValue.mEmployeeFileReaderFast, lEmployee));
				}			
			}
			for(EmployeeFileReaderFast lFileReader: lFileReaders)
			{
				mP2BlocksRead +=lFileReader.GetBlocksRead();
				mP2DiskRead += lFileReader.GetDiskRead();
			}
		}
		finally
		{
			if(lEmployeeFileWriter!=null)
			{
				lEmployeeFileWriter.Close();
			}
			for(EmployeeFileReaderFast lFileReader: lFileReaders)
			{
				lFileReader.Close();
			}
		}
		mP2RecordsWrite+=lEmployeeFileWriter.GetRecordsWritten();
		mP2BlocksWrite +=lEmployeeFileWriter.GetBlocksWritten();
		mP2DiskWrite += lEmployeeFileWriter.GetDiskWrite();
		
		long p2Time = lTimer.GetTime();
		mTotalTime += p1Time;
		
		System.out.println("TPMMS, Phase 2: Blocks Read= " + mP2BlocksRead);
		System.out.println("TPMMS, Phase 2: Disk Read= " + mP2DiskRead);
		System.out.println("TPMMS, Phase 2: Records Written= " + mP2RecordsWrite);
		System.out.println("TPMMS, Phase 2: Blocks Written= " + mP2BlocksWrite);
		System.out.println("TPMMS, Phase 2: Disk Write= " + mP2DiskWrite);
		System.out.println("TPMMS, Phase 2: FINISH, duration="+p2Time+"ms");
		
		System.out.println("");
		
		System.out.println("TPMMS, FINISH, Total Duration="+ mTotalTime +"ms");

	}
	public long getmTotalTime() {
		return mTotalTime;
	}
}
