package controllers;


import java.text.SimpleDateFormat;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import play.Logger;
import play.api.Play;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;

import akka.actor.ActorRef;
import akka.actor.Props;

import async.LocationActor;
import async.LocationRecord;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;

public class Api extends Controller {
	
  public static Result vertex()
  {
	  
	  Logger.info("testing..");
	  
	  return ok();
  }
	
//  private static MathTransform transform;
  private static final SimpleDateFormat sdf = new SimpleDateFormat(
      "yyyy-MM-dd hh:mm:ss");

  public static MathTransform getTransform() {
    return transform.get();
  }

  public static SimpleDateFormat getSdf() {
    return sdf;
  }

  public static ThreadLocal<MathTransform> transform = new ThreadLocal<MathTransform>() {

    @Override
    public MathTransform get() {
      return super.get();
    }

    @Override
    protected MathTransform initialValue() {
      try {
  
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
  
  static {
    System.setProperty("org.geotools.referencing.forceXY", "true");
  }

  private static Map<String, LocationRecord> vehiclesToRecords = Maps.newConcurrentMap();
  
  public static Result location(String vehicleId, String timestamp,
      String latStr, String lonStr, String velocity, String heading,
      String accuracy) {

    final ActorRef locationActor = Akka.system().actorOf(
        new Props(LocationActor.class));

    try {

      final double lat = Double.parseDouble(latStr);
      final double lon = Double.parseDouble(lonStr);
      final Coordinate obsCoords = new Coordinate(lon, lat);
      final Coordinate obsPoint = new Coordinate();
      JTS.transform(obsCoords, obsPoint, transform.get());

      final LocationRecord prevLocation = vehiclesToRecords.get(vehicleId);
      
      final LocationRecord location = new LocationRecord(vehicleId,
          sdf.parse(timestamp), obsCoords, obsPoint,
          velocity != null ? Double.parseDouble(velocity) : null,
          heading != null ? Double.parseDouble(heading) : null,
          accuracy != null ? Double.parseDouble(accuracy) : null,
          prevLocation);

      vehiclesToRecords.put(vehicleId, location);
      
      locationActor.tell(location);

      return ok();
    } catch (final Exception e) {
      return badRequest(e.getMessage());
    }
  }
}