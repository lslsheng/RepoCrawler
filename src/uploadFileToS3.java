import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;


public class uploadFileToS3 {
	private static String bucketName     = "";
	private static String uploadFileName = "";
	private static String access_key_id = "";
	private static String secret_access_key = "";
	private static String propertyPath = "";
	private static String name = "";
	private static String bucketPath = "";
	
	public static void startUpload() throws IOException, AmazonServiceException, AmazonClientException, InterruptedException {
		setUpClass();
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key_id, secret_access_key);
		// Each instance of TransferManager maintains its own thread pool
		// where transfers are processed, so share an instance when possible
		TransferManager tx = new TransferManager(awsCreds);
		// The upload and download methods return immediately, while
		// TransferManager processes the transfer in the background thread pool
		MultipleFileUpload upload = tx.uploadDirectory(bucketName, bucketPath + name, new File(uploadFileName), true);
		// While the transfer is processing, you can work with the transfer object
		upload.waitForCompletion();
		 
		// After the upload is complete, call shutdownNow to release the resources.
		tx.shutdownNow();
	}
	
	public void setFullName_propertyPath (String fullName){
		name = fullName;
		uploadFileName = System.getProperty("user.dir") + "/" +  fullName;
		propertyPath = searchTest.propertyPath; 
	}
	
	
	public static void setUpClass()
	{
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertyPath);
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			access_key_id = prop.getProperty("accessKey");
			secret_access_key = prop.getProperty("secretKey");
			bucketName = prop.getProperty("bucketName");
			bucketPath = prop.getProperty("bucketPath");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

