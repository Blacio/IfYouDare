package com.blacio.ifyoudaree;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class UserData extends MenuActivity {

    private String birthday;
    private String hometown;
    private ImageView imgV;
    private TextView birthdayV,hometownV,nameV,sentV,receivedV,scoreV,levelV;
    private String strJson;
    private ProgressBar pg;
    private boolean b;
    private ActionBar actionBar;
    private String col1,col2;
    private boolean congrats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        actionBar = getSupportActionBar();

        super.setIntent(this);
        MenuActivity.sw = 0;

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        strJson = prefs.getString("FIRST","");
        col1 = prefs.getString("myColor1"," ");
        col2 = prefs.getString("myColor2"," ");
        congrats = prefs.getBoolean("congrats",true);

        b = false;
        Bundle extras = getIntent().getExtras();

        if(extras!=null && extras.getBoolean("LOG")) b=true;


        imgV = (ImageView) findViewById(R.id.photoUser);
        nameV = (TextView) findViewById(R.id.nameUser);
        hometownV = (TextView) findViewById(R.id.cityUser);
        birthdayV = (TextView) findViewById(R.id.ageUser);
        pg = (ProgressBar) findViewById(R.id.progress_bar_user);
        sentV = (TextView)findViewById(R.id.sentUser);
        receivedV = (TextView)findViewById(R.id.receivedUser);
        levelV = (TextView)findViewById(R.id.levelUser);
        scoreV = (TextView)findViewById(R.id.scoreUser);

        pg.setVisibility(View.VISIBLE);


        Intent i = new Intent(this,MyFirebaseMessegingService.class);
        startService(i);
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");

        new getFacebookProfilePicture().execute();

    }


    private class getFacebookProfilePicture extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonData = getJson(strJson);
            getProfileData(jsonData);

            if(b) {

                new SetDataClass();

                JSONArray jsonArray = getJsonFriends(jsonData);

                if (jsonArray != null) {
                    setFriendsIds(jsonArray);
                }
                setDaresDatabase();

            }


            if(b) {
                try {

                    URL imageURL;
                    imageURL = new URL("https://graph.facebook.com/" + SetDataClass.myId + "/picture?type=large");
                    SetDataClass.bitmap = null;
                    SetDataClass.bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());


                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return "1";
        }

        @Override
        protected void onPostExecute(String result) {

            nameV.setText(SetDataClass.myNameUser);
            imgV.setImageBitmap(SetDataClass.bitmap);
            birthdayV.setText(birthday);
            hometownV.setText(hometown);


            SetDataClass.myScore = calculateScore();
            levelV.setText("Level: "+ (Long.parseLong(SetDataClass.myScore)/100+1));
            if(b)  {
                if(!col1.equals(" ") && !col2.equals(" ")) {
                    SetDataClass.color_rows=col1;
                    SetDataClass.color_dial=col2;
                }
                else setColors((int)(Long.parseLong(SetDataClass.myScore)/100+1));
            }
            scoreV.setText("Score: " + SetDataClass.myScore);
            sentV.setText("Dares Sent: " + SetDataClass.dares2.size());
            receivedV.setText("Dares Received: " + SetDataClass.dares3.size());

            pg.setVisibility(View.GONE);
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}

            if((Long.parseLong(SetDataClass.myScore)/100+1)%10==0){
                if(congrats)
                    createCongratsDialog();
            }
            else congrats = false;

        }
    }


    private JSONObject getJson(String strJson) {

        JSONObject jsonData = null;

        if (strJson != null) {

            try {
                jsonData = new JSONObject(strJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonData;
    }


    private JSONArray getJsonFriends(JSONObject jsonData) {

        JSONArray jsonArray = null;

        if (jsonData!=null) {
            try {
                jsonArray = jsonData.getJSONObject("friends").getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }


    private void setFriendsIds(JSONArray jsonArray) {

        URL imageURL;

        try {

            if (jsonArray != null) {
                for (int k = 0; k < jsonArray.length(); k++) {
                    JSONObject job = jsonArray.getJSONObject(k);
                    SetDataClass.ids.add(job.getString("id"));
                    SetDataClass.names.add(job.getString("name"));

                    imageURL = new URL("https://graph.facebook.com/" + job.getString("id") + "/picture?type=large");
                    SetDataClass.pictures.add(BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()));
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }


    private void getProfileData(JSONObject jsonData){
        try {
            hometown = jsonData.getJSONObject("location").getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            birthday = jsonData.getString("birthday");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDaresDatabase(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();


        final DatabaseReference myRef1 = myRef.child(SetDataClass.myId).child("sent");

        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SetDataClass.dares2.clear();
                SetDataClass.ids2.clear();
                SetDataClass.status2.clear();
                SetDataClass.times2.clear();
                SetDataClass.pictures2.clear();
                SetDataClass.names2.clear();

                if(dataSnapshot.hasChildren()){
                    for (DataSnapshot node : dataSnapshot.getChildren()) {

                        if(node.child("dare").getValue(String.class)!=null) {
                            SetDataClass.dares2.add(0,node.child("dare").getValue(String.class));
                            SetDataClass.times2.add(0,node.getKey());
                            sentV.setText("Dares Sent: " + SetDataClass.dares2.size());
                        }
                        if(node.child("id").getValue(String.class)!=null)
                            SetDataClass.ids2.add(0,node.child("id").getValue(String.class));
                        if(node.child("name").getValue(String.class)!=null)
                            SetDataClass.names2.add(0,node.child("name").getValue(String.class));
                        if(node.child("status").getValue(String.class)!=null) {
                            SetDataClass.status2.add(0, node.child("status").getValue(String.class));
                            SetDataClass.myScore = calculateScore();
                            levelV.setText("Level "+ Long.parseLong(SetDataClass.myScore)/100+1);
                            scoreV.setText("Score: " + SetDataClass.myScore);
                        }

                        for (int k = 0; k < SetDataClass.ids2.size(); k++) {
                            for (int q = 0; q < SetDataClass.ids.size(); q++) {
                                if (SetDataClass.ids2.get(k).equals(SetDataClass.ids.get(q)))
                                    try {
                                        SetDataClass.pictures2.add(k, SetDataClass.pictures.get(q));
                                    }catch (Exception e){e.printStackTrace();}
                            }
                        }
                    }

                    if(!SetDataClass.notiBool1) {
                        if (calculateNewSent() != 0)
                            buildNotifications("Sent Challenges", "You have " + calculateNewSent() + " challenges to CHECK", 54636);
                        SetDataClass.notiBool1 = true;
                    }
                    else if(SetDataClass.notiBool1)
                        buildNotifications("Sent Dares", "You have a new challenge to CHECK", 54637);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference myRef2 = myRef.child(SetDataClass.myId).child("received");

        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SetDataClass.dares3.clear();
                SetDataClass.ids3.clear();
                SetDataClass.status3.clear();
                SetDataClass.times3.clear();
                SetDataClass.pictures3.clear();
                SetDataClass.names3.clear();


                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot node : dataSnapshot.getChildren()) {

                        if(node.child("dare").getValue(String.class)!=null) {
                            SetDataClass.dares3.add(0,node.child("dare").getValue(String.class));
                            SetDataClass.times3.add(0,node.getKey());
                            receivedV.setText("Dares Received: " + SetDataClass.dares3.size());
                        }
                        if(node.child("id").getValue(String.class)!=null)
                            SetDataClass.ids3.add(0,node.child("id").getValue(String.class));
                        if(node.child("name").getValue(String.class)!=null)
                            SetDataClass.names3.add(0,node.child("name").getValue(String.class));
                        if(node.child("status").getValue(String.class)!=null) {
                            SetDataClass.status3.add(0, node.child("status").getValue(String.class));
                            SetDataClass.myScore = calculateScore();
                            levelV.setText("Level "+ Long.parseLong(SetDataClass.myScore)/100+1);
                            scoreV.setText("Score: " + SetDataClass.myScore);
                        }

                        for (int k = 0; k < SetDataClass.ids3.size(); k++) {
                            for (int q = 0; q < SetDataClass.ids.size(); q++) {
                                if (SetDataClass.ids3.get(k).equals(SetDataClass.ids.get(q)))
                                    try {
                                        SetDataClass.pictures3.add(k, SetDataClass.pictures.get(q));
                                    }catch (Exception e){e.printStackTrace();}
                            }
                        }

                    }

                    if(!SetDataClass.notiBool2) {
                        if (calculateNewReceived() != 0)
                            buildNotifications("Received Challenges", "You have " + calculateNewReceived() + " NEW challenges", 54635);
                        SetDataClass.notiBool2 = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String calculateScore(){

        long number = 0;

        number +=SetDataClass.dares2.size();

        for(int i=0; i<SetDataClass.status3.size();i++)
            if(SetDataClass.status3.get(i).equals("SOLVED"))
                number+=15;
            else if(SetDataClass.status3.get(i).equals("REJECTED"))
                number-=5;

        number = number + SetDataClass.bonus;

        return String.valueOf(number);
    }


    private void buildNotifications(String s1,String s2,int x){

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle(s1);
        notification.setContentText(s2);
        notification.setSound(alarmSound);


        PendingIntent pendingIntent;

        if(x==54635)
            pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,ReceivedActivity.class),0);
        else pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,SentActivity.class),0);
        notification.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(x, notification.build());
    }


    private long calculateNewSent(){

        long count=0;

        for(int i =0;i<SetDataClass.status2.size();i++)
            if(SetDataClass.status2.get(i).equals("CHECK"))
                count++;

        return count;
    }

    private long calculateNewReceived(){
        long count=0;

        for(int i =0;i<SetDataClass.status3.size();i++)
            if(SetDataClass.status3.get(i).equals("NEW"))
                count++;

        return count;
    }


    private void setColors(int sc){
        switch (sc/20){
            case 1: {
                SetDataClass.color_rows ="#979800";
                SetDataClass.color_dial ="#B3979800";
                break;
            }
            case 2: {
                SetDataClass.color_rows ="#3C722C";
                SetDataClass.color_dial ="#B33C722C";
                break;
            }
            case 3: {
                SetDataClass.color_rows ="#74137A";
                SetDataClass.color_dial ="#B374137A";
                break;
            }
            case 4: {
                SetDataClass.color_rows = "#D28000";
                SetDataClass.color_dial = "#B3D28000";
                break;
            }
            case 5: {
                SetDataClass.color_rows = "#114901";
                SetDataClass.color_dial = "#B3114901";
                break;
            }
            case 6: {
                SetDataClass.color_rows = "#C11717";
                SetDataClass.color_dial = "#B3C11717";
                break;
            }
            default:    break;

        }
    }

    private void createCongratsDialog(){


        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
        editor.putBoolean("congrats",false);
        editor.apply();

        AlertDialog alertDialog = new AlertDialog.Builder(UserData.this).create();
        alertDialog.setTitle("CONGRATULATIONS");
        alertDialog.setMessage("WOW !!! You experience has increased !!!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Thank you",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {

    }

}
