package controllers;

import static akka.pattern.Patterns.ask;
import akka.actor.*;
import akka.dispatch.Future;
import akka.dispatch.OnSuccess;
import au.com.bytecode.opencsv.CSVWriter;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.awt.Color;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingFilter;
import org.openplans.tools.tracking.impl.VehicleState.VehicleStateInitialParameters;
import org.openplans.tools.tracking.impl.VehicleUpdate;
import org.openplans.tools.tracking.impl.VehicleUpdateResponse;
import org.openplans.tools.tracking.impl.graph.InferredEdge;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingBootstrapFilter;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingPLFilter;
//import org.openplans.tools.tracking.impl.util.GeoUtils;
import org.openplans.tools.tracking.impl.util.OtpGraph;
import org.opentripplanner.routing.graph.Edge;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import jobs.ObservationHandler;

import models.*;

@With(Secure.class)
public class Citom extends Controller {
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat(
		      "MM/dd/yyyy HH:mm:ss");
	
	@Before
    static void setConnectedUser() {
        if(Security.isConnected() && Security.check("citom")) {
            renderArgs.put("user", Security.connected());
        }
        else
        	Application.index();
    }
	
	public static void index() {
		render();
	}	
	
	public static void incidents() {
		render();
	}
	
	public static void journey() {
		
		List<Journey> saveJourneys = Journey.findAll();
		
		render(saveJourneys);
	}
	
	public static void area() {
		render();
	}
	
	public static void saveJourney(String name, Double origin_lat, Double origin_lon, Double destination_lat, Double destination_lon, Double speed, Double distance, Double time) {
		
		Journey journey = new Journey();
		
		journey.name = name;
		journey.origin_lat = origin_lat;
		journey.origin_lon = origin_lon;
		journey.destination_lat = destination_lat;
		journey.destination_lon = destination_lon;
		journey.speed = speed;
		journey.distance = distance;
		journey.time = time;
	
		journey.account = Security.getAccount();
		
		journey.save();
		
		Citom.journey();
	}
	
	public static void clearJourney(Long id) {
		
		Journey journey = Journey.findById(id);
	
		journey.delete();
		
		Citom.journey();
	}
	
	public static void alerts(Boolean active, String filter, String fromDate, String toDate, String type) {
		
		List<Alert> alerts = null;
		
		Date from = new Date();
		Date to = new Date();
		
		
		if(filter == null || filter.isEmpty() || filter.equals("today"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			from = cal.getTime();
		}
		else if(filter.equals("yesterday"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			to = cal.getTime();
			
			cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -48);
			from = cal.getTime();
			
		}
		else if(filter.equals("week"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -168);
			from = cal.getTime();
		}
		else if(filter.equals("custom"))
		{	
			try
			{
				from = Citom.sdf.parse(fromDate + " 00:00:01");
				to = Citom.sdf.parse(toDate + " 23:59:59");
			}
			catch(Exception e)
			{
				Logger.info(e.toString());
			}
		}
		else
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			from = cal.getTime();
		}
		
		if(active != null && active)
		{
			if(type != null && !type.isEmpty())
				alerts = Alert.find("active = true and timestamp >= ? and timestamp <= ? and type = ?", from, to, type).fetch();
			else
				alerts = Alert.find("active = true and timestamp >= ? and timestamp <= ?", from, to).fetch();
		}
		else
		{
			if(type != null && !type.isEmpty())
				alerts = Alert.find("timestamp >= ? and timestamp <= ? and type = ?", from, to, type).fetch();
			else
				alerts = Alert.find("timestamp >= ? and timestamp <= ?", from, to).fetch();
		}
			
		if(request.format == "xml")
			renderXml(alerts);
		else
			renderJSON(alerts);
	}
	
	
public static void alertsCsv(Boolean active, String filter, String fromDate, String toDate, String type) {
		
		List<Alert> alerts = null;
		
		Date from = new Date();
		Date to = new Date();
		
		
		if(filter == null || filter.isEmpty() || filter.equals("today"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			from = cal.getTime();
		}
		else if(filter.equals("yesterday"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			to = cal.getTime();
			
			cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -48);
			from = cal.getTime();
			
		}
		else if(filter.equals("week"))
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -168);
			from = cal.getTime();
		}
		else if(filter.equals("custom"))
		{	
			try
			{
				from = Citom.sdf.parse(fromDate + " 00:00:01");
				to = Citom.sdf.parse(toDate + " 23:59:59");
			}
			catch(Exception e)
			{
				Logger.info(e.toString());
			}
		}
		else
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -24);
			from = cal.getTime();
		}
		
		if(active != null && active)
		{
			if(type != null && !type.isEmpty())
				alerts = Alert.find("active = true and timestamp >= ? and timestamp <= ? and type = ? order by timestamp", from, to, type).fetch();
			else
				alerts = Alert.find("active = true and timestamp >= ? and timestamp <= ? order by timestamp", from, to).fetch();
		}
		else
		{
			if(type != null && !type.isEmpty())
				alerts = Alert.find("timestamp >= ? and timestamp <= ? and type = ? order by timestamp", from, to, type).fetch();
			else
				alerts = Alert.find("timestamp >= ? and timestamp <= ? order by timestamp", from, to).fetch();
		}
		
		StringWriter csvString = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(csvString);
		
		String[] headerBase = "type, timestamp, user, active, description, lat, lon".split(",");
		
		csvWriter.writeNext(headerBase);
		 
		for(Alert alert : alerts)
		{
			String[] dataFields = new String[7];
			dataFields[0] = alert.type;
			dataFields[1] = alert.timestamp.toString();
			dataFields[2] = alert.account.username;
			dataFields[3] = alert.active.toString();
			dataFields[4] = alert.description;
			dataFields[5] = alert.location_lat.toString();
			dataFields[6] = alert.location_lon.toString();
			
			csvWriter.writeNext(dataFields);
			
			List<AlertMessage> messages = AlertMessage.find("alert = ?", alert).fetch();
			
			for(AlertMessage message : messages)
			{
				String[] messageFields = new String[7];
				
				messageFields[0] = "-";
				messageFields[1] = message.timestamp.toString();
				messageFields[2] = message.account.username;
				messageFields[4] = message.description;
				
				csvWriter.writeNext(messageFields);
			}
			
			
		}
		
		response.setHeader("Content-Disposition", "attachment; filename=\"alert_data.csv\"");
		response.setHeader("Content-type", "text/csv");
		
		renderText(csvString);
	}
	
	
	public static void alertMessages(Long id) {
		
		Alert alert = Alert.findById(id);
		
		List<AlertMessage> messages = AlertMessage.find("alert = ?", alert).fetch();
		
		if(request.format == "xml")
			renderXml(messages);
		else
			renderJSON(messages);
	}
	
	
	public static void saveAlertMessage(Long id, String message) {
		
		Alert alert = Alert.findById(id);
		
		AlertMessage newMessage = new AlertMessage();
		newMessage.alert = alert;
		newMessage.description = message;
		newMessage.timestamp = new Date();
		newMessage.account = Security.getAccount();
		newMessage.save();
		
		ok();
	}
	
	
	public static void clearAlert(Long id) {
		
		Alert alert = Alert.findById(id);
		
		alert.active = false;
		alert.save();
		
		ok();
	}
	
	public static void saveIncident(Double lat, Double lng, String message, String type) {
		
		Alert alert = new Alert();
		
		alert.location_lat = lat;
		alert.location_lon = lng;
		alert.description = message;
		alert.type = type;
		alert.active = true;
		alert.timestamp = new Date();
		alert.account = Security.getAccount();
		
		alert.save();
		
		ok();
	}

}