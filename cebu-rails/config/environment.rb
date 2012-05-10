# Load the rails application
require File.expand_path('../application', __FILE__)

require 'java'

libdir = File.expand_path('../../lib', __FILE__)

Dir.foreach(libdir) do |f|
  if f.end_with? ".jar"
    require f
  end
end

# Initialize the rails application
CebuRails::Application.initialize!
