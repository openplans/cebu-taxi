package models;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Phone extends Model {

	public String imei;
	
	@OneToMany
	public Operator operator;
	
	@OneToMany
	public Driver driver;
	
	@OneToMany
	public Vehicle vehicle;
}
 