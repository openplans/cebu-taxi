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
    
    public Double v0;
    public Double v1;
    public Double v2;
    public Double v3;
    public Double v4;
    public Double v5;
    public Double v6;
    public Double v7;
    public Double v8;
    public Double v9;
    public Double v10;
    public Double v11;
    public Double v12;
    public Double v13;
    public Double v14;
    public Double v15;
    public Double v16;
    public Double v17;
    public Double v18;
    public Double v19;
    public Double v20;
    public Double v21;
    public Double v22;
    public Double v23;
    
    public Double c0;
    public Double c1;
    public Double c2;
    public Double c3;
    public Double c4;
    public Double c5;
    public Double c6;
    public Double c7;
    public Double c8;
    public Double c9;
    public Double c10;
    public Double c11;
    public Double c12;
    public Double c13;
    public Double c14;
    public Double c15;
    public Double c16;
    public Double c17;
    public Double c18;
    public Double c19;
    public Double c20;
    public Double c21;
    public Double c22;
    public Double c23;
    
    
    public String rbgColor;
}

