package models;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarInsurance {

	private Integer id;
	private DateTime expiration;
	private Integer bonusMalus;
	private Integer polisNr;

    public CarInsurance(DateTime expiration, Integer bonusMalus, Integer polisNr) {
        this(null, expiration, bonusMalus, polisNr);
    }
	
	public CarInsurance(Integer id, DateTime expiration, Integer bonusMalus, Integer polisNr) {
		this.id=id;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
	}
	
	
	public DateTime getExpiration() {
		return expiration;
	}
	public Integer getPolisNr() {
		return polisNr;
	}

	public void setPolisNr(Integer polisNr) {
		this.polisNr = polisNr;
	}

	public void setExpiration(DateTime expiration) {
		this.expiration = expiration;
	}
	public Integer getBonusMalus() {
		return bonusMalus;
	}
	public void setBonusMalus(Integer bonusMalus) {
		this.bonusMalus = bonusMalus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
