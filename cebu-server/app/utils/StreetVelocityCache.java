package utils;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.codehaus.groovy.tools.shell.util.Logger;

import akka.event.Logging;

import models.StreetEdge;


public class StreetVelocityCache {

	HashMap<Integer,Double> streetVelocities = new HashMap<Integer,Double>();
	
	Double meanVelocity;
	public StreetVelocityCache()
	{	
		List edges = StreetEdge.getEdgeVelocityList();
		
		Double total = 0.0;
		
		for(Object o : edges)
		{
			Object[] r = (Object[])o;	
			total += (Double)r[1];
			
			streetVelocities.put((Integer)r[0], (Double)r[1]);
		}
		
		meanVelocity = total / edges.size();
	}

	public Double getStreetVelocity(Integer edgeId)
	{
		if(streetVelocities.containsKey(edgeId))
			return streetVelocities.get(edgeId);
		else
			return meanVelocity;
	}
	
}
