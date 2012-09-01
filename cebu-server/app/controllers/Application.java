package controllers;

import akka.actor.*;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import inference.InferenceService;
import play.*;
import play.mvc.*;
import java.awt.Color;

import jobs.CsvUploadActor;
import jobs.CsvUploadActor.TraceParameters;

import java.io.File;
import java.util.*;

import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingFilter;
import org.openplans.tools.tracking.impl.VehicleState.VehicleStateInitialParameters;
import org.openplans.tools.tracking.impl.graph.InferredEdge;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingBootstrapFilter;
import org.openplans.tools.tracking.impl.statistics.filters.VehicleTrackingPLFilter;
import org.openplans.tools.tracking.impl.util.GeoUtils;

import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import jobs.ObservationHandler;

import models.*;

public class Application extends Controller {
	
	private final static double OFFSET = 2;
	
	private static GeometryFactory gf = new GeometryFactory();
	
	static Config myConfig = ConfigFactory.empty();
	
	static ActorSystem system = ActorSystem.create("MySystem", myConfig);

	static ActorRef locationActor = system.actorOf(new Props(
		      InferenceService.class), "locationActor");
	
	static ActorRef csvActor = system.actorOf(new Props(
		      CsvUploadActor.class), "csvActor");
	
	 private static Map<String, Class<? extends VehicleTrackingFilter>> filtersMap = Maps.newHashMap();
	  static {
	    filtersMap.put(VehicleTrackingPLFilter.class.getName(), VehicleTrackingPLFilter.class);
	    filtersMap.put(VehicleTrackingBootstrapFilter.class.getName(), VehicleTrackingBootstrapFilter.class);
	  }
	  
	  public static Map<String, Class<? extends VehicleTrackingFilter>>
	      getFilters() {
	    return filtersMap;
	  }
	
	  public static void setFilters(
	    Map<String, Class<? extends VehicleTrackingFilter>> filters) {
	    Application.filtersMap = filters;
	  }
	
	public static void index() {
		render();
	}
	
	public static void taxi() {
		render();
	}
	
	public static void citom() {
		render();
	}
	
	
	public static void upload() {
		final Set<String> filters = filtersMap.keySet();
		render(filters);
	}
	
	public static void incidents() {
		render();
	}
	
	
    public static void recent() {
    	Queue history = ObservationHandler.historyQueue;
    	response.setContentTypeIfNotSet("text/plain");
        render(history);
    }
    
    public static void uploadHandler(File csv,
    	    String obs_variance_pair, String road_state_variance_pair,
    	    String ground_state_variance_pair, String off_prob_pair,
    	    String on_prob_pair, String numParticles_str, String seed_str,
    	    String filterTypeName, String debugEnabled) {

    	    if (csv != null) {
    	      final String[] obsPair = obs_variance_pair.split(",");
    	      final Vector obsVariance =
    	          VectorFactory.getDefault().createVector2D(
    	              Double.parseDouble(obsPair[0]),
    	              Double.parseDouble(obsPair[1]));

    	      final String[] roadStatePair =
    	          road_state_variance_pair.split(",");
    	      final Vector roadStateVariance =
    	          VectorFactory.getDefault().createVector2D(
    	              Double.parseDouble(roadStatePair[0]),
    	              Double.parseDouble(roadStatePair[1]));

    	      final String[] groundStatePair =
    	          ground_state_variance_pair.split(",");
    	      final Vector groundStateVariance =
    	          VectorFactory.getDefault().createVector2D(
    	              Double.parseDouble(groundStatePair[0]),
    	              Double.parseDouble(groundStatePair[1]));

    	      final String[] offPair = off_prob_pair.split(",");
    	      final Vector offProbs =
    	          VectorFactory.getDefault().createVector2D(
    	              Double.parseDouble(offPair[0]),
    	              Double.parseDouble(offPair[1]));

    	      final String[] onPair = on_prob_pair.split(",");
    	      final Vector onProbs =
    	          VectorFactory.getDefault().createVector2D(
    	              Double.parseDouble(onPair[0]),
    	              Double.parseDouble(onPair[1]));

    	      final long seed = Long.parseLong(seed_str);
    	      final int numParticles = Integer.parseInt(numParticles_str);

    	      final VehicleStateInitialParameters parameters =
    	          new VehicleStateInitialParameters(obsVariance,
    	              roadStateVariance, groundStateVariance, offProbs,
    	              onProbs, filterTypeName, numParticles, seed);

    	      final boolean debug_enabled =
    	          Boolean.parseBoolean(debugEnabled);

    	      final File dest = new File("/tmp/upload.csv");
    	      csv.renameTo(dest);

    	      final TraceParameters params =
    	          new TraceParameters(dest, parameters, debug_enabled);
    	      csvActor.tell(params);
    	    }

    	    index();
    	  }
    
