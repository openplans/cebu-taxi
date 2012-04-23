package utils;

import java.io.File;

import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

import play.Logger;

public class OtpGraph {
	
	public final GraphServiceImpl gs;
	public final GraphBundle bundle;
		
	public final Graph graph; 
	  
	public final StreetVertexIndexServiceImpl indexService;
	
	public OtpGraph()
	{
		  Logger.info("Loading OTP graph...");
	  
		  gs = new GraphServiceImpl();
	  
		  bundle = new GraphBundle(new File("../src/main/resources/org/openplans/cebutaxi/"));
	  
		  gs.setBundle(bundle);
		  gs.refreshGraph();
	  
		  graph = gs.getGraph();
	  
		  indexService = new StreetVertexIndexServiceImpl(graph);
		  indexService.setup();
		  
		  Logger.info("Graph loaded..");
	  }

}
