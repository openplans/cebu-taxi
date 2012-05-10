require 'java'
require 'inference/inference_service'

class ApiController < ApplicationController
  def location
    inference_service = InferenceService.instance

    location = org.openplans.tools.tracking.impl.Observation.createObservation(
    params[:vehicleId], params[:timestamp], params[:latStr], params[:lonStr],
    params[:velocity], params[:heading], params[:accuracy])
    if location
      inference_service.processRecordFromWeb(location)
    end
    render :text=>""
  end

  def segment
    segmentId = params[:segmentId].to_i
    e = graph.getGraph().getEdgeById(segmentId)

    if e
      @segment = OsmSegment.new(segmentId, e.getGeometry())
    else
      render :text=>"Not Found", :status=>404
    end
  end

  # Process records from a trace. Note: flags are set that cause the records to
  # be handled differently.

  def traceLocation

    location = Observation.createObservation(
    params[:vehicleId], params[:timestamp], params[:latStr], params[:lonStr], params[:velocity], params[:heading], params[:accuracy])

    # TODO set flags for result record handling
    inference_service = InferenceService.instance
    inference_service.processRecord(location)
  end

  def traces
    inference_service = InferenceService.instance
    @traces = inference_service.getTraceResults(params[:vehicleId])
  end

  def vertex

    Rails.logger.info("vertices: " + graph.getVertexCount())

    render :text=>""
  end
  private

  def graph
    InferenceService.graph
  end
end
