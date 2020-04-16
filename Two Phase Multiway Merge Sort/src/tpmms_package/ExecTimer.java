package tpmms_package;

import java.lang.System;

public class ExecTimer
{
	private long mStartTimestamp;
	
	public ExecTimer()
	{
		mStartTimestamp=0;
	}
	
	public void Start()
	{
		mStartTimestamp=System.currentTimeMillis();
	}
	
	public long GetTime()
	{
		return System.currentTimeMillis()-mStartTimestamp;
	}
}
