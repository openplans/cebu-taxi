package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class Phone extends Model {

	public String imei;
	
	@ManyToOne
	public Operator operator;
	
	@ManyToOne
	public Driver driver;
	
	@ManyToOne
	public Vehicle vehicle;
	
	public Date lastUpdate;
	
	public Boolean panic;
	    
    public Double recentLat;
    public Double recentLon;
    
    @Transient
    public List<MessageData> messages = new ArrayList<MessageData>();
    
    public void clearMessages()
    {
    	List<Message> m  = Message.find("fromPhone = ?", this).fetch();
    	for(Message message : m)
    	{
    		message.read = true;
    		message.save();
    	}
    }
    
    public void sendMessage(String message)
    {
    	Message m  = new Message();
    	
    	m.toPhone = this;
    	m.timestamp = new Date();
    	m.read = false;
    	m.body = message;
    	
    	m.save();
    }
    
    
    public void populateUnreadMessages()
    {
    	List<Message> m = Message.find("fromPhone = ? and read = false order by timestamp", this).fetch();
    	for(Message message : m)
    	{			
    		messages.add(new MessageData(message));
    	}
    }
}
 