class OsmSegment
  attr_accessor :id, :geom
  @@factory = ::RGeo::Geographic.simple_mercator_factory()

  def initialize(id, geom) 
    @id = id
    #PORT: this is a hack to convert JTS Geometry objects to RGeo geometries
    @geom = @@factory.parse_wkt(geom.toText)
    
  end
end