package bitmap_package2;

import java.util.BitSet;

public class EmployeeIndex {

	private int mIdProcessed;
	
	private int mId;
	private String mIndexBitSet;

	// These fields are temporary PlaceHolders for Index Processing
	private BitSet mTempBitSet;
	private int mTempDate;
	private int mTempOuputPosition;
	
	
	public EmployeeIndex(int aIdProcessed, int aId, String aIndexBitSet) {
		
		mIdProcessed = aIdProcessed;
		mId = aId;		
		mIndexBitSet = aIndexBitSet;
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
	public String GetBitSet(){
		return mIndexBitSet;
	}
	public void SetBitSet(String aIndexBitSet)
	{
		mIndexBitSet = aIndexBitSet;
	}
	

	// Properties used as a Temporary PlaceHolder to Process index------------
	public int getmTempDate() {
		return mTempDate;
	}
	public void setmTempDate(int mTempDate) {
		this.mTempDate = mTempDate;
	}
	public int getmTempOuputPosition() {
		return mTempOuputPosition;
	}
	public void setmTempOuputPosition(int mTempOuputPosition) {
		this.mTempOuputPosition = mTempOuputPosition;
	}
	public BitSet getTempBitSet() {
		return mTempBitSet;
	}
	public void setTempBitSet(BitSet mBitSet) {
		this.mTempBitSet = mBitSet;
	}
	//-------------------------------------------------------------------------

	public String toString()
	{
		return new String("Processed: " + GetIdProcessed() + ", Id: " + mId + ", BitMapSet: " + mIndexBitSet);
	}
}
