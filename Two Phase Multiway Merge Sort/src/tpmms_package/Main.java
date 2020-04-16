package tpmms_package;

import java.util.ArrayList;

public class Main
{
	
	
	private enum ParameterType
	{
		FILEGENERATION,
		FILEGENERATION_ROWS,
		FILEGENERATION_MULTIPLIER,
		INPUT,
		OUTPUT,
		MEMORY,
		UNKNOWN
	}
	
	public static void main(String[] aArgs)
	{
		
		if(aArgs.length<4)
		{
			System.err.println("Invalid number of arguments");
	        System.exit(1);
		}
        final long MAX_MEM=Runtime.getRuntime().maxMemory();
		
		ParameterType lParameterType=ParameterType.UNKNOWN;
		ArrayList<String> lInputFiles=new ArrayList<String>();
		String lOutputFile=null;
		int numberOfRowsInFile=0;
		int rowsMultiplier=0;		
		int lMemory=(int)(MAX_MEM/10); //RA: By default using half of JVM memory for TPMMS
		// Kim: This will let us have best internal memory
		boolean executeFileGeneration = false;
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
			else if(lArgument.compareToIgnoreCase("-f")==0)
			{
				lParameterType=ParameterType.FILEGENERATION;
			}
			else if(lArgument.compareToIgnoreCase("-r")==0)
			{
				lParameterType=ParameterType.FILEGENERATION_ROWS;
			}
			else if(lArgument.compareToIgnoreCase("-x")==0)
			{
				lParameterType=ParameterType.FILEGENERATION_MULTIPLIER;
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
				case FILEGENERATION_ROWS:
					numberOfRowsInFile=Integer.parseInt(lArgument);
				case FILEGENERATION_MULTIPLIER:
					rowsMultiplier=Integer.parseInt(lArgument);
				case FILEGENERATION:
					executeFileGeneration=true;
					break;					
				default:
					break;
				}
			}
		}
		
		if(executeFileGeneration)
		{
			FileGenerator fileGenerator = new FileGenerator(lInputFiles.get(0),numberOfRowsInFile, lOutputFile, rowsMultiplier);
			try {
				fileGenerator.Generate();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(1);
		}
		
		if((lInputFiles.isEmpty()==true)||(lOutputFile==null)||(lMemory<=0))
		{
			System.err.println("Error: invalid arguments");
	        System.exit(3);
		}
		
		Tpmms lTpmms=null;
		try
		{
			lTpmms=new Tpmms(lMemory, lInputFiles.toArray(new String[lInputFiles.size()]), lOutputFile);
			lTpmms.Process();			
        }		
        catch(Exception aEx)
		{
        	System.err.println("Error: "+aEx.getMessage());
	        System.exit(2);
        }		
	}
}
