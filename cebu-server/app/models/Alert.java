package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Alert extends Model {
	
    public String type;
    public String title;
    public Date timestamp;
    
    public Boolean active;
    
    public Double location_lat;
    public Double location_lon;
    
    @Column(columnDefinition="TEXT")
    public String description;
    
    @ManyToOne
    public Account account;
    
}
