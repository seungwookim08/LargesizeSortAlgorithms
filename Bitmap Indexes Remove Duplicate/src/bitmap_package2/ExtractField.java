package bitmap_package2;


public class ExtractField {

	private int mIdProcessed;
	private int mId;
	
	public ExtractField(int aIdProcessed, int aId) {
		
		mIdProcessed = aIdProcessed;
		mId = aId;		
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
	

	public String toString()
	{
		return new String("Processed: " + GetIdProcessed() + ", Id: " + mId);
	}
}