


import java.io.File;


import play.*;
import utils.OtpGraph;

public class Global extends GlobalSettings {

  public static OtpGraph otpGraph;
  
  @Override
  public void onStart(Application app) {
	  
	  otpGraph = new OtpGraph();
    
  }  
  
  @Override
  public void onStop(Application app) {
    Logger.info("Application shutdown...");
  }  
  
  public void doSomething()
  {
	  Logger.info("doing something...");
  }
    
}