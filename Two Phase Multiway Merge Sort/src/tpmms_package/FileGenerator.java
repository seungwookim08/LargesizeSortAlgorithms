package tpmms_package;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public class FileGenerator {

	private final String iPath, oPath;
	private final int iTuples, oMultiple;
	private final int mBufferSize = 4096;
		
	public FileGenerator(String iPath, int iTuples, String oPath, int oMultiple) {
		
		this.iPath = iPath;
		this.iTuples = iTuples;
		this.oPath = oPath;
		this.oMultiple = oMultiple;
		
		System.out.println("Input File: " + iPath);
		System.out.println("Input Tuples: " + iTuples);
		System.out.println("Output File: " + oPath);
		System.out.println("Output Multiple: " + oMultiple);		
	}
	
	public void Generate() throws IOException, ParseException
	{	
		File outputFile=new File(oPath);
		EmployeeFileWriter fileWriter = new EmployeeFileWriter(outputFile);				

		Employee lEmployee=null;
		int n =0;
		for(int i=0; i<oMultiple; ++i) {

			EmployeeFileReader fileReader = new EmployeeFileReader(iPath);
			
			while((lEmployee=fileReader.ReadRecord())!=null) {
				n++;
				System.out.println(n + " : " + lEmployee.toString());
				
				lEmployee.SetId(lEmployee.GetId()+i);
				fileWriter.WriteRecord(lEmployee);
			}
			
			fileReader.Close();
		}		
		fileWriter.Close();
	}	
}
