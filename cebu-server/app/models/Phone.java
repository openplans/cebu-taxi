package models;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

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
}
 