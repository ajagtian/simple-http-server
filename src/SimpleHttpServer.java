import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 
 * @author akshayjagtiani
 *
 */
public class SimpleHttpServer extends Thread{
	private String host;
	private int port;
	private String wwwroot;
	private FileUtil fileUtil = new FileUtil();
	private LogUtil LOG = LogUtil.getInstance();
	
	/* 
	 * initialize server
	 */
	private void getConfiguration(){
		StringBuilder conf_file = new StringBuilder(System.getProperty("user.dir")).append("/httpd.conf");
		String config=null;
		try {
			config=fileUtil.getFileAsString(conf_file.toString());
		} catch (IOException e) {
			LOG.putLog("Error in reading server configuration");
		}
		host=config.split("\n")[0].split(" ")[config.split("\n")[0].split(" ").length-1].trim();
		port=Integer.valueOf(config.split("\n")[1].split(" ")[config.split("\n")[1].split(" ").length-1].trim());
		wwwroot=config.split("\n")[2].split(" ")[config.split("\n")[2].split(" ").length-1].trim();
	}
	/*
	 * get mime type for an extension
	 */
	private String getMimeType(String extension){
		StringBuilder path = new StringBuilder(System.getProperty("user.dir")).append("/mime.types");
		String mime_types = null;
		try {
			mime_types=fileUtil.getFileAsString(path.toString());
		} catch (IOException e) {
			LOG.putLog("Error in reading mime types "+e.getMessage());
		}
		int i = mime_types.indexOf(extension);
		int j = mime_types.indexOf(" ", i);
		int k = j+1;
		int l = mime_types.indexOf("\n",k);
		return mime_types.substring(k,l).trim();
	}
	
	public void connect(){
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			LOG.putLog("Connection error "+e.getMessage());
		}
		if(socket != null){
			LOG.putLog("Accepting connections on port "+ port);
			while(Boolean.TRUE){
				Socket clientSocket = null;
				try {
					clientSocket = socket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
					PrintStream pout = new PrintStream(out);
					String line_1 = in.readLine();
					// validation check
					if(line_1==null|| line_1.isEmpty()){
						continue;
					}
					while (true) {
						String header = in.readLine();
		                if (header==null || header.length()==0)
		                	break;
		            }
					// bad request check
					if (!line_1.startsWith("GET") || line_1.length()<14 || !line_1.endsWith("HTTP/1.1")) {
						// bad request
			            errorReport(pout, clientSocket, "400", "Bad Request");
			            LOG.putLog("Bad Request Recieved");
			        }
					else{
						String request_host = line_1.substring(4,line_1.length()-9).trim();
                        String path = wwwroot  + request_host;
                        File f = new File(path);
                        if (!f.isFile() && !path.endsWith("/")) {
                                // redirect browser if referring to directory without final '/'
                                pout.print("HTTP/1.0 301 Moved Permanently\r\n Location: http://" + clientSocket.getLocalAddress().getHostAddress() + ":" + clientSocket.getLocalPort() + "/" + request_host + "/\r\n\r\n");
                                LOG.putLog("301 moved permanently");
                        }
                        else{
                        	if (!f.isFile()) { 
                                // if directory, implicitly add 'index.html'
                                path = path + "index.html";
                                f = new File(path);
                            }
                        	// send file to client
                        	FileInputStream file = new FileInputStream(f);
                        	pout.print("HTTP/1.0 200 OK\r\n Content-Type: " + getMimeType(path.substring(path.lastIndexOf(".")+1)) + "\r\n Date: " + new Date() + "\r\n Server: FileServer 1.0\r\n\r\n");
                        	sendResponse(file, out);
                        	LOG.putLog("200 OK");
                        }
					}
					out.flush();
					clientSocket.close();
				} catch (IOException e) {
					LOG.putLog("Could not accept connection from client "+e.getMessage());
				}
				
			}
			
		}
		
	}

	private void errorReport(PrintStream pout, Socket connection,String code, String title) {
		pout.print("HTTP/1.1 " + code + " " + title + "\r\n" + "\r\n"
				+ "<!DOCTYPE html"
				+ "<title>" + code + " " + title + "</title>\r\n"
				+ "</head><body>\r\n" + "<h3>" + title + "</h3>\r\n"
				+ "<p>\r\n" + "<hr><address>SimpleHttpServer at "
				+ connection.getLocalAddress().getHostName() + " Port "
				+ connection.getLocalPort() + "</address>\r\n"
				+ "</body></html>\r\n");
	}

	private void sendResponse(InputStream file, OutputStream out) {
		try {
			byte[] buffer = new byte[1000];
			while (file.available() > 0)
				out.write(buffer, 0, file.read(buffer));
		} catch (IOException e) {
			LOG.putLog("Could not send response "+e.getMessage());
		}
	}
	
	// test
	public static void main(String [] args){
		SimpleHttpServer httpServer = new SimpleHttpServer();
		httpServer.getConfiguration();
		httpServer.connect();
	}
}
