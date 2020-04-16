package bitmap_package2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Main
{
	
	private enum ParameterType
	{
		INPUT,
		OUTPUT,
		MEMORY,
		UNKNOWN
	}
	
	public static void main(String[] aArgs) throws FileNotFoundException, IOException, ParseException, InterruptedException
	{
        final long MAX_MEM=Runtime.getRuntime().maxMemory();
        	        
		ParameterType lParameterType=ParameterType.UNKNOWN;
		ArrayList<String> lInputFiles=new ArrayList<String>();
		String lOutputFile=null;
		int lMemory=(int)(MAX_MEM/10); //RA: By default using half of JVM memory for TPMMS
		// Kim: This will let us have best internal memory
		for(String lArgument: aArgs)
		{			
			if(lArgument.compareToIgnoreCase("-i")==0)
			{
				lParameterType=ParameterType.INPUT;
			}
			else if(lArgument.compareToIgnoreCase("-o")==0)
			{
				lParameterType=ParameterType.OUTPUT;
			}
			else if(lArgument.compareToIgnoreCase("-m")==0)
			{
				lParameterType=ParameterType.MEMORY;
			}
			else if(lArgument.charAt(0)=='-')
			{
				lParameterType=ParameterType.UNKNOWN;
			}
			else
			{
				switch(lParameterType)
				{
				case INPUT:
					lInputFiles.add(lArgument);
					break;
				case OUTPUT:
					if(lOutputFile==null)
					{
						lOutputFile=lArgument;
					}
					break;
				case MEMORY:
					if(lArgument.endsWith("%")==true)
					{
						int lArgInt=Integer.parseInt(lArgument.substring(0, lArgument.length()-1));
						if(lArgInt>=100)
						{
							System.err.println("Invalid memory parameter - using default");
						}
						else
						{
							lMemory=(int)((MAX_MEM*lArgInt)/100);
						}
					}
					else
					{
						int lArgInt=Integer.parseInt(lArgument)*1024;
						if(lArgInt>=MAX_MEM)
						{
							System.err.println("Invalid memory parameter - using default");
						}
						else
						{
							lMemory=lArgInt;
						}
					}
					break;
				default:
					break;
				}
			}
		}		
		
		if((lInputFiles.isEmpty()==true))
		{
			System.err.println("Error: invalid arguments");
	        System.exit(3);
		}

		
			
		System.out.println("Step 1: Creating Indexes, 3 on T1 and 3 on T2 -------------------------------------");		
        ExecTimer lTimerTotal=new ExecTimer();
        long indexTimeTotal = 0;
        lTimerTotal.Start();    
		
		// Indexes to be created
        ArrayList<String> indexPaths = new ArrayList<String>();
                
		ArrayList<Variant> indexes =  new ArrayList<Variant>();	
		VariantEmpId variantEmpId = new VariantEmpId();	
		indexes.add(variantEmpId);
		indexes.add(new VariantGender());
		indexes.add(new VariantDept());
	
		// For each file, create all BitMapIndexes
        ExecTimer lTimer=new ExecTimer();
        long indexTime = 0;
		for(String inputFile : lInputFiles)
		{
			System.out.println("\n --- INPUT FILE: " + inputFile + " ----------------------------------------------" );
	        for(Variant variant : indexes)
	        {
				System.out.println("");
	        	lTimer.Start();	        	
	        	if(variant.IsDenseIndex())
	    		{
	    			CreateDenseBitMapIndex(inputFile, variant);
	    			indexTime = lTimer.GetTime();
	    			System.out.println("Time Creating Index: " + indexTime  + "ms" + "\t" + variant);
	    		}        	
	    		else 
	    		{
	    			String indexFile = CreateSparseBitMapIndex(inputFile, variant);
	    			indexTime = lTimer.GetTime();
		    		System.out.println("Time Creating Index: " + indexTime  + "ms" + "\t" + variant + "\tIndex File " + indexFile);
	    		}	        		        	
	        }
		}
		indexTimeTotal = lTimerTotal.GetTime();	
		System.out.println("\nTotal Time Craeting Indexes: " + indexTimeTotal + "ms");		
		System.out.println("------------------------------------------------------------------------------------\n");
			

		
		
		System.out.println("\nStep 2: Build the Bitmap Indexes and DeDuplicating Files -------------------------\n");
        lTimer=new ExecTimer();
        long duplicateTotalTime = 0;
        lTimer.Start();
		DedupDiskIO dedupDiskIO = new DedupDiskIO();
		BitmapIndexDedup bitmapIndexDedup = new BitmapIndexDedup();
		String DeDuplicatedFile = bitmapIndexDedup.Process(lInputFiles,variantEmpId, dedupDiskIO);	
		
		System.out.println("Supporting Files IDs: Disk Read: " + dedupDiskIO.supportingFilesIDDiskRead);
		System.out.println("Supporting Files IDs: Records Read: " + dedupDiskIO.supportingFilesIDRecordsRead);
		System.out.println("Supporting Files IDs: Disk Written: " + dedupDiskIO.supportingFilesIDDiskWrite);
		System.out.println("Supporting Files IDs: Records Written: " + dedupDiskIO.supportingFilesIDRecordsWrite);
		System.out.println("Supporting Files IDs: Disk I/O: " + (dedupDiskIO.supportingFilesIDDiskRead + dedupDiskIO.supportingFilesIDDiskWrite));

		System.out.println("\nSupporting Files Date: Disk Read: " + dedupDiskIO.supportingFilesDateDiskRead);
		System.out.println("Supporting Files Date: Records Read: " + dedupDiskIO.supportingFilesDateRecordsRead);
		System.out.println("Supporting Files Date: Disk Written: " + dedupDiskIO.supportingFilesDateDiskWrite);
		System.out.println("Supporting Files Date: Records Written: " + dedupDiskIO.supportingFilesDateRecordsWrite);
		System.out.println("Supporting Files Date: Disk I/O: " + (dedupDiskIO.supportingFilesDateDiskRead + dedupDiskIO.supportingFilesDateDiskWrite));

		System.out.println("\nBuild Index and Dedup: Disk Read: " + dedupDiskIO.deduplicateDiskRead);
		System.out.println("Build Index and Dedup: Records Read: " + dedupDiskIO.deduplicateRecordsRead);
		System.out.println("Build Index and Dedup: Disk Written: " + dedupDiskIO.deduplicateDiskWrite);
		System.out.println("Build Index and Dedup: Records Written: " + dedupDiskIO.deduplicateRecordsWrite);
		System.out.println("Supporting Files Dedup: Disk I/O: " + (dedupDiskIO.deduplicateDiskRead + dedupDiskIO.deduplicateDiskWrite));

		System.out.println("\nTotal: Disk Read: " + dedupDiskIO.GetTotalDiskRead());
		System.out.println("Total: Records Read: " + dedupDiskIO.GetTotalRecordsRead());
		System.out.println("Total: Disk Written: " + dedupDiskIO.GetTotalDiskWrite());
		System.out.println("Total: Records Written: " + dedupDiskIO.GetTotalRecordsWrite());
		System.out.println("Total: Disk I/O: " + (dedupDiskIO.GetTotalDiskRead() + dedupDiskIO.GetTotalDiskWrite()));

		duplicateTotalTime = lTimer.GetTime();
		System.out.println("\nTotal Time Craeting Indexes: " + duplicateTotalTime + "ms");		

		System.out.println("------------------------------------------------------------------------------------\n");
		
		
		
		
		System.out.println("\nStep 3: Sorting DeDuplicated File ------------------------------------------------\n");
		Tpmms lTpmms=null;
		try
		{
			String[] finalDeduplicateFile = {DeDuplicatedFile} ;
			lTpmms=new Tpmms(lMemory, finalDeduplicateFile, lOutputFile);
			lTpmms.Process();			
        }		
        catch(Exception aEx)
		{
        	System.err.println("Error: "+aEx.getMessage());
	        System.exit(2);
        }		
		System.out.println("\nSorted DeDuplicated File: " + lOutputFile);
		System.out.println("------------------------------------------------------------------------------------\n");
		System.out.println("\nTotal Time For whole process: " + (indexTimeTotal +duplicateTotalTime + lTpmms.getmTotalTime()) + "ms");		

		
		
		return;
	}
	
	private static void CreateDenseBitMapIndex(String lInputFile, Variant lVariant) throws IOException, ParseException, InterruptedException
	{
		HashMap<Integer, IndexBitSet> mergePositionIndexesDense = IndexBuilder.BuildIndex(lInputFile, lVariant);

		int OriginalBytes = 0;
		int CompressedBytes = 0;
		
		for(Entry<Integer, IndexBitSet> c : mergePositionIndexesDense.entrySet())
		{
			IndexBitSet compressedDenseIndex = IndexBuilder.Compress(c.getValue());
		
			OriginalBytes += c.getValue().mBitsNumber;
			CompressedBytes += compressedDenseIndex.mBitsNumber;				
			
			// We do not save Dense indexes to files, if needed, uncomment code below to verify Index 
			/*
			System.out.println("");
			System.out.print(c.getKey() + " : ");
			c.getValue().Print();
			*/
		}
		
		System.out.println("Original Size: " + OriginalBytes + " bytes : " + "Compressed Size: " + CompressedBytes + " bytes");
	}
	
	private static String CreateSparseBitMapIndex(String lInputFile, Variant lVariant) throws IOException, ParseException, InterruptedException
	{		
		PositionBuilder positionBuilder = new PositionBuilder();
		String positionFilePath = positionBuilder.BuildPosition(lInputFile, lVariant);		
		String bitMapIndexFilePath = positionBuilder.BuildBitMapIndex(positionFilePath, lVariant);	
		return bitMapIndexFilePath;
	}
}
