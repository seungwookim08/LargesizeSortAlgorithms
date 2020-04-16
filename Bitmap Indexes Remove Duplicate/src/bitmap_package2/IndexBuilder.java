package bitmap_package2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class IndexBuilder
{
	public static String ExtractFieldValues(ArrayList<String> filePaths, Variant lVariant, DedupDiskIO dedupDiskIO) throws IOException, ParseException, InterruptedException
	{
		File lTempOutFile=File.createTempFile("com6521_list_", ".tmp");
		ExtractFieldFileWriter lEmployeeExtractFieldWriter=null;
		
		EmployeeFileReader lEmployeeFileReader=null;
		try
		{
			lEmployeeExtractFieldWriter=new ExtractFieldFileWriter(lTempOutFile);
			
			for(String inputFile : filePaths)
			{
				lEmployeeFileReader=new EmployeeFileReader(inputFile);				
				Employee lEmployee=null;
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
					case DATE:
						keyValue =lEmployee.GetDate(); 
						break;
					case DEPT_ID:
						keyValue =lEmployee.GetDept();
						break;
					}				
	
					ExtractField lExtractField = new ExtractField(0, keyValue);
					lEmployeeExtractFieldWriter.WriteRecord(lExtractField);					
				}
				dedupDiskIO.supportingFilesDateDiskRead += lEmployeeFileReader.GetDiskRead();
				dedupDiskIO.supportingFilesDateRecordsRead += lEmployeeFileReader.GetRecordsRead();				
			}
		
			dedupDiskIO.supportingFilesDateDiskWrite += lEmployeeExtractFieldWriter.GetDiskWrite();
			dedupDiskIO.supportingFilesDateRecordsWrite += lEmployeeExtractFieldWriter.GetRecordsWritten();
			
			return lTempOutFile.getAbsolutePath();
		}
		finally
		{
			if(lEmployeeFileReader!=null)
			{
				lEmployeeFileReader.Close();
			}
			if(lEmployeeExtractFieldWriter!=null)
			{
				lEmployeeExtractFieldWriter.Close();
			}
		}			
	}

	public static HashMap<Integer, IndexBitSet> BuildIndex(String indexFilePath, Variant lVariant) throws IOException, ParseException, InterruptedException
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
				case DATE:
					keyValue =lEmployee.GetDate(); 
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

	
	public static HashMap<Variant, IndexBitSet> BuildIndex(String aDataPath, Variant ...aVariants) throws FileNotFoundException, IOException, ParseException
	{
		EmployeeFileReader lFileReader=new EmployeeFileReader(aDataPath);
		int lRecordsNum=(int)lFileReader.GetRecordsInFile();
		HashMap<Variant, IndexBitSet> lResult=new HashMap<Variant, IndexBitSet>();
		Employee lEmployee=null;
		int lRecordIndex=0;
		while((lEmployee=lFileReader.ReadRecord())!=null)
		{
			for (Variant lVariant: aVariants)
			{
				IndexBitSet lBitSet=lResult.get(lVariant);
				if(null==lBitSet)
				{
					lBitSet=new IndexBitSet(lRecordsNum);
					lResult.put(lVariant, lBitSet);
				}
				switch(lVariant.GetVariantType())
				{
				case EMP_ID:
					if(lEmployee.GetId()==((VariantEmpId)lVariant).GetEmpId())
					{
						lBitSet.mBitSet.set(lRecordIndex);						
					}
					break;
				case GENDER:
					if(lEmployee.GetGender()==((VariantGender)lVariant).GetGender())
					{
						lBitSet.mBitSet.set(lRecordIndex);
					}
					break;
				case DEPT_ID:
					if(lEmployee.GetDept()==((VariantDept)lVariant).GetDept())
					{
						lBitSet.mBitSet.set(lRecordIndex);
					}
					break;
				}
			}
			lRecordIndex++;
		}
		
		return lResult;
	}
	
	public static HashMap<Variant, IndexBitSet> CompressIndexes(HashMap<Variant, IndexBitSet> indexes){
		HashMap<Variant, IndexBitSet> lResult=new HashMap<Variant, IndexBitSet>();
		
		indexes.forEach((aKey, aValue) -> 
		{
			lResult.put(aKey, Compress(aValue));
		});
		
		return lResult;
	}
	
	public static IndexBitSet Compress(IndexBitSet aBitSet)
	{
		int lLast=aBitSet.mBitSet.nextSetBit(0);
		if(lLast<0)
		{
			return null;
		}
		
		IndexBitSet lResultBitSet=new IndexBitSet();
		while(true)
		{			
			int lNew=aBitSet.mBitSet.nextSetBit(lLast+1);
			int lValue=lNew<0?(aBitSet.mBitsNumber-lLast-1):(lNew-lLast-1);			
			
			int lValueBitsNumber=0;
			do
			{
				lResultBitSet.mBitSet.set(lResultBitSet.mBitsNumber, (lValue&1)==1);
				lResultBitSet.mBitsNumber++;
				lValueBitsNumber++;
				lValue=lValue>>1;
			} while(lValue>0);
			lResultBitSet.mBitSet.set(lResultBitSet.mBitsNumber+1, lResultBitSet.mBitsNumber+lValueBitsNumber);
			lResultBitSet.mBitsNumber+=lValueBitsNumber;
			if(lNew<0)
			{
				break;
			}			
			lLast=lNew;
		}
		
		return lResultBitSet;
	}

	public static IndexBitSet Decompress(IndexBitSet aBitSet, int bitSetSize)
	{
		class CompressionContext
		{
			public int mHeaderStart;
			public int mHeaderEnd;
			
			public CompressionContext(int aHeaderStart, int aHeaderEnd)
			{
				mHeaderStart=aHeaderStart;
				mHeaderEnd=aHeaderEnd;
			}
		}
		
		int lHeaderEnd=aBitSet.mBitSet.previousClearBit(aBitSet.mBitsNumber-1);
		if(lHeaderEnd<0)
		{
			return null;
		}
		int lHeaderStart=aBitSet.mBitsNumber-1;
		ArrayDeque<CompressionContext> lCompressionContextQeue=new ArrayDeque<CompressionContext>();
		while(true)
		{
			lCompressionContextQeue.push(new CompressionContext(lHeaderStart, lHeaderEnd));
			int lValueBitsNumber=lHeaderStart-lHeaderEnd+1;
			lHeaderStart=lHeaderEnd-lValueBitsNumber-1;
			if(lHeaderStart<0)
			{
				break;
			}
			lHeaderEnd=aBitSet.mBitSet.previousClearBit(lHeaderStart);
			if(lHeaderEnd<0)
			{
				break;
			}
		}
		
		IndexBitSet lResultBitSet=new IndexBitSet();		
		while(true)
		{
			CompressionContext lCompressionContext=lCompressionContextQeue.poll();			
			if(lCompressionContext==null)
			{
				break;
			}
			int lValue=0;
			int lValueBitsNumber=lCompressionContext.mHeaderStart-lCompressionContext.mHeaderEnd+1;
			int lValueEnd=lCompressionContext.mHeaderEnd-lValueBitsNumber;
			for(int lBitCounter=0; lBitCounter<lValueBitsNumber; ++lBitCounter)
			{
				boolean lCurrentBit=aBitSet.mBitSet.get(lValueEnd+lBitCounter);
				lValue+=lCurrentBit?(1<<lBitCounter):0;
			}			
			lResultBitSet.mBitSet.set(lResultBitSet.mBitsNumber);
			lResultBitSet.mBitsNumber+=lValue+1;			
		}		
		
		IndexBitSet lResultBitSetFinal =new IndexBitSet(bitSetSize);
		
		for(int i=lResultBitSet.mBitsNumber-1;i>=0;--i)
		{
			lResultBitSetFinal.mBitSet.set(i+bitSetSize-lResultBitSet.mBitsNumber, lResultBitSet.mBitSet.get(i));
		}		
		return lResultBitSetFinal;
	}
	
	public static void Test()
	{
		//lSet=new BitSet(lRecordsNum);
		
		/*IndexBitSet lTestSet=new IndexBitSet(14);
		lTestSet.mBitSet.set(0);
		lTestSet.mBitSet.set(1);
		lTestSet.mBitSet.set(3);
		lTestSet.mBitSet.set(6);
		lTestSet.mBitSet.set(8);
		lTestSet.mBitSet.set(9);
		lTestSet.mBitSet.set(11);
		lTestSet.mBitSet.set(12);
		lTestSet.mBitSet.set(13);
		
		IndexBitSet lResult=Decompress(lTestSet);
		System.out.println(lResult);*/
		
		IndexBitSet lTestSet_1=new IndexBitSet(12);
		lTestSet_1.mBitSet.set(3);
		lTestSet_1.mBitSet.set(11);
		System.out.println("Original: "+ lTestSet_1);
		
		IndexBitSet lCompressed=Compress(lTestSet_1);
		System.out.println("Compressed: "+ lCompressed);
		
		//IndexBitSet lDecompressed=Decompress(lCompressed);
		//System.out.println("Decompressed: "+ lDecompressed);
	}
}
