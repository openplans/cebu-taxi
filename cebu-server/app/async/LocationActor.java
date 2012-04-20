package async;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class LocationActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  
  // instantiate inference engine here
 
  public void onReceive(Object location) throws Exception {
    if(location instanceof LocationRecord) {
    	
    	// inference ingest here 
    	log.info("Message received:  " + ((LocationRecord)location).getTimestamp().toString());
    	
    }
   
  }
}