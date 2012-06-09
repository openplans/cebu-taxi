package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Phone extends Model {

	public String imei;
	public Operator operator;
	public Driver driver;
	public Vehicle vehicle;
}
 