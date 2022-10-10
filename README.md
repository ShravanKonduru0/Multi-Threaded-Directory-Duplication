# Multi-Threaded-Directory-Duplication
This program uses two threads to copy the contents of one directory to the other and vice versa such that both directories are identical

This program is mainly to demonstrate the use of two threads in java and preventing deadlocks that could occur when trying to access the same directories
paths to directory 1 and directory 2 can be changed in variables path1 and path2. This program only duplicates files and not subdirectories. At the end, both directories will have identical files, assuming both directories have unique file names.


order of events, t1 sends filenames of d1 to t2, t2 takes in and stores  
t2 sends filenames of d2 to t1, t1 takes in and stores  
t1 sends content of each file in filenames of d1 to t2, t2 creates new file with next filename in stored array  
and inputs the same content into the same filename  
t2 sends content of each file in filenames of d2 to t1, and t1 does the same  
at the end both directories are identical  
