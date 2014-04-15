package database.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import models.Car;
import models.CarInsurance;
import models.Reservation;
import database.CarInsuranceDAO;
import database.DataAccessException;

public class JDBCCarInsuranceDAO implements CarInsuranceDAO{
	
	private static final String[] AUTO_GENERATED_KEYS = {"insurance_id"};
	
	private Connection connection;
	
	private PreparedStatement createCarInsuranceStatement;
	private PreparedStatement updateCarInsuranceStatement;
	private PreparedStatement deleteCarInsuranceStatement;
	private PreparedStatement getCarInsurencesStatement;
	private PreparedStatement deleteAllCarInsuranceStatement;
	
	public JDBCCarInsuranceDAO(Connection connection){
		this.connection=connection;
	}
	
	private PreparedStatement getCreateCarInsuranceStatement() throws SQLException{
		if(createCarInsuranceStatement==null){
			createCarInsuranceStatement = connection.prepareStatement("INSERT INTO CarInsurances(insurance_car_id, insurance_expiration, insurance_contract_id, isurance_bonus_malus) VALUES(?,?,?,?)", AUTO_GENERATED_KEYS);
		}
		return createCarInsuranceStatement;
	}
	
	private PreparedStatement getUpdateCarInsuranceStatement() throws SQLException{
		if(updateCarInsuranceStatement==null){
			updateCarInsuranceStatement = connection.prepareStatement("UPDATE CarInsurances SET insurance_car_id=?, insurance_expiration=?, insurance_contract_id=?, isurance_bonus_malus=?) WHERE insurance_id=?");
		}
		return updateCarInsuranceStatement;
	}
	
	private PreparedStatement getDeleteCarInsuranceStatement() throws SQLException{
		if(deleteCarInsuranceStatement==null){
			deleteCarInsuranceStatement = connection.prepareStatement("DELETE FROM CarInsurance WHERE insurance_id=?");
		}
		return deleteCarInsuranceStatement;
	}
	
	private PreparedStatement getDeleteAllCarInsuranceStatement() throws SQLException{
		if(deleteAllCarInsuranceStatement==null){
			deleteAllCarInsuranceStatement = connection.prepareStatement("DELETE FROM CarInsurance WHERE insurance_car_id=?");
		}
		return deleteAllCarInsuranceStatement;
	}
	
	private PreparedStatement getAllCarInsuranceStatement() throws SQLException{
		if(deleteCarInsuranceStatement==null){
			deleteCarInsuranceStatement = connection.prepareStatement("SELECT * FROM CarInsurance WHERE insurance_car_id=?");
		}
		return deleteCarInsuranceStatement;
	}

	@Override
	public CarInsurance createCarInsurance(Date expiration, int bonus_malus, int polisNr, Car car) throws DataAccessException {
		try{
            PreparedStatement ps = getCreateCarInsuranceStatement();
            ps.setInt(1, car.getId());
            ps.setDate(2, new java.sql.Date(expiration.getTime()));
            ps.setInt(3, polisNr);
            ps.setInt(4, bonus_malus);
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                connection.commit();
                return new CarInsurance(keys.getInt(1), expiration, bonus_malus, polisNr, car);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new car insurance", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create car insurance", e);
        }
	}

	@Override
	public void updateCarInsurance(CarInsurance insurance) throws DataAccessException {
		try {
			PreparedStatement ps = getUpdateCarInsuranceStatement();
			ps.setInt(1, insurance.getCar().getId());
			ps.setDate(2, new java.sql.Date(insurance.getExpiration().getTime()));
			ps.setInt(3, insurance.getPolisNr());
			ps.setInt(4, insurance.getBonusMalus());
			
			
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not remove all insurances", ex);
		}
	}

	@Override
	public void deleteCarInsurance(CarInsurance insurance) throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteCarInsuranceStatement();
			ps.setInt(1, insurance.getId());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not remove insurance", ex);
		}
	}

	@Override
	public List<CarInsurance> getAllCarInsurances(Car car)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllCarInsurances(Car car) throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteAllCarInsuranceStatement();
			ps.setInt(1, car.getId());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not remove all insurances", ex);
		}
		
	}
	
}
