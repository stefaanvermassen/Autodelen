package controllers;

import controllers.Security.RoleSecured;
import database.*;
import database.jdbc.JDBCFilter;
import models.Car;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;

import java.util.List;

public class CarPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @RoleSecured.RoleAuthenticated()
    public static Result getList(String search) {
        search = search.trim();
        if (search != "") {
            search = search.replaceAll("\\s+", " ");
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                String cars = "";
                Filter filter = new JDBCFilter();
                filter.putValue(FilterField.CAR_NAME, search);
                List<Car> results = dao.getCarList(FilterField.CAR_NAME, true, 1, MAX_VISIBLE_RESULTS, filter);
                for (Car car : results) {
                    String value = car.getName();
                    for (String part : search.split(" ")) {
                        value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                    }
                    value += " (" + car.getId() + ")";

                    cars += "<li data-uid=\"" + car.getId() + "\"><a href=\"javascript:void(0)\">" + value.replace("#", "strong") + "</a></li>";
                }
                return ok(cars);
            } catch (DataAccessException ex) {
                throw ex;//TODO?
            }
        } else {
            return ok();
        }
    }
}
