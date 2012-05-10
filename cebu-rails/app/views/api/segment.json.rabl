object false
node :id do 
  @segment.id
end
node :segment do 
  RGeo::GeoJSON.encode @segment.geom
end
