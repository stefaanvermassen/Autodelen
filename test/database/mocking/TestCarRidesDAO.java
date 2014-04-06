package database.mocking;

import java.util.ArrayList;
import java.util.List;

import models.CarRide;
import models.Reservation;
import database.CarRideDAO;
import database.DataAccessException;

public class TestCarRidesDAO implements CarRideDAO {
	
	private List<CarRide> rides;
	private int idCounter;
	
	public TestCarRidesDAO(){
		idCounter=0;
		rides = new ArrayList<>();
	}

	@Override
	public CarRide createCarRide(Reservation reservation) throws DataAccessException {
		CarRide ride = new CarRide(reservation);
		rides.add(ride);
		return ride;
	}

	@Override
	public CarRide getCarRide(int id) throws DataAccessException {
		for(CarRide ride : rides){
			if(ride.getReservation().getId()==id){
				return ride;
			}
		}
		return null;
	}

	@Override
	public void updateCarRide(CarRide carRide) throws DataAccessException {
		// ok
	}

}
