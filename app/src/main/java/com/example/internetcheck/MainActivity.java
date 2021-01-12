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
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity {

    private TextView successfulConnection;
    private TextView failedConnection;
    private int successRow = 0;
    private int failedRow = 0;
    private TextView wifiStrength;
    private TextView pingTime;
    private Handler handler;
    private String macAddress;
    private File file;
    private WifiManager wifiManager;
    private List<ScanResult> scan;
    private String time,time2;
    private  Process p;
    private Date date;
    private String res = "";
    private ArrayList<String> result;
    private Integer decibel;
    private String server_value="";
    private String package_size;
    private String interval_value;
    private int interval_ms;
    private int clicked = 0;
    private Runnable r;
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
        TextView wifiStrengthClassification = (TextView) findViewById(R.id.textView27);
        pingTime = (TextView)findViewById(R.id.textView28);

        wifiManager= (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiStrengthClassification.setText("5-Excellent >-50 dBm\n" +
                "\n" +
                "4-Good -50 to -60 dBm\n" +
                "\n" +
                "3-Fair -60 to -70 dBm\n" +
                "\n" +
                "2-Weak < -70 dBm\n"+"\n");


        Spinner server = (Spinner)findViewById(R.id.spinner1);
        Spinner packages = (Spinner)findViewById(R.id.spinner2);
        Spinner interval = (Spinner)findViewById(R.id.spinner3);
        Button startBtn = (Button)findViewById(R.id.startBtn);
       // packages.setEnabled(false);
       // interval.setEnabled(false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.servers,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        server.setAdapter(adapter);

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this,R.array.packages,android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packages.setAdapter(adapter1);

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this,R.array.seconds,android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interval.setAdapter(adapter2);

        server.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                server_value = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        packages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               package_size = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                interval_value = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (clicked == 0) {
                    String command = "ping -c " + package_size + " -s 128 " + server_value;

                    successfulConnection.setText("");
                    successRow=0;
                    failedConnection.setText("");
                    failedRow =0;
                    wifiStrength.setText("");
                    pingTime.setText("");

                    Log.d("FIDGET", command);
                    interval_ms = Integer.valueOf(interval_value) * 1000;
                    Log.d("FIDGET", String.valueOf(interval_ms));
                    handler = new Handler();

                     r = new Runnable() {
                        public void run() {
                            time = "";
                            time2 = "";
                            res = "";
                            // CheckConnection(URL);
                            CheckWifi();
                            getMac();
                            //executeCommand();
                            // executeCmd("ping -c 1 -s 128 10.0.0.11",false);
                            executeCmd(command, false);
                            handler.postDelayed(this, interval_ms);
                        }
                    };

                    handler.postDelayed(r, 2000);
                    startBtn.setText("STOP");
                    clicked++;
                   // longRunningTaskFuture = executorService.submit(r);

                }
                else if(clicked > 0)
                {

                    Toast.makeText(getApplicationContext(),"futok",Toast.LENGTH_SHORT).show();
                    clicked = 0;
                    startBtn.setText("START");
                    //longRunningTaskFuture.cancel(true);
                    handler.removeCallbacks(r);
                }

            }
        });



    }
    private boolean CheckConnection()
    {
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String URL = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=5";
            Connection connection = DriverManager.getConnection(URL);
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
        int number = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        scan = wifiManager.getScanResults();
        //int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), number);
        decibel = wifiInfo.getRssi();
        wifiStrength.setText("A Wifi erőssége = "+decibel+"dBm"+"\n");

    }

    public String getMac() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission Not Granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            WifiManager wifimanage = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiinfo = wifimanage.getConnectionInfo();
            macAddress = wifiinfo.getBSSID();//Get the mac address of the currently connected network;
            wifiStrength.append("\t"+macAddress+ "\t");

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

    public String executeCmd(String cmd, boolean sudo)
    {
        if(wifiManager.isWifiEnabled()) {
            Log.d("PING","WIFI");
            try {
                Log.d("PING","TRY");
                if (!sudo)
                    p = Runtime.getRuntime().exec(cmd);
                else {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                }
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String s;
               // result = new ArrayList<>();
                while ((s = stdInput.readLine()) != null) {
                    res += s + "\n";
                }

                p.waitFor();
                if(p.exitValue()==0) {
                    Log.d("PING","IF");
                    try {
                        int a = res.indexOf("received");
                        time = res.substring(a - 2, a + 8);

                    } catch (Exception e) {
                        time = "0ms";
                    }
                    try {
                        int b = res.indexOf("time=");
                        time2 = res.substring(b,b+12);
                        Log.d("FUTYUL", time2);
                    }catch (Exception asd)
                    {
                        time2 = "0ms";
                    }
                    pingTime.setText(time2);
                    successRow++;
                    successfulConnection.setText(String.valueOf(successRow));
                    date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Logger(time+";"+macAddress+";"+String.valueOf(format.format(date)+";"+time2+";"+decibel+"\n"));
                    p.destroy();
                    return res;
                }
                else
                {
                    try {
                        int a = res.indexOf("received");
                        time = res.substring(a - 2, a + 8);
                        //Log.d("FUTYUL", time);
                    } catch (Exception ex) {
                        time = "0ms";
                    }
                    try {
                        int b = res.indexOf("time=");
                        time2 = res.substring(b,b+12);
                        //Log.d("IDO", time2);
                    }catch (Exception asd)
                    {
                        time2 = "0ms";
                    }
                    Log.d("PING","ELSE");
                    date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Logger(time+";"+macAddress+";"+String.valueOf(format.format(date)+";"+time2+";"+decibel+";"+p.exitValue()+"\n"));
                    p.destroy();
                    failedRow++;
                    failedConnection.setText(String.valueOf(failedRow));
                    pingTime.setText(pingTime +" _Nem 0 process kód");
                    return "catch";
                }


            } catch (Exception e) {
                p.destroy();
                try {
                    int a = res.indexOf("received");
                    time = res.substring(a - 2, a + 8);
                    Log.d("FUTYUL", time);
                } catch (Exception exec) {
                    time = "0ms";
                }
                try {
                    int b = res.indexOf("time=");
                    time2 = res.substring(b,b+12);
                    Log.d("IDO", time2);
                }catch (Exception asd)
                {
                    time2 = "0ms";
                }
                Log.d("PING","CATCH");
                date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Logger(time+";"+macAddress+";"+String.valueOf(format.format(date)+";"+time2+";"+decibel+";"+"catch"+"\n"));
                e.printStackTrace();
                failedRow++;
                failedConnection.setText(String.valueOf(failedRow));
                pingTime.setText("Sikertelen fogadás");
                return "catch";
            }
        }
        else
        {
            Log.d("PING","NINCS_WIFI");
            failedRow++;
            failedConnection.setText(String.valueOf(failedRow));
            date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pingTime.setText("Nincs Wifi");
            Logger("NEM VOLT WIFI;"+"null;"+String.valueOf(format.format(date)+";"+";"+"\n"));
            return "catch";
        }

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
                Toast.makeText(this, "ROSSZ", Toast.LENGTH_SHORT).show();
            }

        }


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////

}