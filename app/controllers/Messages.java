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
        public String useremail;

        public String validate() {
            if("".equals(useremail) || "".equals(subject) || "".equals(body))
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
    public static Result showMessagesPage(int page, int ascInt, String orderBy, String searchString) {
        User user = DatabaseHelper.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.MESSAGE_RECEIVER_ID, user.getId() + "");
        return ok(notificationList(page, field, asc, filter));


    }

    private static Html notificationList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
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

            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                MessageDAO dao = context.getMessageDAO();

                try {
                    User sender = DatabaseHelper.getUserProvider().getUser();
                    User receiver = DatabaseHelper.getUserProvider().getUser(createForm.get().useremail);
                    Message mes = dao.createMessage(sender, receiver, createForm.get().subject, createForm.get().body);
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
     * @param messageId Id of the message that has to be removed
     * @return message index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result markMessageAsRead(int messageId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
           MessageDAO dao = context.getMessageDAO();
           dao.markMessageAsRead(messageId);
            System.out.println("test");
            context.commit();
            return redirect(routes.Messages.showMessages());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
