java_import Java::gov.sandia.cognition.math.matrix.MatrixFactory
java_import Java::gov.sandia.cognition.math.matrix.mtj.decomposition.EigenDecompositionRightMTJ
java_import org.openplans.tools.tracking.impl.util.GeoUtils

class InferenceResultRecord

  attr_reader :originalLat, :originalLon, :kfMeanLat, :kfMeanLon, :kfMajorLat, :kfMajorLon, :kfMinorLat, :kfMinorLon, :graphSegmentIds, :time
  
  def initialize(time, originalLat,
    originalLon, kfMeanLat, kfMeanLon, kfMajorLat,
    kfMajorLon, kfMinorLat, kfMinorLon, idScaleList)
    @time = Date.new(time).strftime("%Y-%m-%d %H:%M:%S")
    @originalLat = originalLat
    @originalLon = originalLon
    @kfMeanLat = kfMeanLat
    @kfMeanLon = kfMeanLon
    @kfMajorLat = kfMajorLat
    @kfMajorLon = kfMajorLon
    @kfMinorLat = kfMinorLat
    @kfMinorLon = kfMinorLon
    @graphSegmentIds = idScaleList
  end

  def self.createInferenceResultRecord(observation, ie)

    belief = ie.bestState.getMovementBelief()

    if belief
      observationMatrix = ie.observationMatrix

      infMean = observationMatrix.times(belief.getMean().clone())
      covar = belief.getCovariance()
#      StandardTrackingFilter.checkPosDef(covar)

      decomp = EigenDecompositionRightMTJ.create(covar)
      shalf = MatrixFactory.getDefault().createIdentity(2, 2)
      shalf.setElement(0, 0, Math.sqrt(decomp.getEigenValue(0).getRealPart()))
      shalf.setElement(1, 1, Math.sqrt(decomp.getEigenValue(1).getRealPart()))
      majorAxis = infMean.plus(observationMatrix.times(decomp.getEigenVectorsRealPart().getColumn(0)).times(shalf).scale(1.98))
      minorAxis = infMean.plus(observationMatrix.times(decomp.getEigenVectorsRealPart().getColumn(1)).times(shalf).scale(1.98))

      kfMean = GeoUtils.convertToLatLon(infMean)
      kfMajor = GeoUtils.convertToLatLon(majorAxis)
      kfMinor = GeoUtils.convertToLatLon(minorAxis)
      
      idScaleList = []
      
      for edge in ie.bestState.getInferredPath() do
        if edge == InferredGraph.getEmptyEdge()
          next
        end
         #FIXME TODO we should probably be using the edge convolutions at each step.

        mean = edge.getVelocityPrecisionDist().getLocation()
        idScaleList << [edge.getEdgeId(), mean]
      end

      return InferenceResultRecord.new(observation.getTimestamp().getTime(),
          observation.getObsCoords().y, observation.getObsCoords().x,
          kfMean.y, kfMean.x, 
          kfMajor.y, kfMajor.x, 
          kfMinor.y, kfMinor.x,
          idScaleList)
    end

    return None
  end

end
