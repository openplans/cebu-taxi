package inference;

import java.util.Map;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import async.LocationRecord;

import com.google.common.collect.HashMultimap;
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

  private static final Multimap<String, InferenceResultRecord> vehicleToTraceResults = HashMultimap
      .create();

  public static ThreadLocal<MathTransform> transform = new ThreadLocal<MathTransform>() {

    @Override
    public MathTransform get() {
      return super.get();
    }

    @Override
    protected MathTransform initialValue() {
      try {
//        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);

        final String googleWebMercatorCode = "EPSG:4326";

        final String cartesianCode = "EPSG:4499";

        final CRSAuthorityFactory crsAuthorityFactory = CRS
            .getAuthorityFactory(true);

        final CoordinateReferenceSystem mapCRS = crsAuthorityFactory
            .createCoordinateReferenceSystem(googleWebMercatorCode);

        final CoordinateReferenceSystem dataCRS = crsAuthorityFactory
            .createCoordinateReferenceSystem(cartesianCode);

        final boolean lenient = true; // allow for some error due to different
                                      // datums
        return CRS.findMathTransform(mapCRS, dataCRS, lenient);
      } catch (final Exception e) {
        e.printStackTrace();
      }

      return null;
    }

  };

  private final LoggingAdapter log = Logging.getLogger(getContext().system(),
      this);

  static {
    System.setProperty("org.geotools.referencing.forceXY", "true");
  }

  @Override
  public void onReceive(Object location) throws Exception {
    if (location instanceof LocationRecord) {

      final LocationRecord locationRecord = (LocationRecord) location;

      final InferenceInstance ie = getInferenceInstance(locationRecord
          .getVehicleId());

      SnappedEdges snappedEdges = ie.update(locationRecord);

      final InferenceResultRecord infResult = InferenceResultRecord
          .createInferenceResultRecord(locationRecord, ie, snappedEdges);
      
      vehicleToTraceResults.put(locationRecord.getVehicleId(), infResult);
      
      log.info("Message received:  " + locationRecord.getTimestamp().toString());

    }

  }

  public static InferenceInstance getInferenceInstance(String vehicleId) {
    InferenceInstance ie = vehicleToInstance.get(vehicleId);

    if (ie == null) {
      ie = new InferenceInstance(vehicleId);
      vehicleToInstance.put(vehicleId, ie);
    }

    return ie;
  }

  public static MathTransform getCRSTransform() {
    return transform.get();
  }

}
