package controllers;

import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.text.SimpleDateFormat;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;

import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;
import akka.actor.ActorRef;
import akka.actor.Props;
import async.LocationActor;
import async.LocationRecord;

public class Api extends Controller {

  public static Result location(String vehicleId, String timestamp, String latStr,
      String lonStr, String velocity, String heading, String accuracy) {

    final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
    final ActorRef locationActor = Akka.system().actorOf(
        new Props(LocationActor.class));

    try {

      String googleWebMercatorCode = "EPSG:4326";
      
      String cartesianCode = "EPSG:4499";
       
      CRSAuthorityFactory crsAuthorityFactory = CRS.getAuthorityFactory(true);
       
      CoordinateReferenceSystem mapCRS = crsAuthorityFactory.createCoordinateReferenceSystem(googleWebMercatorCode);
       
      CoordinateReferenceSystem dataCRS = crsAuthorityFactory.createCoordinateReferenceSystem(cartesianCode);
                             
      boolean lenient = true; // allow for some error due to different datums
      MathTransform transform = CRS.findMathTransform(mapCRS, dataCRS, lenient);
      
      final double lat = Double.parseDouble(latStr);
      final double lon = Double.parseDouble(lonStr);
      Coordinate obsCoords = new Coordinate(lon, lat);
      Coordinate obsPoint = new Coordinate();
      JTS.transform(obsCoords, obsPoint, transform);
      
//      Vector xyPoint = VectorFactory.getDefault().createVector2D(obsPoint.x, obsPoint.y);
      
      final LocationRecord location = new LocationRecord(vehicleId, sdf.parse(timestamp), 
          lat, lon, obsPoint.x, obsPoint.y,
          velocity != null ? Double.parseDouble(velocity) : null,
          heading != null ? Double.parseDouble(heading) : null,
          accuracy != null ? Double.parseDouble(accuracy) : null);

      locationActor.tell(location);

      return ok();
    }

    catch (final Exception e) {
      return badRequest();
    }
  }

}