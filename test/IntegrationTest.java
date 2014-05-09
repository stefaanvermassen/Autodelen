import database.DataProvider;
import database.mocking.TestDataAccessProvider;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import static org.fluentlenium.core.filter.FilterConstructor.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                DataProvider.setDataAccessProvider(new TestDataAccessProvider()); // Required!!

            }
        });
    }



}
