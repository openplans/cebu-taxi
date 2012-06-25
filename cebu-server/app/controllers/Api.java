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

import api.AuthResponse;

import com.vividsolutions.jts.geom.Coordinate;

import jobs.ObservationHandler;

import models.*;

public class Api extends Controller {

	static SimpleDateFormat locationDateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
	
	public static void operator(String imei)
	{
		Logger.info("Operator Auth request for IMEI " + imei); 
		
		if(imei == null)
			unauthorized("IMEI Required");
		
		Phone phone = Phone.find("imei = ?", imei).first();
		
		if(phone != null)
		{
			AuthResponse authResponse = new AuthResponse();
			
			authResponse.id = new Long(100);
			authResponse.name = phone.operator.name;
			
			if(phone.driver != null)
			{
				authResponse.driverId = phone.driver.driverId;
				authResponse.driverName = phone.driver.name;
			}
			
			if(phone.vehicle != null)
			{
				authResponse.bodyNumber = phone.vehicle.bodyNumber;
			}
			
			authResponse.gpsInterval = 5;
			authResponse.updateInterval = 30;
			
			if(request.format == "xml")
				renderXml(authResponse);
			else
				renderJSON(authResponse);
		}
		else
		{
			Logger.info("Unknown phone entry for IMEI " + imei); 
			unauthorized("Unknown Phone IMEI");
		}
	}
	
	public static void register(String imei, Long operator)
	{
		if(imei != null && !imei.isEmpty() && operator != null)
		{
			Phone phone = Phone.find("imei = ?", imei).first();
			
			if(phone == null)
			{
				Logger.info("Creating phone entry for IMEI " + imei); 
				phone = new Phone();
				phone.imei = imei;
			}
			
			Operator operatorObj = Operator.findById(operator);
			
			if(operatorObj == null)
			{
				Logger.info("Unknown operator: " + operator); 
				badRequest();
			}
			
			phone.operator = operatorObj;
			
			phone.save();
			
			ok();
		}
		else
		{
			List<Operator> operators = Operator.findAll();
			
			if(request.format == "xml")
				renderXml(operators);
			else
				renderJSON(operators);
		}
	}
	
	public static void login(String imei, String driver, String body)
	{
		if(imei == null)
			unauthorized("IMEI Required");
		
		Phone phone = Phone.find("imei = ?", imei).first();
		
		if(phone == null)
		{
			Logger.info("Unknown phone entry for IMEI " + imei); 
			unauthorized("Unknown Phone IMEI");
		}
		
		if(driver == null)
			badRequest();
		
		Driver driverObj = Driver.find("driverId = ?", driver).first();
		
		if(driverObj == null)
		{
			Logger.info("Unknown Driver Id " + driver); 
			
			driverObj = new Driver();
			driverObj.driverId = driver;
			driverObj.save();
		
		}
		
		if(body == null)
			badRequest();
		
		Vehicle veichie = Vehicle.find("bodyNumber = ?", body).first();
		
		if(veichie == null)
		{
			Logger.info("Unknown vehicle, createing record for body number " + body); 
			
			veichie = new Vehicle();
			veichie.bodyNumber = body;
			veichie.save();
		}
		
		phone.driver = driverObj;
		phone.vehicle = veichie;
		
		phone.save();

		AuthResponse authResponse = new AuthResponse();
		
		authResponse.id = new Long(100);
		authResponse.name = phone.operator.name;
		
		if(phone.driver != null)
		{
			authResponse.driverId = phone.driver.driverId;
			authResponse.driverName = phone.driver.name;
		}
		
		if(phone.vehicle != null)
		{
			authResponse.bodyNumber = phone.vehicle.bodyNumber;
		}
		
		authResponse.gpsInterval = 5;
		authResponse.updateInterval = 30;
		
		if(request.format == "xml")
			renderXml(authResponse);
		else
			renderJSON(authResponse);
	}
	
	public static void logout(String imei)
	{
		if(imei == null)
			unauthorized("IMEI Required");
		
		Phone phone = Phone.find("imei = ?", imei).first();
		
		if(phone == null)
		{
			Logger.info("Unknown phone entry for IMEI " + imei); 
			unauthorized("Unknown Phone IMEI");
		}
		
		phone.driver = null;
		phone.vehicle = null;
		
		phone.save();

		ok();
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
	    		ObservationHandler.addObservation(observation, line);	    		
	   		
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
