package io.flutter;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.content.Context;
import androidx.core.content.ContextCompat;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftpos.apiservice.aidl.printer.IOnPrinterListener;
import io.flutter.util.TestHelper;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_LEFT;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_RIGHT;
import com.ftpos.library.smartpos.buzzer.Buzzer;
import com.ftpos.library.smartpos.crypto.Crypto;
import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.led.Led;
import com.ftpos.library.smartpos.magreader.MagReader;
import com.ftpos.library.smartpos.memoryreader.MemoryReader;
import com.ftpos.library.smartpos.nfcreader.NfcReader;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.psamreader.PsamReader;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;
import static java.lang.Math.ceil;
import java.lang.reflect.Method;
import android.text.TextUtils;
import com.example.new_park_app.R;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.demo_test/printReceipt";
    private static Paint paint = null;
    
    private Printer printer1;

    private Context mContext;

    final String TAG = "FtSDKDemo";

    private Button mExit;
    private Button mClear;
    private TextView mTitle;


    private TextView mShowResultTv;

    public static KeyManager keyManager = null;
    public static Printer printer = null;
    public static Device device = null;
    public static Crypto crypto = null;
    public static MemoryReader memoryReader = null;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("on onStart== ", "yes");
        getDeviceModel();
        Log.d("getDeviceModel", "mDeviceModel:" + String.valueOf(mDeviceModel));
        mContext = this;
        if(printer == null){
            printer = Printer.getInstance(mContext);
        }

        try {

            ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
                @Override
                public void onSuccess() {
                    Log.e("bind service called ", "success");
                    printer = Printer.getInstance(mContext);
                    device = Device.getInstance(mContext);
                    crypto = Crypto.getInstance(mContext);
                    memoryReader = MemoryReader.getInstance(mContext);

                    keyManager = KeyManager.getInstance(mContext);
                    String packageName = getApplicationContext().getPackageName();
                    Log.d("bindPosServer===", "binded:");
                    if (mDeviceModel != DEVICE_MODE_UNKOWN && mDeviceModel != DEVICE_MODE_F100) {
                        // After connecting to the Service, it must be called once to init the
                        // KeyManager, no need to call repeatedly
                        int ret = keyManager.setKeyGroupName(packageName);
                        Log.e("mDeviceModel - ", String.valueOf(mDeviceModel));
                        if (ret != ERR_SUCCESS) {
                        }
                    }
                }

                @Override
                public void onFail(int var1) {
                    Log.e("binding == ", "onFail");
                }
            });
        } catch (Exception e) {
            Log.e("binding === ", "onFail");
        } 

        try {
            //Check if write permission is available.
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // No permission to write, to apply for permission to write, will pop up a dialog box.
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                (call, result) -> {
                    if (call.method.equals("printReceipt")) { //this is method channel name
                        Log.e("method channl java = ", "called");
                        bindService();
                    } else {
                        result.notImplemented();
                    }
                }
        );
    }

    // binding the service
    void bindService(){
        Log.e("on onStart== ", "yes");
        getDeviceModel();
        Log.d("getDeviceModel", "mDeviceModel:" + String.valueOf(mDeviceModel));
        mContext = this;
        if(printer == null){
            printer = Printer.getInstance(mContext);
        }

        try {

            ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
                @Override
                public void onSuccess() {
                    Log.e("bind service called ", "success");
                    printer = Printer.getInstance(mContext);
                    device = Device.getInstance(mContext);
                    crypto = Crypto.getInstance(mContext);
                    memoryReader = MemoryReader.getInstance(mContext);
                    keyManager = KeyManager.getInstance(mContext);
                    String packageName = getApplicationContext().getPackageName();

                    Log.d("bindPosServer===", "binded:");
                    if (mDeviceModel != DEVICE_MODE_UNKOWN && mDeviceModel != DEVICE_MODE_F100) {
                        // After connecting to the Service, it must be called once to init the
                        // KeyManager, no need to call repeatedly
                        int ret = keyManager.setKeyGroupName(packageName);
                        Log.e("mDeviceModel - ", String.valueOf(mDeviceModel));
                        if (ret != ERR_SUCCESS) {
                        }
                    }

                    // print receipt method calling
                    printReceipt();
                }

                @Override
                public void onFail(int var1) {
                    Log.e("binding == ", "onFail");
                }
            });
        } catch (Exception e) {
            Log.e("binding === ", "onFail");
        } 

        try {
            //Check if write permission is available.
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // No permission to write, to apply for permission to write, will pop up a dialog box.
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void logMsg(){

    }

    // printing the receipt
    public String printReceipt(){
        try {

            int ret;
            ret = printer.open();
            if(ret != ERR_SUCCESS){
                Log.e("yhf","open failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return "";
            }

            ret = printer.startCaching();
            if(ret != ERR_SUCCESS){
                Log.e("yhf","startCaching failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return "";
            }

            ret = printer.setGray(3);
            if(ret != ERR_SUCCESS){
                Log.e("yhf","startCaching failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return "";
            }

            PrintStatus printStatus = new PrintStatus();
            ret = printer.getStatus(printStatus);
            if(ret != ERR_SUCCESS){
                Log.e("yhf","getStatus failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return "";
            }

            Log.e("yhf","Temperature = "+ printStatus.getmTemperature() + "\n" );
            Log.e("yhf","Gray = "+ printStatus.getmGray() + "\n" );
            if(!printStatus.getmIsHavePaper()){
                Log.e("yhf","Printer out of paper\n");
                return "";
            }

            printer.setAlignStyle(PRINT_STYLE_CENTER);
            printer.printStr("MICHAEL KORS\n");

            printer.setAlignStyle(PRINT_STYLE_LEFT);
            printer.printStr("Please retain this receipt\n");
            printer.printStr("for your exchange.\n");
            printer.printStr("this gift was thoughtfully purchased\n");
            printer.printStr("for you at Michael Kors Chinook Centre.\n");

            ret = printer.getUsedPaperLenManage();
            if(ret < 0){
                Log.e("yhf","getUsedPaperLenManage failed"+ String.format(" errCode = 0x%x\n" , ret) );
            }


            Bitmap bitmap = Bitmap.createBitmap(384, 400, Bitmap.Config.RGB_565);

            int k_CurX = 0;
            int k_CurY = 0;
            int k_TextSize = 24;
            paint = new Paint();
            paint.setTextSize(k_TextSize);
            paint.setColor(Color.BLACK);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(Color.parseColor("#FFFFFF"));

            Paint.FontMetrics fm = paint.getFontMetrics();
            int k_LineHeight = (int)ceil(fm.descent-fm.ascent);
            String displayStr = "MICHAEL KORS";
            int lineWidth = getTextWidth(displayStr);
            k_CurX = (384-lineWidth)/2;
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight+5;
            displayStr = "Please retain this receipt";
            k_CurX = 0;
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;
            displayStr = "for your exchange.";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "this gift was thoughtfully purchased";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "for you at Michael Kors Chinook ";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "Centre.";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;


            Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, 384, k_CurY);

            ret = printer.printBmp(newbitmap);
            if(ret != ERR_SUCCESS){
                Log.e("yhf","printBmp failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return "";
            }

            if(bitmap != null && !bitmap.isRecycled()){
                Bitmap mFreeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
                canvas.setBitmap(mFreeBitmap);
                canvas = null;
                // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                bitmap.recycle();
                bitmap = null;
                paint.setTypeface(null);
                paint = null;
            }
            if(newbitmap != null && !newbitmap.isRecycled()){
                newbitmap.recycle();
                newbitmap = null;
            }

            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    Log.e("yhf","print success\n");
                    printer.feed(32);
                }

                @Override
                public void onError(int i) {
                    Log.e("yhf","printBmp failed"+ String.format(" errCode = 0x%x\n", i) );
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static int getTextWidth(String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) ceil(widths[j]);
            }
        }

        return iRet;
    }

    public final static int DEVICE_MODE_UNKOWN = -1;
    public final static int DEVICE_MODE_F100 = 1;
    public final static int DEVICE_MODE_F200 = 0;
    public final static int DEVICE_MODE_F600_300 = 2;
    private static int mDeviceModel = DEVICE_MODE_UNKOWN;

    public static int getDeviceModel() {
        if (mDeviceModel == DEVICE_MODE_UNKOWN) {
            // String deviceModel = android.os.Build.MODEL;
            String deviceModel = getSystemProperty("ro.product.model", "null");
            if (deviceModel.equals("F100") || android.os.Build.MODEL.equals("full_k61v1_32_bsp_1g")) {
                mDeviceModel = DEVICE_MODE_F100;
            } else if (deviceModel.equals("F300") || deviceModel.equals("F600")) {
                mDeviceModel = DEVICE_MODE_F600_300;
            } else {
                mDeviceModel = DEVICE_MODE_F200;
            }
        }
        return mDeviceModel;
    }

    private static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            // Log.e("SDK", "getSystemProperty: Unable to read system properties");
        }
        return android.os.Build.MODEL;
    }
}