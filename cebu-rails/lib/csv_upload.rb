require 'csv'

java_import org.openplans.tools.tracking.impl.Observation
java_import org.openplans.tools.tracking.impl.TimeOrderException

class CsvUpload
  def self.read(file)
    Rails.logger.info("processing gps data")
    
    #FIXME TODO reset only data relevant to a re-run trace.
    InferenceService.instance.clearInferenceData()
    Observation.clearRecordData()

    CSV::Reader.parse(file, ';') do |line|

      begin

        location = Observation.createObservation(line[3], line[1], line[5], 
                                                 line[7], line[10], nil, nil)
        #TODO set flags for result record handling
        InferenceService.instance.processRecord(location)
        Rails.logger.info("processed time: " + line[1])
      rescue TimeOrderException
        Rails.logger.info("bad time order: " + line) 
      rescue Exception => e
        Rails.logger.info("bad csv line: " + line.to_s + "\n Exception:" + e.to_s)  #bad line
        puts e.backtrace
        break
      end
    end
  end
end
