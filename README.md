# TPPMS
This is a project of Two-Phase Merge Multiway Merge-Sort (TPPMS).  
Run Main.java file.  
This program requires some arguments. -i for 2 input files (you can use the sample data in dat forlder) and -o for output file.   
The path should be absolute path (relative path may not work in windows).   
You can create some files, with -f for file generation, -r for the number of rows, and -x for a multiplier of these rows.   
i.e. -f -r 10000 -x 100 will create 1,000,000 tuples. But please note that you need to have 1 input file with -i and 1 output file with -o.   
There is a special parameter -m, which allocates how many memory we use for the list. By default, it is 10% but you can change such as -m 5%. Usually, over 20%, GC error occurs.   