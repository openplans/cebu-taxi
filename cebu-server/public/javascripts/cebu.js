
var startLatLng = new L.LatLng(10.3181373, 123.8956844); 

var mbUrl = 'http://{s}.tiles.mapbox.com/v3/openplans.map-g4j0dszr/{z}/{x}/{y}.png';

var cebuUrl = 'http://ec2-50-16-122-177.compute-1.amazonaws.com/tiles/{z}/{x}/{y}.png';

var mbAttrib = 'Traffic overlay powered by OpenPlans Vehicle Tracking Tools, Map tiles &copy; Mapbox (terms).';
var mbOptions = {
  maxZoom : 17,
  attribution : mbAttrib
};

		
$(document).ready(function() {

  map = new L.Map('map');

  var mb = new L.TileLayer(mbUrl, mbOptions);
  map.addLayer(mb);

  var cebu = new L.TileLayer(cebuUrl, mbOptions);
  map.addLayer(cebu);

  map.setView(startLatLng, 15, true);


});
