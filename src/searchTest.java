import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class searchTest {
	public static int number = 0;
	public static String curFullName = "";
	private static int starLimit = 0;
	public static String propertyPath = "";

	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, JSONException {  
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
	      Calendar start = Calendar.getInstance();
	      System.out.println("start date:");
	      start.setTime(sdf.parse(ReadLine()));
	      Calendar end = Calendar.getInstance();
	      System.out.println("end date:");
	      end.setTime(sdf.parse(ReadLine()));
	      System.out.println("property path:");
	      propertyPath = ReadLine();
	      
	      
	      // run it day by day
	      for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
	          String dateStr = dateToString(date);
	      	  System.out.println(dateStr);
	      	  getRepoList(dateStr);
	      }	
   }
   
    
  
    static void getRepoList(String Date) throws IOException, JSONException{
    	URL url = new URL("https://api.github.com/search/repositories?q=%20%20created%3A" + Date + "%20language%3Ajava%20stars:>" + starLimit);
//    	URL url = new URL("https://api.github.com/search/repositories?q=%20%20created%3A" + Date + "%20language%3Ajava");
		
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
    	for (int i = 0; i < arr.length(); i++)
    	{
    	    String fullName = arr.getJSONObject(i).getString("full_name");
    	    String Url = arr.getJSONObject(i).getString("html_url");
    	    String name = arr.getJSONObject(i).getString("name");
    	    System.out.println(fullName + " is under checking!");
  	        if(checkLicense(fullName)){
	  	        Runtime rt = Runtime.getRuntime();
	  	        Process pr = rt.exec("git clone " + Url);
	  	        if (pr.waitFor() == 0){
	  	        	number ++;
	  	        	System.out.println("Downloading " + fullName + " it's " + number);
	  	        	rt.exec("mv " + name + " " + fullName.replace("/", "-"));
	  	        	curFullName = fullName.replace("/", "-");
	  	        	cleanUpRepo thread = new cleanUpRepo();
		  	        thread.start();
	  	        }  
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
			return jsonParserForLicense(br2);
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
   
    public static String ReadLine () throws java.io. IOException {
	    String str = "";
	    char c;
	    while ((c=(char)System.in.read ()) != '\n') {
	    	str += c;
	    }
	    return str;
    }
    
}
