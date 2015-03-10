package httpClient;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;







public class HttpClient {

	//public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
	//}
	
	  public enum entryType{
		  AUTHER, TAG, IMAGE, IMAGETAG
		  
	  }
	
	  private List<String> cookies;
	  
	  Hashtable <String, Integer> tags = null;
	  
	 
	  private final String USER_AGENT = "Mozilla/5.0";
	 
	  public static void main(String[] args) throws Exception {
		  
		  
		  
		  
		  
		  HttpClient http = new HttpClient();
		  
		  
		  
		  H2server server = http.new H2server();
		  server.connect("~/test");
		  
		  server.init();
		  server.clearAllData();
		  
		  server.addEntry(entryType.TAG, 1, "tagname", false);
		  server.addEntry(entryType.AUTHER, 2, "authername");
		  server.addEntry(entryType.IMAGE, 3, "imagename", -1, "location", 2);
		  server.addEntry(entryType.IMAGETAG, 3, 1);
		  //server.printAll();
		  
		  System.out.println( server.ifExistbyName(entryType.AUTHER, "authername"));
		  System.out.println( server.ifExistbyName(entryType.IMAGE, "imagename"));
		  System.out.println( server.ifExistbyName(entryType.TAG, "tagname"));
		  
		  System.out.println( server.ifExistbyID(entryType.AUTHER, 2));
		  System.out.println( server.ifExistbyID(entryType.IMAGE, 3));
		  System.out.println( server.ifExistbyID(entryType.TAG, 1));
		  System.out.println( server.ifExistbyID(entryType.IMAGETAG, 1, 1));
		  
		  
		  server.disconnect();
		  /*
		  http. logon();
		  
		  int unTagged = http.procBookmark();
		  
		  System.out.println("untagged bookmarks is "+unTagged);
		  
		  http.procTag(unTagged);
		  */
		  
		
	  }
	  
	  /***********************
	   * Input: jsoup doc
	   * Compare the recommended tags and the saved tags
	   * Return a list of tag that should be add to the image
	   ***********************/
	  
	  private List<String> prepareTag(Document doc)
	  {
		
		  if (this.tags == null)
			  return null;
		  
		  List<String> ret = new ArrayList<String>();
		  
		  Element tempcloudTags = doc.select("div.recommend-tag").first();
		  	  
		  Elements cloudTags = tempcloudTags.children().get(1).children();
		  for (Element cloudTag : cloudTags)
		  {
			  String tagName = cloudTag.text();
			  tagName = tagName.replace("*", "");
			  //System.out.println(tagName);
			  if (this.tags.containsKey(tagName))
			  {
				  
				  ret.add(tagName);
				  this.tags.put(tagName, this.tags.get(tagName)-1);
				  
				  
			  }
			  
		  }
		  
		  return ret;
		  
	  }
	  
	  
	  
	  /*****************************
	   * return the post data in string format
	   * 
	   * **************************/	  
	  private String prepareForm(Document doc, List<String> tagsInput) throws Exception 
	  {
		  
		  Element layout_body = doc.select("div.layout-body").first();
		  Element Section = layout_body.children().first();
		  Elements form = Section.getElementsByTag("input");
		  List<String> paramList = new ArrayList<String>();
		  
		  StringBuilder tagCompile = new StringBuilder();
		  for (String curTag : tagsInput)
		  {
			  
			  if (tagCompile.length()!=0)
				  tagCompile.append("+");
			  tagCompile.append( URLEncoder.encode(curTag, "UTF-8"));
			  
			  
		  }
		  
		  
		  for (Element inputElement : form)
		  {
			  	String key = inputElement.attr("name");
				String value = inputElement.attr("value");
				
				if (key.equals("tag"))
				{
					value = tagCompile.toString();
					paramList.add(key + "=" + value);
					continue;
				}
				if (key == "")
					continue;
				if (key.equals("restrict") && value.equals("1"))
					continue;
					
				
				paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
			  
			  
		  }
		  

		  
		  StringBuilder result = new StringBuilder();
			for (String param : paramList) {
				if (result.length() == 0) {
					result.append(param);
				} else {
					result.append("&" + param);
				}
			}
			return result.toString();
	  }
	  
