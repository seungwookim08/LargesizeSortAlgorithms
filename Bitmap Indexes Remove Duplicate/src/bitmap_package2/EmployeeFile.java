package bitmap_package2;

import java.text.SimpleDateFormat;

public interface EmployeeFile
{	
	public static final int EMPID_SIZE=8;
	public static final int DATE_SIZE=10;
	public static final int NAME_SIZE=25;
	public static final int GEN_SIZE=1;
	public static final int DEPT_SIZE=3;
	public static final int SOCNUM_SIZE=9;
	public static final int ADDRESS_SIZE=44;//RA: 43 as per project description, yet 44 is in sample file
	public static final int EOR_SIZE=1;//RA: terminator character sequence size - in our case sequence is '\n'	
	public static final int RECORD_SIZE=EMPID_SIZE+DATE_SIZE+NAME_SIZE+GEN_SIZE+DEPT_SIZE+SOCNUM_SIZE+ADDRESS_SIZE+EOR_SIZE;
	public static final int BLOCK_SIZE=4*1024;
	public static final int RECORDS_PER_BLOCK=40;
	public static final int USED_BLOCK_SIZE=RECORD_SIZE*RECORDS_PER_BLOCK;	
	
	public static final SimpleDateFormat DATE_FORMATTER=new SimpleDateFormat("yyyy-MM-dd");
	
	public void Close();
}
