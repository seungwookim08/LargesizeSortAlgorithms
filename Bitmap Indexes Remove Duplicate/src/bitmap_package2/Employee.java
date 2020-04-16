package bitmap_package2;

import java.math.BigInteger;
import java.util.Date;


public class Employee
{
	private int mId;
	private int mDate;
	private String mName;
	private Gender mGender;
	private int mDept;
	private BigInteger mSocNum;
	private String mAddress;
	String mData;
	
	public Employee(int aId, int aDate, String aName, Gender aGender, int aDept, BigInteger aSocNum, String aAddress)
	{
		mId=aId;
		mDate= aDate;
		mName=aName;
		mGender=aGender;
		mDept=aDept;
		mSocNum=aSocNum;
		mAddress=aAddress;
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
	
	public int GetDate()
	{
		return mDate;
	}
	
	public String GetName()
	{
		return mName;
	}
	
	public Gender GetGender()
	{
		return mGender;		
	}
	
	public int GetDept()
	{
		return mDept;
	}
	
	public BigInteger GetSocNum()
	{
		return mSocNum;
	}
	
	public String GetAddress()
	{
		return mAddress;
	}
	
	public String GetData(){
		return mData;
	}	
	
	public String toString()
	{
		return new String("Id: "+mId+", Date: "+mDate+", Name: "+mName+", Gender: "+mGender.toString()+", Department: "+mDept+", SIN: "+mSocNum.toString()+", Address: "+mAddress);
	}
}
