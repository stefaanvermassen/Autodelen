/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import database.fields.CarField;
import database.fields.InfoSessionField;
import models.*;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface InfoSessionDAO {

    public Filter<InfoSessionField> createInfoSessionFilter();

    public InfoSession createInfoSession(InfoSessionType type, User host, Address address, DateTime time, int maxEnrollees) throws DataAccessException;
    public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException;
    public boolean deleteInfoSession(int id) throws DataAccessException;

    public int getAmountOfInfoSessions(Filter<InfoSessionField> filter) throws DataAccessException;
    public List<InfoSession> getInfoSessionsAfter(DateTime since) throws DataAccessException; // TODO: delete this method, use with pages
    public List<InfoSession> getInfoSessionsAfter(DateTime since, InfoSessionField orderBy, boolean asc, int page, int pageSize, Filter<InfoSessionField> filter) throws DataAccessException;
    // TODO: above method: get DateTime since in filter-argument

    public void updateInfosessionTime(InfoSession session) throws DataAccessException;
    public void updateInfoSessionAddress(InfoSession session) throws DataAccessException;
    public void registerUser(InfoSession session, User user) throws DataAccessException;
    public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException;
    public void unregisterUser(InfoSession session, User user) throws DataAccessException;
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException;
    public InfoSession getAttendingInfoSession(User user) throws DataAccessException;
}
