package utils;

import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.graph_builder.impl.map.StreetMatcher;

import com.vividsolutions.jts.geom.Coordinate;


public interface OtpGraph {
	
	public int getVertexCount();

	public GraphBundle getBundle();

	public Graph getGraph();

	public GraphServiceImpl getGs();

	public StreetVertexIndexServiceImpl getIndexService();

	public TraverseOptions getOptions();

	public StreetMatcher getStreetMatcher();
  
	public EdgeInformation getEdgeInformation(Edge edge);

	public SnappedEdges snapToGraph(Coordinate obsCoords, Coordinate prevObsCoords);

}
