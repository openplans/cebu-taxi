package controllers;

import java.text.SimpleDateFormat;

import akka.actor.ActorRef;
import akka.actor.Props;
import async.Location;
import async.LocationActor;

import play.*;
import play.libs.Akka;
import play.mvc.*;

import views.html.*;

public class Api extends Controller {
  
  public static Result location(String vehicleId, String timestamp, String lat, String lon, String velocity, String heading, String accuracy) {
	  
	  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
	  ActorRef locationActor = Akka.system().actorOf(new Props(LocationActor.class));
	  
	  try	{
		  Location location = new Location();
		  
		  location.vehicleId = vehicleId;
		  location.timestamp = sdf.parse(timestamp);
		  location.lat = Double.parseDouble(lat);
		  location.lon = Double.parseDouble(lon);
		  location.velocity = velocity != null ? Double.parseDouble(velocity) : null;
		  location.heading = heading != null ? Double.parseDouble(heading) : null;
		  location.accuracy = accuracy != null ? Double.parseDouble(accuracy) : null;
		  
		  locationActor.tell(location);
		  
		  return ok();
	  }
	  
	  catch(Exception e) {
		  return badRequest();
	  }
  }  
 
  
}