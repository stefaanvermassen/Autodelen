package database.mocking;

import java.util.List;
import java.util.ArrayList;

import models.Car;
import models.Reservation;
import models.User;

import org.joda.time.DateTime;

import database.DataAccessException;
import database.Filter;
import database.FilterField;
import database.ReservationDAO;

public class TestReservationDAO implements ReservationDAO{
	
	private List<Reservation> reservations;
	private int idCounter;
	
	public TestReservationDAO(){
		idCounter=0;
		reservations = new ArrayList<>();
	}

	@Override
	public Reservation createReservation(DateTime from, DateTime to, Car car, User user) throws DataAccessException {
		Reservation reservation = new Reservation(idCounter++, car, user,from,to);
		reservations.add(reservation);
		return reservation;
	}

	@Override
	public void updateReservation(Reservation reservation) throws DataAccessException {
		// ok
	}

	@Override
	public Reservation getReservation(int id) throws DataAccessException {
		for(Reservation reservation : reservations){
			if(reservation.getId()==id){
				return new Reservation(reservation.getId(), reservation.getCar(), 
						reservation.getUser(), reservation.getFrom(), reservation.getTo());
			}
		}
		return null;
	}

	@Override
	public void deleteReservation(Reservation reservation) throws DataAccessException {
		if(reservations.contains(reservation)){
			reservations.remove(reservation);
		}
	}

	@Override
	public List<Reservation> getReservationListForUser(int userId) throws DataAccessException {
		List<Reservation> list = new ArrayList<>();
		for(Reservation res : reservations){
			if(res.getUser().getId()==userId){
				list.add(res);
			}
		}
		return list;
	}

	@Override
	public List<Reservation> getReservationListForCar(int carID) throws DataAccessException {
		List<Reservation> list = new ArrayList<>();
		for(Reservation res : reservations){
			if(res.getCar().getId()==carID){
				list.add(res);
			}
		}
		return list;
	}
	
	@Override
	public int getAmountOfReservations(Filter filter)
			throws DataAccessException {
		return 0; // TODO: add filter methods
	}

	@Override
	public List<Reservation> getReservationListPage(FilterField orderBy,
			boolean asc, int page, int pageSize, Filter filter)
			throws DataAccessException {
		return null; // TODO: add filter methods
	}
	
}
