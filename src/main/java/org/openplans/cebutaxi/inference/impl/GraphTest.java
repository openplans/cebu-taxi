package org.openplans.cebutaxi.inference.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

import com.vividsolutions.jts.geom.Coordinate;

import au.com.bytecode.opencsv.CSVReader;

public class GraphTest {

  private static final org.apache.log4j.Logger log = Logger .getLogger(GraphTest.class);

  public static void main(String[] args) {
    GraphServiceImpl gs = new GraphServiceImpl();
    GraphBundle bundle = new GraphBundle(new File("../OpenTripPlanner/cebu-osm-bundle"));
    gs.setBundle(bundle);
    gs.refreshGraph();
    Graph graph = gs.getGraph();
    
    StreetVertexIndexServiceImpl indexService = new StreetVertexIndexServiceImpl(graph);
    indexService.setup();
    
    TraverseOptions options = new TraverseOptions(TraverseMode.CAR);
    
    SimpleDateFormat sdf = new SimpleDateFormat("d/F/y H:m:s");
    
    final CSVReader gps_reader;
    try {
      gps_reader = new CSVReader(
          new FileReader("src/main/resources/org/openplans/cebutaxi/test_data/Cebu-Taxi-GPS/Day4-Taxi-1410-2101.txt"), '\t');
      String[] nextLine;
      gps_reader.readNext();
      log.info("processing gps data");
  
      while ((nextLine = gps_reader.readNext()) != null) {
        log.info("processing record time " + nextLine[0] + " " + nextLine[1]);
        Date datetime = sdf.parse(nextLine[0] + " " + nextLine[1]);
        double lat = Double.parseDouble(nextLine[2]);
        double lon = Double.parseDouble(nextLine[3]);
        
        Coordinate obsPoint = new Coordinate(lon, lat);
        log.info("attempting snap to graph for point " + obsPoint.toString());
        
        Vertex snappedVertex =  indexService.getClosestVertex(obsPoint, null, options);
        if (snappedVertex != null) {
          double dist = snappedVertex.distance(obsPoint);
          log.info("distance to graph for point: " + dist);
          log.info("street=" + snappedVertex.getName());
        }
//        processGps(datetime, lat, lon);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    
    


  }

}
