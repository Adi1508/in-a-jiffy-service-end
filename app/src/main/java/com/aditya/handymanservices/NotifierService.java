package com.aditya.handymanservices;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class NotifierService extends Service {



    private RequestQueue rQueue;
    Context ctx;
    List<Object> list;
    Gson gson;
    SharedPreferences sharedPrefrences;
    SharedPreferences sharedPreferences;
    int flag;
    String services_req;
    String location_details;
    String postTitle, postContent, value;
    JSONArray jsonArray;
    String userlat, userlong;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NotifierService(Context context) {
        ctx = context;
        rQueue = getRequestQueue();
    }

    public NotifierService() {


    }

    public RequestQueue getRequestQueue() {
        if (rQueue == null) {
            rQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return rQueue;
    }


    //to restart the service when the application is removed from the recents
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime() + 5000,
                restartPendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    private boolean alreadyNotified(String key) {
        //Log.d("Method called","method called");
        sharedPrefrences = getApplicationContext().getSharedPreferences("Notifications", 0);
        if (sharedPrefrences.getBoolean(key, false)) {
            return true;
        } else {
            return false;
        }
    }

    private void saveNotificationKey(String key) {
        //Log.d("Method called","method called");
        sharedPrefrences = getApplicationContext().getSharedPreferences("Notifications", 0);
        SharedPreferences.Editor editor = sharedPrefrences.edit();
        editor.putBoolean(key, true);
        editor.commit();
    }

    private void setupNotificationListener() {
        String url = "https://handymanv1.herokuapp.com/api/fetchData";
        System.out.println("URL BEFORE: "+url);
        jsonArray = new JSONArray();
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String s) {

                System.out.println(s);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                    System.out.println("JSONObjecet: "+jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {

                    jsonArray = (JSONArray)jsonObject.get("response");
                    System.out.println("Json array length: "+jsonArray.length());
                    for(int i=0;i<jsonArray.length(); i++){
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        if(jsonObject1.get("services").equals(services_req)){

                            String[] arr1 = location_details.split(" ");
                            userlat = jsonObject1.get("lat").toString();
                            userlong = jsonObject1.get("lng").toString();
                            Double dist = distance(Double.parseDouble(arr1[0]),Double.parseDouble(arr1[1]), Double.parseDouble(userlat), Double.parseDouble(userlong));
                            postTitle = jsonObject1.get("number").toString();
                            System.out.println(dist<=5);
                            if(dist<=5){
                                if (!alreadyNotified(postTitle)) {
                                    Log.d("not notified", "");
                                    createNotif("New Notificaton", "Hello ");
                                    saveNotificationKey(postTitle);
                                } else {
                                    System.out.print("already notified");
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("Error", "Error");
            }
        });

        getRequestQueue().add(request);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.print("onstartcommand");
        location_details = intent.getStringExtra("location_details");
        services_req = intent.getStringExtra("services_req");
        System.out.println("location in intent: "+location_details);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent service = new Intent(this, NotifierService.class);
        startService(service);
        System.out.println("DESTROY");
    }

    private void createNotif(String value, String title) {

        Spanned sp = Html.fromHtml(value);
        Log.d("Sapnned", sp.toString());

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.inajiffy)
                .setContentTitle(title.toString())
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(sp.toString());
        Intent notificationintent = null;
        notificationintent = new Intent(this, Accept.class);
        notificationintent.putExtra("worker_location",location_details);
        notificationintent.putExtra("destination_details", userlat+" "+userlong);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationintent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setAutoCancel(true);
        manager.notify(1, builder.build());
    }

    CountDownTimer cdt = new CountDownTimer(15000, 15000) {
        @Override
        public void onTick(long millisUntilFinished) {
            createNotification();
        }

        @Override
        public void onFinish() {
            cdt.start();
        }
    }.start();

    private void createNotification() {

        setupNotificationListener();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        System.out.println("Distance: "+dist);
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
