package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    Spinner spInterval;
    Spinner spFileAmount;
    Button save;
    int interval=10;

    SessionManagement session;

    String[] intervals={"1","2","3","4","5","6","7","8","9","10",
            "11","12","13","14","15","16","17","18","19","20",
            "21","22","23","24","25","26","27","28","29","30"};

    String[] fileAmounts={"1","2","3","4","5","6","7","8","9","10",
            "11","12","13","14","15","16","17","18","19","20",
            "21","22","23","24","25","26","27","28","29","30",
            "31","32","33","34","35","36","37","38","39","40",
            "41","42","43","44","45","46","47","48","49","50",
            "51","52","53","54","55","56","57","58","59","60"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManagement(getApplicationContext());

        spInterval=findViewById(R.id.sp_set_interval);
        spFileAmount=findViewById(R.id.sp_set_file_number);
        save=findViewById(R.id.btn_save);

        ArrayAdapter adapter=new ArrayAdapter(this, R.layout.spinner_item,intervals);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spInterval.setAdapter(adapter);

        HashMap<String, String> userPrefs = session.getUserDetails();

        interval=Integer.parseInt(userPrefs.get(SessionManagement.KEY_INTERVAL));
        spInterval.setSelection(interval-1);
    }

    public void save_button(View view){
        session.saveUserPref("",String.valueOf(spInterval.getSelectedItemPosition()+1),"");
        finish();
    }


}