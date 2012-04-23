package async;

import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.util.Date;

public class LocationRecord {

  private final String vehicleId;
  private final Date timestamp;
  private final double lat;
  private final double lon;
  private final double x;
  private final double y;
  private final Vector projPoint;
  private final Double velocity;
  private final Double heading;
  private final Double accuracy;

  public LocationRecord(String vehicleId, Date timestamp, double lat,
      double lon, double x, double y, Double velocity, Double heading,
      Double accuracy) {
    super();
    this.vehicleId = vehicleId;
    this.timestamp = timestamp;
    this.lat = lat;
    this.lon = lon;
    this.x = x;
    this.y = y;
    this.velocity = velocity;
    this.heading = heading;
    this.accuracy = accuracy;
    this.projPoint = VectorFactory.getDefault().createVector2D(x, y);
  }

  public Vector getProjPoint() {
    return projPoint;
  }
  
  public String getVehicleId() {
    return vehicleId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public Double getVelocity() {
    return velocity;
  }

  public Double getHeading() {
    return heading;
  }

  public Double getAccuracy() {
    return accuracy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accuracy == null) ? 0 : accuracy.hashCode());
    result = prime * result + ((heading == null) ? 0 : heading.hashCode());
    long temp;
    temp = Double.doubleToLongBits(lat);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
    result = prime * result + ((velocity == null) ? 0 : velocity.hashCode());
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LocationRecord other = (LocationRecord) obj;
    if (accuracy == null) {
      if (other.accuracy != null) {
        return false;
      }
    } else if (!accuracy.equals(other.accuracy)) {
      return false;
    }
    if (heading == null) {
      if (other.heading != null) {
        return false;
      }
    } else if (!heading.equals(other.heading)) {
      return false;
    }
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) {
      return false;
    }
    if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) {
      return false;
    }
    if (timestamp == null) {
      if (other.timestamp != null) {
        return false;
      }
    } else if (!timestamp.equals(other.timestamp)) {
      return false;
    }
    if (vehicleId == null) {
      if (other.vehicleId != null) {
        return false;
      }
    } else if (!vehicleId.equals(other.vehicleId)) {
      return false;
    }
    if (velocity == null) {
      if (other.velocity != null) {
        return false;
      }
    } else if (!velocity.equals(other.velocity)) {
      return false;
    }
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
      return false;
    }
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
      return false;
    }
    return true;
  }

}
