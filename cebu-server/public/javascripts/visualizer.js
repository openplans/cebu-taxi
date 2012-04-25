/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

var dataUrl = "/api/traces?vehicleId=861785000285407";

var startLatLng = new L.LatLng(10.3181373, 123.8956844); // Portland OR

var map;

var vertexLayer = null, edgeLayer = null;

var group = new L.LayerGroup();
var overlay = new L.LayerGroup();

var lines = null;

var i = 0;

var marker1 = null;
var marker1 = null;

var interval = null;
/* INITIALIZATION */



$(document).ready(function() {

    map = new L.Map('map');

    var cloudmadeUrl = 'http://{s}.tiles.mapbox.com/v3/mapbox.mapbox-streets/{z}/{x}/{y}.png',
        cloudmadeAttrib = 'Map data &copy; 2011 OpenStreetMap contributors, Imagery &copy; 2011 CloudMade',
        cloudmade = new L.TileLayer(cloudmadeUrl, {maxZoom: 17, attribution: cloudmadeAttrib});
        
    map.setView(startLatLng, 15, true).addLayer(cloudmade);
       
   $("#controls").hide();
	$("#pause").hide();
   
    $("#loadData").click(loadData);
    $("#next").click(nextPoint);
    $("#prev").click(prevPoint);
    $("#play").click(playData);
    $("#pause").click(pauseData);
    $("#showData").click(showData);
    $("#playData").click(playData);
    
    

	map.addLayer(group);
	map.addLayer(overlay);
   
	
});

function loadData()
{ 

    $.get(dataUrl, function(data){
	
	$("#loadData").hide();

	lines = data;
	$("#controls").show();

	initSlider();

	map.invalidateSize();
   });

}

function initSlider()
{
	$("#slider").slider({min: 0, max: lines.length});

	$( "#slider" ).bind( "slidechange", function(){ 
		i = $("#slider").slider( "option", "value" ); 
		moveMarker(); 	
	});

	$( "#slider" ).bind( "slide", function(event, ui) {
		pauseData();
	});

}

function playData()
{
	$("#play").hide();
	$("#pause").show();

	interval = setInterval(moveMarker, 500);
}

function pauseData()
{
	$("#play").show();
	$("#pause").hide();

	clearInterval(interval);
}


function nextPoint()
{
	pauseData();

	$("#slider").slider( "option", "value", i);
}

function prevPoint()
{
	pauseData();

	i = i - 2;

	$("#slider").slider( "option", "value", i);
}

function showData()
{
	group.clearLayers();

	for(line_id in lines)
	{
		if(line_id >0)
		{

			
			var new_marker = new L.Circle(new L.LatLng(parseFloat(lines[line_id].originalLat), parseFloat(lines[line_id].originalLon)), 10, {color: '#00c', lat: parseFloat(lines[line_id].kfMeanLat), lon: parseFloat(lines[line_id].kfMeanLon)});
			group.addLayer(new_marker);

			new_marker.on('click', function(e){
					
				overlay.clearLayers();

				var overlay_marker = new L.Circle(new L.LatLng(e.target.options.lat, e.target.options.lon), 10, {color: '#0c0'});
				overlay.addLayer(overlay_marker);
				
			});
		}	
	}
}

function moveMarker()
{
	if(i != $("#slider").slider( "option", "value"))
		$("#slider").slider( "option", "value", i);

	renderMarker();

	i++;
}

