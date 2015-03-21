import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;


public class cleanUpRepo extends Thread {
	private static int id = 1;
	private static String fullName = "";
    public void run(){
//       System.out.println("MyThread running");
       id = searchTest.number;
       fullName = searchTest.curFullName;
       try {
    	   mv_clean_folder();
    	   cleanUp();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       uploadFileToS3 upload = new uploadFileToS3();
       upload.setFullName_propertyPath(fullName);
       try {
			upload.startUpload();
		} catch (AmazonClientException | IOException
				| InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
    }
   
	static void mv_clean_folder () throws IOException{
    	Runtime rt = Runtime.getRuntime();
	    rt.exec("mkdir temp" + id);
    	listf(System.getProperty("user.dir") + "/" + fullName);
    }
    
    public static void listf(String directoryName) throws IOException {
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
            	if (isJava(file)){
            		moveFile(file);
            	}
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath());
            }
        }
    }
    
    public static boolean isJava (File file){
    	String filename = file.getName();
    	String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
    	String java = "java";
    	if (extension.equals(java) ){
//    		System.out.println(file.getName());
    		return true;
    	}
    	return false;
    }
    
    public static void moveFile(File file) throws IOException{
    	Runtime rt = Runtime.getRuntime();
    	rt.exec("cp " + file.getAbsolutePath() + " " + System.getProperty("user.dir") + "/temp" + id);
    }
    
    public static void cleanUp() throws IOException, InterruptedException{
    	Runtime rt = Runtime.getRuntime();
//    	System.out.println("mv temp" + id + " " + id);
    	Process pr = rt.exec("rm -rf " + fullName);
    	pr.waitFor();
    	rt.exec("mv temp" + id + " " + fullName);
    }
    
 }
