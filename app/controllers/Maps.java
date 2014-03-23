package controllers;

import controllers.Security.RoleSecured;
import database.AddressDAO;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import models.Address;
import play.data.*;
import play.libs.F;
import play.libs.WS;
import play.mvc.Result;
import com.fasterxml.jackson.databind.JsonNode;

import static play.libs.F.Function;
import static play.libs.F.Promise;

import views.html.maps.*;

import play.mvc.*;

/**
 * Created by Cedric on 3/2/14.
 */
public class Maps extends Controller {

    private static final String TILE_URL = "http://tile.openstreetmap.org/%d/%d/%d.png";
    private static final String ADDRESS_RESOLVER = "http://nominatim.openstreetmap.org/search";

    public static class MapDetails {
        private double latitude;
        private double longtitude;
        private int zoom;
        private String message;

        public MapDetails(double latitude, double longtitude, int zoom, String message){
            this.latitude = latitude;
            this.longtitude = longtitude;
            this.zoom = zoom;
            this.message = message;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongtitude() {
            return longtitude;
        }

        public int getZoom() {
            return zoom;
        }

        public String getMessage() {
            return message;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Promise<Result> getMap(int zoom, int x, int y) {
        final Promise<Result> resultPromise = WS.url(String.format(TILE_URL, zoom, x, y)).get().map(
                new Function<WS.Response, Result>() {
                    public Result apply(WS.Response response) {
                        return ok(response.getBodyAsStream()).as("image/jpeg");
                    }
                }
        );
        return resultPromise;
    }

    @RoleSecured.RoleAuthenticated()
    public static Promise<Result> getLatLong(int addressId) {
        try {
            return getLatLongPromise(addressId).map(
                    new Function<F.Tuple<Double, Double>, Result>() {
                        public Result apply(F.Tuple<Double, Double> coordinates) {
                            return ok("Coordinates: Lat=" + coordinates._1 + ";Lon=" + coordinates._2);
                        }
                    });
        } catch(DataAccessException ex) {
            return Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return badRequest("Address id does not exist.");
                }
            });
        }
    }

    public static Promise<F.Tuple<Double, Double>> getLatLongPromise(int addressId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()){
            AddressDAO dao = context.getAddressDAO();
            Address address = dao.getAddress(addressId);
            if(address != null ){
                final Promise<F.Tuple<Double, Double>> resultPromise = WS.url(ADDRESS_RESOLVER)
                        .setQueryParameter("street", address.getNumber() + " " + address.getStreet())
                        .setQueryParameter("city", address.getCity())
                        .setQueryParameter("country", "Belgium")
                        .setQueryParameter("postalcode", address.getZip())
                        .setQueryParameter("format", "json").get().map(
                                new Function<WS.Response, F.Tuple<Double, Double>>() {
                                    public F.Tuple<Double, Double> apply(WS.Response response) {
                                        JsonNode node = response.asJson();
                                        if(node.size() > 0) {
                                            JsonNode first = node.get(0);
                                            return new F.Tuple<>(first.get("lat").asDouble(), first.get("lon").asDouble());
                                        } else return null;
                                    }
                                }
                        );
                return resultPromise;
            } else throw new DataAccessException("Could not find address by ID");
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showMap(){
        return ok(simplemap.render(new MapDetails(51.1891253d, 4.2355338d, 13, "Afspraak 27/04/2014")));
    }
}