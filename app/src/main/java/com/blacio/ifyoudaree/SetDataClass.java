package com.blacio.ifyoudaree;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

class SetDataClass {

    static String myName,myId,myScore,myNameUser;
    static List<Integer> vec;
    static List<String> ids,ids2,ids3;
    static List<Bitmap> pictures,pictures2,pictures3;
    static List<String> names,dares2,dares3;
    static List<String> names2,names3;
    static List<String> status2,status3;
    static List<String> times2,times3;
    static android.graphics.Bitmap bitmap;
    static boolean notiBool1,notiBool2;
    static boolean sws,swr;
    static String color_rows;
    static String color_dial;
    static long bonus;


    SetDataClass(){
        ids = new ArrayList<>();
        pictures = new ArrayList<>();
        names = new ArrayList<>();
        color_rows = "#3F51B5";
        color_dial = "#B33F51B5";

        ids2 = new ArrayList<>();
        pictures2 = new ArrayList<>();
        status2 = new ArrayList<>();
        dares2 = new ArrayList<>();
        names2 = new ArrayList<>();

        ids3 = new ArrayList<>();
        pictures3 = new ArrayList<>();
        status3 = new ArrayList<>();
        dares3 = new ArrayList<>();
        names3 = new ArrayList<>();

        times2 = new ArrayList<>();
        times3 = new ArrayList<>();

        vec = new ArrayList<>();
        sws = false;
        swr = false;
        notiBool1 = false;
        notiBool2 = false;

        Profile profile = Profile.getCurrentProfile();
        myId = profile.getId();
        myName = profile.getName();

       // if(profile.getMiddleName().length()<2)
            myNameUser = profile.getFirstName() +"\n"+profile.getLastName();
        //else myNameUser = profile.getFirstName() +"\n"+profile.getMiddleName() +"\n"+profile.getLastName();

        bitmap=null;


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child("top").child(Profile.getCurrentProfile().getId()).child("bonus");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                bonus=0;

                if (dataSnapshot.hasChildren())
                    for (DataSnapshot node : dataSnapshot.getChildren()) {
                        bonus = bonus + Long.parseLong(node.getValue(String.class));
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    static void createProblemDialog(Context context){


        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Please, check your internet connection !");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    static boolean checkForProblem(Context context){
        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
}