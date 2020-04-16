package bitmap_package2;


public class EmployeePosition {

	private int mIdProcessed;
	
	private int mId;
	private String mPositionInFile;
	
	public EmployeePosition(int aIdProcessed, int aId, String aPositionInFile) {
		
		mIdProcessed = aIdProcessed;
		mId = aId;		
		mPositionInFile = aPositionInFile;
	}
	
	
	public int GetIdProcessed()
	{
		return mIdProcessed;
	}
	public void SetIdProcessed(int aIdProcessed)
	{
		mIdProcessed =aIdProcessed;
	}

	public int GetId()
	{
		return mId;
	}	
	
	public String GetPositionInFile(){
		return mPositionInFile;
	}

	public void SetPositionInFile(String aPositionInFile)
	{
		mPositionInFile = aPositionInFile;
	}
	public void AddPositionInFile(String aPositionInFile)
	{
		mPositionInFile = mPositionInFile + ";" + aPositionInFile; 
	}

	public String toString()
	{
		return new String("Processed: " + GetIdProcessed() + ", Id: " + mId + ", PositionInFile: " + mPositionInFile);
	}
}

