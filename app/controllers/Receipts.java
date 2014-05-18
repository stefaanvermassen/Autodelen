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
import com.itextpdf.text.pdf.*;
import java.net.URL;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

public class Receipts extends Controller {
    private boolean loanerState = false;
    private boolean carState = false;

    private Date date;

    private List<CarRide> rides;
    private List<Refuel> refuels;
    private List<CarCost> carcosts;

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
	    String vervaldatum= "15 maart 2014";
	    String startPeriode= "15 maart 2014";
	    String eindPeriode= "15 maart 2014";
	    User gebruiker= DataProvider.getUserProvider().getUser();

	    /*Hoofdding*/
	    String imageUrl = "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-frc3/t1.0-1/c3.0.50.50/p50x50/969296_656566794396242_1002112915_s.jpg";
            Image image2 = Image.getInstance(new URL(imageUrl));
            document.add(image2);

	    /*Tabel*/
	    createTable(document);

	    /*Voetnoot*/
	    /*In templates steken?*/
	    Font f=new Font(FontFamily.COURIER, 8);
	    Font f2=new Font(FontFamily.COURIER, 6);
	    document.add(new Paragraph("Rekeningnummer 523-080452986-86 -IBAN BE78 5230 8045 -BIC Code TRIOBEBB",f));
	    document.add(new Paragraph("Degage! vzw - Fuchsiastraat 81, 9000 Gent",f));
	    document.add(new Paragraph("Gelieve de afrekening te betalen voor " + vervaldatum,f));
	    document.add(new Paragraph("Bij betaling, gelieve het nummer van de afrekening te vermelden",f2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

private static void createTable(Document document)
      throws BadElementException, DocumentException {
    //**variabelen
    int aantalCategorien= 3;
    int[] categorienBovengrens=new int[aantalCategorien];
    double[] categoriePrijs=new double[aantalCategorien];
    for(int i=1;i<=aantalCategorien;i++){
        //Bovengrens
        categorienBovengrens[i-1]=i*100;
        //Prijs
        categoriePrijs[i-1]=i*0.21;
    }
    int aantalRitten=10;

    int amountOfComulns=5+aantalCategorien;
    PdfPTable table = new PdfPTable(amountOfComulns);

    /*Hoofdding*/
    add(table,"Auto", true);
    add(table,"Datum", true);
    add(table,"Aantal kms", true);
    
	for(int j=0;j<aantalCategorien;j++){
	    String cell="0-";
	    if(j>0){
		cell=categorienBovengrens[j-1]+"-";
	    }
	    
	    add(table,cell+categorienBovengrens[j]+" km", true);
	}
    add(table,"Kostprijs",  true);
    add(table,"Tank-",  true);

    add(table,"",  true);
    add(table,"",  true);
    add(table,"",  true);
    
	for(int j=0;j<aantalCategorien;j++){
	    add(table,"kms aan "+categoriePrijs[j]);
	}
    add(table,"kilometers", true);
    add(table,"beurten", true);
    



    /*Middenstuk*/
    for(int i=0;i<aantalRitten;i++){
	//**variabelen
	String naamAuto="Klorofil";
	String date="1986";
	int aantalkm = 999;
	int kostprijskm=10;
	int tankbeurten=44;
	
	add(table,naamAuto);
	add(table,date);
	add(table,""+aantalkm);

	for(int j=0;j<aantalCategorien;j++){
	    //**variabelen
            int kmInCategorie=5;
	    
	    add(table,""+kmInCategorie);
	}
	add(table,""+kostprijskm, true);
	add(table,""+tankbeurten);

    }

    /*Slot*/

    document.add(table);
}


    public static void add(PdfPTable table, String contents, boolean fat) {
	Font f=new Font(FontFamily.COURIER, 8);
	if(fat){ 
	    f=new Font(FontFamily.COURIER, 8, Font.BOLD);
        }
	PdfPCell cell=new PdfPCell(new Paragraph(contents,f));
	//cell.setBorderLeft(Rectangle.NO_BORDER);
	//cell.setBorderRight(Rectangle.NO_BORDER);
	//cell.setBorderColorLeft(BaseColor.WHITE);
        //cell.setBorderColorRight(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderColorTop(BaseColor.BLACK);
        cell.setBorderColorBottom(BaseColor.BLACK);
        cell.setBorderWidthBottom(0);
        table.addCell(cell);
    }

    public static void add(PdfPTable table, String contents) {
	add( table,  contents, false);
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
