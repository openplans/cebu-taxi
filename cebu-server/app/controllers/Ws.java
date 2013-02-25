package controllers;

import static play.mvc.Http.WebSocketEvent.BinaryFrame;

import com.conveyal.trafficprobe.TrafficProbeProtos.LocationUpdate;

import play.Logger;
import play.mvc.Http.WebSocketClose;
import play.mvc.Http.WebSocketEvent;
import play.mvc.Http.WebSocketFrame;
import play.mvc.WebSocketController;

public class Ws extends WebSocketController {

	public static void location() {
	
		while(inbound.isOpen()) {
			
			try
			{
				WebSocketEvent e = await(inbound.nextEvent());
				
				if(e instanceof WebSocketFrame) {
	                  WebSocketFrame frame = (WebSocketFrame)e;
	                  
	                  if(frame.isBinary) {
	                	  
	                	  	byte[] data = frame.binaryData;
	                	  	
	                	  	LocationUpdate locationUpdate = LocationUpdate.parseFrom(data);
	                	  	
	                	  	Logger.info("Websocket binary frame recieved: " + locationUpdate.getLocationList().size() + " updates/" + data.length + " bytes from phone " + locationUpdate.getPhone());
	                	  	
	                	  	models.LocationUpdate.pbLocationUpdate(locationUpdate);
	                  }
	                  else {
	                	  
	                	  Logger.info("Websocket text frame recieved: " + frame.textData);
	                	  
	                	 // if(frame.textData.equals("CONNECTION_TEST"))
	                	 //	  outbound.send("CONNECTED");
	                  }
		                  
	             }
	             if(e instanceof WebSocketClose) {
	                 Logger.info("Socket closed");
	             }
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		        
	    }
	}
}
