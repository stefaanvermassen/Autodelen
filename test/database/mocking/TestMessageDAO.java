package database.mocking;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import play.libs.Time;
import models.Message;
import models.User;
import database.DataAccessException;
import database.MessageDAO;

public class TestMessageDAO implements MessageDAO{
	
	private List<Message> messages;
	private int idCounter;
	
	public TestMessageDAO(){
		messages = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public int getNumberOfUnreadMessages(int userId) throws DataAccessException {
		int counter = 0;
		for(Message message : messages){
			if(!message.isRead()){
				counter++;
			}
		}
		return counter;
	}

	@Override
	public void markMessageAsRead(int messageID) throws DataAccessException {
		for(Message message : messages){
			if(message.getId()==messageID){
				message.setRead(true);
				return;
			}
		}
	}

	@Override
	public Message createMessage(User sender, User receiver, String subject,
			String body, DateTime timestamp) throws DataAccessException {
		Message message = new Message(idCounter++,sender, receiver, false, subject, body, timestamp);
		messages.add(message);
		return message;
	}
	
}
