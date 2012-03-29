package org.openplans.cebutaxi.inference.impl;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.signals.LinearDynamicalSystem;
import gov.sandia.cognition.statistics.bayesian.KalmanFilter;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultAffineCS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import au.com.bytecode.opencsv.CSVReader;

public class GraphTest {

  private static final org.apache.log4j.Logger log = Logger .getLogger(GraphTest.class);

  public static void main(String[] args) {
    GraphServiceImpl gs = new GraphServiceImpl();
    GraphBundle bundle = new GraphBundle(new File("src/main/resources/org/openplans/cebutaxi/"));
    
    gs.setBundle(bundle);
    gs.refreshGraph();
    Graph graph = gs.getGraph();
    
    StreetVertexIndexServiceImpl indexService = new StreetVertexIndexServiceImpl(graph);
    indexService.setup();
    
    TraverseOptions options = new TraverseOptions(TraverseMode.CAR);
    
    SimpleDateFormat sdf = new SimpleDateFormat("d/F/y H:m:s");
    
    
    /*
     * State transition matrix
     */
    Matrix G = MatrixFactory.getDefault().createIdentity(4, 4);
    G.setElement(0, 2, 1);
    G.setElement(1, 3, 1);
    
    /*
     * State covariance
     */
    Matrix A = MatrixFactory.getDefault().createIdentity(4, 4).scale(1d/4d);
    A.setElement(0, 2, 1d/2d);
    A.setElement(1, 3, 1d/2d);
    A.setElement(2, 0, 1d/2d);
    A.setElement(3, 1, 1d/2d);
    A.scale(20d);
    
    Matrix O = MatrixFactory.getDefault().createIdentity(2, 4);
    
    Matrix measurementCovariance = MatrixFactory.getDefault().createIdentity(2, 2).scale(20.0);
    Matrix modelCovariance = MatrixFactory.getDefault().createIdentity(4, 4).scale(50.0);
    
    LinearDynamicalSystem model = new LinearDynamicalSystem(G, A, O);
    KalmanFilter filter = new KalmanFilter(model, modelCovariance, measurementCovariance);
    MultivariateGaussian belief = null;
    
    final CSVReader gps_reader;
    final FileWriter test_output;
    try {
      String googleWebMercatorCode = "EPSG:4326";
      
      String cartesianCode = "EPSG:4499";
       
      CRSAuthorityFactory crsAuthorityFactory = CRS.getAuthorityFactory(true);
       
      CoordinateReferenceSystem mapCRS = crsAuthorityFactory.createCoordinateReferenceSystem(googleWebMercatorCode);
       
      CoordinateReferenceSystem dataCRS = crsAuthorityFactory.createCoordinateReferenceSystem(cartesianCode);
                             
      boolean lenient = true; // allow for some error due to different datums
      MathTransform transform = CRS.findMathTransform(mapCRS, dataCRS, lenient);
      
      test_output = new FileWriter("src/main/resources/org/openplans/cebutaxi/test_data/test_output.txt"); 
      test_output.write("time,original_lat,original_lon,kf_lat,kf_lon,graph_segment_id\n");
      
      
//      MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, 
//          DefaultGeocentricCRS.CARTESIAN);
      gps_reader = new CSVReader(
          new FileReader("src/main/resources/org/openplans/cebutaxi/test_data/Cebu-Taxi-GPS/Day4-Taxi-1410-2101.txt"), '\t');
      String[] nextLine;
      gps_reader.readNext();
      log.info("processing gps data");
  
      while ((nextLine = gps_reader.readNext()) != null) {
        
        StringBuilder sb = new StringBuilder();
        
        Date datetime = sdf.parse(nextLine[0] + " " + nextLine[1]);
        
        log.info("processing record time " + datetime.toString());
        
        double lat = Double.parseDouble(nextLine[2]);
        double lon = Double.parseDouble(nextLine[3]);
        
        sb.append(datetime.getTime()).append(",");
        sb.append(lat).append(",").append(lon).append(",");
        
        /*
         * Transform gps observation to cartesian coordinates
         */
        Coordinate obsCoords = new Coordinate(lon, lat);
        Coordinate obsPoint = new Coordinate();
        JTS.transform(obsCoords, obsPoint, transform);
        
        Vector xyPoint = VectorFactory.getDefault().createVector2D(obsPoint.x, obsPoint.y);
        
        /*
         * Initialize or update the kalman filter
         */
        if (belief == null) {
          belief = filter.createInitialLearnedObject();
          belief.setMean(xyPoint.stack(VectorFactory.getDefault().createVector2D(1.5, 1.5)));
        } else {
          filter.update(belief, xyPoint);
        }
        
        /*
         * Transform state mean position coordinates to lat, lon
         */
        Coordinate kfPoint = new Coordinate(belief.getMean().getElement(0), belief.getMean().getElement(1));
        Coordinate kfCoords= new Coordinate();
        JTS.transform(kfPoint, kfCoords, transform.inverse());
        sb.append(kfCoords.y).append(",").append(kfCoords.x).append(",");
        
        log.info("filter belief=" + belief.toString());
        
        log.info("attempting snap to graph for point " + obsCoords.toString());
        /*
         * Snap to graph
         */
        Vertex snappedVertex =  indexService.getClosestVertex(obsCoords, null, options);
        if (snappedVertex != null) {
          double dist = snappedVertex.distance(obsCoords);
          log.info("distance to graph: " + dist);
          log.info("vertexLabel=" + snappedVertex.getLabel());
          sb.append(snappedVertex.getName());
        } else {
          sb.append("NA");
        }
        
        test_output.write(sb.toString() + "\n");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (FactoryException e) {
      e.printStackTrace();
    } catch (MismatchedDimensionException e) {
      e.printStackTrace();
    } catch (TransformException e) {
      e.printStackTrace();
    }
    
    


  }

}
