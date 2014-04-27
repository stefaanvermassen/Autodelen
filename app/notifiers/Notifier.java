package notifiers;


import controllers.routes;
import database.*;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.mvc.Http;


/**
 * Created by Stefaan Vermassen on 16/02/14.
 */

public class Notifier extends Mailer {

    public static final String NOREPLY = "Zelensis <noreply@zelensis.ugent.be>";

    public static void sendVerificationMail(User user, String verificationUrl) {
        String mail = "";
        setSubject("Verifieer uw Dégage-account");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.VERIFICATION);
            mail = replaceUserTags(user, template.getBody());
            String vUrl = "http://" + Http.Context.current().request().host() + routes.Login.register_verification(user.getId(), verificationUrl).toString();
            mail = mail.replace("%verification_url%", vUrl);
        } catch (DataAccessException ex) {
            throw ex;
        }

        if (!play.api.Play.isDev(play.api.Play.current())) {
            send(mail);
        }
    }

    public static void sendWelcomeMail(User user) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.WELCOME);
            mail = replaceUserTags(user, template.getBody());
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        }catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void sendMembershipStatusChanged(User user, boolean approved, String comment) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template;
            if(approved){
                template = dao.getTemplate(MailType.MEMBERSHIP_APPROVED);
            }else{
                template = dao.getTemplate(MailType.MEMBERSHIP_REFUSED);
            }
            mail = replaceUserTags(user, template.getBody());
            mail = mail.replace("%comment%", comment);
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        }catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void sendInfoSessionEnrolledMail(User user, InfoSession infoSession) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.INFOSESSION_ENROLLED);
            mail = replaceUserTags(user, template.getBody());
            mail = replaceInfoSessionTags(infoSession, mail);
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void sendReservationApproveRequestMail(User user, Reservation carReservation) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.RESERVATION_APPROVE_REQUEST);
            mail = replaceUserTags(user, template.getBody());
            mail = replaceCarReservationTags(carReservation, mail);
            mail = mail.replace("%reservation_url%", routes.Drives.details(carReservation.getId()).url());
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }


    public static void sendReservationApprovedByOwnerMail(User user, Reservation carReservation) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            CarDAO cdao = context.getCarDAO();
            Car car = cdao.getCar(carReservation.getCar().getId());
            EmailTemplate template = dao.getTemplate(MailType.RESERVATION_APPROVED_BY_OWNER);
            mail = replaceUserTags(user, template.getBody());
            mail = replaceCarReservationTags(carReservation, mail);
            mail = mail.replace("%reservation_car_address%", car.getLocation().toString());
            mail = mail.replace("%reservation_url%", routes.Drives.details(carReservation.getId()).url());
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void sendReservationRefusedByOwnerMail(User user, Reservation carReservation, String reason) {
        String mail = "";
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.RESERVATION_REFUSED_BY_OWNER);
            mail = replaceUserTags(user, template.getBody());
            mail = replaceCarReservationTags(carReservation, mail);
            mail = mail.replace("%reservation_reason%", reason);
            NotificationDAO notificationDAO = context.getNotificationDAO();
            notificationDAO.createNotification(user, template.getSubject(), mail);
            if(template.getSendMail()){
                setSubject(template.getSubject());
                addRecipient(user.getEmail());
                addFrom(NOREPLY);
                send(mail);
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void sendPasswordResetMail(User user, String verificationUrl) {
        String mail = "";
        setSubject("Uw wachtwoord opnieuw instellen");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.PASSWORD_RESET);
            mail = replaceUserTags(user, template.getBody());
            String vUrl = "http://" + Http.Context.current().request().host() + routes.Login.resetPassword(user.getId(), verificationUrl).toString();
            mail = mail.replace("%password_reset_url%", vUrl);
        } catch (DataAccessException ex) {
            throw ex;
        }

        if (!play.api.Play.isDev(play.api.Play.current())) {
            send(mail);
        }
    }

    public static void sendReminderMail(User user) {
        String mail = "";
        setSubject("Ongelezen berichten");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.REMINDER_MAIL);
            mail = replaceUserTags(user, template.getBody());
        } catch (DataAccessException ex) {
            throw ex;
        }
        send(mail);
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            SchedulerDAO dao = context.getSchedulerDAO();
            dao.setReminded(user);
            context.commit();
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    private static String replaceUserTags(User user, String template) {
        template = template.replace("%user_firstname%", user.getFirstName());
        template = template.replace("%user_lastname%", user.getLastName());
        //TODO: replace address only when provided
        return template;
    }

    private static String replaceInfoSessionTags(InfoSession infoSession, String template) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, d MMM yyyy HH:mm");
        template = template.replace("%infosession_date%", fmt.print(infoSession.getTime()));
        template = template.replace("%infosession_address%", infoSession.getAddress().toString());
        return template;
    }

    private static String replaceCarReservationTags(Reservation carReservation, String template) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, d MMM yyyy HH:mm");
        template = template.replace("%reservation_from%", fmt.print(carReservation.getFrom()));
        template = template.replace("%reservation_to%", fmt.print(carReservation.getTo()));
        //TODO: reservation_url
        return template;
    }

}
