package controllers;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opentripplanner.routing.graph.Edge;

import inference.InferenceService;

import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;

import static play.libs.Json.toJson;
import utils.OtpGraph;

import akka.actor.ActorRef;
import akka.actor.Props;
import async.LocationRecord;

import com.google.inject.Inject;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Api extends Controller {
	
//  private static MathTransform transform;
  private static final SimpleDateFormat sdf = new SimpleDateFormat(
      "yyyy-MM-dd hh:mm:ss");


  @Inject
  public static OtpGraph graph;

  public static OtpGraph getGraph() {
    return graph;
  }
  
  
  public static Result vertex(Integer edgeId)
  {
	  
	  Edge e = graph.getGraph().getEdgeById(edgeId);
	  
	  Geometry geom = e.getGeometry();
	  
	  
	  return ok(toJson(geom));
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