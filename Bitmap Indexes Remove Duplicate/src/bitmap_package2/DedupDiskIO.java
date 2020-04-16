package bitmap_package2;

public class DedupDiskIO {

	public int supportingFilesIDRecordsRead = 0;
	public int supportingFilesIDDiskRead = 0;
	public int supportingFilesIDRecordsWrite = 0;
	public int supportingFilesIDDiskWrite = 0;

	public int supportingFilesDateRecordsRead = 0;
	public int supportingFilesDateDiskRead = 0;
	public int supportingFilesDateRecordsWrite = 0;
	public int supportingFilesDateDiskWrite = 0;

	public int deduplicateRecordsRead = 0;
	public int deduplicateDiskRead = 0;
	public int deduplicateRecordsWrite = 0;
	public int deduplicateDiskWrite = 0;
	
	
	public int GetTotalRecordsRead() {
		return supportingFilesIDRecordsRead+supportingFilesDateRecordsRead+deduplicateRecordsRead;
	}

	public int GetTotalDiskRead() {
		return supportingFilesIDDiskRead+supportingFilesDateDiskRead+deduplicateDiskRead;
	}

	public int GetTotalRecordsWrite() {
		return supportingFilesIDRecordsWrite+supportingFilesDateRecordsWrite+deduplicateRecordsWrite;
	}
	
	public int GetTotalDiskWrite() {
		return supportingFilesIDDiskWrite+supportingFilesDateDiskWrite+deduplicateDiskWrite;
	}	

}
