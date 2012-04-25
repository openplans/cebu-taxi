package inference;

import java.util.Collection;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import async.LocationRecord;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This class is an Actor that responds to LocationRecord messages and processes
 * 
 * @author bwillard
 * 
 */
public class InferenceService extends UntypedActor {

  private static final Map<String, InferenceInstance> vehicleToInstance = Maps
      .newConcurrentMap();

  private static final Multimap<String, InferenceResultRecord> vehicleToTraceResults = LinkedHashMultimap
      .create();



  private final LoggingAdapter log = Logging.getLogger(getContext().system(),
      this);

  @Override
  public void onReceive(Object location) throws Exception {
    if (location instanceof LocationRecord) {

      final LocationRecord locationRecord = (LocationRecord) location;

      final InferenceInstance ie = getInferenceInstance(locationRecord
          .getVehicleId());
      
      SnappedEdges snappedEdges;
      synchronized(ie) {
         snappedEdges = ie.update(locationRecord);
      }

      final InferenceResultRecord infResult = InferenceResultRecord
          .createInferenceResultRecord(locationRecord, ie, snappedEdges);
      
      vehicleToTraceResults.put(locationRecord.getVehicleId(), infResult);
      
      log.info("Message received:  " + locationRecord.getTimestamp().toString());

    }

  }
  
  public static Collection<InferenceResultRecord> getTraceResults(String vehicleId)
  {
	  return vehicleToTraceResults.get(vehicleId);
  }

  public static InferenceInstance getInferenceInstance(String vehicleId) {
    InferenceInstance ie = vehicleToInstance.get(vehicleId);

    if (ie == null) {
      ie = new InferenceInstance(vehicleId);
      vehicleToInstance.put(vehicleId, ie);
    }

    return ie;
  }

}
