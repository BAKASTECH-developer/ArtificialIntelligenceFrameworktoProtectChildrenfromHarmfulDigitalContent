package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManagement {
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "UserPref";

    // Interval
    public static final String KEY_INTERVAL = "interval";

    // User ID
    public static final String KEY_USER_ID = "userId";

    // File Amount
    public static final String KEY_FILE_AMOUNT = "fileAmount";



    // Constructor
    public SessionManagement(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void saveUserPref(String userID,String interval, String fileAmount){

        // Storing userID in pref
        editor.putString(KEY_USER_ID, userID);

        // Storing interval in pref
        editor.putString(KEY_INTERVAL, interval);

        // Storing fileAmount in pref
        editor.putString(KEY_FILE_AMOUNT, fileAmount);

        // commit changes
        editor.commit();
    }
    public void setUserId(String userID){

        // Storing userID in pref
        editor.putString(KEY_USER_ID, userID);

        // commit changes
        editor.commit();
    }
    public void setInterval(String interval){

        // Storing userID in pref
        editor.putString(KEY_INTERVAL, interval);

        // commit changes
        editor.commit();
    }
    public void setFileAmount(String fileAmount){

        // Storing userID in pref
        editor.putString(KEY_FILE_AMOUNT, fileAmount);

        // commit changes
        editor.commit();
    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user id
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        // user interval
        user.put(KEY_INTERVAL, pref.getString(KEY_INTERVAL, null));

        // user fileAmount
        user.put(KEY_FILE_AMOUNT, pref.getString(KEY_FILE_AMOUNT, null));

        // return user
        return user;
    }



}
