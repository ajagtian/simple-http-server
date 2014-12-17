import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
public class FileUtil {
	private LogUtil LOG = LogUtil.getInstance();
	public  String getFileAsString(String path) throws IOException{
		File file = new File(path);
		byte b [] = new byte[200];
		if(file.exists()){
			FileInputStream fin = new FileInputStream(file);
			fin.read(b);
			fin.close();
		}
		else {
			LOG.putLog("configuration file missing");
		}
		return new String(b);
	}
}
