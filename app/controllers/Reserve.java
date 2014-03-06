package controllers;

import database.*;
import models.*;
import play.api.templates.Html;
import play.mvc.*;
import views.html.*;
import views.html.reserve2;

import java.util.List;

public class Reserve extends Controller {

    public static Result index() {
        return ok(showIndex());
    }

    public static Html showIndex() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            List<Car> cars = dao.getCarList();
            return reserve.render(cars);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public static Result reserve(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);
            return ok(reserve2.render(car));
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

}
