import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;

public class mtdd{

	//helper function to get the list of file names in a directory, without directory names
	//returns as a Set of Strings, credit to https://www.baeldung.com/java-list-directory-files
	public static Set<String> listFilesSet(String path) {
    		return Stream.of(new File(path).listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet());
	}

	public static void main(String[] args) throws InterruptedException, IOException{
		SynchronousQueue<String> queue = new SynchronousQueue<>();//pipeline mechanism for communication between threads
		String path1 = "/home/d1";//path of first directory d1
		String path2 = "/home/d2";//path of second directory d2

		Thread t1 = new Thread(new Runnable(){//thread 1, with visibility to d1 directory
			public void run(){
				Set<String> files1 = listFilesSet(path1);//gets all filenames in directory d1
				try{
					queue.put(files1.toString());//sends d1 filenames to thread 2

					String files2 = queue.take();
					//reads in d2 filenames, happends after thread 2 reads d1 filenames and sends d2 filenames
					files2 = files2.substring(1, files2.length()-1);
					String[] filesSplit = files2.replaceAll("\\s", "").split(",");//converts to string array

					for(String file : files1){//reads content of files in d1 and sends to thread 2
						Path filePath = Path.of(path1 + "/" + file);//combines specified directory path and filename
						String fileString = Files.readString(filePath);
						queue.put(fileString);
					}

					for(String file: filesSplit){//takes in content of files in d2 and puts in new files in d1
                                                String fileString = queue.take();
                                                File newFile = new File(path1 + "/" + file);
                                                if(newFile.createNewFile()){//creates new file in d1
                                                        FileWriter writer = new FileWriter(path1 + "/" + file);
                                                        writer.write(fileString);//writes content passed in from thread 2 to new file
                                                        writer.close();
                                                }else{
                                                System.out.println("File not created");
                                                }
                                        }
					System.out.println("Files from d2 have been made and copied in d1");
				} catch (InterruptedException|IOException e){//exception handling
					e.printStackTrace();
				}
			}
		});

		Thread t2 = new Thread(new Runnable(){//thread 2, with visibility to d2 directory
        	        public void run(){
        	                Set<String> files2 = listFilesSet(path2);//gets all filenames in directory d2
				try{
                                        String files1 = queue.take();//reads in d1 filenames, queue waits for thread 1 to send first
					files1 = files1.substring(1, files1.length()-1);
					String[] filesSplit = files1.replaceAll("\\s", "").split(",");//converts to string array
					queue.put(files2.toString());//sends d2 filenames to thread 1

					for(String file: filesSplit){//takes in content of files in d1 and puts in new files in d2
						String fileString = queue.take();
						File newFile = new File(path2 + "/" + file);
						if(newFile.createNewFile()){//creates new file in d2
							FileWriter writer = new FileWriter(path2 + "/" + file);
							writer.write(fileString);//writes content passed in from thread 1 to new file
							writer.close();
						}else{
						System.out.println("File not created");
						}
					}
					for(String file : files2){//reads content of files in d2 and sends to thread 1
                                                Path filePath = Path.of(path2 + "/" + file);//combines specified directory path and filename
                                                String fileString = Files.readString(filePath);
                                                queue.put(fileString);
                                        }
					System.out.println("Files from d1 have been made and copied in d2");
                                } catch (InterruptedException|IOException e){//exception handling
                                        e.printStackTrace();
                                }
        	        }
        	});

		t1.start();//starts thread 1 in main
		t2.start();//starts thread 2 in main

		//order of events, t1 sends filenames of d1 to t2, t2 takes in and stores
		//t2 sends filenames of d2 to t1, t1 takes in and stores
		//t1 sends content of each file in filenames of d1 to t2, t2 creates new file with next filename in stored array
		//and inputs the same content into the same filename
		//t2 sends content of each file in filenames of d2 to t1, and t1 does the same
		//at the end both directories are identical
	}
}
