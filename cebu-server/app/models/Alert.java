package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Alert extends Model {
	
    public String type;
    public String title;
    public Date timestamp;
    
    public Double location_lat;
    public Double location_lon;
    
    public String description;
    
}
