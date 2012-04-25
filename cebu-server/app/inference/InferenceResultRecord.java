package inference;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.decomposition.EigenDecompositionRightMTJ;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;

import java.util.Date;
import java.util.List;

import utils.GeoUtils;

import async.LocationRecord;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

import controllers.Api;

public class InferenceResultRecord {

  private final String time;
  private final double originalLat;
  private final double originalLon;
  private final double kfMeanLat;
  private final double kfMeanLon;
  private final double kfMajorLat;
  private final double kfMajorLon;
  private final double kfMinorLat;
  private final double kfMinorLon;
  private final List<Integer> graphSegmentIds;

  private InferenceResultRecord(long time, double originalLat,
    double originalLon, double kfMeanLat, double kfMeanLon, double kfMajorLat,
    double kfMajorLon, double kfMinorLat, double kfMinorLon,
    List<Integer> graphSegmentIds) {
    this.time = Api.sdf.format(new Date(time));
    this.originalLat = originalLat;
    this.originalLon = originalLon;
    this.kfMeanLat = kfMeanLat;
    this.kfMeanLon = kfMeanLon;
    this.kfMajorLat = kfMajorLat;
    this.kfMajorLon = kfMajorLon;
    this.kfMinorLat = kfMinorLat;
    this.kfMinorLon = kfMinorLon;
    this.graphSegmentIds = graphSegmentIds;
  }

  public List<Integer> getGraphSegmentIds() {
    return graphSegmentIds;
  }

  public double getKfMajorLat() {
    return kfMajorLat;
  }

  public double getKfMajorLon() {
    return kfMajorLon;
  }

  public double getKfMeanLat() {
    return kfMeanLat;
  }

  public double getKfMeanLon() {
    return kfMeanLon;
  }

  public double getKfMinorLat() {
    return kfMinorLat;
  }

  public double getKfMinorLon() {
    return kfMinorLon;
  }

  public double getOriginalLat() {
    return originalLat;
  }

  public double getOriginalLon() {
    return originalLon;
  }

  public String getTime() {
    return time;
  }

  public static InferenceResultRecord createInferenceResultRecord(
    LocationRecord locationRecord, InferenceInstance ie,
    SnappedEdges snappedEdges) {

    final MultivariateGaussian belief = ie.getBelief();

    if (belief != null) {
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

      final Coordinate kfMean = GeoUtils.convertToLatLon(infMean);
      final Coordinate kfMajor = GeoUtils.convertToLatLon(majorAxis);
      final Coordinate kfMinor = GeoUtils.convertToLatLon(minorAxis);

      return new InferenceResultRecord(locationRecord.getTimestamp().getTime(),
          locationRecord.getObsCoords().x, locationRecord.getObsCoords().y,
          kfMean.x, kfMean.y, kfMajor.x, kfMajor.y, kfMinor.x, kfMinor.y,
          Lists.newArrayList(snappedEdges.getSnappedEdges()));
    }

    return null;
  }

}
