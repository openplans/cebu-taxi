package utils;

import inference.EdgeInformation;
import inference.SnappedEdges;

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentripplanner.graph_builder.impl.map.StreetMatcher;
import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.location.StreetLocation;

import play.Logger;
import async.LocationRecord;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;

import com.typesafe.plugin.inject.InjectPlugin;
import com.typesafe.plugin.inject.ManualInjectionPlugin;

import play.api.Play;

public class OtpGraphImpl implements OtpGraph {
	
	private final GraphServiceImpl gs;
	private final GraphBundle bundle;
		
	private final Graph graph; 
	  
	private final StreetVertexIndexServiceImpl indexService;
	
	private final static TraverseOptions options = new TraverseOptions(
		      TraverseMode.CAR);

	private final Map<Edge, EdgeInformation> edgeToInfo = Maps.newConcurrentMap();
	
	private StreetMatcher streetMatcher;
	
	public OtpGraphImpl()
	{
		  Logger.info("Loading OTP graph...");
	  
		  gs = new GraphServiceImpl();
	  
		  bundle = new GraphBundle(new File("../src/main/resources/org/openplans/cebutaxi/"));
	  
		  gs.setBundle(bundle);
		  gs.refreshGraph();
	  
		  graph = gs.getGraph();
	  
		  streetMatcher = new StreetMatcher(graph);
		  indexService = new StreetVertexIndexServiceImpl(graph);
		  indexService.setup();
		  
		  Logger.info("Graph loaded..");
	}
	
	public int getVertexCount()
	{
		return graph.getVertices().size();
	}

	public GraphBundle getBundle() {
	    return bundle;
	  }

	  public Graph getGraph() {
	    return graph;
	  }

	  public GraphServiceImpl getGs() {
	    return gs;
	  }

	  public StreetVertexIndexServiceImpl getIndexService() {
	    return indexService;
	  }

	  public TraverseOptions getOptions() {
	    return options;
	  }

	  public StreetMatcher getStreetMatcher() {
	    return streetMatcher;
	  }
	  
	  public EdgeInformation getEdgeInformation(Edge edge) {
	    EdgeInformation edgeInfo = edgeToInfo.get(edge);
	    
      if (edgeInfo == null) {
        edgeInfo = new EdgeInformation(edge);
        edgeToInfo.put(edge, edgeInfo);
      }
      
	    return edgeInfo;
	  }

	  /**
	   * Snaps the observed location to a graph edge, computes edges traveled
	   * between observations (when applicable), and returns both sets of edges. 
	   * 
	   * @param loc
	   * @return
	   */
	  public SnappedEdges snapToGraph(Coordinate obsCoords, Coordinate prevObsCoords) {

	    final Vertex snappedVertex = indexService.getClosestVertex(obsCoords, null,
	        options);
	    final List<Edge> pathTraversed = Lists.newArrayList();
	    final Set<Integer> snappedEdges = Sets.newHashSet();
	    if (snappedVertex != null && (snappedVertex instanceof StreetLocation)) {
	      
	      final StreetLocation snappedStreetLocation = (StreetLocation) snappedVertex;
//	      final double dist = snappedVertex.distance(obsCoords);
	      
	      if (prevObsCoords != null && !prevObsCoords.equals2D(obsCoords)) {
	        final CoordinateSequence movementSeq = JTSFactoryFinder
	            .getGeometryFactory().getCoordinateSequenceFactory()
	            .create(new Coordinate[] { prevObsCoords, obsCoords });
	        final Geometry movementGeometry = JTSFactoryFinder.getGeometryFactory()
	            .createLineString(movementSeq);
	        
	        /*
	         * Find the edges between the two observed points.
	         */
	        final List<Edge> minimumConnectingEdges = streetMatcher
	            .match(movementGeometry);

	        for (final Edge edge : Objects.firstNonNull(minimumConnectingEdges,
	            ImmutableList.<Edge> of())) {
	          final Integer edgeId = graph.getIdForEdge(edge);
	          if (edgeId != null)
	            snappedEdges.add(edgeId);
	        }
	        
	        pathTraversed.addAll(minimumConnectingEdges);
	      } else {

	        /*
	         * Just find the edge for the isolate point
	         */
	        for (final Edge edge : Objects.firstNonNull(
	            snappedStreetLocation.getOutgoingStreetEdges(),
	            ImmutableList.<Edge> of())) {
	          final Integer edgeId = graph.getIdForEdge(edge);
	          if (edgeId != null) {
	            snappedEdges.add(edgeId);
	          }
	        }
	      }
	    }
	    return new SnappedEdges(snappedEdges, pathTraversed);
	  }
		
}