function renderMarker()
{
	if(i>0)
	{	
		group.clearLayers();
		overlay.clearLayers();

		

		var marker2 = new L.Circle(new L.LatLng(parseFloat(lines[i].kfMeanLat), parseFloat(lines[i].kfMeanLon)), 10, {fill: true, color: '#0c0'});
		group.addLayer(marker2);

		var majorAxis = new L.Polyline([new L.LatLng(parseFloat(lines[i].originalLat), parseFloat(lines[i].originalLon)),new L.LatLng(parseFloat(lines[i].kfMajorLat), parseFloat(lines[i].kfMajorLon))], {fill: true, color: '#c00'})
		
		group.addLayer(majorAxis);


		var minorAxis = new L.Polyline([new L.LatLng(parseFloat(lines[i].originalLat), parseFloat(lines[i].originalLon)),new L.LatLng(parseFloat(lines[i].kfMinorLat), parseFloat(lines[i].kfMinorLon))], {fill: true, color: '#c0c'});

		group.addLayer(minorAxis);

		var marker1 = new L.Circle(new L.LatLng(parseFloat(lines[i].originalLat), parseFloat(lines[i].originalLon)), 10, {fill: true, color: '#00c'});
		group.addLayer(marker1);


		map.panTo(new L.LatLng(parseFloat(lines[i].originalLat), parseFloat(lines[i].originalLon)));

		renderGraph();

		$("#count_display").html(lines[i].time + ' (' + i + ')');
	}
}


function renderGraph()
{
	for(var j in lines[i].graphSegmentIds)
	{
		$.get('/api/segment', {segmentId: lines[i].graphSegmentIds[j]}, function(data) {

			var geojson = new L.GeoJSON();
			geojson.addGeoJSON(data.geom);
			overlay.addLayer(geojson);

		});
	}

}



/* COMPONENTS FUNCTIONS */ 

function showComponents(event) {

    var url = hostname + '/opentripplanner-api-webapp/ws/components/polygons';
        
    $.ajax(url, {
        dataType: 'jsonp',
        success: function(data) {
            drawComponents(data.components);
        }
    });
}

function drawComponents(comps) {

    var geojson = new L.GeoJSON();
    
    for (var i = 0; i < comps.length; i++) {
        var obj = comps[i];
        for(x in obj) {
            console.log(" - "+obj[x]);
            geojson.addGeoJSON(obj[x]);
        }  
    }
        
    map.addLayer(geojson);        
}

function CSVToArray( strData, strDelimiter ){
    // Check to see if the delimiter is defined. If not,
    // then default to comma.
    strDelimiter = (strDelimiter || ",");

    // Create a regular expression to parse the CSV values.
    var objPattern = new RegExp(
            (
                    // Delimiters.
                    "(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +

                    // Quoted fields.
                    "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +

                    // Standard fields.
                    "([^\"\\" + strDelimiter + "\\r\\n]*))"
            ),
            "gi"
            );


    // Create an array to hold our data. Give the array
    // a default empty first row.
    var arrData = [[]];

    // Create an array to hold our individual pattern
    // matching groups.
    var arrMatches = null;


    // Keep looping over the regular expression matches
    // until we can no longer find a match.
    while (arrMatches = objPattern.exec( strData )){

            // Get the delimiter that was found.
            var strMatchedDelimiter = arrMatches[ 1 ];

            // Check to see if the given delimiter has a length
            // (is not the start of string) and if it matches
            // field delimiter. If id does not, then we know
            // that this delimiter is a row delimiter.
            if (
                    strMatchedDelimiter.length &&
                    (strMatchedDelimiter != strDelimiter)
                    ){

                    // Since we have reached a new row of data,
                    // add an empty row to our data array.
                    arrData.push( [] );

            }


            // Now that we have our delimiter out of the way,
            // let's check to see which kind of value we
            // captured (quoted or unquoted).
            if (arrMatches[ 2 ]){

                    // We found a quoted value. When we capture
                    // this value, unescape any double quotes.
                    var strMatchedValue = arrMatches[ 2 ].replace(
                            new RegExp( "\"\"", "g" ),
                            "\""
                            );

            } else {

                    // We found a non-quoted value.
                    var strMatchedValue = arrMatches[ 3 ];

            }


            // Now that we have our value string, let's add
            // it to the data array.
            arrData[ arrData.length - 1 ].push( strMatchedValue );
    }

    // Return the parsed data.
    return( arrData );
}


/* VERTEX/EDGE COUNTER */

function updateCount(event) {
    var sw = map.getBounds().getSouthWest();
    var ll = sw.lat+','+sw.lng;

    var ne = map.getBounds().getNorthEast();
    var ur = ne.lat+','+ne.lng;

    var url = hostname + '/opentripplanner-api-webapp/ws/internals/countFeatures';
            
    $.ajax(url, {
        data: { 
            lowerLeft: ll,
            upperRight: ur               
        },
        dataType: 'jsonp',

            
        success: function(data) {
            $("#count_display").html('v='+data.vertices+", e="+data.edges);
        }
    });
}

