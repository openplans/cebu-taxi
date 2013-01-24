package models;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.hibernate.annotations.Type;
import org.openplans.tools.tracking.impl.ObservationData;

import com.google.gson.annotations.Expose;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import play.db.jpa.Model;

@Entity
public class LocationUpdate extends Model {
	
	@Expose
	public String imei;
    
	public Date timestamp;
	
	@Expose
	public Date adjustedTimestamp;
	
	public Date received;
	public Date sent;
	
	@Expose
    public Double lat;
	@Expose
	public Double lon;
    
    public Boolean panic;
    
    public Boolean boot;
    public Boolean shutdown;  
    public Boolean charging;
    
    @Expose
    public Boolean failedNetwork;
    @Expose
    public Double battery;
    @Expose
    public Integer signal;
    
    @Expose
    public Double velocity;
    
    @Expose
    public Double heading;
    
    @Expose
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
    	
    	if(lastUpdate != null && this.received != null)
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
    
    static public void natveInsert(EntityManager em, String imei, ObservationData obs, Boolean charging, Double battery, Date original, Date sent, Date received, Boolean boot, Boolean shutdown, Boolean failedNetwork, Integer signal)
    {
    	Query idQuery = em().createNativeQuery("SELECT NEXTVAL('hibernate_sequence');");
    	BigInteger nextId = (BigInteger)idQuery.getSingleResult();
    	

    	em.createNativeQuery("INSERT INTO locationupdate (id, imei, adjustedtimestamp, lat, lon, velocity, heading, gpserror, shape, charging, battery, sent, received, boot, failednetwork, signal, shutdown, timestamp)" +
    			"  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText( ?, 4326), ?, ?, ?, ?, ?, ?, ?, ?, ?);")
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
    			.setParameter(16, signal)
    			.setParameter(17, shutdown)
    			.setParameter(18,  original)
    			.executeUpdate();
    	
    }
    
    static public void natveInsert(EntityManager em, String imei, Boolean charging, Double battery, Date original, Date adjusted, Date sent, Date received, Boolean boot, Boolean shutdown, Boolean failedNetwork, Integer signal)
    {
    	Query idQuery = em().createNativeQuery("SELECT NEXTVAL('hibernate_sequence');");
    	BigInteger nextId = (BigInteger)idQuery.getSingleResult();

		em.createNativeQuery("INSERT INTO locationupdate (id, imei, charging, battery, sent, received, boot, failednetwork, signal, shutdown, timestamp, adjustedtimestamp)" +
    			"  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")
    			.setParameter(1,  nextId)
    			.setParameter(2,  imei)
    			.setParameter(3, charging)
    			.setParameter(4, battery)
    			.setParameter(5, sent)
    			.setParameter(6, received)
    			.setParameter(7, boot)
    			.setParameter(8, failedNetwork)
    			.setParameter(9, signal)
    			.setParameter(10, shutdown)
    			.setParameter(11,  original)
    			.setParameter(12,  adjusted)
    			.executeUpdate();
    	
    }
    
    public void calcAdjustedTime()
    {
    	Long timeDelta = received.getTime() - sent.getTime();
		
		Calendar calendar = Calendar.getInstance();
	
		if(timestamp != null)
			calendar.setTimeInMillis(timestamp.getTime() + timeDelta);
		else
			calendar.setTimeInMillis(sent.getTime() + timeDelta);
    	
		em().createNativeQuery("UPDATE locationupdate SET adjustedtimestamp = ? WHERE id = ?;")
    			.setParameter(1,  calendar.getTime())
    			.setParameter(2,  this.id)
    			.executeUpdate();
    	
    }

}
 
