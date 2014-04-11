package controllers.util;

import play.api.Play;

/**
 * Created by Cedric on 4/11/2014.
 */
public class ConfigurationHelper {

    //TODO: Fix this hack with default values??
    public static String getConfigurationString(String name){
        return Play.current().configuration().getStringList(name).get().get(0);
    }
}
