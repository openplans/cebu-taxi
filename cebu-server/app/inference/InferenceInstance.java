package inference;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;

import org.openplans.cebutaxi.inference.impl.StandardTrackingFilter;

import async.LocationRecord;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class holds inference data for a particular vehicle
 * 
 * @author bwillard
 * 
 */
public class InferenceInstance {

  private static final double gVariance = 50d;
  private static final double aVariance = 25d;
  private static final long avgTimeDiff = 1;
  private static final double initialAngularRate = Math.PI / 2d;

  final private String vehicleId;
  final private StandardTrackingFilter filter;

  public static double getInitialangularrate() {
    return initialAngularRate;
  }

  public long getPrevTime() {
    return prevTime;
  }

  public Coordinate getPrevObsCoords() {
    return prevObsCoords;
  }

  public Matrix getObservationMatrix() {
    return observationMatrix;
  }

  private long prevTime = 0;
  private final Coordinate prevObsCoords = null;
  final Matrix observationMatrix = StandardTrackingFilter
      .getObservationMatrix();

  public static double getGvariance() {
    return gVariance;
  }

  public static double getAvariance() {
    return aVariance;
  }

  public static long getAvgTimeDiff() {
    return avgTimeDiff;
  }

  public static double getInitialAngularRate() {
    return initialAngularRate;
  }

  public StandardTrackingFilter getFilter() {
    return filter;
  }

  public MultivariateGaussian getBelief() {
    return belief;
  }

  private MultivariateGaussian belief;

  public String getVehicleId() {
    return vehicleId;
  }

  public InferenceInstance(String vehicleId) {

    this.vehicleId = vehicleId;
    this.filter = new StandardTrackingFilter(gVariance, aVariance);
  }

  public void update(LocationRecord record) {
    final Vector xyPoint = record.getProjPoint();
    final double timeDiff = prevTime == 0 ? 0 : (record.getTimestamp()
        .getTime() - prevTime) / 1000;
    prevTime = record.getTimestamp().getTime();

    if (belief == null) {
      belief = filter.createInitialLearnedObject();
      belief.setMean(VectorFactory.getDefault()
          .copyArray(
              new double[] { xyPoint.getElement(0), 0d, xyPoint.getElement(1),
                  0d }));
    }
    
    if (timeDiff > 0) {
      // filter.measure(belief, xyPoint);
      // filter.predict(belief);
      filter.update(belief, xyPoint);
    }

  }

}
