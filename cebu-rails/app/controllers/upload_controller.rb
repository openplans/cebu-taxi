require 'java'
require 'inference/inference_service'

class UploadController < ApplicationController
  def upload
    if request.method == 'GET'
      return
    end

    csv = params[:csv]

    if csv
      CsvUpload.read(csv)

      render :text=>"File uploaded"
    else
      return redirect_to(:action=>:upload)
    end

    
  end

end
