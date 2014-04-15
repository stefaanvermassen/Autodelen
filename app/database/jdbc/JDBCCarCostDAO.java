package database.jdbc;

import database.CarCostDAO;
import database.DataAccessException;
import database.DatabaseHelper;
import models.Car;
import models.CarCost;
import models.Message;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public class JDBCCarCostDAO implements CarCostDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"carcost_id"};
    private Connection connection;
    private PreparedStatement createCarCostStatement;

    public JDBCCarCostDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getCreateCarCostStatement() throws SQLException {
        if (createCarCostStatement == null) {
            createCarCostStatement = connection.prepareStatement("INSERT INTO CarCosts (car_cost_car_id, car_cost_amount, " +
                    "car_cost_description, car_cost_time, car_cost_mileage) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarCostStatement;
    }

    @Override
    public CarCost createCarCost(Car car, int amount, BigDecimal mileage, String description, DateTime time) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateCarCostStatement();
            ps.setInt(1, car.getId());
            ps.setInt(2, amount);
            ps.setString(3, description);
            //Todo: use actual time from form
            ps.setTimestamp(4, new Timestamp(new DateTime().getMillis()));
            ps.setBigDecimal(5, mileage);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating carcost.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new CarCost(keys.getInt(1), car, amount, mileage, description, time);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for carcost.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create carcost", e);
        }
    }
}
