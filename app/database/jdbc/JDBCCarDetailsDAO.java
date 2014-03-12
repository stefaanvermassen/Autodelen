package database.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.CarDetails;
import database.CarDetailsDAO;
import database.DataAccessException;

public class JDBCCarDetailsDAO implements CarDetailsDAO{
	
	private static final String[] AUTO_GENERATED_KEYS = {"details_id"};
	
	private Connection connection;
    private PreparedStatement getCarDetailsStatement;
    private PreparedStatement updateCarDetailsStatement;
    private PreparedStatement deleteCarDetailsStatement;
    private PreparedStatement createCarDetailsStatement;

    public JDBCCarDetailsDAO(Connection connection) {
        this.connection = connection;
    }
    
    private PreparedStatement getCarDetailsStatement() throws SQLException{
    	if(getCarDetailsStatement==null){
    		getCarDetailsStatement=connection.prepareStatement("SELECT * FROM TechnicalCarDetails WHERE details_id=?");
    	}
    	return getCarDetailsStatement;
    }
    
    private PreparedStatement getUpdateCarDetailsStatement() throws SQLException{
    	if(updateCarDetailsStatement==null){
    		updateCarDetailsStatement=connection.prepareStatement("UPDATE TechnicalCarDetails SET car_license_plate=?, car_chasis_number=?, car_registration=? WHERE details_id=?");
    	}
    	return updateCarDetailsStatement;
    }
    
    private PreparedStatement getDeleteCarDetailsStatement() throws SQLException {
    	if(deleteCarDetailsStatement==null){
    		deleteCarDetailsStatement=connection.prepareStatement("DELETE FROM TechnicalCarDetails WHERE details_id=?");
    	}
    	return deleteCarDetailsStatement;
    }
    
    private PreparedStatement getCreateCarDetailsStatement() throws SQLException {
    	if(createCarDetailsStatement==null){
    		createCarDetailsStatement=connection.prepareStatement("INSERT INTO TechnicalCarDetails(car_license_plate, car_chasis_number, car_registration) VALUES (?,?,?)",AUTO_GENERATED_KEYS);
    	}
    	return createCarDetailsStatement;
    }
    
    public static CarDetails populateCarDetails(ResultSet rs) throws SQLException{
    	return new CarDetails(rs.getInt("details_id"),rs.getString("car_license_plate"),rs.getString("car_registration"),rs.getInt("car_chasis_number"));
    }

	@Override
	public CarDetails createCarDetails(String licensePlate, int chasisNumber, String registration) throws DataAccessException {
		try {
			PreparedStatement ps = getCreateCarDetailsStatement();
			ps.setString(1, licensePlate);
			ps.setInt(2, chasisNumber);
			ps.setString(3,registration);
			ps.executeUpdate();
			try (ResultSet keys = ps.getResultSet()){
				keys.next();
				return populateCarDetails(keys);
			} catch (SQLException ex){
				throw new DataAccessException("failed to get primary key for new car details",ex);
			}
		} catch (SQLException ex){ 
			throw new DataAccessException("failer to create new car details", ex);
		}
	}

	@Override
	public void updateCarDetails(CarDetails carDetails) throws DataAccessException {
		try {
			PreparedStatement ps = getUpdateCarDetailsStatement();
			ps.setString(1, carDetails.getLicensePlate());
			ps.setString(2, carDetails.getRegistration());
			ps.setInt(3, carDetails.getChasisNumber());
			ps.setInt(4, carDetails.getId());
			if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car details update affected 0 rows.");
		} catch (SQLException ex){
			throw new DataAccessException("failed to update car details", ex);
		}		
	}

	@Override
	public CarDetails getCarDetails(int id) throws DataAccessException {
		try {
			PreparedStatement ps = getCarDetailsStatement();
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCarDetails(rs);
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading car details resultset", ex);
            }
		} catch (SQLException ex) {
			throw new DataAccessException("failed to get car details",ex);
		}
	}

	@Override
	public void deleteCarDetails(CarDetails carDetails)	throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteCarDetailsStatement();
			ps.setInt(1, carDetails.getId());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete car details",ex);
		}
	}	
}
