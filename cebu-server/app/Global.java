import java.io.File;


import play.*;
import utils.OtpGraph;

import com.typesafe.plugin.inject.InjectPlugin;

public class Global extends GlobalSettings {

	
  @Override
  public void onStart(Application app) {
	 
	  // init otp graph
	  //app.plugin(InjectPlugin.class).getInstance(OtpGraphImpl.class);
	  
	  //Logger.warn("getting an instance from guice:"+ app.plugin(InjectPlugin.class).getInstance(OtpGraph.class));
	 
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