	  /*************************
	   * the main function that process the images
	   * TODO
	   ************************/
	  
	  private void procTag(int untagged) throws Exception {
		  
		  
		  int lastPage = (int)Math.floor(untagged/20.0);
		  String untaggedUrl = "http://www.pixiv.net/bookmark.php?tag=%E6%9C%AA%E5%88%86%E9%A1%9E&p="+ lastPage;
		  System.out.println("untagged = " + untagged);
		  System.out.println("lastPage = " + lastPage);
		  
		  
		  
		  
		  String urlPrefix = "http://www.pixiv.net/";
		  
		  while (lastPage != 0)
		  {
			  String page = this.GetPageContent(untaggedUrl);
			  
			  Document doc = Jsoup.parse(page, "UTF-8");
			  
			  Elements bookmarkedImgs = doc.select("li.image-item");
			  
			  for (Element bookmarkedImg:bookmarkedImgs)
			  {
				  Element link = bookmarkedImg.select("a").get(4);
				  
				  String relHref = link.attr("href");
				  
				  String absHref = urlPrefix + relHref;
				  
				  System.out.println(absHref);
				  
				  String html = this.GetPageContent(absHref);
				  
				  Document docF = Jsoup.parse(html, "UTF-8");
				  
				  List<String> curTags = this.prepareTag(docF);
				  
				  if(curTags.isEmpty())
					  continue;
				  
				  String curForm = this.prepareForm(docF, curTags);
				  
				  Pattern intsOnly = Pattern.compile("\\d+");
				  Matcher makeMatch = intsOnly.matcher(relHref);
				  makeMatch.find();
				  String ID = makeMatch.group();
				  System.out.println(ID);
				  
				  
				  String addBookmark = "http://www.pixiv.net/bookmark_add.php?id=" + ID;
				  
				  this.sendPostRef(addBookmark, curForm, absHref);
				  
				  untagged --;
			  }
			  lastPage--;
		  }
		
	}
	  
	  
	  
	  /*******************
	   * add all the saved tags to the hashtable
	   * 
	   * ****************/
	  private int procBookmark() throws Exception {
		  if (this.tags!= null)
			  this.tags = null;
		  System.gc();
		  this.tags = new Hashtable<String, Integer>();
		  int Untagged = 0;
		  String bookmarkUrl = "http://www.pixiv.net/bookmark.php";
		  String page = this.GetPageContent(bookmarkUrl);
		  //System.out.println(page);
		  Document doc = Jsoup.parse(page, "UTF-8");
		  Element bookmarkList = doc.select("ul.tagCloud").first();
		  
		  Elements tagList = bookmarkList.children();
		  for (Element tag:tagList)
		  {
			  String curtag = tag.child(0).text();
			  System.out.println(curtag);
			  //String [] tagName = curtag.split(Pattern.quote("("));
			  int i = curtag.lastIndexOf("(");
			  String [] tagName = {curtag.substring(0, i), curtag.substring(i+1)};
			  
			  
			  
			  tagName[1] = tagName[1].split(Pattern.quote(")"))[0];
			  if (!(tagName[0].equals("§π§Ÿ§∆") || tagName.equals("Œ¥∑÷Óê")))
			  {
				  this.tags.put(tagName[0], Integer.parseInt(tagName[1]));			  
			  }
			  if (tagName[0].equals("Œ¥∑÷Óê"))
			  {
				  
				  Untagged = Integer.parseInt(tagName[1]);
				  
				  
			  }
			  
			  
			  
		  }
		  
		  
		  return Untagged;
	  }
	  
	  private void logon() throws Exception {
		  
		  	String url = "http://www.pixiv.net/";
			
			 
			//HttpClient http = new HttpClient();
		 
			// make sure cookies is turn on
			CookieHandler.setDefault(new CookieManager());
			
			
			
			// 1. Send a "GET" request, so that you can extract the form's data.
			// initial cookies
			String page = this.GetPageContent(url);
			
			
			String postParams = this.getFormParams( "lhc19910415stuart@126.com", "wjswbhwrhrgb");
		 
			// 2. Construct above post's content and then send a POST request for
			// authentication
			this.sendPost("http://www.pixiv.net/login.php", postParams);
		 
			
			//String result = this.GetPageContent(url);
			//System.out.println(result);
		  
		  
	  }
	 
