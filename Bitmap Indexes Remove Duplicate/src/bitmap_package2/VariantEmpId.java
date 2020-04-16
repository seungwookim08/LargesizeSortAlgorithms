package bitmap_package2;

public class VariantEmpId extends Variant
{
	private int mEmpId;
	
	public VariantEmpId(int aEmpId)
	{
		super(VariantType.EMP_ID, false);
		mEmpId=aEmpId;
	}

	public VariantEmpId()
	{
		super(VariantType.EMP_ID, false);
	}

	public int GetEmpId()
	{
		return mEmpId;
	}
	
	public String toString()
	{
		return new String("Field type=EMP_ID");		
	}
}
