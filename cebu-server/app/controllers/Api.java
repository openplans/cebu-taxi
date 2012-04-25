package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ObjectNode;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opentripplanner.routing.graph.Edge;

import inference.InferenceService;

import play.Logger;
import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import utils.GeoJSONSerializer;
import utils.OtpGraph;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.OsmSegment;
import async.LocationRecord;

import com.google.inject.Inject;

public class Api extends Controller {
	
//  private static MathTransform transform;
  public static final SimpleDateFormat sdf = new SimpleDateFormat(
      "yyyy-MM-dd hh:mm:ss");

  public static OtpGraph graph = new OtpGraph();
  
  public static ObjectMapper jsonMapper = new ObjectMapper();
  

  public static OtpGraph getGraph() {
    return graph;
  } 
  
  public static Result segment(Integer segmentId) throws JsonGenerationException, JsonMappingException, IOException {
	  Edge e = graph.getGraph().getEdgeById(segmentId);
	  
	  if(e != null) {
		  
		  OsmSegment osmSegment = new OsmSegment(segmentId, e.getGeometry());
		  
		  return ok(jsonMapper.writeValueAsString(osmSegment)).as("text/json");
	  }
	  else
		  return badRequest();
  }
  
  public static Result traces(String vehicleId) throws JsonGenerationException, JsonMappingException, IOException {
	  
	  
	  return ok(jsonMapper.writeValueAsString(InferenceService.getTraceResults(vehicleId))).as("text/json");
  }
  
  public static WebSocket<String> streamTraces() {
	  return new WebSocket<String>() {
	      
	    
	    public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
	      
	    
	    	out.write("Hello!");
	        out.close();
	    
	  }
	};
  }
	
  public static Result location(String vehicleId, String timestamp,
    String latStr, String lonStr, String velocity, String heading,
    String accuracy) {

    final ActorRef locationActor = Akka.system().actorOf(
        new Props(InferenceService.class));

    try {

      final LocationRecord location = LocationRecord.createLocationRecord(
          vehicleId, timestamp, latStr, lonStr, velocity, heading, accuracy);
      locationActor.tell(location);

      return ok();
    } catch (final Exception e) {
      return badRequest(e.getMessage());
    }
  }

  /**
   * Process records from a trace.  Note: flags are set that cause the 
   * records to be handled differently.
   * 
   * @param csvFileName
   * @param vehicleId
   * @param timestamp
   * @param latStr
   * @param lonStr
   * @param velocity
   * @param heading
   * @param accuracy
   * @return
   */
  public static Result traceLocation(String csvFileName, String vehicleId,
    String timestamp, String latStr, String lonStr, String velocity,
    String heading, String accuracy) {

    // TODO set flags for result record handling
    return location(vehicleId, timestamp, latStr, lonStr, velocity, heading,
        accuracy);

  }

  public static Result vertex() {
    Logger.info("vertices: " + graph.getVertexCount());

    return ok();
  }

}