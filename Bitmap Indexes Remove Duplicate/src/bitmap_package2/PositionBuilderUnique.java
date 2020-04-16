package bitmap_package2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class PositionBuilderUnique {

	private boolean keepMergingIndexes = false;
	private int lRecordsNum =0;
	private int indexProcessmentLimit = 30000;
	private DedupDiskIO mDedupDiskIO;		
	
	public PositionBuilderUnique(DedupDiskIO aDedupDiskIO)
	{
		mDedupDiskIO = aDedupDiskIO;
	}
	
	public int getlRecordsNum() {
		return lRecordsNum;
	}

	public String BuildPosition(ArrayList<String> inputFiles, Variant lVariant) throws IOException, ParseException, InterruptedException
	{
        ExecTimer lTimerTotal=new ExecTimer();
        long indexTimeTotal = 0;
        lTimerTotal.Start();    
        
		ExecTimer lTimer=new ExecTimer();
        long indexTime = 0;

        lTimer.Start();        	
		String pseudoBitMapIndexFilePath = BuildPseudoIndex(inputFiles, lVariant);
		indexTime = lTimer.GetTime();	
		//System.out.println("Pseudo: " + indexTime + "ms");

        lTimer.Start();
        //System.out.println("Record Number:" + lRecordsNum);
        //System.out.println("Processment Limit:" + indexProcessmentLimit);
        
		String bitMapIndexFilePath = MergePositionIndexes(pseudoBitMapIndexFilePath, lVariant, true);
		indexTime = lTimer.GetTime();	
		//System.out.println("First Merge: " + indexTime + "ms");

		while(keepMergingIndexes)
		{
			Path oldbitMapIndexFilePath = Paths.get(bitMapIndexFilePath);

	        lTimer.Start();        	
			bitMapIndexFilePath = MergePositionIndexes(bitMapIndexFilePath, lVariant, false);
			indexTime = lTimer.GetTime();	
			System.out.println("Merge: " + indexTime + "ms");
			
			Files.deleteIfExists(oldbitMapIndexFilePath);
		}

		indexTimeTotal = lTimerTotal.GetTime();	
		//System.out.println("Position Total: " + indexTimeTotal + "ms");
			
		
		return bitMapIndexFilePath;
	}
	
	public String BuildBitMapIndex(String aDataPath, Variant lVariant) throws IOException, ParseException
	{

		File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
		EmployeeIndexFileWriter lEmployeeIndexFileWriter=null;
		EmployeePositionFileReader lPositionFileReader=null;
		try
		{
			lEmployeeIndexFileWriter=new EmployeeIndexFileWriter(lTempOutFile, lVariant);

			lPositionFileReader=new EmployeePositionFileReader(aDataPath);
			EmployeePosition lEmployeePosition=null;
			
			while((lEmployeePosition=lPositionFileReader.ReadRecord())!=null)
			{
				IndexBitSet lBitSet=new IndexBitSet(lRecordsNum);
				
				String positions = lEmployeePosition.GetPositionInFile();
				
				String delimiter = ";";
		        String[] positionArray = positions.split(delimiter);
		        for (String p : positionArray)
		        {
		        	lBitSet.mBitSet.set(Integer.valueOf(p));	
		        }

		        	IndexBitSet	IndexBitSetCompressed = IndexBuilder.Compress(lBitSet);
		        	EmployeeIndex lIndex = new EmployeeIndex(0, lEmployeePosition.GetId(), IndexBitSetCompressed.toString());
					lEmployeeIndexFileWriter.WriteRecord(lIndex);
			}
		
		}
		finally
		{
			if(lEmployeeIndexFileWriter!=null)
			{
				lEmployeeIndexFileWriter.Close();
			}
			if(lPositionFileReader!=null)
			{
				lPositionFileReader.Close();
			}
		}	
		return lTempOutFile.getAbsolutePath();
	}
	
	public String BuildPseudoIndex(ArrayList<String> inputFiles, Variant lVariant) throws IOException, ParseException
	{

		File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
		EmployeePositionFileWriter lEmployeePositionFileWriter=null;
		EmployeeFileReader lFileReader=null;
		try
		{
			lEmployeePositionFileWriter=new EmployeePositionFileWriter(lTempOutFile);
			Employee lEmployee=null;
			int lRecordPosition=0;

			for(String inputFile : inputFiles)
			{
				lFileReader=new EmployeeFileReader(inputFile);		
				while((lEmployee=lFileReader.ReadRecord())!=null)
				{
					int keyValue = 0;
					
					switch(lVariant.GetVariantType())
					{
					case EMP_ID:
						keyValue =lEmployee.GetId(); 
						break;
					case GENDER:
						keyValue =lEmployee.GetGender().Value(); 
						break;
					case DEPT_ID:
						keyValue =lEmployee.GetDept();
						break;
					}
	
					EmployeePosition lEmployeePosition = new EmployeePosition(0, keyValue, Integer.toString(lRecordPosition));
					lEmployeePositionFileWriter.WriteRecord(lEmployeePosition);
					
					lRecordPosition++;
				}		
				
				mDedupDiskIO.supportingFilesIDDiskRead += lFileReader.GetDiskRead();
				mDedupDiskIO.supportingFilesIDRecordsRead += lFileReader.GetRecordsRead();
			}
			
			mDedupDiskIO.supportingFilesIDDiskWrite += lEmployeePositionFileWriter.GetDiskWrite();
			mDedupDiskIO.supportingFilesIDRecordsWrite += lEmployeePositionFileWriter.GetRecordsWritten();
			
			lRecordsNum = lRecordPosition;
			if(indexProcessmentLimit>lRecordsNum)
			{
				indexProcessmentLimit=lRecordsNum;
			}
		}
		finally
		{
			if(lEmployeePositionFileWriter!=null)
			{
				lEmployeePositionFileWriter.Close();
			}
			if(lFileReader!=null)
			{
				lFileReader.Close();
			}
		}	
		return lTempOutFile.getAbsolutePath();
	}
	

	public String MergePositionIndexes(String indexFilePath, Variant lVariant, boolean isFirstRound) throws IOException, ParseException, InterruptedException
	{
		
		keepMergingIndexes=false;
		
		File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
		EmployeePositionFileWriter lEmployeePositionFileWriter=null;
		EmployeePositionFileReader lFileReader=null;
		try
		{
			lEmployeePositionFileWriter=new EmployeePositionFileWriter(lTempOutFile);
			
			HashMap<Integer,EmployeePosition> employeeIndexInProcessement = new HashMap<Integer,EmployeePosition>(); 
			
			lFileReader=new EmployeePositionFileReader(indexFilePath);
			
			EmployeePosition lEmployeeIndex=null;
			while((lEmployeeIndex=lFileReader.ReadRecord())!=null)
			{
				if(lEmployeeIndex.GetIdProcessed()==0)
				{
					EmployeePosition employeeIndexDestination = employeeIndexInProcessement.get(lEmployeeIndex.GetId());
					if(employeeIndexDestination==null)
					{
						if(employeeIndexInProcessement.size()<=indexProcessmentLimit)
						{
							employeeIndexInProcessement.put(lEmployeeIndex.GetId(), lEmployeeIndex);
							lEmployeeIndex.SetIdProcessed(1);
						}
						else
						{
							lEmployeeIndex.SetIdProcessed(0); 
							// These indexes will be processed next time the Merge Function is called
							lEmployeePositionFileWriter.WriteRecord(lEmployeeIndex);
							keepMergingIndexes = true;
						}
					} else {
						employeeIndexDestination.AddPositionInFile(lEmployeeIndex.GetPositionInFile());
					}
				}else {
					// Already processed indexes
					lEmployeePositionFileWriter.WriteRecord(lEmployeeIndex);					
				}
			}		
			
			lFileReader.Close();
								
			for(EmployeePosition employeeIndexFilter : employeeIndexInProcessement.values()) {
				// These are the processed indexes
				lEmployeePositionFileWriter.WriteRecord(employeeIndexFilter);
			}
			
			mDedupDiskIO.supportingFilesIDDiskRead += lFileReader.GetDiskRead();
			mDedupDiskIO.supportingFilesIDRecordsRead += lFileReader.GetRecordsRead();
			mDedupDiskIO.supportingFilesIDDiskWrite += lEmployeePositionFileWriter.GetDiskWrite();
			mDedupDiskIO.supportingFilesIDRecordsWrite += lEmployeePositionFileWriter.GetRecordsWritten();
		}
		finally
		{
			if(lEmployeePositionFileWriter!=null)
			{
				lEmployeePositionFileWriter.Close();
			}
			if(lFileReader!=null)
			{
				lFileReader.Close();
			}
		}	
		
		return lTempOutFile.getAbsolutePath();
		
	}

	public HashMap<Integer, IndexBitSet> MergePositionIndexesDense(String indexFilePath, Variant lVariant) throws IOException, ParseException, InterruptedException
	{
			
		EmployeeFileReader lEmployeeFileReader=null;
		try
		{
			lEmployeeFileReader=new EmployeeFileReader(indexFilePath);
			int lRecordNumber =  (int)lEmployeeFileReader.GetRecordsInFile(); 
			int bSize =lRecordNumber * 4;
			lVariant.SetBitMapSize(bSize);
					
			HashMap<Integer,IndexBitSet> employeeIndexInProcessement = new HashMap<Integer,IndexBitSet>(); 
					
			Employee lEmployee=null;

			int lRecordIndex=0;
			while((lEmployee=lEmployeeFileReader.ReadRecord())!=null)
			{
				int keyValue = 0;
				switch(lVariant.GetVariantType())
				{
				case EMP_ID:
					keyValue =lEmployee.GetId(); 
					break;
				case GENDER:
					keyValue =lEmployee.GetGender().Value(); 
					break;
				case DEPT_ID:
					keyValue =lEmployee.GetDept();
					break;
				}
				
				IndexBitSet employeeIndexBitSet = employeeIndexInProcessement.get(keyValue);
				
				if(employeeIndexBitSet==null)
				{
					employeeIndexBitSet = new IndexBitSet(lRecordNumber);
					employeeIndexBitSet.mBitSet.set(lRecordIndex);
					employeeIndexInProcessement.put(keyValue, employeeIndexBitSet);					
				} else {
					employeeIndexBitSet.mBitSet.set(lRecordIndex);
				}				
				++lRecordIndex;
			}		
						
			return employeeIndexInProcessement;		
		}
		finally
		{
			if(lEmployeeFileReader!=null)
			{
				lEmployeeFileReader.Close();
			}
		}	
		
	}

}
