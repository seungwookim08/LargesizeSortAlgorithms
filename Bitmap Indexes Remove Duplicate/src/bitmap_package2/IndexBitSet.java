package bitmap_package2;

import java.util.BitSet;

public class IndexBitSet
{
	public BitSet mBitSet;
	public int mBitsNumber;
	public Object mExtra;
	
	public IndexBitSet()
	{
		mBitSet=new BitSet();
		mBitsNumber=0;
		mExtra=null;
	}
	
	public IndexBitSet(int aInitialBitsNumber)
	{
		mBitSet=new BitSet(aInitialBitsNumber);
		mBitsNumber=aInitialBitsNumber;
	}

	public void fromString(String bitSetIndex)
	{	
	    char[] bitArray=bitSetIndex.toCharArray();

	    int n=mBitsNumber-1;
	    for(char bitValue:bitArray)
	    {
	    	if(bitValue=='1')
	    	{
	    		mBitSet.set(n);
	    	}	    
	    	n--;
	    }
	}

	public void Print()
	{		
		for(int lCounter=0; lCounter<mBitsNumber; ++lCounter)
		{
			if(lCounter<1000)
			{
				if(mBitSet.get(lCounter)==true)
				{
					System.out.print("1");
				}
				else
				{
					System.out.print("0");
				}
			}
		}
	}
	
	public String toString()
	{
		String lResult=new String();
		
		for(int lCounter=mBitsNumber-1; lCounter>=0; --lCounter)
		{
			if(mBitSet.get(lCounter)==true)
			{
				lResult+="1";
			}
			else
			{
				lResult+="0";
			}
		}
		
		return lResult;
	}
}
