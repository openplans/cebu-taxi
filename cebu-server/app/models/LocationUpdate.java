package models;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.hibernate.annotations.Type;
import org.openplans.tools.tracking.impl.ObservationData;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import play.db.jpa.Model;

@Entity
public class LocationUpdate extends Model {
	
	public String imei;
    
	public Date timestamp;
	
	public Date received;
	public Date sent;
	
    public Double lat;
    public Double lon;
    
    public Boolean panic;
    
    public Boolean boot;  
    public Boolean charging;
    public Boolean failedNetwork;
    public Double battery;
    
    public Double velocity;
    public Double heading;
    public Double gpsError;
    
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Point shape;
    
    public ObservationData getObservationData()
    {
    	ObservationData obsData = new ObservationData(this.imei, this.timestamp, new Coordinate(this.lat, this.lon), this.velocity, this.heading, this.gpsError);
    	
    	return obsData;
    }
    
    public Long getMinutesSinceLastUpdate()
    {
    	LocationUpdate lastUpdate = LocationUpdate.find("imei = ? and id < ? order by id desc", this.imei, this.id).first();
    	
    	if(lastUpdate != null)
    	{
    		return (this.received.getTime() - lastUpdate.received.getTime()) / 1000 / 60;  
    	}
    	else 
    		return new Long(0);
    }
    
    public Long getMinutesSinceLastGoodNetwork()
    {
    	LocationUpdate lastUpdate = LocationUpdate.find("imei = ? and id < ? and failednetwork = false order by id desc", this.imei, this.id).first();
    	
    	if(lastUpdate != null)
    	{
    		return (this.received.getTime() - lastUpdate.received.getTime()) / 1000 / 60;  
    	}
    	else 
    		return new Long(0);
    }
    
    static public void natveInsert(EntityManager em, String imei, ObservationData obs, Boolean charging, Double battery, Date sent, Date received, Boolean boot, Boolean failedNetwork)
    {
    	Query idQuery = em().createNativeQuery("SELECT NEXTVAL('hibernate_sequence');");
    	BigInteger nextId = (BigInteger)idQuery.getSingleResult();
    	
    	if(obs != null)
    	{
	    	em.createNativeQuery("INSERT INTO locationupdate (id, imei, timestamp, lat, lon, velocity, heading, gpserror, shape, charging, battery, sent, received, boot, failednetwork)" +
	    			"  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText( ?, 4326), ?, ?, ?, ?, ?, ?);")
	    			.setParameter(1,  nextId)
	    			.setParameter(2,  obs.getVehicleId())
	    			.setParameter(3,  obs.getTimestamp())
	    			.setParameter(4,  obs.getObsCoordsLatLon().y)
	    			.setParameter(5,  obs.getObsCoordsLatLon().x)
	    			.setParameter(6,  obs.getVelocity())
	    			.setParameter(7,  obs.getHeading())
	    			.setParameter(8,  obs.getAccuracy())
	    			.setParameter(9,  "POINT(" + obs.getObsCoordsLatLon().y +  " " + obs.getObsCoordsLatLon().x + ")")
	    			.setParameter(10, charging)
	    			.setParameter(11, battery)
	    			.setParameter(12, sent)
	    			.setParameter(13, received)
	    			.setParameter(14, boot)
	    			.setParameter(15, failedNetwork)
	    			.executeUpdate();
    	}
    	else
    	{
    		em.createNativeQuery("INSERT INTO locationupdate (id, imei, charging, battery, sent, received, boot, failednetwork)" +
	    			"  VALUES(?, ?, ?, ?, ?, ?, ?, ?);")
	    			.setParameter(1,  nextId)
	    			.setParameter(2,  imei)
	    			.setParameter(3, charging)
	    			.setParameter(4, battery)
	    			.setParameter(5, sent)
	    			.setParameter(6, received)
	    			.setParameter(7, boot)
	    			.setParameter(8, failedNetwork)
	    			.executeUpdate();
    	
    	}
    }

}
 
