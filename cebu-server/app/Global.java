


import java.io.File;


import play.*;
import utils.OtpGraph;

public class Global extends GlobalSettings {

  private static OtpGraph otpGraph;
  
  public static OtpGraph getOtpGraph() {
    return otpGraph;
  }

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