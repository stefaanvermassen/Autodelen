package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.AutocompleteValue;
import models.Message;
import models.User;
import org.joda.time.DateTime;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.notifiers.addmessage;
import views.html.notifiers.messages;

import java.util.ArrayList;
import java.util.List;

public class Messages extends Controller {


    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the submission of a new message.
     */
    public static class MessageCreationModel {
        public String subject;
        public String body;
        public String useremail;

        public String validate() {

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
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            MessageDAO dao = context.getMessageDAO();
            List<Message> messageList = dao.getReceivedMessageListForUser(user.getId());
            return ok(messages.render(messageList));
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
                    Message mes = dao.createMessage(sender, receiver, createForm.get().subject, createForm.get().body, new DateTime());
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
     * Interface with the client-sided autocomplete widget, here used as a user-picker.
     * @param term user-entered text in the autocomplete input-box
     * @return a list for the names that match the search term.
     */
    @RoleSecured.RoleAuthenticated()
    public static Result autocompleteLabelAndValue(final String term) {
        final List<AutocompleteValue> response = new ArrayList<>();
        List<User> resultList = null;
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            resultList = dao.getAllUsers();
        } catch (DataAccessException ex) {
            throw ex; //TODO: what if this fails?
        }
        for (User u : resultList) {
            final String label = u.getFirstName();
            final String value = u.getEmail();
            if (label.toLowerCase().startsWith(term.toLowerCase())) {
                response.add(new AutocompleteValue(value, label));
            }
            if (response.size() == AUTOCOMPLETE_MAX) {
                break;
            }
        }
        return ok(Json.toJson(response));
    }

}
