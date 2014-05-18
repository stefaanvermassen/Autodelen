package controllers;

import com.itextpdf.text.pdf.draw.LineSeparator;
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
import providers.SettingProvider;
import views.html.receipts.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import play.api.templates.Html;
import com.itextpdf.text.*;
import java.io.FileOutputStream;
import com.itextpdf.text.pdf.*;
import java.net.URL;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

public class Receipts extends Controller {
    private boolean loanerState = false;
    private boolean carState = false;

    private static Date date = new Date(1401580800000L);

    private static List<CarRide> rides;
    private static List<Refuel> refuels;
    private static List<CarCost> carcosts;

    private static final int PAGE_SIZE = 10;

    /**
     * @return The users index-page with all users
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.PROFILE_ADMIN})
    public static Result index() {
	newReceipt("/Users/karsgoos/Documents/r1.pdf");
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
            //**if has car
	    //**for each car
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
		User user=DataProvider.getUserProvider().getUser();
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
	    //**variabelen
	    User gebruiker = DataProvider.getUserProvider().getUser();
	    int afrekeningnr = 103;

	    /*Hoofdding*/


	    /**String imageUrl = "http://zelensis.ugent.be/prod/assets/images/logosmall.png";
            Image image = Image.getInstance(new URL(imageUrl));
            image.setAlignment(Image.RIGHT | Image.TEXTWRAP);
            image.scaleAbsolute(45f, 45f);
            document.add(image);**/

        DateTime report = new DateTime(date);

        PdfPTable table = new PdfPTable(3);
	    add(table,"Afrekening n°:");
	    add(table,""+afrekeningnr, true);
	    add(table,"");
	    add(table,"Naam:");
	    add(table,""+gebruiker, true);
	    add(table,"");
	    add(table,"Adres:");
	    add(table,""+gebruiker.getAddressDomicile(), true);
	    add(table,"");
	    add(table,"Periode:", false, false);
	    add(table,"vanaf " + new SimpleDateFormat("dd-MM-yyyy").format(report.minusMonths(3).toDate()), false, false);
	    add(table, "t.e.m. " + new SimpleDateFormat("dd-MM-yyyy").format(report.minusDays(1).toDate()), false, false);

            table.setSpacingAfter(20);

            document.add(table);

	    /*Tabel*/
	    createTable(document);

