package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.MessageDAO;
import models.Message;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.messages;

import java.util.List;

public class Messages extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result showMessages() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            MessageDAO dao = context.getMessageDAO();
            List<Message> messageList = dao.getReceivedMessageListForUser(user.getId());
            return ok(messages.render(messageList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
