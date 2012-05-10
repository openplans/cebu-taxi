java_import org.openplans.tools.tracking.impl.Standard2DTrackingFilter
java_import org.openplans.tools.tracking.impl.VehicleTrackingFilter
java_import org.openplans.tools.tracking.impl.InferredGraph
java_import Java::gov.sandia.cognition.math.UnivariateStatisticsUtil

 #This class holds inference data for a particular vehicle 
 # @author bwillard
class InferenceInstance
  attr_reader :bestState, :prevTime, :belief, :observationMatrix, :vehicleId
 
  @@inferredGraph = InferredGraph.new(InferenceService.graph)

  def initialize(vehicleId)
    @vehicleId = vehicleId
    @filter = nil
    @prevTime = 0
    @observationMatrix = Standard2DTrackingFilter.getObservationMatrix()
    @belief = nil
    @bestState = nil
    
  end
  
  #Update the tracking filter and the graph's edge-velocity distributions.
  def update(obs)
    updateFilter(obs)
    
     # Use some estimate of the best state 
     # TODO determine which is best for us
     # FIXME probably shouldn't do this here

    @bestState = @belief.getMaxValueKey()
    movementBelief = @bestState.getMovementBelief()

    for edge in @bestState.getInferredPath() do
      # FIXME simply a hack for now (mean coordinates velocity)
      edge.updateVelocity(UnivariateStatisticsUtil.computeMean([
              movementBelief.getMean().getElement(1),
              movementBelief.getMean().getElement(3)]))
    end
  end

  def updateFilter(obs)

    if @filter == nil || @belief == nil
      @filter = VehicleTrackingFilter.new(obs, @@inferredGraph)
      @belief = @filter.createInitialLearnedObject()
    else
      @filter.update(@belief, obs)
    end

  end
end
