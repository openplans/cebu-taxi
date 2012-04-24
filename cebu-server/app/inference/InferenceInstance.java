package inference;

import java.util.List;

import gov.sandia.cognition.math.UnivariateStatisticsUtil;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;

import org.openplans.cebutaxi.inference.impl.StandardTrackingFilter;
import org.opentripplanner.routing.graph.Edge;

import utils.OtpGraph;

import async.LocationRecord;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

import controllers.Api;

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

  private long prevTime = 0;

  private final Coordinate prevObsCoords = null;

  final Matrix observationMatrix = StandardTrackingFilter
      .getObservationMatrix();

  private MultivariateGaussian belief;

  public InferenceInstance(String vehicleId) {

    this.vehicleId = vehicleId;
    this.filter = new StandardTrackingFilter(gVariance, aVariance);
  }

  public MultivariateGaussian getBelief() {
    return belief;
  }

  public StandardTrackingFilter getFilter() {
    return filter;
  }

  public Matrix getObservationMatrix() {
    return observationMatrix;
  }

  public Coordinate getPrevObsCoords() {
    return prevObsCoords;
  }

  public long getPrevTime() {
    return prevTime;
  }

  public String getVehicleId() {
    return vehicleId;
  }


  /**
   * Update the tracking filter and the graph's edge-velocity distributions.
   * @param record
   */
  public SnappedEdges update(LocationRecord record) {
    OtpGraph graph = Api.getGraph();
    SnappedEdges snappedEdges = null;
    if (graph != null) {
      // TODO when possible, use tracked graph locations.
      Coordinate obsCoords = record.getObsCoords();
      Coordinate prevObsCoords = record.getPrevLoc() != null ? record.getPrevLoc().getObsCoords() : null;
      snappedEdges = graph.snapToGraph(obsCoords, prevObsCoords);
    }
    
    updateFilter(record, snappedEdges);
    
    if (belief != null 
        && snappedEdges != null 
        && snappedEdges.getPathTraversed() != null) {
      for (Edge edge : snappedEdges.getPathTraversed()) {
        EdgeInformation edgeInfo = graph.getEdgeInformation(edge);
        
        // FIXME simply a hack for now (mean coordinates velocity)
        edgeInfo.updateVelocity(UnivariateStatisticsUtil.computeMean(
            Lists.newArrayList(new Double[] {belief.getMean().getElement(1), belief.getMean().getElement(3)})
            ));
      }
      
    }
    
    return snappedEdges;
    
  }

  /**
   * Update the tracking filter for the given pathTraversed.
   * 
   * @param record
   * @param snappedEdges
   */
  public void updateFilter(LocationRecord record, SnappedEdges snappedEdges) {
    
    // FIXME XXX TODO need to use path traversed
    
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

  public static double getAvariance() {
    return aVariance;
  }

  public static long getAvgTimeDiff() {
    return avgTimeDiff;
  }

  public static double getGvariance() {
    return gVariance;
  }

  public static double getInitialangularrate() {
    return initialAngularRate;
  }

  public static double getInitialAngularRate() {
    return initialAngularRate;
  }

}
