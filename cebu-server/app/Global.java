


import java.io.File;

import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

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