/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Address;
import models.InfoSession;
import models.User;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface InfoSessionDAO {
    public InfoSession createInfoSession(User host, Address address, DateTime time) throws DataAccessException;
    public InfoSession getInfoSession(int id) throws DataAccessException;
    public boolean deleteInfoSession(int id) throws DataAccessException;
    public List<InfoSession> getInfoSessionsAfter(DateTime since) throws DataAccessException;
    public void updateInfoSession(InfoSession session) throws DataAccessException;
    public void registerUser(InfoSession session, User user) throws DataAccessException;
}
