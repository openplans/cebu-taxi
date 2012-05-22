require 'java'
require 'inference/inference_service'

class ApiController < ApplicationController

  def index
  end

  def location

    imei = params[:imei]

    imei.gsub!(/\W/, '')

    datetime, latitude, longitude, speed, heading, gpserror = request.raw_post.split ','

    #store CSV

    datetime =~ /(\d{4})(\d{2})(\d{2})T(\d{2})(\d{2})(\d{2})/
    datetime = "#{$1}-#{$2}-#{$3} #{$4}:#{$5}:#{$6}"

    Storage.store(imei + ".csv")

    inference_service = InferenceService.instance

    location = org.openplans.tools.tracking.impl.Observation.createObservation(
       imei, datetime, latitude, longitude, speed, heading, gpserror)

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
