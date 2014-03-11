package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarInsurance {

	private int id;
	private Date expiration;
	private int bonusMalus;
	private int polisNr;
	private Car car;
	
	
	public CarInsurance(int id,Date expiration, int bonus_malus , Car car){
		this(0,expiration,bonus_malus,0,car);
	}
	
	public CarInsurance(int id, Date expiration, int bonusMalus, int polisNr, Car car) {
		this.id=id;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
		this.car = car;
	}
	
	
	public Date getExpiration() {
		return expiration;
	}
	public int getPolisNr() {
		return polisNr;
	}

	public void setPolisNr(int polisNr) {
		this.polisNr = polisNr;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	public int getBonusMalus() {
		return bonusMalus;
	}
	public void setBonusMalus(int bonusMalus) {
		this.bonusMalus = bonusMalus;
	}
	public Car getCar() {
		return car;
	}
	public void setCar(Car car) {
		this.car = car;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	
}
