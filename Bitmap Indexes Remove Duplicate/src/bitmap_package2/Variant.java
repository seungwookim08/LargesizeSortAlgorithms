package bitmap_package2;

public abstract class Variant
{	
	public static enum VariantType
	{
		EMP_ID,
		GENDER,
		DEPT_ID,
		DATE
	};
	
	private VariantType mVariantType;
	private boolean mIsDenseIndex;
	private int mBitMapSize;
	
	protected Variant(VariantType aVariantType)
	{
		mVariantType=aVariantType;
		mIsDenseIndex=false;
	}

	protected Variant(VariantType aVariantType, boolean aIsDenseIndex)
	{
		mVariantType=aVariantType;
		mIsDenseIndex=aIsDenseIndex;
	}

	public VariantType GetVariantType()
	{
		return mVariantType;
	}

	public boolean IsDenseIndex()
	{
		return mIsDenseIndex;
	}
	
	public int GetBitMapSize()
	{
		return mBitMapSize;
	}
	public void SetBitMapSize(int aBitMapSize)
	{
		mBitMapSize=aBitMapSize;
	}
	
	public abstract String toString();
}
