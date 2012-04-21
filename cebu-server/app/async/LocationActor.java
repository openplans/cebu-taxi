package async;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.decomposition.EigenDecompositionRightMTJ;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;

import java.text.SimpleDateFormat;

import org.geotools.geometry.jts.JTS;
import org.openplans.cebutaxi.inference.impl.StandardTrackingFilter;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.vividsolutions.jts.geom.Coordinate;

import controllers.Api;

public class LocationActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  // instantiate inference engine here
  private static final double gVariance = 50d;
  private static final double aVariance = 25d;
  private static final long avgTimeDiff = 1;
  private static final double initialAngularRate = Math.PI / 2d;
  private final SimpleDateFormat sdf = new SimpleDateFormat("F/d/y H:m:s");

  public LocationActor() {

  }

  @Override
  public void onReceive(Object location) throws Exception {
    if (location instanceof LocationRecord) {
      /*
       * Update the motion filter
       */
      final DenseMatrix covar;
      final Vector infMean;

      final long prevTime = 0;
      
      final long timeDiff = 0;

      final StandardTrackingFilter filter = new StandardTrackingFilter(
          gVariance, aVariance);
      MultivariateGaussian belief = null;

      final Coordinate prevObsCoords = null;
      final Matrix O = StandardTrackingFilter.getObservationMatrix();
      
      Vector xyPoint = ((LocationRecord) location).getProjPoint();

//      belief = updateFilter(timeDiff, xyPoint, filter, belief);
      if (timeDiff > 0) {
        // filter.measure(belief, xyPoint);
        // filter.predict(belief);
        filter.update(belief, xyPoint);

        infMean = O.times(belief.getMean().clone());
        covar = (DenseMatrix) O.times(belief.getCovariance().times(
            O.transpose()));
      } else {
        covar = (DenseMatrix) O.times(belief.getCovariance().times(
            O.transpose()));
        infMean = O.times(belief.getMean());
      }

      final EigenDecompositionRightMTJ decomp = EigenDecompositionRightMTJ
          .create(covar);
      final Matrix Shalf = MatrixFactory.getDefault().createIdentity(2, 2);
      Shalf.setElement(0, 0, Math.sqrt(decomp.getEigenValue(0).getRealPart()));
      Shalf.setElement(1, 1, Math.sqrt(decomp.getEigenValue(1).getRealPart()));
      final Vector majorAxis = infMean.plus(decomp.getEigenVectorsRealPart()
          .times(Shalf).scale(1.98).getColumn(0));
      final Vector minorAxis = infMean.plus(decomp.getEigenVectorsRealPart()
          .times(Shalf).scale(1.98).getColumn(1));

      /*
       * Transform state mean position coordinates to lat, lon
       */
      final Coordinate kfMean = new Coordinate();
      JTS.transform(
          new Coordinate(infMean.getElement(0), infMean.getElement(1)), kfMean,
          Api.getTransform().inverse());
      final Coordinate kfMajor = new Coordinate();
      JTS.transform(
          new Coordinate(majorAxis.getElement(0), majorAxis.getElement(1)),
          kfMajor, Api.getTransform().inverse());
      final Coordinate kfMinor = new Coordinate();
      JTS.transform(
          new Coordinate(minorAxis.getElement(0), minorAxis.getElement(1)),
          kfMinor, Api.getTransform().inverse());

      // inference ingest here
      log.info("Message received:  "
          + ((LocationRecord) location).getTimestamp().toString());

    }

  }
}