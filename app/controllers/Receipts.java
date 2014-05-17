package controllers;

import database.*;
import models.*;
import models.CarCost;
import models.CarRide;
import models.Refuel;
import controllers.util.*;
import controllers.Security.RoleSecured;
import org.joda.time.DateTime;
import play.mvc.*;
import providers.DataProvider;
import views.html.receipts.*;

import java.sql.Date;
import java.util.List;
import play.api.templates.Html;
import com.itextpdf.text.*;
import java.io.FileOutputStream;
import com.itextpdf.text.pdf.PdfWriter;
import java.net.URL;

public class Receipts extends Controller {

    private static final int PAGE_SIZE = 10;

    /**
     * @return The users index-page with all users
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.PROFILE_ADMIN})
    public static Result index() {
	newReceipt("/home/maryna/r/r1.pdf");
        return ok(receipts.render());
    }

    /**
     *
     * @param page The page in the userlists
     * @param ascInt An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of users of the corresponding page
     */
   // @RoleSecured.RoleAuthenticated()
    public static Result showReceiptsPage(int page, int ascInt, String orderBy, String date) {
        // TODO: orderBy not as String-argument?
        FilterField receiptsField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
	Filter filter = Pagination.parseFilter(date);
        return ok(receiptsList(page, receiptsField, asc, filter));
    }

    private static Html receiptsList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            User currentUser = DataProvider.getUserProvider().getUser();
            ReceiptDAO dao = context.getReceiptDAO();

            if(orderBy == null) {
                orderBy = FilterField.RECEIPT_DATE;
            }
            List<Receipt> listOfReceipts = dao.getReceiptsList(orderBy, asc, page, PAGE_SIZE, filter, currentUser);

            int amountOfResults = dao.getAmountOfReceipts(filter, currentUser);
	    //int amountOfResults = listOfReceipts.size();
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            //if(){rendernew()}
            return receiptspage.render(listOfReceipts, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static void newReceipt(String filename) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
	    generatePDF(document);
	    addtoDataBase(filename);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
    }

    public static void addtoDataBase(String filename) {
	try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            ReceiptDAO dao = context.getReceiptDAO();
            try {
		//**datum**
		DateTime date=DateTime.now(); 
		//**gebruiker**
		User user=DataProvider.getUserProvider().getUser();;
		//**price**
		int price=15;

                FileDAO fdao = context.getFileDAO();
		File file=fdao.createFile(filename, filename, "pdf", -1);

                Receipt receipt = dao.createReceipt(filename, date, file, user, price);
            } catch (DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw ex; //TODO: show gracefully
        }
    }

    public static void generatePDF(Document document) {
        try {
	    String imageUrl = "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-frc3/t1.0-1/c3.0.50.50/p50x50/969296_656566794396242_1002112915_s.jpg";
            Image image2 = Image.getInstance(new URL(imageUrl));
            document.add(image2);

            //addMetaData(document);
            //addTitlePage(document);
	    //Anchor anchor = new Anchor("First Chapter", catFont);
	    //createTable(Section subCatPart);
	    //Beter meer in variabelen
	    document.add(new Paragraph("Rekeningnummer 523-080452986-86 -IBAN BE78 5230 8045 -BIC Code TRIOBEBB"));
	    document.add(new Paragraph("Degage! vzw - Fuchsiastraat 81, 9000 Gent"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

private static void createTable(Section subCatPart)
      throws BadElementException {/*
    PdfPTable table = new PdfPTable(3);

    // t.setBorderColor(BaseColor.GRAY);
    // t.setPadding(4);
    // t.setSpacing(4);
     t.setBorderWidth(0);

    PdfPCell c1 = new PdfPCell(new Phrase("Afrekening"));
    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(c1);

    c1 = new PdfPCell(new Phrase("nummer"));
    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(c1);

    c1 = new PdfPCell(new Phrase("Table Header 3"));
    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(c1);
    table.setHeaderRows(1);

    table.addCell("1.0");
    table.addCell("1.1");
    table.addCell("1.2");
    table.addCell("2.1");
    table.addCell("2.2");
    table.addCell("2.3");

    subCatPart.add(table);
*/
  }



/*    public void endPeriod() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO crdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            CarCostDAO ccdao = context.getCarCostDAO();
            crdao.endPeriod();
            rdao.endPeriod();
            ccdao.endPeriod();
	    File f = generateExcel();
	    addFile(f);
        } catch(DataAccessException ex) {
            throw ex;
        }
    }


    public void generateExcel() {
        //...
	double amount = 0;

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO cdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            List<CarRide> rides = cdao.getBillRidesForLoaner(date, user);
            List<Refuel> refuels = rdao.getBillRefuelsForLoaner(date, user);

            for (CarRide ride : rides) {
                amount += ride.getCost();
            }

            for (Refuel refuel: refuels) {
                amount -= refuel.getAmount().doubleValue();
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
    }



    public double getLoanerBillAmount(Date date, int user) {
        double amount = 0;

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO cdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            List<CarRide> rides = cdao.getBillRidesForLoaner(date, user);
            List<Refuel> refuels = rdao.getBillRefuelsForLoaner(date, user);

            for (CarRide ride : rides) {
                amount += ride.getCost();
            }

            for (Refuel refuel: refuels) {
                amount -= refuel.getAmount().doubleValue();
            }
        } catch(DataAccessException ex) {
            throw ex;
        }

        return amount;
    }

    public double getCarBillAmount(Date date, int car) {
        double deprecationAmount = 0;
        double refuelAmount = 0;
        double carCostAmount = 0;

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO crdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            CarCostDAO ccdao = context.getCarCostDAO();
            List<CarRide> rides = crdao.getBillRidesForCar(date, car);
            List<Refuel> refuels = rdao.getBillRefuelsForCar(date, car);
            List<CarCost> carcosts = ccdao.getBillCarCosts(date, car);

            int distanceOwner = 0;
            int distanceOthers = 0;

            for (CarRide ride : rides) {
                if (ride.getReservation().getUser() == ride.getReservation().getCar().getOwner()) {
                    distanceOwner += ride.getEndMileage() - ride.getStartMileage();
                } else {
                    distanceOthers += ride.getEndMileage() - ride.getStartMileage();
                }
            }

            deprecationAmount = distanceOthers * DataProvider.getSettingProvider().getDouble("deprecation_cost", new DateTime(date.getTime()));

            double refuelOthers = 0;
            double refuelOwner = 0;

            for (Refuel refuel: refuels) {
                if (refuel.getCarRide().getReservation().getUser() == refuel.getCarRide().getReservation().getCar().getOwner()) {
                    refuelOwner += refuel.getAmount().doubleValue();
                } else {
                    refuelOthers += refuel.getAmount().doubleValue();
                }
            }

            refuelAmount = refuelOwner - (refuelOwner + refuelOthers) * distanceOwner / (distanceOwner + distanceOthers);

            for (CarCost carcost : carcosts) {
                carCostAmount += carcost.getAmount().doubleValue();
            }

            carCostAmount *= distanceOthers / (distanceOwner + distanceOthers);
        } catch(DataAccessException ex) {
            throw ex;
        }

        return deprecationAmount + refuelAmount + carCostAmount;
    }*/
}
