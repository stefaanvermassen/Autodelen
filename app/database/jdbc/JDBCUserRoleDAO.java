package database.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

import models.UserRole;
import database.DataAccessException;
import database.UserRoleDAO;

public class JDBCUserRoleDAO implements UserRoleDAO{

	private Connection connection;
	private PreparedStatement insertUserRolesStatement;
    private PreparedStatement removeUserRolesStatement;
    private PreparedStatement getUserRolesStatement;
    
	public JDBCUserRoleDAO(Connection connection) {
		this.connection = connection;
	}
	
    private PreparedStatement getRemoveUserRolesStatement() throws SQLException{
    	if(removeUserRolesStatement == null){
    		removeUserRolesStatement = connection.prepareStatement("DELETE FROM UserRoles WHERE userrole_userid=? AND userrole_role=?");    		
    	}
    	return removeUserRolesStatement;
    }   

	private PreparedStatement getInsertUserRolesStatement() throws SQLException{
    	if(insertUserRolesStatement == null){
    		insertUserRolesStatement = connection.prepareStatement("INSERT INTO UserRoles(userrole_userid, userrole_role) VALUES (?,?)");
    	}
    	return insertUserRolesStatement;
    }
    
    private PreparedStatement getUserRolesStatement() throws SQLException {
    	if(getUserRolesStatement == null){
    		getUserRolesStatement = connection.prepareStatement("SELECT userrole_role FROM UserRoles WHERE userrole_userid = ?");
    	}
    	return getUserRolesStatement;
    }
    
	@Override
	public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException {
		try {
			PreparedStatement ps = getUserRolesStatement();
			ps.setInt(1, userId);
			EnumSet<UserRole> roleSet = EnumSet.of(UserRole.USER); // by default
			try (ResultSet rs = ps.executeQuery()){
				while(rs.next()){
						roleSet.add(UserRole.valueOf(rs.getString("userrole_role")));
                	}
                return roleSet;
			} catch (SQLException ex){
				throw new DataAccessException("Error reading resultset",ex);
			}			
		} catch (SQLException ex) {
			throw new DataAccessException("Could not get userroles",ex);
		}
		
	}

	@Override
	public void addUserRole(int userId, UserRole role) throws DataAccessException {
		try {
			PreparedStatement ps = getInsertUserRolesStatement();
			ps.setInt(1, userId);
			ps.setString(2, role.name());
			ps.executeUpdate();
		} 	catch (SQLException ex) {
			throw new DataAccessException("Could not add userrole",ex);
		}	
	}

	@Override
	public void removeUserRole(int userId, UserRole role) throws DataAccessException {
		try {
			PreparedStatement ps = getRemoveUserRolesStatement();
			ps.setInt(1, userId);
			ps.setString(2, role.name());
			ps.executeUpdate();
		} 	catch (SQLException ex) {
			throw new DataAccessException("Could not remove userrole",ex);
		}
	}

}
