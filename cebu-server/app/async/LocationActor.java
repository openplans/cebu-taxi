package async;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.decomposition.EigenDecompositionRightMTJ;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;
import inference.InferenceInstance;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.geotools.geometry.jts.JTS;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;

import controllers.Api;

public class LocationActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  // instantiate inference engine here
  private final SimpleDateFormat sdf = new SimpleDateFormat("F/d/y H:m:s");

  private final Map<String, InferenceInstance> vehicleToInstance = Maps
      .newConcurrentMap();

  public LocationActor() {

  }

  @Override
  public void onReceive(Object location) throws Exception {
    if (location instanceof LocationRecord) {

      final LocationRecord locationRecord = (LocationRecord) location;

      InferenceInstance ie = vehicleToInstance.get(locationRecord
          .getVehicleId());

      if (ie == null) {
        ie = new InferenceInstance(locationRecord.getVehicleId());
        vehicleToInstance.put(locationRecord.getVehicleId(), ie);
      }

      ie.update(locationRecord);

      /*
       * Update the motion filter
       */

      final MultivariateGaussian belief = ie.getBelief();
      final Matrix O = ie.getObservationMatrix();

      final Vector infMean = O.times(belief.getMean().clone());
      final DenseMatrix covar = (DenseMatrix) O.times(belief.getCovariance()
          .times(O.transpose()));

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

      log.info("Message received:  " + locationRecord.getTimestamp().toString());

    }

  }
}