	    /*Voetnoot*/
	    /*In templates steken?*/
	    Font f=new Font(FontFamily.COURIER, 8);
	    Font f2=new Font(FontFamily.COURIER, 6);
	    document.add(new Paragraph("Rekeningnummer 523-080452986-86 -IBAN BE78 5230 8045 -BIC Code TRIOBEBB",f));
	    document.add(new Paragraph("Degage! vzw - Fuchsiastraat 81, 9000 Gent",f));
	    document.add(new Paragraph("Gelieve de afrekening te betalen voor " + new SimpleDateFormat("dd-MM-yyyy").format(report.plusMonths(3).toDate()),f));
	    document.add(new Paragraph("Bij betaling, gelieve het nummer van de afrekening te vermelden",f2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

private static void createTable(Document document)
      throws BadElementException, DocumentException {
    document.add(new Paragraph("Ritten"));
    //**variabelen
    SettingProvider provider = DataProvider.getSettingProvider();
    int levels = provider.getInt("cost_levels", new DateTime(date));

    int amountOfComulns = 4 + levels;
    PdfPTable drivesTable = new PdfPTable(amountOfComulns);
    drivesTable.setWidthPercentage(100);
    drivesTable.setSpacingBefore(5);
    drivesTable.setSpacingAfter(10);

    /*Hoofdding*/
    add(drivesTable,"Auto", true, false);
    add(drivesTable,"Datum", true, false);
    add(drivesTable,"Afstand", true, false);

    int lower = 0;
    int upper = 0;

    for(int j=0; j < levels; j++){
        if (j > 0)
            lower = upper;

        if (j < levels - 1) {
            upper = provider.getInt("cost_limit_" + j, new DateTime(date));
            add(drivesTable,lower + "-" + upper + "km", true, false);
        } else {
            add(drivesTable, ">" + upper + "km", true, false);
        }
	}
    add(drivesTable,"Ritprijs",  true, false);

    add(drivesTable,"",  true);
    add(drivesTable,"",  true);
    add(drivesTable,"",  true);
    
	for(int j=0; j < levels; j++){
	    add(drivesTable,"€" + provider.getDouble("cost_" + j, new DateTime(date)) + "/km");
	}

    add(drivesTable,"",  true);

    try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
        CarRideDAO dao = context.getCarRideDAO();
        rides = new ArrayList<>();
        rides.add(dao.getCarRide(101));
    }catch (Exception e) {}

    int totalDistance = 0;
    double totalCost = 0;
    double[] totals = new double[levels];

    /*Middenstuk*/
    for (CarRide ride : rides) {
	//**variabelen
    int distance = ride.getEndMileage() - ride.getStartMileage();
    totalDistance += distance;
	
	add(drivesTable, ride.getReservation().getCar().getName());
	add(drivesTable, new SimpleDateFormat("dd-MM-yyyy").format(ride.getReservation().getFrom().toDate()));
	add(drivesTable, "" + distance);

        double rideCost = 0;
        if (true) {
            for (int level = 0; level < levels; level++)
                add(drivesTable, "--");
        } else {
            int level;
            lower = 0;
            for (level = 0; level < levels; level++) {
                int limit = 0;
                double cost;
                boolean stop = false;

                if (level == levels - 1 || distance <= (limit = provider.getInt("cost_limit_" + level, new DateTime(date)))) {
                    cost = distance * provider.getDouble("cost_" + level, new DateTime(date));
                    stop = true;
                } else {
                    cost = (limit - lower) * provider.getDouble("cost_" + level, new DateTime(date));
                    distance -= (limit - lower);
                }

                totals[level] += cost;
                rideCost += cost;
                add(drivesTable, "€ " + cost);

                if (stop) {
                    level++;
                    break;
                }

                lower = limit;
            }

            for (int i = level; i < levels; i++) {
                add(drivesTable,"");
            }
        }

        totalCost += rideCost;

	add(drivesTable, "€ " + rideCost, true);

    }

    /*Slot*/

    //**variabelen: totalen


    add(drivesTable, "TOTALEN", true);
    add(drivesTable, "");
    add(drivesTable, totalDistance + " km", true);

	for(int j = 0; j < levels; j++){
	    add(drivesTable, "€ " + totals[j], true);
	}

    add(drivesTable, "€ " + totalCost, true);

    document.add(drivesTable);

    document.add(new Paragraph("Tankbeurten"));
    PdfPTable refuelsTable = new PdfPTable(3);

    add(refuelsTable, "Auto", true);
    add(refuelsTable, "Datum", true);
    add(refuelsTable, "Prijs", true);
    refuelsTable.setWidthPercentage(100);
    refuelsTable.setSpacingBefore(5);
    refuelsTable.setSpacingAfter(10);
    document.add(refuelsTable);
}


    public static void add(PdfPTable table, String contents, boolean fat) {
        add(table, contents, fat, true);
    }

    public static void add(PdfPTable table, String contents, boolean fat, boolean border) {
        Font f=new Font(FontFamily.COURIER, 8);
        if(fat){
            f=new Font(FontFamily.COURIER, 8, Font.BOLD);
        }
        PdfPCell cell=new PdfPCell(new Paragraph(contents,f));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (border) {
            cell.setPaddingBottom(5);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BaseColor.BLACK);
        } else {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        table.addCell(cell);
    }

    public static void add(PdfPTable table, String contents) {
        add(table, contents, false);
    }

    public void endPeriod() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO crdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            CarCostDAO ccdao = context.getCarCostDAO();
            crdao.endPeriod();
            rdao.endPeriod();
            ccdao.endPeriod();
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    public void getLoanerBillData(Date date, int user) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO cdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            rides = cdao.getBillRidesForLoaner(date, user);
            refuels = rdao.getBillRefuelsForLoaner(date, user);

            this.date = date;

            loanerState = true;
            carState = false;
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    public void getCarBillData(Date date, int car) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarRideDAO crdao = context.getCarRideDAO();
            RefuelDAO rdao = context.getRefuelDAO();
            CarCostDAO ccdao = context.getCarCostDAO();
            rides = crdao.getBillRidesForCar(date, car);
            refuels = rdao.getBillRefuelsForCar(date, car);
            carcosts = ccdao.getBillCarCosts(date, car);

            this.date = date;

            carState = true;
            loanerState = false;
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    public double getLoanerBillAmount() {
        double amount = 0;

        if (loanerState) {
            for (CarRide ride : rides) {
                amount += ride.getCost();
            }

            for (Refuel refuel: refuels) {
                amount -= refuel.getAmount().doubleValue();
            }
        }

        return amount;
    }

    public double getCarBillAmount() {
        double deprecationAmount = 0;
        double refuelAmount = 0;
        double carCostAmount = 0;

        if (carState) {
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
        }

        return deprecationAmount + refuelAmount + carCostAmount;
    }
}
