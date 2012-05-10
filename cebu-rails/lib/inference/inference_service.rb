require 'monitor'
require 'java'
java_import org.openplans.tools.tracking.impl.util.OtpGraph

class InferenceService
  include MonitorMixin

  @@graph = OtpGraph.new

  def self.graph
    return @@graph
  end

  def initialize
    super
    @vehicleToInstance = {}
    @vehicleToTraceResults = {}
  end
  
  @@instance = InferenceService.new
  def self.instance
    @@instance
  end

  def clearInferenceData
    @vehicleToInstance = {}
    @vehicleToTraceResults = {}
  end
  
  def processRecord(observation)
    ie = getInferenceInstance(observation.getVehicleId())
    ie.update(observation)
    infResult = InferenceResultRecord.createInferenceResultRecord(observation, ie)
    
    @vehicleToTraceResults[observation.getVehicleId()] ||= []
    if !@vehicleToTraceResults[observation.getVehicleId()].member? infResult
      @vehicleToTraceResults[observation.getVehicleId()] << infResult
    end
  end
  
  def processRecordFromWeb(observation)
    synchronize do
      processRecord(observation)
      Rails.logger.info("Message received:  " + observation.getTimestamp().to_s)
    end
  end
  
  def getInferenceInstance(vehicleId)
    ie = @vehicleToInstance[vehicleId]

    unless ie
      ie = InferenceInstance.new(vehicleId)
      @vehicleToInstance[vehicleId] = ie
    end

    return ie;
  end

  def getTraceResults(vehicleId)
    return @vehicleToTraceResults[vehicleId]
  end
  private_class_method :new
end
