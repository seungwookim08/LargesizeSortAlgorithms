package bitmap_package2;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;

public class BitmapIndexDedup {

	private DedupDiskIO mDedupDiskIO;
	
	public String Process(ArrayList<String> lInputFiles, VariantEmpId variantEmpId, DedupDiskIO dedupDiskIO) throws IOException, ParseException, InterruptedException {
		
		mDedupDiskIO=dedupDiskIO;
		
        ExecTimer lTimerDedupTotal=new ExecTimer();
        long indexTimeDedupTotal = 0;
        lTimerDedupTotal.Start();    

		
		//-- POSITION FILE -------------------------------------------------------
		// -----------------------------------------------------------------------
		PositionBuilderUnique positionBuilder = new PositionBuilderUnique(dedupDiskIO);
		String positionFilePath = positionBuilder.BuildPosition(lInputFiles, variantEmpId);
		System.out.println("Supporting File IDs Position: " + positionFilePath);
		// --------------------------------------------------------------------------	
		
		//-- DATE FILE -------------------------------------------------------
		//--------------------------------------------------		
		// Create File Only with Date field. This will allow to load more lines in memory for processing with Index	
		VariantDate lVariant = new VariantDate(); 
		String extractFieldOutput  = IndexBuilder.ExtractFieldValues(lInputFiles, lVariant, dedupDiskIO);
		System.out.println("Supporting File Date Only: " + extractFieldOutput);
		//--------------------------------------------------
		
		// -- BUILD FINAL BITSET ----------------------------------------------
		// --------------------------------------------------------------------		

		//This will be the end result of this process: A BitSet deduplicated
		IndexBitSet finalFileBitSet = new IndexBitSet(positionBuilder.getlRecordsNum());		
		
		EmployeePositionFileReader lPositionFileReader=null;
		try
		{
			lPositionFileReader=new EmployeePositionFileReader(positionFilePath);
			ArrayList<EmployeeIndex> bitmapIndexes = new ArrayList<EmployeeIndex>();
			
			int indexCount = 0;
			int maximumBitsinBitsetForProcessing = 50000000;
			int limitIndexProcessing = maximumBitsinBitsetForProcessing/positionBuilder.getlRecordsNum();
			
			System.out.println("Limit Indexes per Round:" + limitIndexProcessing);
			EmployeePosition lEmployeePosition=null;			
			while((lEmployeePosition=lPositionFileReader.ReadRecord())!=null)
			{
				++indexCount;
				
				//This BitSet has size considering all the files, example: 20.000
				BitSet lBitSet=new BitSet(positionBuilder.getlRecordsNum());
				EmployeeIndex employeeIndex = new EmployeeIndex(0, lEmployeePosition.GetId(), "");
				employeeIndex.setTempBitSet(lBitSet);
				bitmapIndexes.add(employeeIndex);
				
				String positions = lEmployeePosition.GetPositionInFile();				
				String delimiter = ";";
		        String[] positionArray = positions.split(delimiter);
		        for (String p : positionArray)
		        {
		        	lBitSet.set(Integer.valueOf(p));	
		        }
		        
		        if(indexCount>limitIndexProcessing)
		        {
		        	DeDuplicate(bitmapIndexes, extractFieldOutput, finalFileBitSet);
		        	indexCount=0;
		        }
			}	
			
			dedupDiskIO.deduplicateDiskRead += lPositionFileReader.GetDiskRead();
			dedupDiskIO.deduplicateRecordsRead += lPositionFileReader.GetRecordsRead();		

	        if(indexCount>0)
	        {
	        	DeDuplicate(bitmapIndexes, extractFieldOutput, finalFileBitSet);
	        }				
		}
		finally
		{
			if(lPositionFileReader!=null)
			{
				lPositionFileReader.Close();
			}
		}	
		// --------------------------------------------------------------------

		
		// -- BUILD FINAL OUTPUT ----------------------------------------------
		// --------------------------------------------------------------------		

		File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
		EmployeeFileWriter lEmployeeFileWriter=null;
		EmployeeFileReaderFast lFileReader=null;
		try
		{
			lEmployeeFileWriter=new EmployeeFileWriter(lTempOutFile);
			Employee lEmployee=null;
			int lRecordPosition=0;

			for(String inputFile : lInputFiles)
			{
				lFileReader=new EmployeeFileReaderFast(inputFile);		
				while((lEmployee=lFileReader.ReadRecord())!=null)
				{
	
					if(finalFileBitSet.mBitSet.get(lRecordPosition))
					{
						lEmployeeFileWriter.WriteRecord(lEmployee);	
					}
					
					++lRecordPosition;
				}		
			}
		}
		finally
		{
			if(lEmployeeFileWriter!=null)
			{
				lEmployeeFileWriter.Close();
			}
			if(lFileReader!=null)
			{
				lFileReader.Close();
			}
		}	
		dedupDiskIO.deduplicateDiskWrite += lEmployeeFileWriter.GetDiskWrite();
		dedupDiskIO.deduplicateRecordsWrite += lEmployeeFileWriter.GetRecordsWritten();
		// --------------------------------------------------------------------

		indexTimeDedupTotal = lTimerDedupTotal.GetTime();	
		System.out.println("\nDeDuplicated File: " + lTempOutFile.getAbsolutePath());
		System.out.println("DeDuplication time: " + indexTimeDedupTotal + "ms\n");
		
		return lTempOutFile.getAbsolutePath();
	}
	
	private void DeDuplicate(ArrayList<EmployeeIndex> employeeIndexes, String dateFilePath, IndexBitSet finalFilesBitSet) throws IOException, ParseException {		
		
		ExtractFieldFileReader lFileReader=new ExtractFieldFileReader(dateFilePath);
		ExtractField extractField=null;
		int rowCount = 0;
		while((extractField=lFileReader.ReadRecord())!=null)
		{
			int extractedDate = extractField.GetId();
			
			for(EmployeeIndex employeeIndex : employeeIndexes)
			{
				if(employeeIndex.getTempBitSet().get(rowCount)) {
					if(employeeIndex.getmTempDate()<extractedDate)
					{
						employeeIndex.setmTempDate(extractedDate);
						employeeIndex.setmTempOuputPosition(rowCount);
					}
				}
			}
			++rowCount;			
		}

		mDedupDiskIO.deduplicateDiskRead += lFileReader.GetDiskRead();
		mDedupDiskIO.deduplicateRecordsRead += lFileReader.GetRecordsRead();		
		
		lFileReader.Close();

		//Marking Rows that will be kept in the Final Output
		for(EmployeeIndex employeeIndex : employeeIndexes)
		{
			finalFilesBitSet.mBitSet.set(employeeIndex.getmTempOuputPosition());
		}
		
		employeeIndexes.clear();
	}	
}
