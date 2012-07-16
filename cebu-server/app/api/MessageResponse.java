package api;

import java.util.Date;

import javax.persistence.ManyToOne;

import models.Message;
import models.Phone;

public class MessageResponse {
	
	public Long id;
	public Long parentId;
    public Date timestamp;
    
    public String body;
    
    public MessageResponse(Message message)
    {
    	this.id = message.id;
    	
    	if(message.parent != null)
    		this.parentId = message.parent.id;
    
    	this.timestamp = message.timestamp;
    	
    	this.body = message.body;
    }
}
	