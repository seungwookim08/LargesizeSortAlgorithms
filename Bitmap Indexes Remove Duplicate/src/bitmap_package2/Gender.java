package bitmap_package2;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public enum Gender
{
	M(0), F(1);
	 
	private final int mValue;
	
	private static final Map<Integer, Gender> gIntMapper=new HashMap<Integer, Gender>();
	//private static final Map<Gender, String> gStringMapper=new HashMap<Gender, String>();
    static
    {
        for(Gender lGen: Gender.values())
        {
        	gIntMapper.put(lGen.mValue, lGen);
        }	            
    }

    private Gender(int aValue)
    {
        mValue=aValue;
    }
 
    public int Value()
    {
    	return mValue;
    }
    
    public static Gender FromInt(int aValue) throws ParseException
    {
    	if(gIntMapper.containsKey(aValue)!=true)
    	{
    		throw new ParseException("Invalid end-or-record roken", 0);
    	}
        return gIntMapper.get(aValue);
    }
}