    public static void writedc() {
    	Api.getGraph().writeDataCube(new File(Play.configuration.getProperty("application.dcPath")));
    	
    	ok();
    }
    
    public static void velocities() {
    	
    	HashMap<Integer, Double> baseline = Api.getGraph().getDataCube().filterAndGroup(new HashMap<String, Integer>(), "edge");
    	
    	HashMap<String, Integer> filter = new HashMap<String, Integer>();
    	
    	//filter.put("interval", 22); 	
    	
    	HashMap<Integer, Double> h12 =  Api.getGraph().getDataCube().filterAndGroup(filter, "interval");
    	
    	HashMap<Integer, Double> delta = new HashMap<Integer, Double>();
    	
    	for(Integer edgeId : h12.keySet())
    	{
    		if(baseline.containsKey(edgeId))
    			delta.put(edgeId, h12.get(edgeId) - baseline.get(edgeId));
    	}
    	
    	//renderJSON(h12);
   
    	
    	
    	for(Integer edgeId : baseline.keySet())
    	{
			StreetEdge streetEdge = new StreetEdge();
			streetEdge.edgeId = edgeId; 
			
			if(baseline.containsKey(edgeId))
			{
				Double meanVelocity = baseline.get(edgeId);
				
    			float hue = 120 * (30 / (float)meanVelocity.floatValue());
    			Color edgeColor = Color.getHSBColor(hue, 1.0f, 0.5f);
    			
    			String rgb = Integer.toHexString(edgeColor.getRGB());
    		    
    			streetEdge.meanVelocity = meanVelocity;
    			
    			// TODO store variance in data cube;	    	
    			streetEdge.velocityVarience = 1.0; // edge.getVelocityPrecisionDist().getMean().getElement(1);
    			
    			streetEdge.rbgColor = rgb.substring(2, rgb.length());
			}
			
			MathTransform transform;
			
		    try {
		    	
		    	if (Api.getGraph().getEdge(edgeId).getGeometry() != null)
		    	{
			        Coordinate[] oldCoords = Api.getGraph().getEdge(edgeId).getGeometry().getCoordinates();
			        int nCoords = oldCoords.length;
			        Coordinate[] newCoords = new Coordinate[nCoords];
			        
			        for (int i = 0; i < nCoords - 1; ++i) {
			            Coordinate coord0 = oldCoords[i];
			            Coordinate coord1 = oldCoords[i+1];
			            
			            double dx = coord1.x - coord0.x;
			            double dy = coord1.y - coord0.y;
			            
			            double length = Math.sqrt(dx * dx + dy * dy);
			            
			            Coordinate c0 = new Coordinate(coord0.x - OFFSET * dy / length, coord0.y - OFFSET * dx / length);
			            Coordinate c1 = new Coordinate(coord1.x - OFFSET * dy / length, coord1.y - OFFSET * dx / length);
			            newCoords[i] = c0;
			            newCoords[i+1] = c1; //will get overwritten except at last iteration
			        }
			        
			        
			        transform = GeoUtils.getCRSTransform().inverse();
				    final Geometry transformed = JTS.transform( gf.createLineString(newCoords), transform);
				    transformed.setSRID(4326);
					
				    streetEdge.shape = (LineString)transformed;
		    	}
		    	
		      
		    }
		    catch(Exception e)
		    {
		    	Logger.error("Can't transform geom.");
		    }
			
			streetEdge.save();
    	}
    	
    	renderJSON(baseline);
    }

}