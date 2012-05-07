package inference;

import org.openplans.tools.tracking.impl.InferredGraph;
import org.openplans.tools.tracking.impl.InferredGraph.InferredEdge;
import org.openplans.tools.tracking.impl.Observation;
import org.openplans.tools.tracking.impl.Standard2DTrackingFilter;
import org.openplans.tools.tracking.impl.VehicleState;
import org.openplans.tools.tracking.impl.VehicleTrackingFilter;

import gov.sandia.cognition.math.UnivariateStatisticsUtil;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.statistics.DataDistribution;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;


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

  final private String vehicleId;
  private VehicleTrackingFilter filter;

  private final long prevTime = 0;

  final Matrix observationMatrix = Standard2DTrackingFilter
      .getObservationMatrix();

  private DataDistribution<VehicleState> belief;
  private VehicleState bestState;
  private static InferredGraph inferredGraph = new InferredGraph(Api.getGraph());

  public InferenceInstance(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public VehicleState getBestState() {
    return bestState;
  }

  public Matrix getObservationMatrix() {
    return observationMatrix;
  }

  public long getPrevTime() {
    return prevTime;
  }

  public DataDistribution<VehicleState> getStateBelief() {
    return belief;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  /**
   * Update the tracking filter and the graph's edge-velocity distributions.
   * 
   * @param record
   */
  public void update(Observation obs) {

    updateFilter(obs);
    
    /*
     * Use some estimate of the best state 
     * TODO determine which is best for us
     * FIXME probably shouldn't do this here
     */
    bestState = belief.getMaxValueKey();
    final MultivariateGaussian movementBelief = bestState.getMovementBelief();


    for (final InferredEdge edge : bestState.getInferredPath()) {
      // FIXME simply a hack for now (mean coordinates velocity)
      edge.updateVelocity(UnivariateStatisticsUtil.computeMean(Lists
          .newArrayList(new Double[] {
              movementBelief.getMean().getElement(1),
              movementBelief.getMean().getElement(3) })));
    }

  }

  private void updateFilter(Observation obs) {

    if (filter == null || belief == null) {
      filter = new VehicleTrackingFilter(obs, inferredGraph);
      belief = filter.createInitialLearnedObject();
    } else {
      filter.update(belief, obs);
    }

  }

}
