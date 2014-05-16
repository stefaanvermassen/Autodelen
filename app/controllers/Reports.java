package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import models.Reservation;
import models.User;
import models.UserRole;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.reports;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/05/14.
 */
public class Reports extends Controller {

    public static Result index() {
        return ok(reports.render());
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result getUsers()  {
        File file = new File("users.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            UserDAO userDao = context.getUserDAO();
            List<User> userList = userDao.getUserList(FilterField.USER_NAME, true, 1, userDao.getAmountOfUsers(null), null);
            try(FileOutputStream out = new FileOutputStream(file)){
                Workbook wb = new XSSFWorkbook();
                Sheet s = wb.createSheet("Gebruikers");
                int rNum = 0;
                Row row = s.createRow(rNum);
                String[] header = {"Id", "Voornaam", "Familienaam", "Email", "Telefoon", "Gsm", "Straat (domicilie)",
                        "Huisnummer (domicilie)", "Postcode (domicilie)", "Stad (domicilie)", "Land (domicilie)", "Straat (verblijf)",
                        "Huisnummer (Verblijf)", "Postcode (verblijf)", "Stad (verblijf)", "Land (verblijf)", "Geslacht", "Rijbewijs",
                        "Gebruikersstatus", "Identiteitskaart", "Schadeverleden"};
                for(int i=0; i<header.length; i++){
                    Cell cell = row.createCell(i);
                    cell.setCellValue(header[i]);
                }
                rNum++;
                User user = null;
                for(int i=0; i<userList.size(); i++){
                    user = userList.get(i);
                    row = s.createRow(i+1);
                    int j=0;
                    row.createCell(j++).setCellValue(user.getId());
                    row.createCell(j++).setCellValue(user.getFirstName());
                    row.createCell(j++).setCellValue(user.getLastName());
                    row.createCell(j++).setCellValue(user.getEmail());
                    row.createCell(j++).setCellValue(user.getPhone());
                    row.createCell(j++).setCellValue(user.getCellphone());
                    if(user.getAddressDomicile() != null){
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getStreet());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getNumber());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getZip());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getCity());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getCountry());
                    }else{
                        j+=5;
                    }
                    if(user.getAddressResidence() != null){
                        row.createCell(j++).setCellValue(user.getAddressResidence().getStreet());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getNumber());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getZip());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getCity());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getCountry());
                    }else{
                        j+=5;
                    }
                    row.createCell(j++).setCellValue(user.getGender().name());
                    row.createCell(j++).setCellValue((user.getDriverLicense()!= null)? user.getDriverLicense().getId(): "");
                    row.createCell(j++).setCellValue(user.getStatus().name());
                    row.createCell(j++).setCellValue((user.getIdentityCard() != null)?user.getIdentityCard().getRegistrationNr(): "");
                    row.createCell(j++).setCellValue(user.getDamageHistory());
                }
                wb.write(out);
                return ok(file, file.getName());
            }
        } catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result getReservations(){
        File file = new File("reservations.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
           ReservationDAO reservationDAO = context.getReservationDAO();
            Filter filter = Pagination.parseFilter("");
            filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, "");
            filter.putValue(FilterField.RESERVATION_CAR_ID, "");
           List<Reservation> reservationList = reservationDAO.getReservationListPage(FilterField.RESERVATION_CAR_ID, true, 1, reservationDAO.getAmountOfReservations(filter), filter);
           try(FileOutputStream out = new FileOutputStream(file)){
               Workbook wb = new XSSFWorkbook();
               CreationHelper createHelper = wb.getCreationHelper();
               Sheet s = wb.createSheet("Reservaties");
               int rNum = 0;
               Row row = s.createRow(rNum);
               String[] header = {"Id", "Auto(ID)", "Autonaam", "Lener(ID)", "Lener voornaam","Lener familienaam", "Lener email", "Lener telefoon", "Lener gsm", "Van", "Tot", "Status", "Bericht"};
               for(int i=0; i<header.length; i++){
                   Cell cell = row.createCell(i);
                   cell.setCellValue(header[i]);
               }
               rNum++;
               Reservation reservation = null;
               for(int i=0; i<reservationList.size(); i++){
                   reservation = reservationList.get(i);
                   row = s.createRow(i+1);
                   int j=0;
                   row.createCell(j++).setCellValue(reservation.getId());
                   row.createCell(j++).setCellValue(reservation.getCar().getId());
                   row.createCell(j++).setCellValue(reservation.getCar().getName());
                   row.createCell(j++).setCellValue(reservation.getUser().getId());
                   row.createCell(j++).setCellValue(reservation.getUser().getFirstName());
                   row.createCell(j++).setCellValue(reservation.getUser().getLastName());
                   row.createCell(j++).setCellValue(reservation.getUser().getEmail());
                   row.createCell(j++).setCellValue(reservation.getUser().getPhone());
                   row.createCell(j++).setCellValue(reservation.getUser().getCellphone());
                   Cell cell;
                   CellStyle cellStyle = wb.createCellStyle();
                   cellStyle.setDataFormat(
                           createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                   cell = row.createCell(j++);
                   cell.setCellValue(reservation.getFrom().toDate());
                   cell.setCellStyle(cellStyle);
                   cell = row.createCell(j++);
                   cell.setCellValue(reservation.getTo().toDate());
                   cell.setCellStyle(cellStyle);
                   row.createCell(j++).setCellValue(reservation.getStatus().getDescription());
                   row.createCell(j++).setCellValue(reservation.getMessage());
               }
               wb.write(out);
               return ok(file, file.getName());
           }
        } catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
