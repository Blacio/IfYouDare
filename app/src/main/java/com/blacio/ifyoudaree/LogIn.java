package com.blacio.ifyoudaree;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity {

    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private Button butCont,butDel;
    private ProgressBar progressDel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Bundle extras = getIntent().getExtras();
        if(extras!=null && extras.getBoolean("Rst"))
            restartActivity();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        FirebaseApp.initializeApp(this);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {

        }
        catch (NoSuchAlgorithmException e) {

        }

        progressDel = (ProgressBar)findViewById(R.id.progressBarDel);
        progressDel.setVisibility(View.GONE);

            mAuth = FirebaseAuth.getInstance();

            callbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setReadPermissions(Arrays.asList(
                    "public_profile", "user_birthday", "user_location", "user_friends"));

            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResult) {

                    progressDel.setVisibility(View.VISIBLE);
                    takeDetails(loginResult.getAccessToken());
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(LogIn.this, "Authentication cancelled.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException e) {
                    Toast.makeText(LogIn.this, "Authentication error. Check out your internet connection.",
                            Toast.LENGTH_SHORT).show();
                }
            });


        butCont = (Button)findViewById(R.id.butonContinue);
        butDel = (Button)findViewById(R.id.butonDelete);


        if (Profile.getCurrentProfile() == null || AccessToken.getCurrentAccessToken() == null) {
            butCont.setVisibility(View.GONE);
            butDel.setVisibility(View.GONE);
        }


        }

    private void takeDetails(AccessToken token) {

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
                        editor.putString("FIRST", response.getJSONObject().toString());
                        editor.apply();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "birthday,location,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    private void handleFacebookAccessToken(final AccessToken token) {


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref = database.getReference();
                            ref.child(Profile.getCurrentProfile().getId()).child("received").child("token").setValue(FirebaseInstanceId.getInstance().getToken());


                            DatabaseReference topRef = ref.child("top").child(Profile.getCurrentProfile().getId());

                            Map<String, Object> childrensTogether = new HashMap<>();
                            childrensTogether.put("/bonus/"+1, "0");
                            childrensTogether.put("name", Profile.getCurrentProfile().getName());
                            childrensTogether.put("score", "0");

                            topRef.updateChildren(childrensTogether);

                            Toast.makeText(LogIn.this, "Authentication succes.",
                                    Toast.LENGTH_SHORT).show();

                            buildNotifications(Profile.getCurrentProfile().getFirstName());

                            progressDel.setVisibility(View.GONE);
                            butCont.setVisibility(View.VISIBLE);
                            butDel.setVisibility(View.VISIBLE);

                        } else {
                            Toast.makeText(LogIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                      }
                    }
                });
    }

    private void updateUI(){
            Intent i = new Intent(this,UserData.class);
            i.putExtra("LOG",true);
            startActivity(i);
    }


    public void continueButton(View view) {


        if (checkConnection()){
            if (Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null) {
                checkIfBlocked();
            }
        }
        else showDialogConnection();

    }

    private boolean checkConnection(){
        ConnectivityManager cm =(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private void showDialogConnection(){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_menu);
            builder.setCancelable(false);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
    }

    public void deleteButton(View view){

        if(checkConnection()) {
            if (Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference ref = database.getReference().child(Profile.getCurrentProfile().getId());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.sureDelete);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        progressDel.setVisibility(View.VISIBLE);
                        dialog.dismiss();

                        ref.removeValue();

                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();

                        progressDel.setVisibility(View.GONE);

                        Toast.makeText(LogIn.this, "Account deleted",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        progressDel.setVisibility(View.GONE);
                    }
                });
                builder.create().show();
            }
        }else showDialogConnection();
    }

    private void restartActivity(){
        Intent mStartActivity = new Intent(this, LogIn.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }


    private void buildNotifications(String s1){

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Welcome, "+ s1);
        notification.setContentText("Get ready for challenges !");
        notification.setSound(alarmSound);


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(567233, notification.build());
    }

    private void createAgreeDialog(final boolean b){
        AlertDialog alertDialog = new AlertDialog.Builder(LogIn.this).create();
        alertDialog.setTitle("Restricted content");
        alertDialog.setMessage(getString(R.string.do_not));
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "I AGREE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (b) {

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref = database.getReference();
                            ref.child("policyAgree").child(Profile.getCurrentProfile().getId()).setValue("1");

                            takeDetails(AccessToken.getCurrentAccessToken());
                            updateUI();
                        } else {
                            Toast.makeText(LogIn.this, "Your account is blocked!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        alertDialog.show();
    }

    private void checkIfBlocked(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("policyAgree").hasChild(Profile.getCurrentProfile().getId())) {
                    if (!dataSnapshot.child("blockedUsers").hasChild(Profile.getCurrentProfile().getId())) {
                        takeDetails(AccessToken.getCurrentAccessToken());
                        updateUI();}
                     else if  (dataSnapshot.child("blockedUsers").hasChild(Profile.getCurrentProfile().getId())){
                        if(dataSnapshot.child("blockedUsers").child(Profile.getCurrentProfile().getId()).getChildrenCount()<=10){
                        takeDetails(AccessToken.getCurrentAccessToken());
                        updateUI();
                        }
                        else {

                            Toast.makeText(LogIn.this, "There are too many people who have blocked you! Your account has been blocked!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }else createAgreeDialog(!dataSnapshot.child("blockedUsers").hasChild(Profile.getCurrentProfile().getId()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