/* VERTEX FUNCTIONS */

var collapsedVertices;

function refreshVertices(event) {
    
    var sw = map.getBounds().getSouthWest();
    var ll = sw.lat+','+sw.lng;

    var ne = map.getBounds().getNorthEast();
    var ur = ne.lat+','+ne.lng;
  
    var url = hostname + '/opentripplanner-api-webapp/ws/internals/vertices';
        
    $.ajax(url, {
        data: { 
            lowerLeft: ll,
            upperRight: ur               
        },
        dataType: 'jsonp',
        
        success: drawVertices
    });
}

function drawVertices(data) {
    var total = 0;
    collapsedVertices = new Object();
    for (var i = 0; i < data.vertices.length; i++) {
        var v = data.vertices[i];

        var key = v.x+"#"+v.y;
        total++;
        if(!collapsedVertices.hasOwnProperty(key))
            collapsedVertices[key] = new Array(v);
        else 
            collapsedVertices[key].push(v);
            
    }
    console.log("total="+total+", collapsedVertices="+Object.keys(collapsedVertices).length)

    if(vertexLayer != null) map.removeLayer(vertexLayer);
    
    vertexLayer = new L.LayerGroup();
    for(var key in collapsedVertices) {
        var coords = key.split('#');
        var marker = new L.CircleMarker(new L.LatLng(parseFloat(coords[1]), parseFloat(coords[0])), { radius: 5 } );
        var vertexArr = collapsedVertices[key];
        var popupText = "";
        if(vertexArr.length > 8) popupText += "<div style='height:120px; overflow: auto;'>";
        popupText += "<b>"+vertexArr.length+" vertices here:</b>";
        for(var i = 0; i < vertexArr.length; i++) {
            popupText += "<br><a href='javascript:showVertexInfo(\""+key+"\","+i+")'>"+vertexArr[i].label+"</a>";
        }
        if(vertexArr.length > 8) popupText += "</div>";
        marker.bindPopup(popupText);
        vertexLayer.addLayer(marker);
    }
    map.addLayer(vertexLayer);
}
        
    
function showVertexInfo(key, index) {
    var contents = "", title="";
    var v = collapsedVertices[key][index];
    for(var prop in v) {
        contents += prop+": "+v[prop]+"<br>";                    
        if(prop == "label") title = v[prop]; 
    }
    $("<div style='font-size: 12px;' title='"+title+"'>"+contents+"</div>").dialog()
}

/* EDGE FUNCTIONS */

function refreshEdges(event) {
    
    console.log("edges");
    
    var sw = map.getBounds().getSouthWest();
    var ll = sw.lat+','+sw.lng;

    var ne = map.getBounds().getNorthEast();
    var ur = ne.lat+','+ne.lng;
  
    var url = hostname + '/opentripplanner-api-webapp/ws/internals/edges';
        
    $.ajax(url, {
        data: { 
            lowerLeft: ll,
            upperRight: ur               
        },
        dataType: 'jsonp',        
        success: drawEdges
    });
}

function drawEdges(data) {
    var count = 0;
    
    if(edgeLayer != null) map.removeLayer(edgeLayer);
    
    edgeLayer = new L.LayerGroup();
    
    for (var i = 0; i < data.edges.length; i++) {
        var e = data.edges[i];
        var coords = new Array();
        if(e.edge.geometry != null && e.edge.mode == "BUS") {
            count++;
            console.log(e);
            var geom = e.edge.geometry;
            for(var ci = 0; ci < geom.coordinates.length; ci++) {
                coords.push(new L.LatLng(geom.coordinates[ci][1], geom.coordinates[ci][0]));
            }
            
            var polyline = new L.Polyline(coords);                  
            edgeLayer.addLayer(polyline);
        }
        
    }
    map.addLayer(edgeLayer);
    console.log("edges w/ geom = "+count);
}


