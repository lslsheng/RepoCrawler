import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;


public class CleanUpRepo extends Thread {
	private int id;
	private String fullName;
	private String repoPath;
	
	private final static Logger LOGGER = Logger.getLogger(CleanUpRepo.class.getName());
	
	public CleanUpRepo(int id, String fullName, String repoPath) {
		this.id = id;
		this.fullName = fullName;
		this.repoPath = repoPath;
	}
	
    public void run(){
//       System.out.println("MyThread running");
//       id = SearchTest.number;
//       fullName = SearchTest.curFullName;
//       try {
//    	   this.mv_clean_folder();
//    	   this.cleanUp();
//		} catch (IOException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
       UploadFileToS3 upload = new UploadFileToS3();
       upload.setFullName_propertyPath(this.repoPath, this.fullName);
       try {
			upload.startUpload();
		} catch (AmazonClientException | IOException
				| InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
    }
   
	private void mv_clean_folder() throws IOException{
    	Runtime rt = Runtime.getRuntime();
    	String tempDir = this.repoPath + "/temp" + this.id;
	    rt.exec("mkdir " + tempDir);
	    LOGGER.info(this.fullName + ": making temp directory at: " + tempDir);
    	listf(System.getProperty("user.dir") + "/" + this.repoPath + "/" + this.fullName);
    }
    
    public void listf(String directoryName) throws IOException {
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
            	if (isJava(file)){
            		this.moveFile(file);
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
    
    public void moveFile(File file) throws IOException{
    	Runtime rt = Runtime.getRuntime();
    	rt.exec("cp " + file.getAbsolutePath() + " " + System.getProperty("user.dir") + "/" + this.repoPath + "/temp" + this.id);
    }
    
    public void cleanUp() throws IOException, InterruptedException{
    	Runtime rt = Runtime.getRuntime();
//    	System.out.println("mv temp" + id + " " + id);
    	String tempDir = this.repoPath + "/temp" + this.id;
    	String repoDir = this.repoPath + "/" + this.fullName;
    	Process pr = rt.exec("rm -rf " + repoDir);
    	pr.waitFor();
    	rt.exec("mv " + tempDir + " " + repoDir);
    	LOGGER.info(this.fullName + ": moving temp directory: " + tempDir + ", to repo directory: " + repoDir);
    }
    
 }
