package utils;

import java.io.File;

import org.opentripplanner.model.GraphBundle;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

import com.typesafe.plugin.inject.InjectPlugin;
import com.typesafe.plugin.inject.ManualInjectionPlugin;

import play.Logger;
import play.api.Play;

public class OtpGraphImpl implements OtpGraph {
	
	private final GraphServiceImpl gs;
	private final GraphBundle bundle;
		
	private final Graph graph; 
	  
	private final StreetVertexIndexServiceImpl indexService;
	
	public OtpGraphImpl()
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
	
	public int getVertexCount()
	{
		return graph.getVertices().size();
	}
		
}
