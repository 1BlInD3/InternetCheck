package com.example.internetcheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import net.sourceforge.jtds.jdbc.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity {

    private TextView successfulConnection;
    private TextView failedConnection;
    private int successRow = 0;
    private int failedRow = 0;
    private TextView wifiStrength;
    private TextView wifiStrengthClassification;
    private TextView pingTime;
    private Handler handler;
    private String macAddress;
    private File file;
    private String URL = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=5";
    private Connection connection;
    private WifiManager wifiManager;
    private List<ScanResult> scan;
    private String time;
    private String time2;
    private String name = "DOGGER.txt";
    private NetworkChangeReceiver nr = new NetworkChangeReceiver();
    private static final int REQUEST_STORAGE = 112;
    private boolean first = true;
    private Date date;
    private String res = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        successfulConnection = (TextView)findViewById(R.id.textView2);
        failedConnection = (TextView)findViewById(R.id.textView4);
        wifiStrength = (TextView)findViewById(R.id.textView26);
        wifiStrengthClassification = (TextView)findViewById(R.id.textView27);
        pingTime = (TextView)findViewById(R.id.textView28);


        wifiStrengthClassification.setText("5-Excellent >-50 dBm\n" +
                "\n" +
                "4-Good -50 to -60 dBm\n" +
                "\n" +
                "3-Fair -60 to -70 dBm\n" +
                "\n" +
                "2-Weak < -70 dBm\n"+"\n");

        handler = new Handler();


        final Runnable r = new Runnable() {
            public void run() {

               // CheckConnection(URL);
                CheckWifi();
                getMac();
                //executeCommand();
                executeCmd("ping -c 1 -s 128 10.0.0.11",false);
               // Logger(macAddress+"\t"+time+"\t"+"\n");
                handler.postDelayed(this, 2000);
            }
        };

        handler.postDelayed(r, 2000);



    }
    private boolean CheckConnection(String connString)
    {
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connection = DriverManager.getConnection(URL);
            if(connection != null)
            {
                Log.d("DONKEY","Csatlakozva");
                successRow ++;
                successfulConnection.setText(String.valueOf(successRow));
                return true;
            }
            else
            {
                Log.d("DONKEY","Nincs csatlakozva");
                failedRow ++;
                failedConnection.setText(String.valueOf(failedRow));
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DONKEY","Nincs csatlakozva");
            failedRow ++;
            failedConnection.setText(String.valueOf(failedRow));
            return false;

        }
    }

    private void CheckWifi()
    {
        wifiManager= (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int number = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        scan = wifiManager.getScanResults();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), number);
        wifiStrength.setText("A Wifi erőssége = "+String.valueOf(level)+"\n");

    }



    public String getMac() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission Not Granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            WifiManager wifimanage = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiinfo = wifimanage.getConnectionInfo();
            macAddress = wifiinfo.getBSSID();//Get the mac address of the currently connected network;
            wifiStrength.append("\t"+macAddress);

        }
        return macAddress;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted keep going status
                    Log.d("MALAC", "PERMISSION GRANTED");
                    WifiManager wifimanage = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    WifiInfo wifiinfo = wifimanage.getConnectionInfo();
                    macAddress = wifiinfo.getBSSID();//Get the mac address of the currently connected network;
                    Log.d("MALAC",macAddress);
                } else {
                    Log.d("MALAC", "PERMISSION DENIED");
                }
        }
    }

    public String executeCmd(String cmd, boolean sudo){
        try {

            Process p;
            if(!sudo)
                p= Runtime.getRuntime().exec(cmd);
            else{
                p= Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            }
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String s;
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
            }
            time = res.substring(167,177);
            int a = res.indexOf("time=");
            time2 = res.substring(245,260);
           // char a = res.charAt(96);
            Log.d("FUTYUL", String.valueOf(a));
            p.destroy();
            Log.d("CSENG",res+"\n");
            //wifiStrength.append("\t"+time);
            pingTime.setText(time);
            successRow ++;
            successfulConnection.setText(String.valueOf(successRow));
            date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Logger(time +"\t"+" | "+"\t" + String.valueOf(format.format(date)+"\n"));
            return res;
        } catch (Exception e) {
            Logger("ELDOBTAM ______________________________"+res);
            e.printStackTrace();
            failedRow ++;
            failedConnection.setText(String.valueOf(failedRow));
            pingTime.setText("0");
        }
        return "";

    }

    public void Logger(String data)
    {
        File path = this.getExternalFilesDir(null);
        File file = new File(path,"LoggerDogger.txt");

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            try {
                FileOutputStream stream = new FileOutputStream(file,true);
                stream.write(data.getBytes());
                stream.close();
                Log.d("IRAS", "Becsukta");
            } catch (Exception e) {

            }

        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

}