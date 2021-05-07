package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;
/**
 *      ArtificialIntelligenceFrameworktoProtectChildrenfromHarmfulDigitalContent
 *      Copyright (C) 2021 BAKAS BİLİŞİM ELEKTRONİK YAZILIM DANIŞMANLIK SANAYİ VE TİCARET LİMİTED ŞİRKETİ
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.

 *     This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.

 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent.R;

public class HomePageActivity extends AppCompatActivity {

    ImageView lastScreenshot;//gallery grid view
    GridViewAdapter customGridAdapter;//Adapter for gallery grid view
    Button btn_startStop;//Start Stop button
    Button btn_menu;
    TextView classifierResultText;
    MediaProjection mProjection;//Media Projection variable for screenshot
    int mWidth ;//Screen width
    int mHeight ;//Screen height
    int mDensity ;//Screen density
    int resultCode;//Result after user permission request to screenshot
    Intent data;//Data from permission request
    int startStopState=0;//0 for stop 1 for start
    Handler gwRefreshHandler = new Handler();//Timer for refreshing gallery timed to 1 sec
    public static ScreenshotService screenshotService;//Screenshot service that runs in background and takes screenshots
    private static boolean mServiceConnected;//Boolean value that shows if screenshot service is connected
    ProgressDialog dialog;//Mail sending dialog
    String lastLogFileDir="";//Keeps the last log file's directory
    String lastScreenshotsFileDir="invalid_path";//Keeps the last screenshots' file's directory
    SessionManagement session;//Class for keeping user settings
    HashMap<String, String> userPrefs;
    int interval,fileAmount;
    String userId;

    //Connects service
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        //On service connected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            screenshotService = ((ScreenshotService.LocalBinder) service).getService();//Get service and assign
            mServiceConnected = true;//set service connected true
        }
        //On service disconnected
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            screenshotService = null;//drop service
            mServiceConnected=false;//set service connected false
        }
    };

    //Binds service to activity
    public void doBindService() {
        Intent ssServiceIntent = new Intent(this, ScreenshotService.class);//Create intent with this activity and Screenshot service
        bindService(ssServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//Binding service and calls connection method
    }

    //On activity create
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Setting toolbar as designed
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_INTERVAL)==null){
            session.setInterval("10");
        }
        else {
            interval = Integer.parseInt(userPrefs.get(SessionManagement.KEY_INTERVAL));
        }
        if(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT)==null){
            session.setFileAmount("10");
        }
        else {
            fileAmount = Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        }


        //Checking write on disk permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        //Items from xml file
        lastScreenshot=findViewById(R.id.lastScreenshotImage);
        btn_startStop=findViewById(R.id.btn_start_stop);
        btn_menu=findViewById(R.id.btn_menu);
        classifierResultText=findViewById(R.id.classifierResultText);

        //Starting gallery refreshing
        startRefreshHandler();

        //Getting screen size and density
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        mDensity = metrics.densityDpi;

        Intent ssServiceIntent = new Intent(this, ScreenshotService.class);//Create intent with this activity and Screenshot service
        bindService(ssServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//Binding service and calls connection method


        //Listener for Start Stop button click
        btn_startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartStopCall();
            }
        });

        //Listener for Menu button click
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startStopState==0){//If start stop button is on stop state
                    Intent intent=new Intent(HomePageActivity.this,MenuActivity.class);
                    startActivity(intent);//Start menu activity
                }
            }
        });
    }

    //On activity resume
    @Override
    protected void onResume() {
        super.onResume();
        if(screenshotService==null){//If no services bound
            doBindService();//Bind service
        }
        else if(screenshotService.stoppedAtBackground==true){
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        }
        final IntentFilter filter = new IntentFilter();// Intent filter
        filter.addAction("Mail_Sent");//Action mail sent
        registerReceiver(mailUpdateReceiver, filter);//Registering the broadcast receiver

    }

    //On activity pause
    @Override
    protected void onPause() {
        unregisterReceiver(mailUpdateReceiver);//Unregistering the broadcast receiver
        super.onPause();
    }

    //On permission request result this method starts getting screen data
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.resultCode=resultCode;
        this.data=data;
        //Starting media stream
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mProjection = projectionManager.getMediaProjection(resultCode, data);
        lastScreenshotsFileDir = new SimpleDateFormat("MMdd_HHmmss").format(new Date());//Getting timestamp
        screenshotService.initialize(mProjection, lastScreenshotsFileDir,interval);//starting auto screenshot

    }

    //Getting all pictures in specified folder adding them to array list and returns array list
    //For grid view gallery
    /*private ArrayList updateGallery(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots");
        if (!folder.exists()) {//If folder doesn't exist
            folder.mkdirs();//Create folder
        }

        final ArrayList imageItems = new ArrayList();//Declaring an array list of pictures

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        for (int i = 0; i < imageFiles.length; i++) {//for all files in folder
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());//Create bitmap from pictures
            imageItems.add(new ImageItem(bitmap, "Image#" + i));//add bitmaps to array list
        }

        return imageItems;// return array list
    }*/

    //Updates the last screenshot image every 1 sec
    private void updatePicture(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots/"+lastScreenshotsFileDir);
        if (!folder.exists()) {//If folder doesn't exist
            return;
        }

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        if(imageFiles.length>0) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[imageFiles.length - 1].getAbsolutePath());//Create bitmap from pictures
            lastScreenshot.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 600, 800, false));//Resizing to 800x600
            classifierResultText.setVisibility(View.VISIBLE);//Set prediction result text visible
            if(screenshotService.predictionResult=="SAFE"){
                classifierResultText.setText("SAFE");
                classifierResultText.setTextColor(Color.GREEN);
            }
            else if (screenshotService.predictionResult=="VIOLENCE"){
                classifierResultText.setText("VIOLENCE");
                classifierResultText.setTextColor(Color.RED);
            }
            else if (screenshotService.predictionResult=="SEXUALITY"){
                classifierResultText.setText("SEXUALITY");
                classifierResultText.setTextColor(Color.RED);
            }

        }
    }

    //Loop that refreshing gallery every 1 sec
    private void startRefreshHandler(){
        gwRefreshHandler.postDelayed(new Runnable() {
            public void run() {

                updatePicture();//Update picture

                //For grid view gallery
                /*gw_gallery.setAdapter(null);//Dropping adapter
                customGridAdapter = new GridViewAdapter(context, R.layout.row_grid, updateGallery());//Re-creating adapter
                gw_gallery.setAdapter(customGridAdapter);//Setting adapter*/

                gwRefreshHandler.postDelayed(this, 1000);//Creating loop with 1 sec
            }
        }, 1000);//1 sec delay
    }

    //Sending email of screenshots
    public void sendEmail()
    {
        //Opening progress dialog with text sending mail
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending mail...");
        dialog.show();



        Handler handler1 = new Handler();//Timer to wait before mail sending due to problems can occur while stopping screenshot
        handler1.postDelayed(new Runnable() {
            public void run() {
                Mail mail =new Mail(HomePageActivity.this,lastLogFileDir,Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots/"+lastScreenshotsFileDir);//Create mail object
                mail.execute();//Execute mail sending
            }
        }, 1000);   //1 seconds

    }

    /*//Deletes all screenshots saved
    private void deleteScreenshots(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots");
        if (!folder.exists()) {//If folder doesn't exist
            return;
        }

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        for(int i=imageFiles.length-1;i>=0;i--){
            imageFiles[i].delete();//Delete file
        }
    }*/


    //Listening the broadcasts
    private final BroadcastReceiver mailUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case "Mail_Sent"://Case of mail sending completed
                    dialog.dismiss();//Close dialog
                    break;

                default:
                    break;
            }
        }
    };

    public void btnStartStopCall(){
        if(startStopState==0){//If state is stop
            startStopState=1;//Set state as start
            btn_startStop.setText("Stop");//Set button text as Stop

            updatePrefs();

            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(projectionManager.createScreenCaptureIntent(),1);

        }
        else if(startStopState==1){//If state is start
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
            lastLogFileDir=screenshotService.stopScreenshot();//Stop taking screenshots
            sendEmail();//Send email on stop
        }
    }

    public void updatePrefs(){
        userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_INTERVAL)==null){
            session.setInterval("10");
        }
        else {
            interval = Integer.parseInt(userPrefs.get(SessionManagement.KEY_INTERVAL));
        }
        if(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT)==null){
            session.setFileAmount("10");
        }
        else {
            fileAmount = Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        }
    }
}