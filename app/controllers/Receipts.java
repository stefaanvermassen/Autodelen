package controllers;

import database.*;
import models.CarCost;
import models.CarRide;
import models.Refuel;
import org.joda.time.DateTime;
import play.mvc.*;
import providers.DataProvider;
import views.html.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Receipts extends Controller {
    private boolean loanerState = false;
    private boolean carState = false;

    private Date date;

    private List<CarRide> rides;
    private List<Refuel> refuels;
    private List<CarCost> carcosts;

    public static Result index() {
        Map<Date, BigDecimal> receiptList = new HashMap<>();
        receiptList.put(new Date(0), new BigDecimal(5));
        receiptList.put(new Date(1), new BigDecimal(5));
        receiptList.put(new Date(2), new BigDecimal(5));
        return ok(receipts.render(receiptList));
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