package comp6521_la1;

public class Employee
{
	private int mId;
	private int mDate;
	String mData;

	public Employee()
	{
	}
	public Employee(int aId, int aDate,  String aData)
	{
		mId=aId;		
		mDate= aDate; 
		mData = aData;
	}
	
	public int GetId()
	{
		return mId;
	}
	public void SetId(int value)
	{
		mId=value;
		mData = Integer.toString(value) + mData.substring(8);
	}	

	public int GetDate()
	{
		return mDate;
	}

	public String GetData(){
		return mData;
	}
	
}
