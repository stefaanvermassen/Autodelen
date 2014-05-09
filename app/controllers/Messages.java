package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import models.AutocompleteValue;
import models.Message;
import models.User;
import org.joda.time.DateTime;
import play.api.templates.Html;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.notifiers.addmessage;
import views.html.notifiers.messages;
import views.html.notifiers.messagespage;

import java.util.ArrayList;
import java.util.List;

public class Messages extends Controller {

    private static final int PAGE_SIZE = 10;

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the submission of a new message.
     */
    public static class MessageCreationModel {
        public String subject;
        public String body;
        public Integer userId;

        public String validate() {
            if(userId == null || userId == 0 || "".equals(subject) || "".equals(body))
                return "Vul alle velden in";

            return null;
        }

    }

    public static int AUTOCOMPLETE_MAX = 10;


    /**
     * Method: GET
     *
     * @return index page containing all the received messages of a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showMessages() {
        return ok(messages.render());
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showReceivedMessagesPage(int page, int ascInt, String orderBy, String searchString) {
        User user = DataProvider.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.MESSAGE_RECEIVER_ID, user.getId() + "");
        return ok(messageList(page, field, asc, filter));
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showSentMessagesPage(int page, int ascInt, String orderBy, String searchString) {
        User user = DataProvider.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.MESSAGE_SENDER_ID, user.getId() + "");
        return ok(messageList(page, field, asc, filter));
    }

    private static Html messageList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            MessageDAO dao = context.getMessageDAO();
            List<Message> messageList = dao.getMessageList(orderBy, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfMessages(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return messagespage.render(messageList, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return a new message form
     */

    @RoleSecured.RoleAuthenticated()
    public static Result newMessage() {
        Form<MessageCreationModel> editForm = Form.form(MessageCreationModel.class);
        return ok(addmessage.render(editForm));
    }

    /**
     * Method: GET
     *
     * @return a new message form, with the user already filled in, for reply purposes
     */

    @RoleSecured.RoleAuthenticated()
    public static Result reply(int userId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return redirect(routes.Messages.showMessages());
            }
            MessageCreationModel model = new MessageCreationModel();
            model.userId = user.getId();
            Form<MessageCreationModel> editForm = Form.form(MessageCreationModel.class);
            return ok(addmessage.render(editForm.fill(model)));

        }catch (DataAccessException ex) {
            throw ex;
        }
    }


    /**
     * Method: POST
     *
     * Creates a new message based on submitted form data
     *
     * @return the messages index list
     */

    @RoleSecured.RoleAuthenticated()
    public static Result createNewMessage() {
        Form<MessageCreationModel> createForm = Form.form(MessageCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(addmessage.render(createForm));
        } else {

            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                MessageDAO dao = context.getMessageDAO();

                try {
                    User sender = DataProvider.getUserProvider().getUser();
                    User receiver = context.getUserDAO().getUser(createForm.get().userId, false);
                    Message mes = dao.createMessage(sender, receiver, createForm.get().subject, createForm.get().body);
                    DataProvider.getCommunicationProvider().invalidateMessages(receiver.getId()); // invalidate the message
                    DataProvider.getCommunicationProvider().invalidateMessageNumber(receiver.getId());
                    if (mes != null) {
                        return redirect(
                                routes.Messages.showMessages() // return to infosession list
                        );
                    } else {
                        createForm.error("Failed to create message in database. Contact administrator.");
                        return badRequest(addmessage.render(createForm));
                    }
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }

    /**
     * Method: GET
     *
     * @param messageId Id of the message that has to be marked as read
     * @return message index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result markMessageAsRead(int messageId) {
        User user = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
           MessageDAO dao = context.getMessageDAO();
           dao.markMessageAsRead(messageId);
            context.commit();
            DataProvider.getCommunicationProvider().invalidateMessages(user.getId());
            return redirect(routes.Messages.showMessages());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
