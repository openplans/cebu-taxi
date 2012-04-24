package controllers;

import javax.inject.Inject;

import inference.InferenceService;
import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;
import utils.OtpGraph;
import akka.actor.ActorRef;
import akka.actor.Props;
import async.LocationRecord;


public class Api extends Controller {

  @Inject
  public static OtpGraph graph;

  public static OtpGraph getGraph() {
    return graph;
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