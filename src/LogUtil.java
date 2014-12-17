import java.util.logging.Logger;
public class LogUtil {
	private static LogUtil instance;
	private static final Logger LOG = Logger.getLogger("SimpleHttpServer");
	private LogUtil(){
		// singleton object
	}
	public static LogUtil getInstance(){
		if(instance == null){
			instance =  new LogUtil();
		}
		return instance;
	}
	public void putLog(String message){
		LOG.info(message);
	}
}