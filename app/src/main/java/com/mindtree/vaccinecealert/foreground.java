package com.mindtree.vaccinecealert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class foreground extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    boolean alertBool=false;
    Timer timer;
    public foreground() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        serviceNotify(intent,"0");
        timer=new Timer();
        final int[] i = {1};
        TimerTask task=new TimerTask(){

            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
                String strDate = "Current Time : " + mdformat.format(calendar.getTime());
                Log.d("call:", i[0]+" "+strDate);
                getData(intent,i[0]);
                i[0]++;

            }
        };
        timer.schedule(task,01,5000);


        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {

        super.onDestroy();
        timer.cancel();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannelService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Un-available notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private  void serviceNotify(Intent intent,String msg)
    {
        Log.d("counter", msg);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedfile", MODE_PRIVATE);
        String pin=sharedPreferences.getString("pincode","service not working");

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannelService();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Un-available at "+pin)
                .setContentText("counter: "+msg)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setNotificationSilent()
                .build();
        startForeground(1, notification);
    }
    private void createNotificationChannelAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Available notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void alertNotify(Intent intent,String alertText){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedfile", MODE_PRIVATE);
        String pin=sharedPreferences.getString("pincode","service not working");

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannelAvailable();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Available at "+pin)
                .setContentText(alertText)
                .setSmallIcon(R.drawable.ic_avail)
                .setContentIntent(pendingIntent)
                .setColor(Color.parseColor("#00ff00"))
                .setNotificationSilent()
                .build();
        startForeground(1, notification);
    }
    private void getData(Intent intent,int i){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        SharedPreferences sharedPreferences = getSharedPreferences("sharedfile", MODE_PRIVATE);
        String pin=sharedPreferences.getString("pincode","");
//        Log.d("check2", pin);
//        String pin="333704";
        String datestr=formatter.format(date);
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        Log.d("start:","begins");
        String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode="+pin+"&date="+datestr;
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
//                Log.d("response:",response.toString());
                try {
                    JSONArray centerList = response.getJSONArray("centers");
                    int flag=0;
                    for(int i=0;i<centerList.length();i++)
                    {
                        JSONObject centerObj=centerList.getJSONObject(i);
                        JSONObject sessions=(centerObj.getJSONArray("sessions")).getJSONObject(0);
                        String dose1=sessions.getString("available_capacity_dose1");
                        String dose2=sessions.getString("available_capacity_dose2");
                        if(Integer.parseInt(dose1)>0||Integer.parseInt(dose2)>0){
                            String centerName = centerObj.getString("name");
                            String pincode = centerObj.getString("pincode");


                            String ageLimit=sessions.getString("min_age_limit");

                            String date=sessions.getString("date");
                            Log.d("hospital name:",centerName);
                            Log.d("pincode:",pincode);
                            Log.d("age limit:",ageLimit);
                            Log.d("dose 1:",dose1);
                            Log.d("dose 2:",dose2);
                            Log.d("date:",date);
                            alertBool=true;
                            flag=1;
                            alertNotify(intent,centerName+" |age:"+ageLimit+" |dose1:"+dose1+" |dose2:"+dose2);
                        }
                    }
                    if(!alertBool)
                    {
                        serviceNotify(intent,i+"");
                        Log.d("xyz:","api end");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                        Log.d("error:","error");
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }
}