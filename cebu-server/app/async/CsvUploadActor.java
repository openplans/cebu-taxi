package async;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import controllers.Api;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import au.com.bytecode.opencsv.CSVReader;

public class CsvUploadActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
 
  public void onReceive(Object csvFile) throws Exception {
    if(csvFile instanceof File) {
    	
    	CSVReader gps_reader = new CSVReader(new FileReader((File)csvFile), ';');
          String[] line;
          gps_reader.readNext();
          log.info("processing gps data");
          
          while ((line = gps_reader.readNext()) != null) {

        	 try
        	 {
        		 Api.traceLocation(((File) csvFile).getName(), line[3], line[4], line[5], line[7], line[10], null, null);
        	 } 
        	 catch(Exception e)
        	 {
        		 log.info("bad csv line: " + line);// bad line
        	 }
        	  
          }
    }
   
  }
}