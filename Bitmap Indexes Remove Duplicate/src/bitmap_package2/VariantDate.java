package bitmap_package2;

import bitmap_package2.Variant.VariantType;

public class VariantDate extends Variant
{
	private int mDate;
	
	public VariantDate(int aDate)
	{
		super(VariantType.DATE, true);
		mDate=aDate;
	}

	public VariantDate()
	{
		super(VariantType.DATE, true);
	}
	
	public int GetDate()
	{
		return mDate;
	}
	
	public String toString()
	{
		return new String("Field type=DATE");		
	}
}
