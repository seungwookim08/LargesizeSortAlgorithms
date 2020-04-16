package bitmap_package2;

public interface EmployeePositionFile {

	public static final int BLOCK_SIZE=4*1024;
	public static final int INDEXPROCESS_SIZE=1;
	public static final int EMPID_SIZE=8;
	public static final int POSITION_SIZE=120;
	public static final int EOR_SIZE=1;
	public static final int INDEX_SIZE= INDEXPROCESS_SIZE + EMPID_SIZE + POSITION_SIZE + EOR_SIZE;	
	public static final int RECORDS_PER_BLOCK=31;
	public static final int USED_BLOCK_SIZE=INDEX_SIZE*RECORDS_PER_BLOCK;	
	
	public void Close();
}
