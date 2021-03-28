package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;

    public MyApplication() {
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
