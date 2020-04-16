package bitmap_package2;

public class VariantDept extends Variant
{
	private int mDept;
	
	public VariantDept(int aDept)
	{
		super(VariantType.DEPT_ID, true);
		mDept=aDept;
	}

	public VariantDept()
	{
		super(VariantType.DEPT_ID, true);
	}
	
	public int GetDept()
	{
		return mDept;
	}
	
	public String toString()
	{
		return new String("Field type=DEPT");		
	}
}
