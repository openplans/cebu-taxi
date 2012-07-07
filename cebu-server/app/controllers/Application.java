package controllers;


import akka.actor.*;
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
import org.openplans.tools.tracking.impl.graph.InferredEdge;
import org.openplans.tools.tracking.impl.util.GeoUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import jobs.ObservationHandler;

import models.*;

public class Application extends Controller {
	
	static Config myConfig = ConfigFactory.empty();
	
	static ActorSystem system = ActorSystem.create("MySystem", myConfig);

	static ActorRef locationActor = system.actorOf(new Props(
		      InferenceService.class), "locationActor");
	
	static ActorRef csvActor = system.actorOf(new Props(
		      CsvUploadActor.class), "csvActor");
	
	
	public static void index() {
		render();
	}
	
    public static void recent() {
    	Queue history = ObservationHandler.historyQueue;
    	response.setContentTypeIfNotSet("text/plain");
        render(history);
    }
    
    public static void uploadHandler(File csv, String debugEnabled) {

        if (csv != null) {
          final boolean debug_enabled = Boolean.parseBoolean(debugEnabled);
          final File dest = new File("/tmp/upload.csv");
          csv.renameTo(dest);
          TraceParameters params = new TraceParameters(dest, debug_enabled);
          csvActor.tell(params);
        }
        
        index();
      }
    
    public static void velocities() {
    	Collection<InferredEdge> edges = Api.getGraph().getInferredEdges();
    	
    	StreetEdge.deleteAll();
    	
    	for(InferredEdge edge : edges)
    	{
    		if(edge.getVelocityPrecisionDist().getMean().getElement(0) != 4.4)
    		{
    			StreetEdge streetEdge = new StreetEdge();
    			
    			float hue = 120 * (30 / (float)edge.getVelocityPrecisionDist().getMean().getElement(0));
    			Color edgeColor = Color.getHSBColor(hue, 1.0f, 0.5f);
    			
    			String rgb = Integer.toHexString(edgeColor.getRGB());
    		    
    			streetEdge.meanVelocity = edge.getVelocityPrecisionDist().getMean().getElement(0);
    			streetEdge.velocityVarience = edge.getVelocityPrecisionDist().getMean().getElement(1);
    			streetEdge.rbgColor = rgb.substring(2, rgb.length());
    			
    			
    			MathTransform transform;
    		    try {
    		      transform = GeoUtils.getCRSTransform().inverse();
    		      final Geometry transformed = JTS.transform(edge.getGeometry(), transform);
    		      transformed.setSRID(4326);
    			
    			streetEdge.shape = (LineString)transformed;
    		    }
    		    catch(Exception e)
    		    {
    		    	Logger.error("Can't transform geom.");
    		    }
    			
    			streetEdge.save();
    		}
    	}
    	render(edges);
    }

}