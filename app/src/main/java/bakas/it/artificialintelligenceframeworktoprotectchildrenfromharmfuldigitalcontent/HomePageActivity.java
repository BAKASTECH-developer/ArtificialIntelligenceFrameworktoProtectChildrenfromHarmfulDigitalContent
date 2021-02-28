package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent.R;

public class HomePageActivity extends AppCompatActivity {

    GridView gw_gallery;//gallery grid view
    GridViewAdapter customGridAdapter;//Adapter for gallery grid view
    Button btn_startStop;//Start Stop button
    MediaProjection mProjection;//Media Projection variable for screenshot
    int mWidth ;//Screen width
    int mHeight ;//Screen height
    int mDensity ;//Screen density
    int resultCode;//Result after user permission request to screenshot
    Intent data;//Data from permission request
    int startStopState=0;//0 for stop 1 for start
    Handler screenshotHandler = new Handler();//Timer for screenshot timed to 10 sec
    Handler gwRefreshHandler = new Handler();//Timer for refreshing gallery timed to 1 sec
    Context context=this;//Current activity as context
    ImageReader mImageReader;

    //On activity create
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Setting toolbar as designed
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        //Items from xml file
        gw_gallery=findViewById(R.id.gw_gallery);
        btn_startStop=findViewById(R.id.btn_start_stop);

        //Starting gallery refreshing
        startRefreshHandler();

        //Getting screen size and density
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        mDensity = metrics.densityDpi;

        //Requesting permission from user to screen record
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},00);


        //Listener for Start Stop button click
        btn_startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startStopState==0){//If state is stop
                    startStopState=1;//Set state as start
                    btn_startStop.setText("Stop");//Set button text as Stop

                    MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(projectionManager.createScreenCaptureIntent(),1);

                }
                else if(startStopState==1){//If state is start
                    startStopState=0;//Set state as stop
                    btn_startStop.setText("Start");//Set button text as Start
                    stopScreenshot();//Stop taking screenshots
                }
            }
        });


    }

    //On permission request result this method starts getting screen data
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.resultCode=resultCode;
        this.data=data;

        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mProjection = projectionManager.getMediaProjection(resultCode, data);
        screenshotHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScreenshot();//Start taking screenshots
                screenshotHandler.postDelayed(this,10000);//creating loop with 10 secs delay
            }
        }, 10000);//10 secs delay

    }

    //Gets a bitmap and compressing it to JPG and saving
    public void saveBitmap(Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();//External Storage Path
        File myDir = new File(root + "/Parental_Control_Screenshots");//Adding our folder to path
        myDir.mkdirs();//Creating our folder if doesn't exist

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//Getting timestamp
        String fname = "Screenshot_"+ timeStamp +".jpg";//File name

        File file = new File(myDir, fname);//Creating file
        if (file.exists()) file.delete ();//Overwriting file if file already exist
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Gets media projection and placing it on image reader then saving it
    @SuppressLint("WrongConstant")
    private void startScreenshot(){
        final MediaProjection Projection=mProjection;//copying the projection
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);//Getting device window manager
        Display display = wm.getDefaultDisplay();//Display variable
        final DisplayMetrics metrics = new DisplayMetrics();//Metric values of current display
        display.getMetrics(metrics);//Getting metrics
        Point size = new Point();//Point variable to get real size
        display.getRealSize(size);//getting sizes of screen
        mWidth = size.x;//screen width
        mHeight = size.y;//screen height
        mDensity = metrics.densityDpi;//screen density

        mImageReader= ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);//Image reader to get image from media projection

        final Handler handler = new Handler();//Handler

        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;//Projection flags
        Projection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, handler);//Creating virtual display and saving it to mImageReader

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {//Saving an image on image eradder after calculations
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);//Setting listener to null

                Image image = reader.acquireLatestImage();//Creating image

                final Image.Plane[] planes = image.getPlanes();//Planes of image
                final ByteBuffer buffer = planes[0].getBuffer();//Byte buffer that will be a bitmap

                //bitmap sizes calculations
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                // create bitmap
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride), metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);//getting pixels from buffer to bitmap

                image.close();
                reader.close();

                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());//Getting real size bitmap
                bmp.recycle();

                saveBitmap(realSizeBitmap);//Saving bitmap
            }
        }, handler);
    }

    //Getting all pictures in specified folder adding them to array list and returns array list
    private ArrayList updateGallery(){
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
    }
    //On stop button pressed it stops screen capturing and drops screenshotHandler timer
    private void stopScreenshot(){
        screenshotHandler.removeCallbacksAndMessages(null);
        mProjection.stop();//Stopping screen capture
    }

    //Loop that refreshing gallery every 1 sec
    private void startRefreshHandler(){
        gwRefreshHandler.postDelayed(new Runnable() {
            public void run() {
                gw_gallery.setAdapter(null);//Dropping adapter
                customGridAdapter = new GridViewAdapter(context, R.layout.row_grid, updateGallery());//Re-creating adapter
                gw_gallery.setAdapter(customGridAdapter);//Setting adapter
                gwRefreshHandler.postDelayed(this, 1000);//Creating loop with 1 sec
            }
        }, 1000);//1 sec delay
    }
}