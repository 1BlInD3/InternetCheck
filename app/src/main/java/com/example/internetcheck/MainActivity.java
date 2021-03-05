package com.example.internetcheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.heatmaps.WeightedLatLng;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    private String time, time2;
    private Process p;
    private Date date;
    private String res = "";
    private ArrayList<String> result;
    private Integer decibel;
    private String server_value = "";
    private String package_size;
    private String interval_value;
    private int interval_ms;
    private int clicked = 0;
    private int clicked2 = 0;
    private Runnable r;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest request;
    private LocationCallback locationCallback;
    private ArrayList<Double> lat = new ArrayList<>();
    private ArrayList<Double> lon = new ArrayList<>();
    private ArrayList<LatLng> poly = new ArrayList<>();
    private Button GPS;
    private ArrayList <String> dec = new ArrayList<>();
    private CheckBox accuratePosition;
    private TextView text666;
    private int y = 0;
    private ArrayList<WeightedLatLng> points = new ArrayList<>();
    private ArrayList<LatLng> coords = new ArrayList<>();
    private ArrayList<Integer> classification = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text666 = findViewById(R.id.textView666);
        accuratePosition = findViewById(R.id.checkBox);
        GPS = findViewById(R.id.GPS);

        GPS.setOnClickListener(v -> {
            if(clicked2==0) {
                GPS.setText("Mérés folyamatban...");
                startLocationUpdates();
                lat.clear();
                lon.clear();
                dec.clear();
                points.clear();
                y = 0;
                text666.setText(String.valueOf(y));
                clicked2++;
            }
            else
            {
                GPS.setText("GPS mérés");
                fusedLocationClient.removeLocationUpdates(locationCallback);
                clicked2 = 0;
                if(lat.size()>0) {
                    Logger(lat, lon, dec);
                    generateKml(lat, lon, dec);
                    generateWeight(lat, lon, classification);
                    coords(lat,lon);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("COORDINATES",points);
                    bundle.putSerializable("Coord",coords);
                    bundle.putSerializable("dec",dec);
                    MapsFragment mapsFragment = new MapsFragment();
                    mapsFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,mapsFragment,"MAP").addToBackStack("MAP").commit();
                }
                else
                {
                    Toast.makeText(this,"Ües a lista",Toast.LENGTH_SHORT).show();
                }
            }
        });

        poly.add(new LatLng(46.365919,17.783944));
        poly.add(new LatLng(46.365948,17.784091));
        poly.add(new LatLng(46.365836,17.783991));
        poly.add(new LatLng(46.365860,17.784143));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        request = new LocationRequest()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException)
                {
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                        resolvableApiException.startResolutionForResult(MainActivity.this,1);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                if (accuratePosition.isChecked()) {
                    for (Location location : locationResult.getLocations()) {
                        if (PolyUtil.containsLocation(location.getLatitude(), location.getLongitude(), poly, false)) {
                            try {
                                y++;
                                lat.add(location.getLatitude());
                                lon.add(location.getLongitude());
                                Log.d("LOCATIONS", "onLocationResult: ");
                                CheckdBm();
                                text666.setText(String.valueOf(y));
                            } catch (Exception e) {
                                Log.d("LOCATIONS", String.valueOf(e));
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                {
                    for (Location location : locationResult.getLocations()) {
                        y++;
                            try {
                                lat.add(location.getLatitude());
                                lon.add(location.getLongitude());
                                Log.d("LOCATIONS", "onLocationResult: ");
                                CheckdBm();
                                text666.setText(String.valueOf(y));
                            } catch (Exception e) {
                                Log.d("LOCATIONS", String.valueOf(e));
                                e.printStackTrace();
                            }
                        }
                    }
                }
        };

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        successfulConnection = findViewById(R.id.textView2);
        failedConnection =  findViewById(R.id.textView4);
        wifiStrength = findViewById(R.id.textView26);
        TextView wifiStrengthClassification =  findViewById(R.id.textView27);
        pingTime = findViewById(R.id.textView28);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiStrengthClassification.setText("5-Excellent >-50 dBm\n" +
                "\n" +
                "4-Good -50 to -60 dBm\n" +
                "\n" +
                "3-Fair -60 to -70 dBm\n" +
                "\n" +
                "2-Weak < -70 dBm\n" + "\n");


        Spinner server = (Spinner) findViewById(R.id.spinner1);
        Spinner packages = (Spinner) findViewById(R.id.spinner2);
        Spinner interval = (Spinner) findViewById(R.id.spinner3);
        Button startBtn = (Button) findViewById(R.id.startBtn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.servers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        server.setAdapter(adapter);

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.packages, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packages.setAdapter(adapter1);

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this, R.array.seconds, android.R.layout.simple_spinner_item);
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
                    successRow = 0;
                    failedConnection.setText("");
                    failedRow = 0;
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
                            CheckWifi();
                            getMac();
                            executeCmd(command, false);

                            handler.postDelayed(this, interval_ms);
                        }
                    };

                    handler.postDelayed(r, 2000);
                    startBtn.setText("STOP");
                    clicked++;
                    // longRunningTaskFuture = executorService.submit(r);

                } else if (clicked > 0) {

                    Toast.makeText(getApplicationContext(), "futok", Toast.LENGTH_SHORT).show();
                    clicked = 0;
                    startBtn.setText("START");
                    //longRunningTaskFuture.cancel(true);
                    handler.removeCallbacks(r);
                    atlag ();
                }

            }
        });


    }

    private boolean CheckConnection() {
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String URL = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=5";
            Connection connection = DriverManager.getConnection(URL);
            if (connection != null) {
                Log.d("DONKEY", "Csatlakozva");
                successRow++;
                successfulConnection.setText(String.valueOf(successRow));
                return true;
            } else {
                Log.d("DONKEY", "Nincs csatlakozva");
                failedRow++;
                failedConnection.setText(String.valueOf(failedRow));
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DONKEY", "Nincs csatlakozva");
            failedRow++;
            failedConnection.setText(String.valueOf(failedRow));
            return false;

        }
    }

    private void CheckWifi() {
        int number = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        scan = wifiManager.getScanResults();
        //int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), number);
        decibel = wifiInfo.getRssi();
        wifiStrength.setText("A Wifi erőssége = " + decibel + "dBm" + "\n");

    }
    private void CheckdBm()
    {
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            scan = wifiManager.getScanResults();
            if(wifiInfo.getRssi()>-50)
            {
                classification.add(5);
            }
            else if(wifiInfo.getRssi()<-50 && wifiInfo.getRssi()>-60)
            {
                classification.add(4);
            }
            else if(wifiInfo.getRssi()<-60 && wifiInfo.getRssi()>-70)
            {
                classification.add(3);
            }
            else if(wifiInfo.getRssi()<-70 && wifiInfo.getRssi()>-80)
            {
                classification.add(2);
            }
            else
            {
                classification.add(1);
            }
            dec.add(String.valueOf(wifiInfo.getRssi()));
        }catch(Exception e)
        {
            dec.add("0");
        }
    }

    public String getMac() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission Not Granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            WifiManager wifimanage = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiinfo = wifimanage.getConnectionInfo();
            macAddress = wifiinfo.getBSSID();//Get the mac address of the currently connected network;
            wifiStrength.append("\t" + macAddress + "\t");
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
                    Log.d("MALAC", macAddress);
                } else {
                    Log.d("MALAC", "PERMISSION DENIED");
                }
        }
    }

    public String executeCmd(String cmd, boolean sudo) {
        if (wifiManager.isWifiEnabled()) {
            Log.d("PING", "WIFI");
            try {
                Log.d("PING", "TRY");
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
                if (p.exitValue() == 0) {
                    Log.d("PING", "IF");
                    try {
                        int a = res.indexOf("received");
                        time = res.substring(a - 2, a + 8);

                    } catch (Exception e) {
                        time = "0ms";
                    }
                    try {
                        int b = res.indexOf("time=");
                        time2 = res.substring(b, b + 12);
                        Log.d("FUTYUL", time2);
                    } catch (Exception asd) {
                        time2 = "0ms";
                    }
                    pingTime.setText(time2);
                    successRow++;
                    successfulConnection.setText(String.valueOf(successRow));
                    date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Logger(time + ";" + macAddress + ";" + String.valueOf(format.format(date) + ";" + time2 + ";" + decibel + "\n"));
                    p.destroy();
                    return res;
                } else {
                    try {
                        int a = res.indexOf("received");
                        time = res.substring(a - 2, a + 8);
                        //Log.d("FUTYUL", time);
                    } catch (Exception ex) {
                        time = "0ms";
                    }
                    try {
                        int b = res.indexOf("time=");
                        time2 = res.substring(b, b + 12);
                        //Log.d("IDO", time2);
                    } catch (Exception asd) {
                        time2 = "0ms";
                    }
                    Log.d("PING", "ELSE");
                    date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Logger(time + ";" + macAddress + ";" + String.valueOf(format.format(date) + ";" + time2 + ";" + decibel + ";" + p.exitValue() + "\n"));
                    p.destroy();
                    failedRow++;
                    failedConnection.setText(String.valueOf(failedRow));
                    pingTime.setText(pingTime + " _Nem 0 process kód");
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
                    time2 = res.substring(b, b + 12);
                    Log.d("IDO", time2);
                } catch (Exception asd) {
                    time2 = "0ms";
                }
                Log.d("PING", "CATCH");
                date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Logger(time + ";" + macAddress + ";" + String.valueOf(format.format(date) + ";" + time2 + ";" + decibel + ";" + "catch" + "\n"));
                e.printStackTrace();
                failedRow++;
                failedConnection.setText(String.valueOf(failedRow));
                pingTime.setText("Sikertelen fogadás");
                return "catch";
            }
        } else {
            Log.d("PING", "NINCS_WIFI");
            failedRow++;
            failedConnection.setText(String.valueOf(failedRow));
            date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pingTime.setText("Nincs Wifi");
            Logger("NEM VOLT WIFI;" + "null;" + String.valueOf(format.format(date) + ";" + ";" + "\n"));
            return "catch";
        }

    }

    public void Logger(String data) {
        File path = this.getExternalFilesDir(null);
        File file = new File(path, "LoggerDogger.txt");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                FileOutputStream stream = new FileOutputStream(file, true);
                stream.write(data.getBytes());
                stream.close();
                Log.d("IRAS", "Becsukta");
            } catch (Exception e) {
                Toast.makeText(this, "ROSSZ", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private void Logger(ArrayList<Double> lat, ArrayList<Double> lon, ArrayList<String> decibel)
    {
        File path = this.getExternalFilesDir(null);
        File file = new File(path, "GPSlog.txt");
        String data = "";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                FileOutputStream stream = new FileOutputStream(file, true);
                for (int i = 0; i<lat.size(); i++)
                {
                    data = String.valueOf(i)+","+lat.get(i)+","+lon.get(i)+","+decibel.get(i)+"\n";
                    stream.write(data.getBytes());
                }
                stream.close();
                Log.d("IRAS", "Becsukta");
            } catch (Exception e) {
                Toast.makeText(this, "ROSSZ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateWeight(ArrayList<Double> lat, ArrayList<Double> lon, ArrayList<Integer> decibel)
    {
        WeightedLatLng weightedLatLng;
        for (int i = 0; i< lat.size(); i++)
        {
            weightedLatLng = new WeightedLatLng(new LatLng(lat.get(i),lon.get(i)),Double.valueOf(decibel.get(i)));
            points.add(weightedLatLng);
        }
    }

    private void coords(ArrayList<Double> lat, ArrayList<Double> lon)
    {
        LatLng latLng;
        for (int i = 0; i< lat.size(); i++)
        {
            latLng = new LatLng(lat.get(i),lon.get(i));
            coords.add(latLng);
        }
    }


    private void generateKml(ArrayList<Double> lat, ArrayList<Double> lon, ArrayList<String> decibel)
    {
        File path = this.getExternalFilesDir(null);
        File file = new File(path, "GPSlog.KML");
        String kml = ""+
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"+
                "<Document id = \"root_doc\">\n"+
                "<Schema name = \"fusetech\" id=\"fusetech\">\n"+
                     "<SimpleField name = \"id\" type=\"int\"></SimpleField>\n"+
                     "<SimpleField name = \"lat\" type=\"float\"></SimpleField>\n"+
                     "<SimpleField name = \"lon\" type=\"float\"></SimpleField>\n"+
                     "<SimpleField name = \"dBm\" type=\"int\"></SimpleField>\n"+
                "</Schema>\n"+
                "<Folder><name>fusetech</name>\n";
        for (int i = 0; i<lat.size();i++)
        {
            kml +="<Placemark>\n"+
                    "<ExtendedData><SchemaData schemaUrl=\"#fusetech\">\n"+
                        "<SimpleData name = \"id\">"+i+"</SimpleData>\n"+
                        "<SimpleData name = \"lat\">"+lat.get(i)+"</SimpleData>\n"+
                        "<SimpleData name = \"lon\">"+lon.get(i)+"</SimpleData>\n"+
                        "<SimpleData name = \"dBm\">"+decibel.get(i)+"</SimpleData>\n"+
                    "</SchemaData></ExtendedData>\n"+
                    "<Point><coordinates>"+lon.get(i)+","+lat.get(i)+","+","+0+"</coordinates></Point>\n"+
                    "</Placemark>";

        /*""+
                    "<Placemark>"+
                    "<name>"+i+"</name>"+
                    "<description>"+decibel.get(i)+"</description>"+
                    "<Point>"+
                    "<coordinates>"+lat.get(i)+","+lon.get(i)+","+0+"</coordinates>"+
                    " </Point>"+
                     "</Placemark>";*/
        }
        kml += "</Folder>\n"+
                "</Document></kml>";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                FileOutputStream stream = new FileOutputStream(file, true);
                stream.write(kml.getBytes());
                stream.close();
                Log.d("IRAS", "Becsukta");
            } catch (Exception e) {
                Toast.makeText(this, "ROSSZ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startLocationUpdates()
    {
        if(fusedLocationClient != null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Permission Not Granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void atlag ()
    {


        Double a = 0.0, atlag = 0.0;
        Double b = 0.0, atlag2 = 0.0;
        for(int i = 0; i<lat.size(); i++)
        {
          a += lat.get(i);
        }
        for(int i = 0; i<lon.size();i++)
        {
            b += lon.get(i);
        }
        atlag = a/lat.size();
        atlag2 = b/lon.size();

        Collections.sort(lat);
        double median, median2;
        if (lat.size() % 2 == 0)
            median = (lat.get(lat.size()/2) + lat.get(lat.size()/2 - 1))/2;
        else
            median = lat.get(lat.size()/2);

        if (lon.size() % 2 == 0)
            median2 = (lon.get(lon.size()/2) + lon.get(lon.size()/2 - 1))/2;
        else
            median2 = lon.get(lon.size()/2);
        Log.d("ATLAG", String.valueOf(atlag)+"\t"+String.valueOf(atlag2));
        Log.d("ATLAG", String.valueOf(median)+"\t"+String.valueOf(median2));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

}