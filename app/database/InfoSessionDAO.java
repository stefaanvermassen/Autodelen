/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.InfoSession;
import models.User;

/**
 *
 * @author Laurent
 */
public interface InfoSessionDAO {
    public InfoSession createInfoSession(User host, String address, String time) throws DataAccessException;
    public InfoSession getInfoSession(int id) throws DataAccessException;
    public void registerUser(InfoSession session, User user) throws DataAccessException;
}
