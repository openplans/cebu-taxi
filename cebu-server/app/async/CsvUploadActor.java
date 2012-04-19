package async;

import java.io.File;
import java.io.FileReader;

import controllers.Api;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import au.com.bytecode.opencsv.CSVReader;

public class CsvUploadActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  
  // instantiate inference engine here
 
  public void onReceive(Object csvFile) throws Exception {
    if(csvFile instanceof File) {
    	
    	CSVReader gps_reader = new CSVReader(new FileReader((File)csvFile), ',');
          String[] nextLine;
          gps_reader.readNext();
          log.info("processing gps data");
          
          while ((nextLine = gps_reader.readNext()) != null) {
        	  
        	 Api.location(((File)csvFile).getName(), nextLine[0], nextLine[2], nextLine[3], nextLine[4], null, null);      	  
        	  
          }
    }
   
  }
}