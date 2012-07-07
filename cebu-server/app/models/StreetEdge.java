package models;

import javax.persistence.Entity;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;

import play.db.jpa.Model;

@Entity
public class StreetEdge extends Model {
 
    public Integer edgeId;
    
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public LineString shape;
    
    public Double meanVelocity;
    public Double velocityVarience;
    
    public String rbgColor;
}
