
import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;

public class WebWorker implements Runnable
{

private Socket socket;
private String URL = "";
private String mimeType = "";


/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;

}



/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      writeHTTPHeader(os,mimeType);
      writeContent(os, URL);// to write content of URL
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
  // String 
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         
       System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
         //to see if it is a GET request
         if (line.substring(0,3).equals("GET")){ 
             URL = line.substring(5).split(" ")[0];
         } 
      }catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return;
}




/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   //create a string for URL and replaces it
   String temp = URL;
   temp = temp.replace("\\", "/");
   
   //to see if URL is a file
   File f = new File(temp);
   Date d = new Date();
   //try{
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write(mimeType.getBytes());
   os.write("test".getBytes());
   try{
   //to see if file is really file
   if(f.isFile()){
      //if it is get this message
      os.write("HTTP/1.1 200 OK\n".getBytes());
   }else{
      //if not it will get this message
      os.write("HTTP/1.1 404 Not Found\n".getBytes());
   }
   
   //look at file
   InputStream type = new FileInputStream(f);
      //htm
      if(temp.endsWith("htm"))
         mimeType = "text/html";
      //html   
      if(temp.endsWith("html"))
         mimeType = "text/html";
      //for gif
      if(temp.endsWith("gif"))
         mimeType = "image/gif";
      //for png
      if(temp.endsWith("png"))
         mimeType = "image/png";
      //for jpeg
      if(temp.endsWith("jpeg"))
         mimeType = "image/jpeg";
       //save contentType in global variable mimeType  
       contentType = mimeType;
       
       
    //test to see content type     
   System.out.print("HTTP/1.0 200 OK\n"+"Content-type: "+contentType+"\n");    
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Aaron's very own server\n".getBytes());
   os.write("Last-Modified: Wed, 18 September 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   }
   catch (FileNotFoundException x) {
        System.out.print("HTTP/1.0 404 Not Found\r\n"+
          "Content-type: text/html\r\n\r\n"+
          "<html><head></head><body>"+f+" not found</body></html>\n");
        os.close();
      }
    
    catch (IOException x) {
      System.out.println(x);
     }
}
/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String tempURL) throws Exception
{
      tempURL = tempURL.replace("/", "\\"); 
      //use to check if tempURL is a file
      File file = new File(tempURL);
      //get file path from file systems and use trim to eliminates leading and trailing spaces
      Path url = FileSystems.getDefault().getPath(tempURL.trim());
      
      SimpleDateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy");
      Date date = new Date();
      
     

         if(file.isFile()){
            byte[] b = Files.readAllBytes(url);
            String fileContents = new String(b, StandardCharsets.UTF_8);

            //date tag
            String dateTag = dateFormat.format(date);
            fileContents = fileContents.replace("<cs371date>", dateTag);

            //server tag
            String serverTag = "Aaron's Server";
            fileContents = fileContents.replace("<cs371server", serverTag);
            //to serve the files
            os.write(b);
            //to serve the tags
            //os.write(fileContents.getBytes());
          
         }
         else{

            // Error page
         ;
            String error = "error.htm";

            byte[] e = Files.readAllBytes(Paths.get(error));
            String fileContents = new String(e, StandardCharsets.UTF_8);

            os.write("<center> Error, check URL </center>".getBytes());
            os.write(fileContents.getBytes());
         }
     
   }//end writeContent
} // end class

