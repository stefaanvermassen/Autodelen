package database.mocking;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import models.Address;
import models.Enrollee;
import models.EnrollementStatus;
import models.InfoSession;
import models.InfoSessionType;
import models.User;

import org.joda.time.DateTime;

import database.DataAccessException;
import database.Filter;
import database.InfoSessionDAO;
import database.FilterField;
import database.jdbc.JDBCFilter;

public class TestInfoSessionDAO implements InfoSessionDAO {
	
	private List<InfoSession> sessions;
	private int idCounter;
	
	
	public TestInfoSessionDAO(){
		sessions = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==id){
				InfoSession newSession = new InfoSession(session.getId(), session.getType(), session.getTime(), session.getAddress(), session.getHost(), session.getMaxEnrollees());
				if(withAttendees){
					List<Enrollee> enrolled = new ArrayList<>();
					for(Enrollee enrollee : session.getEnrolled()){
						enrolled.add(enrollee);
					}
					newSession.setEnrolled(enrolled);
				}
				return session;
			}
		}
		return null;
	}

	@Override
	public boolean deleteInfoSession(int id) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==id){
				sessions.remove(session);
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateInfoSessionAddress(InfoSession session) throws DataAccessException {
		// is ok
	}

	@Override
	public void registerUser(InfoSession session, User user) throws DataAccessException {
		session.addEnrollee(new Enrollee(user, EnrollementStatus.ENROLLED));
	}

	@Override
	public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException {
		session.addEnrollee(new Enrollee(user, status));
		
	}

	@Override
	public void unregisterUser(InfoSession session, User user)	throws DataAccessException {
		for(Enrollee enrollee : session.getEnrolled()){
			if(enrollee.getUser().getId()==user.getId()){
				session.deleteEnrollee(enrollee);
				return;
			}
		}		
	}

	@Override
	public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==infoSessionId){
				for(Enrollee enrollee : session.getEnrolled()){
					if(enrollee.getUser().getId()==userId){
						session.deleteEnrollee(enrollee);
						return;
					}
				}
			}
		}
	}

	@Override
	public InfoSession getAttendingInfoSession(User user) throws DataAccessException {
		for(InfoSession session : sessions){
			for(Enrollee enrollee : session.getEnrolled()){
				if(enrollee.getUser().getId()==user.getId()){
					return session;
				}
			}
		}
		return null;
	}

	@Override
	public Filter createInfoSessionFilter() {
		return new JDBCFilter();
	}

	@Override
	public InfoSession createInfoSession(InfoSessionType type, User host,
			Address address, DateTime time, int maxEnrollees)
			throws DataAccessException {
		InfoSession session = new InfoSession(idCounter++, type, time, address, host, maxEnrollees);
		sessions.add(session);
		return session;
	}

	@Override
	public int getAmountOfInfoSessions(Filter filter) throws DataAccessException {
		return 0; // TODO: implement filter methods
	}

    @Override
    public List<InfoSession> getInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        return null;
    }

	@Override
	public void updateInfosessionTime(InfoSession session)
			throws DataAccessException {
		// ok
		
	}

}
