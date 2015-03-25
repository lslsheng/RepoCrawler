import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class SearchTest {
	private static int STAR_LIMIT = 10;
	public static String REPO_PATH = "repos";
	public static String propertyPath = "";

	private final static Logger LOGGER = Logger.getLogger(SearchTest.class.getName());
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, JSONException {  
		  
		  if (args.length > 3) {
			  LOGGER.warning("Proper use: java -jar repoCrawler.jar 2010.01.01 2010.01.01 S3.properties");
		  }
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
	      Calendar start = Calendar.getInstance();
	      start.setTime(sdf.parse(args[0]));
	      Calendar end = Calendar.getInstance();
	      end.setTime(sdf.parse(args[1]));
	      propertyPath = args[2];
	      
	      // run it day by day
	      for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
	          String dateStr = dateToString(date);
	      	  LOGGER.info(dateStr);
	      	  getRepoList(dateStr);
		      try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("finished.log", true)))) {
		        out.println(dateStr);
		      } catch (IOException e) {
			    e.printStackTrace();
		      }
	      }	
   }
   
    
  
    static void getRepoList(String Date) throws IOException, JSONException{
    	URL url = new URL("https://api.github.com/search/repositories?q=%20%20created%3A" + Date + "%20language%3Ajava%20stars:>" + STAR_LIMIT);
//    	URL url = new URL("https://api.github.com/search/repositories?q=%20%20created%3A" + Date + "%20language%3Ajava");
    	
    	LOGGER.info("Searching for repos: " + url);
		
    	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		try {
			jsonParserForRepoList(br);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    }
    
    static void jsonParserForRepoList(BufferedReader br) throws JSONException, IOException, InterruptedException{
    	String message = IOUtils.toString(br);
    	JSONObject obj = new JSONObject(message);
    	JSONArray arr = obj.getJSONArray("items");
    	LOGGER.info("Found " + arr.length() + " GitHub repos.");
    	for (int i = 0; i < arr.length(); i++)
    	{
    	    String fullName = arr.getJSONObject(i).getString("full_name");
    	    String Url = arr.getJSONObject(i).getString("html_url");
    	    
    	    LOGGER.info(fullName + ": checking for license...");
  	        if(checkLicense(fullName)){
  	        	LOGGER.info(fullName + ": found license.");
	  	        Runtime rt = Runtime.getRuntime();
	  	        fullName = fullName.replace("/", "-");
	  	        String repoDir = REPO_PATH + "/" + fullName;
	  	        Process pr = rt.exec("git clone " + Url + " " + repoDir);
  	        	LOGGER.info(fullName + ": cloning into " + repoDir + "...");
	  	        int gitWaitForCode = pr.waitFor();
	  	        if (gitWaitForCode == 0){
	  	        	int timestamp = (int) (System.currentTimeMillis() / 1000L);
	  	        	CleanUpRepo thread = new CleanUpRepo(timestamp, fullName, REPO_PATH);
		  	        thread.start();
	  	        } else {
	  	        	LOGGER.warning(fullName + ": cloning into " + repoDir + " exited with error " + gitWaitForCode);
	  	        }
  	        } else {
  	        	LOGGER.info(fullName + ": could not find license.");
  	        }
	  	    try {
	  	        Thread.sleep(10000);                
	  	    } catch(InterruptedException ex) {
	  	        Thread.currentThread().interrupt();
	  	    }
    	}
    }
    
    
    static boolean checkLicense (String query){
    	try {
			URL url = new URL("https://api.github.com/search/code?q=Apache+License%2Brepo%3A" + query + "+path:/");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			if (jsonParserForLicense(br))
				return true;
			URL url2 = new URL("https://api.github.com/search/code?q=MIT+License%2Brepo%3A" + query + "+path:/");
			BufferedReader br2 = new BufferedReader(new InputStreamReader(url2.openStream()));
			if (jsonParserForLicense(br2))
				return true;
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
    }
    
    static boolean jsonParserForLicense(BufferedReader br) throws JSONException, IOException, InterruptedException{
    	Thread.sleep(6000); 
    	String message = IOUtils.toString(br);
    	JSONObject obj = new JSONObject(message);
    	JSONArray arr = obj.getJSONArray("items");
    	for (int i = 0; i < arr.length(); i++)
    	{
    	    String fileName = arr.getJSONObject(i).getString("name");
    	    if (fileName.toLowerCase().contains("license"))
    	    	return true;
    	}
    	return false;
    }
    
    @SuppressWarnings("deprecation")
	static String dateToString(Date date){
		 int year = date.getYear()+1900;
         int month = date.getMonth() + 1;
         int day = date.getDate();
         String dateStr = year+"-";
         if (month<10)
         	dateStr += "0"+month+"-";
         else 
         	dateStr += month+"-";
         if (day<10)
         	dateStr += "0"+day;
         else 
         	dateStr += day;
    	return dateStr;
    }
}
