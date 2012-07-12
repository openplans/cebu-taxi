package api;

import java.util.Date;

import javax.persistence.ManyToOne;

import models.Message;
import models.Phone;

public class MessageResponse {
	
	public Long message_id;
    public Date timestamp;
    
    public String body;
    
    public MessageResponse(Message message)
    {
    	if(message.parent != null)
    		this.message_id = message.parent.id;
    
    	this.timestamp = message.timestamp;
    	
    	this.body = message.body;
    }
}
	