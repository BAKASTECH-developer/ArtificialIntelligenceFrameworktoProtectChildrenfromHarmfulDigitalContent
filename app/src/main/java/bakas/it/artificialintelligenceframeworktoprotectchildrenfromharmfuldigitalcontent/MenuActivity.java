package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    Spinner spInterval;
    Spinner spFileAmount;
    EditText txtUserId;
    Button save;
    int interval=10;
    int fileAmount=10;
    String userId="";

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
        txtUserId=findViewById(R.id.txt_set_user_id);
        save=findViewById(R.id.btn_save);

        ArrayAdapter adapterInterval=new ArrayAdapter(this, R.layout.spinner_item,intervals);
        adapterInterval.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spInterval.setAdapter(adapterInterval);

        ArrayAdapter adapterFileAmount=new ArrayAdapter(this, R.layout.spinner_item,fileAmounts);
        adapterFileAmount.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spFileAmount.setAdapter(adapterFileAmount);

        HashMap<String, String> userPrefs = session.getUserDetails();

        interval=Integer.parseInt(userPrefs.get(SessionManagement.KEY_INTERVAL));
        spInterval.setSelection(interval-1);

        userId=String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
        txtUserId.setText(userId);

        fileAmount=Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        spFileAmount.setSelection(fileAmount-1);
    }

    public void save_button(View view){
        session.saveUserPref(txtUserId.getText().toString(),String.valueOf(spInterval.getSelectedItemPosition()+1),String.valueOf(spFileAmount.getSelectedItemPosition()+1));
        finish();
    }


    public void help_button(View view){
        Intent intent=new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
}