package controllers;

import play.*;
import play.mvc.*;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.openplans.tools.tracking.impl.Observation;

import org.apache.commons.io.IOUtils;

import api.Operator;

import com.vividsolutions.jts.geom.Coordinate;

import jobs.ObservationHandler;

import models.*;

public class Api extends Controller {

	static SimpleDateFormat locationDateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
	
	public static void operator(String imei)
	{
		Logger.info("Operator Auth request for IMEI " + imei); 
		
		Operator operator = new Operator();
		
		operator.id = new Long(100);
		operator.name = "Spyder";
		operator.gpsInterval = 5;
		operator.updateInterval = 30;
		
		if(request.format == "xml")
			renderXml(operator);
		else
			renderJSON(operator);
	}
	
	
    public static void location(String imei, String content) throws IOException {
    
    	// test request via curl:
    	// 
    	// curl -d "20120430T133023,124.02342,34.43622,8.33,124,200" http://localhost:9000/api/location?imei=myIMEI    	
    	
		// check for valid request
		
    	if(imei == null || imei.trim().isEmpty())
    		badRequest();
    
   	
    	// copy POST body to string

    	String requestBody = null;
    	String message = "";
    	
		if(content != null)
		{
			requestBody = content;
			
		}
		else if(request.method == "POST")
        {
              requestBody = params.get("body");
        }
		
		message = "location message received: imei=" + imei + " " + content;
    	
    	if(requestBody == null || requestBody.isEmpty())
    		badRequest();
    		
    	// requests can contain multiple requests, split on newline
    	
    	String[] lines = requestBody.split("\n");
    	 	
    	for(String line : lines)
    	{
    		// request format: 20120430T133023,124.02342,34.43622,8.33,124,200
    		
    		String[] lineParts = line.trim().split(",");
    		
    		
    		if(lineParts.length != 6)
    			badRequest();
    	
    		try
    		{
    			
	    		Date dateTime = locationDateFormat.parse(lineParts[0].replace("T", " "));
	    		Double lat = Double.parseDouble(lineParts[1]);
	    		Double lon = Double.parseDouble(lineParts[2]);
	    		Double velocity = Double.parseDouble(lineParts[3]);
	    		Double heading = Double.parseDouble(lineParts[4]);
	    		Double gpsError = Double.parseDouble(lineParts[5]);
	    		
	    		Observation observation = Observation.createObservation(imei, dateTime, new Coordinate(lat, lon), velocity, heading, gpsError);
	    		
	    		// using a local queue to handle logging/inference...for now.
	    		ObservationHandler.addObservation(observation, message);	    		
	   		
    		}
    		catch(Exception e)
    		{
    			Logger.error("Bad location update string: ", line);
    			
    			// couldn't parse results
    			badRequest();
    		}
    	}
    	
        ok();
    }

}
