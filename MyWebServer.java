import java.io.*;
import java.net.*;

public class MyWebServer {
    public static void main(String a[]) throws IOException {                        
        int q_len = 6;                                                             
        int port = 2540;                                                            
        Socket sock;                                                              

        ServerSocket servsock = new ServerSocket(port, q_len);   //open connection at port 2540                   
        System.out.println("MyWebServer starting up, listening at port 2540. \n");
        while (true) {
            sock = servsock.accept();      //block and accept client browser                                        
            new ListenWorker(sock).start();   //start the listener thread                                           
        }
    }
    
}

class ListenWorker extends Thread {    
    Socket sock;                   
    ListenWorker (Socket s) {sock = s;} //constructor
    //to local sock
    public void run(){
	//instantize the input and output variables
	PrintStream writer = null;
	BufferedReader reader = null;
	try {
	    writer = new PrintStream(sock.getOutputStream());
        reader= new BufferedReader
	  (new InputStreamReader(sock.getInputStream()));
      
      String request=reader.readLine();//accept the Get request
      System.out.println(request);
      
     if(!request.contains("ico") || request!=null){//if null or favicon reject
        String formatArray[]=request.split(" ");//split into a 3 index array
        String fname=formatArray[1];//get the file name from the request
        String Mtype=MimeType(fname);//get the mimetype
        String type=type(fname);//the trype of file or directory
        System.out.println(fname);
        System.out.println(Mtype);

        if(type=="txt"){//if text file
            displayFile(writer,Mtype,new File(fname.substring(1)));//call display file method with a prinstream content type 
        }else if (type=="html"){//if html file
            displayFile(writer,Mtype,new File(fname.substring(1)));//call display file method with a prinstream content type 
        }else if(type=="cgi"){
            String details[]=new String[3];//instantize array
            String arr[]=fname.split("&");//split the name and 2 numbers 
            details[0]=arr[0].substring(arr[0].indexOf("?")+1);//get the name
            details[1]=arr[1];//get the  first number
            details[2]=arr[2];//get 2nd number
            addnums(details,Mtype,writer);//call method
        }else if(type=="directory"){//if type is directoty
            System.out.println(fname);
            displayDirectory(Mtype, writer,new File("./"+fname.substring(1)).listFiles());//call method with the content type , printstream and file array
        }

     }
	} catch (IOException x) {
	    System.out.println("Connetion reset. Listening again...");
    }
    
    }
    /**
     * 
     * @param printstream to send write the byte contents to the browser
     * @param fileinputstream //to be converted to bytes 
     * @param flen //the int length of the file
     * 
     *  method reply writes the content of files to the browser
     */
    void Reply(PrintStream printstream, FileInputStream fileinputstream, int flen){
       try
       {
        byte buffer[] = new byte[flen];
        int bytes=fileinputstream.read(buffer);
        printstream.write(buffer, 0, bytes);
      	printstream.flush();
      	printstream.close();
      	fileinputstream.close();
         
       }
       catch (Exception e)  { System.out.println(e); }
    }
    void addnums(String[] details,String mimeType,PrintStream printstream){
        String name=details[0]=details[0].substring(details[0].indexOf("=")+1);//perform regex to get the name of the file 
        int sum=Integer.parseInt(details[1].substring(details[1].indexOf("=")+1))+Integer.parseInt(details[2].substring(details[2].indexOf("=")+1));//sum the args given from the clinet
        String answer="Hello "+name+" The sum of the 2 numbers is "+sum;
        System.out.println(answer);
        sendHeader(printstream,mimeType,answer.length());//send the html headers content type length and ok

        printstream.println(answer);
        printstream.println("<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n");//send back submit
    }
     void sendHeader(PrintStream printstream,String mimeType,int len){//send headers 
        printstream.println("HTTP/1.1 200 OK");
    	printstream.print("Content-Length: " + len);
    	printstream.print("Content-type: "+ mimeType + "\r\n\r\n");
    }
    
   
    public void displayFile(PrintStream printstream,String mime_Type,File pfile) throws IOException {
    	
    int len=(int)pfile.length();//get the int length of the files
    sendHeader(printstream,mime_Type,len);//send the headers
    FileInputStream fileinputstream = new FileInputStream(pfile);//create input strem to read the files
    Reply(printstream,fileinputstream,len);//send the contents to the browser

    }

    public void displayDirectory(String mimeType, PrintStream printstream,File[] Dir) throws IOException{
        
         
    try{       
     
    File file=new File("file.html");//create  a file to store html
	FileOutputStream fileOutputStream=null;
	PrintStream printStream=null;     
    fileOutputStream=new FileOutputStream(file);//create an ouptput stream to write to the file
	printStream=new PrintStream(fileOutputStream);//create stream

  
   printStream.print("<!DOCTYPE html>");//write html doctype
   printStream.print("<html><body><h1>Folder!</h1>");//write html into the file 
   for(File path:Dir){// add html files and directory links to the file
       if(path.isDirectory()){
           printStream.print("<a href=\"" + path.getName()  + "/\">/" + path.getName() + "/</a><br>");
           System.out.println( path.getName());
       }else{
           printStream.print("<a href=\"" + path.getName() + "\">" + path.getName() + "</a>" +"<br>");
           System.out.println( path.getName());
       }
   }
   printStream.print("</body></html>");//write last line into the file
     
   printStream.close();
    displayFile(printstream,mimeType,file);//call method to display files
      
    } catch (IOException e) {
      System.out.println("oops");
      e.printStackTrace();
    }
    }

    /**
     * 
     * @param path
     * @return contenttype depending on file type
     */
    private  String MimeType(String path)
    {
        if (path.endsWith(".html") || path.endsWith("/")) 
            return "text/html";
        else if (path.endsWith(".txt") || path.endsWith(".java")) 
            return "text/plain";
        else if(path.startsWith("/cgi/")){
            return "text/html";
        }
        else    
            return "text/plain";
    }

    /**
     * returns type of file directory
     */
    private String type(String path){
        if(path.endsWith("/")){
            return "directory";
        }else if(path.startsWith("/cgi/")){
            return "cgi";
        }else if(path.endsWith(".txt") || path.endsWith(".java")){
            return "txt";
        }
        return "txt";
    }
    
    
      
    
}