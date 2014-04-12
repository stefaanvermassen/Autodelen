package database.mocking;

import java.util.ArrayList;
import java.util.List;

import database.Filter;
import database.FilterField;
import models.Notification;
import models.User;

import org.joda.time.DateTime;

import database.DataAccessException;
import database.NotificationDAO;

public class TestNotificationDAO implements NotificationDAO{

	private List<Notification> notifications;
	private int idCounter;
	
	public TestNotificationDAO(){
		notifications = new ArrayList<>();
		idCounter=0;
	}

    @Override
    public int getAmountOfNotifications(Filter filter) throws DataAccessException {
        return 0;
    }

    @Override
	public List<Notification> getNotificationListForUser(int userId) throws DataAccessException {
		List<Notification> list = new ArrayList<>();
		for(Notification notification : notifications){
			if(notification.getUser().getId()==userId){
				list.add(notification);
			}
		}
		return list;
	}

    @Override
    public List<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        return null;
    }

    @Override
	public Notification createNotification(User user, String subject, String body, DateTime timestamp) throws DataAccessException {
		Notification notification = new Notification(idCounter++, user, false, subject, body, timestamp);
		notifications.add(notification);
		return notification;
	}

	@Override
	public int getNumberOfUnreadNotifications(int userId) throws DataAccessException {
		int counter = 0;
		for(Notification not : notifications){
			if(not.getUser().getId()==userId){
				counter++;
			}
		}
		return counter;
	}
	
}
