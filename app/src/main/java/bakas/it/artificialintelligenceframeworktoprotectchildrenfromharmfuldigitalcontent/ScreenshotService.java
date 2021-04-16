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
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent.classifier.Classifier;

public class ScreenshotService extends Service {

    int mWidth ;//Screen width
    int mHeight ;//Screen height
    int mDensity ;//Screen density
    ImageReader mImageReader;//Image reader
    MediaProjection mProjection;//Media Projection variable for screenshot
    Handler screenshotHandler = new Handler();//Timer for screenshot timed to 10 sec
    private final IBinder mBinder = new LocalBinder();//Gets current service object
    String logs="";//Text that will be written to log file
    Classifier classifier=new Classifier();//Classifier object
    int screenshotCount=0;//A counter for last screenshot session and counts the amount how many screenshot taken
    public boolean stoppedAtBackground=false;// Shows if app stopped taking screenshots while on background
    int maxScreenshot=12;//Maximum amount of screenshot per season
    String lastScreenshotsFileDir="";//Direction of screenshots from last session
    String predictionResult="";//Keeps the prediction result

    public ScreenshotService() {//Empty constructor
    }

    //Returns current service object
    public class LocalBinder extends Binder {
        public ScreenshotService getService() {
            return ScreenshotService.this;
        }
    }
    //On service bound
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    //Starts auto screenshot and takes a screenshot every 10 secs
    public boolean initialize(MediaProjection mProjection, final String timeStamp, final int interval) {
        screenshotCount=0;
        this.mProjection=mProjection;//Currently running media projection
        this.lastScreenshotsFileDir=Environment.getExternalStorageDirectory().toString()+"/Parental_Control_Screenshots/"+timeStamp;
        screenshotHandler.postDelayed(new Runnable() {//10 sec timer for screenshot
            @Override
            public void run() {
                if(screenshotCount<maxScreenshot){
                    startScreenshot(timeStamp);//Start taking screenshots
                    screenshotHandler.postDelayed(this,interval*1000);//creating loop with value of interval secs delay
                }
            }
        }, 0);//0 secs delay

        return true;
    }

    //Gets a bitmap and compressing it to JPG and saving
    public String saveBitmap(Bitmap bitmap,String newFolderName) {
        File myDir = new File(lastScreenshotsFileDir);//Adding our folder to path
        myDir.mkdirs();//Creating our folder if doesn't exist

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//Getting timestamp
        String fname = "Screenshot_"+ timeStamp +".jpg";//File name
        String prediction="";//result of prediction;
        File file = new File(myDir, fname);//Creating file
        if (file.exists()) file.delete ();//Overwriting file if file already exist
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);
            Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
            bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
            bmp=Bitmap.createScaledBitmap(bmp, 320, 320, false);//Resizing to 320x320
            classifier.classify(bmp);//Sending 320x320 bitmap to classifier
            prediction=classifier.predict();
            if(prediction.equals("BAKAS BILISIM framework classification result: Normal Content")){
                predictionResult="SAFE";
            }else if(prediction.equals("BAKAS BILISIM framework classification result: Violence Content")){
                predictionResult="VIOLENCE";
            }else{
                predictionResult="SUSPICIOUS";
            }
            bitmap=drawTextToBitmap(predictionResult,bitmap);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);//Saving screenshot
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return predictionResult;

    }

    //Draws text at the center of Bitmap
    public Bitmap drawTextToBitmap(String gText,Bitmap bitmap) {

        float scale = MyApplication.getInstance().getResources().getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        if(gText=="SAFE") {
            paint.setColor(Color.rgb(32, 197, 14));//green
        }
        else if(gText=="SUSPICIOUS") {
            paint.setColor(Color.rgb(214, 209, 56));//yellow
        }
        else {
            paint.setColor(Color.rgb(214, 53, 18));//red
        }
        // text size in pixels
        paint.setTextSize((int) (20 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    //Gets media projection and placing it on image reader then saving it
    @SuppressLint("WrongConstant")
    public void startScreenshot(final String newFolderName){
        classifier.initialize();
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

                predictionResult=saveBitmap(realSizeBitmap,newFolderName);//Saving bitmap
                String timeStamp = new SimpleDateFormat("dd.MM-HH:mm:ss").format(new Date());//Getting timestamp
                logs+=timeStamp+" "+predictionResult+"\n";//Adding a new line to logs
            }
        }, handler);



        screenshotCount+=1;
        if(screenshotCount==maxScreenshot+1){
            finishSession();
        }
    }

    //On stop button pressed it stops screen capturing and drops screenshotHandler timer
    public String stopScreenshot(){
        screenshotHandler.removeCallbacksAndMessages(null);
        mProjection.stop();//Stopping screen capture
        String directory=createLogFile();//Creating log file
        return directory;
    }

    //Creates log file with logs string and timestamp as name
    public String createLogFile(){
        String timeStamp = new SimpleDateFormat("ddMM-HHmm").format(new Date());//Getting timestamp
        String fname = "BBPC-"+ timeStamp +".txt";//File name
            /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(MyApplication.getInstance().openFileOutput(fname, Context.MODE_PRIVATE));//File writer
            outputStreamWriter.write(logs);//Writing logs to file
            outputStreamWriter.close();//Close writer*/

        String root = Environment.getExternalStorageDirectory().toString();//External Storage Path
        File myDir = new File(root + "/Parental_Control_Log_Files");//Adding our folder to path
        myDir.mkdirs();//Creating our folder if doesn't exist

        File file = new File(myDir, fname);//Creating file
        if (file.exists()) file.delete ();//Overwriting file if file already exist
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);
            out.write(logs.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myDir+"/"+fname;
    }

    private void finishSession(){
        stoppedAtBackground=true;//finish flag
        String lastLogFileDir=stopScreenshot();//Stop taking screenshots
        sendEmail(lastLogFileDir);//Send email on stop
    }

    //Sending email of screenshots
    public void sendEmail(final String lastLogFileDir)
    {
        Mail mail =new Mail(null,lastLogFileDir,lastScreenshotsFileDir);//Create mail object
        mail.execute();//Execute mail sending
    }

}
