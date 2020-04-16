package bitmap_package2;

public class VariantGender extends Variant
{
	private Gender mGender;
	
	public VariantGender(Gender aGender)
	{
		super(VariantType.GENDER, true);
		mGender=aGender;
	}
	
	public VariantGender()
	{
		super(VariantType.GENDER, true);
	}
	
	public Gender GetGender()
	{
		return mGender;
	}	
	
	public String toString()
	{
		return new String("Field type=GENDER");		
	}
}
