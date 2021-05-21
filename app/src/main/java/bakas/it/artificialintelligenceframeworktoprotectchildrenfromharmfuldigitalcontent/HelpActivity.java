package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class HelpActivity extends AppCompatActivity {

    String head ="";
    String body ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

    }
    public void setInterval(View view){
        head ="Set Interval";
        body ="\nWith this option, you can select the delay between screenshots.\n" +
                "\n" +
                "Minimum 1, maximum 30 seconds can be selected.";
        create_alert();
    }
    public void setUserId(View view){
        head ="Set User ID";
        body ="\nWith this option, you can enter a user name which will be appear on log file.";
        create_alert();
    }
    public void setFileNumber(View view){
        head ="Set File Number";
        body ="\nWith this option, you can select the amount of screenshots in a session.\n" +
                "\n" +
                "Minimum 1, maximum 60 screenshots can be selected.";
        create_alert();
    }


    public void create_alert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this,R.style.AlertDialogStyle);
        builder.setCancelable(true);
        builder.setTitle(head);
        builder.setMessage(body);

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    public void back_buton(View view)
    {
        this.finish();
    }

}