	  private void sendPost(String url, String postParams) throws Exception {
	 
		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
	 
		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "www.pixiv.net");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
			"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,zh-CN;q=0.4");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "http://www.pixiv.net/");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
	 
		conn.setDoOutput(true);
		conn.setDoInput(true);
	 
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();
	 
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);
	 
		BufferedReader in = 
	             new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
	 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		//System.out.println(response.toString());
	 
	  }
	  
	  
	  private void sendPostRef(String url, String postParams, String referer) throws Exception {
			 
			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		 
			// Acts like a browser
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Host", "www.pixiv.net");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Origin", "www.pixiv.net");
			conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,zh-CN;q=0.4");
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Referer", referer);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
		 
			conn.setDoOutput(true);
			conn.setDoInput(true);
		 
			// Send post request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();
		 
			int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + postParams);
			System.out.println("Response Code : " + responseCode);
		 
			BufferedReader in = 
		             new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
		 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//System.out.println(response.toString());
		 
		  }
	  
	  
	 
	  private String GetPageContent(String url) throws Exception {
	 
		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
	 
		// default is GET
		conn.setRequestMethod("GET");
	 
		conn.setUseCaches(false);
	 
		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
	 
		BufferedReader in = 
	            new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
	 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	 
		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
	 
		return response.toString();
	 
	  }
	 
	  /*************************
	   * use for login
	   * 
	   * 
	   * @param username
	   * @param password
	   * @return
	   * @throws UnsupportedEncodingException
	   */
	  public String getFormParams(String username, String password)
			throws UnsupportedEncodingException {
	 
		System.out.println("Extracting form's data...");
		/*
		Document doc = Jsoup.parse(html);
	 
		// Google form id
		
		
		Element loginform = doc.getElementById("gaia_loginform");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
	 
			if (key.equals("Email"))
				value = username;
			else if (key.equals("Passwd"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}*/
		
		List<String> paramList = new ArrayList<String>();
		paramList.add("mode" + "=" + URLEncoder.encode("login", "UTF-8"));
		paramList.add("return_to" + "=" + URLEncoder.encode("/", "UTF-8"));
		paramList.add("pixiv_id" + "=" + URLEncoder.encode(username, "UTF-8"));
		paramList.add("pass" + "=" + URLEncoder.encode(password, "UTF-8"));
		paramList.add("skip" + "=" + URLEncoder.encode("1", "UTF-8"));
	 
		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	  }
	 
	  public List<String> getCookies() {
		return cookies;
	  }
	 
	  public void setCookies(List<String> cookies) {
		if (cookies == null) return;
		this.cookies = cookies;
		System.out.println("cookie set to");
		System.out.println(cookies);
	  }
	  
	  
	  private class H2server
	  {
		  
		  Connection con;
		  Statement stmt;
		  
		  
		  
		  
		  private boolean connect(String location)
		  {
			  
			  try 
			  {
				  	/*Class.forName("org.h2.Driver");
				  	Connection con = DriverManager.getConnection("jdbc:h2:" + location, "name", "" );	//"jdbc:h2:./test", "test", "" 
				  	stmt = con.createStatement();*/
				  	Class.forName("org.h2.Driver");
		            con = DriverManager.getConnection("jdbc:h2:./test", "test", "" );
		            stmt = con.createStatement();
				  
				  	return true;
			  }
			  catch( Exception e )
		      {
		            System.out.println( e.getMessage() );
		            return false;
		      }
			  
			  
		  }
		  
		  private void disconnect()
		  {
			  
			  try
			  {
				  
				  stmt.close();
		            con.close();
				  
			  }
			  catch( Exception e )
		      {
		            System.out.println( e.getMessage() );
		            
		      }
			  
		  }
		  
		  private boolean init()
		  {
			  
			  if (con == null) return false;
			  
			  try
			  {
				  String createTableIfNotExist = "CREATE TABLE IF NOT EXISTS ";
		            
		            stmt.executeUpdate( createTableIfNotExist
							  +"Auther("
							  + "A_ID INT, "
							  + " PRIMARY KEY(A_ID), "
							  + "NAME VARCHAR(255))");
		            
					  stmt.executeUpdate( createTableIfNotExist +
							  "Image ("
							  + "I_ID INT , "
							  + "PRIMARY KEY(I_ID), "
							  + "NAME VARCHAR(255), "
							  + "SIZE INT, "
							  + "LOCATION VARCHAR(255), "
							  + "A_ID INT, "
							  + "FOREIGN KEY (A_ID) REFERENCES Auther(A_ID))" );
					  stmt.executeUpdate( createTableIfNotExist
							  +"Tag("
							  + "T_ID INT , "
							  + "PRIMARY KEY (T_ID), "
							  + "T_NAME VARCHAR(255), "
							  + "REG BOOLEAN)");
					  
					  
					  stmt.executeUpdate( createTableIfNotExist
							  +"ImageTag("
							  + "I_ID INT, "
							  + " FOREIGN KEY(I_ID) REFERENCES Image(I_ID), "
							  + "T_ID INT,"
							  + " FOREIGN KEY (T_ID) REFERENCES Tag(T_ID))");
				  
			  }
			  
			  catch(Exception e)
			  {
				  	System.out.println( e.getMessage() );
		            return false;
				  
				  
			  }
			  
			  return true;
		  }
		  /*********************
		   * @AUTHERAUTHER: int A_ID, string name 
		   * @IMAGE: int I_ID, string name, int size, string location, int A_ID
		   * @TAG: int T_ID, String T_NAME, bool REG
		   * @IMAGETAG: int I_ID, int T_ID
		   * 
		   */
		  private void addEntry (entryType nameOfTable, Object...obs) throws Exception
		  {
			  
			  
			  StringBuilder inputsb = new StringBuilder();
			  inputsb.append("INSERT INTO ");
			  int initialLength = inputsb.length();
			  
			  switch (nameOfTable)
			  {
			  case AUTHER:
				  
				  for (Object ob : obs)
				  {
					  if (inputsb.length()!=initialLength)
					  {
						  inputsb.append(", ");
					  }else
					  {
						  inputsb.append("AUTHER VALUES (");
						  
					  }
					  
					  if (ob instanceof String)
					  {
						  inputsb.append(String.format("'%s'", ob));
						  
					  }else
					  inputsb.append(ob);
					  
					  
				  }
				  
				  break;
				  
			  case IMAGE:
				  
				  for (Object ob : obs)
				  {
					  if (inputsb.length()!=initialLength)
					  {
						  inputsb.append(", ");
					  }else
					  {
						  inputsb.append("IMAGE VALUES (");
						  
					  }
					  if (ob instanceof String)
					  {
						  inputsb.append(String.format("'%s'", ob));
						  
					  }else
					  inputsb.append(ob);
					  
					  
				  }
				  break;
				  
			  case TAG:
				  
				  for (Object ob : obs)
				  {
					  if (inputsb.length()!=initialLength)
					  {
						  inputsb.append(", ");
					  }else
					  {
						  inputsb.append("TAG VALUES (");
						  
					  }
					  if (ob instanceof String)
					  {
						  inputsb.append(String.format("'%s'", ob));
						  
					  }else
					  inputsb.append(ob);
					  
					  
				  }
				  
				  break;
				  
			  case IMAGETAG:
				  
				  for (Object ob : obs)
				  {
					  if (inputsb.length()!=initialLength)
					  {
						  inputsb.append(", ");
					  }else
					  {
						  inputsb.append("IMAGETAG VALUES (");
						  
					  }
					  if (ob instanceof String)
					  {
						  inputsb.append(String.format("'%s'", ob));
						  
					  }else
					  inputsb.append(ob);
					  
					  
				  }
				  
				  break;
				  
			  default:
				  break;
			  	
			  
			  
			  }
			  inputsb.append(")");
			  String command = inputsb.toString();
			  System.out.println(command);
			  stmt.executeUpdate(command);
			  
		  }
		  
		  
		  /***
		   * 
		   * use for debug
		   * @throws Exception
		   */
		  private void clearAllData() throws Exception
		  {
			  stmt.executeUpdate("DELETE  FROM IMAGETAG");
			  stmt.executeUpdate("DELETE  FROM IMAGE");
			  stmt.executeUpdate("DELETE  FROM AUTHER");
			  stmt.executeUpdate("DELETE  FROM TAG");
			  
			  
		  }
		  /*******
		   * 
		   * use for debug
		   * @throws Exception
		   */
		  private void printAll() throws Exception
		  {
			  	System.out.println( "Table image" );
			  	ResultSet rs = stmt.executeQuery("SELECT * FROM IMAGE");
	            while( rs.next() )
	            {
	                
	                int iid = rs.getInt("I_ID");
	                System.out.print( iid  +" ");
	                
	                String name = rs.getString("NAME");
	                System.out.print( name +" " );
	                
	                int size = rs.getInt("size");
	                System.out.print( size  +" ");
	                
	                String location = rs.getString("location");
	                System.out.print( location  +" ");
	                
	                int aid = rs.getInt("A_ID");
	                System.out.println( aid );
	            }
	            
	            
	            System.out.println( "Table auther" );
			  	rs = stmt.executeQuery("SELECT * FROM AUTHER");
	            while( rs.next() )
	            {
	                
	                int aid = rs.getInt("A_ID");
	                System.out.print( aid  +" ");
	                
	                String name = rs.getString("NAME");
	                System.out.println( name );
	                
	               
	                
	                
	            }
	            
	            
	            System.out.println( "Table tag" );
			  	rs = stmt.executeQuery("SELECT * FROM TAG");
	            while( rs.next() )
	            {
	                
	                int aid = rs.getInt("T_ID");
	                System.out.print( aid  +" ");
	                
	                String name = rs.getString("T_NAME");
	                System.out.print( name  +" ");
	                
	                boolean reg = rs.getBoolean("REG");
	                System.out.println( reg );
	                
	                
	            }
	            
	            
	            System.out.println( "Table imagetag" );
			  	rs = stmt.executeQuery("SELECT * FROM IMAGETAG");
	            while( rs.next() )
	            {
	                
	            	int Iid = rs.getInt("I_ID");
	                System.out.print( Iid +" ");
	            	
	                int Tid = rs.getInt("T_ID");
	                System.out.println( Tid );
	                
	                
	                
	                
	            }
	            
			  
			  
		  }
		  
		  private boolean ifExistbyName(entryType table, String name) throws Exception
		  {
			  
			  StringBuilder inputsb = new StringBuilder();
			  
			  inputsb.append("SELECT * FROM ");
			  switch (table)
			  {
			  case AUTHER:
				  inputsb.append("AUTHER WHERE NAME = ");
				  inputsb.append(String.format("'%s'", name));
				  break;
				  
			  case IMAGE:
				  inputsb.append("IMAGE WHERE NAME = ");
				  inputsb.append(String.format("'%s'", name));
				  break;
				  
			  case TAG:
				  inputsb.append("TAG WHERE T_NAME = ");
				  inputsb.append(String.format("'%s'", name));
				  break;
				  
			  case IMAGETAG:
				  return false;
				  
				  
			  default:
				  return false;
			  
			  
			  }
			  ResultSet rs =stmt.executeQuery(inputsb.toString());
			  if (!rs.next() )
				  return false;
			  return true;
		  }
		  
		  private boolean ifExistbyID(entryType table, int...ids) throws Exception
		  {
			  
			  StringBuilder inputsb = new StringBuilder();
			  
			  inputsb.append("SELECT * FROM ");
			  switch (table)
			  {
			  case AUTHER:
				  inputsb.append("AUTHER WHERE A_ID = ");
				  inputsb.append(ids[0]);
				  break;
				  
			  case IMAGE:
				  inputsb.append("IMAGE WHERE I_ID = ");
				  inputsb.append(ids[0]);
				  break;
				  
			  case TAG:
				  inputsb.append("TAG WHERE T_ID = ");
				  inputsb.append(ids[0]);
				  break;
				  
			  case IMAGETAG:
				  inputsb.append("IMAGETAG WHERE I_ID = ");
				  inputsb.append(ids[0]);
				  inputsb.append("AND T_ID = ");
				  inputsb.append(ids[1]);
				  break;
				  
				  
			  default:
				  return false;
			  
			  
			  }
			  ResultSet rs =stmt.executeQuery(inputsb.toString());
			  if (!rs.next() )
				  return false;
			  return true;
		  }
		  
		  
	  }
	  
	  
	  

}





