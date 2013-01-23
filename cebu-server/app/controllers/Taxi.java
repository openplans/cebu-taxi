package controllers;

import static akka.pattern.Patterns.ask;
import akka.actor.*;
import akka.dispatch.Future;
import akka.dispatch.OnSuccess;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.awt.Color;

import java.io.File;
import java.math.BigInteger;
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
public class Taxi extends Controller {
	
	@Before
    static void setConnectedUser() {
        if(Security.isConnected() && Security.check("taxi")) {
            renderArgs.put("user", Security.connected());
        }
        else
        	Application.index();
    }
	
	public static void activeTaxis() {
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -15);
		Date recentDate = cal.getTime();
		
		List<Phone> phones;
		
		if(Security.getAccount().operator == null)
			phones = Phone.find("lastUpdate > ? order by id", recentDate).fetch();
		else
			phones = Phone.find("lastUpdate > ? and operatator = ? order by id", recentDate, Security.getAccount().operator).fetch();
		
		for(Phone phone : phones)
		{
			phone.populateUnreadMessages();
		}
		
		if(request.format == "xml")
			renderXml(phones);
		else
			renderJSON(phones);
	}
	
	public static void index() {
			
		render();
	}
	
	
	public static void omReport() {
		
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		List<Phone> phones;
	
		if(Security.getAccount().operator == null)
			phones = Phone.find("not lastUpdate is null order by lastUpdate desc").fetch();
		else
			phones = Phone.find("operatator = ? and not lastUpdate is null order by lastUpdate desc", Security.getAccount().operator).fetch();
			
		for(Phone phone : phones)
		{
			if(phone.vehicle != null)
				vehicles.add(phone.vehicle);
		}
		
		render(vehicles);
	}
	
	
}