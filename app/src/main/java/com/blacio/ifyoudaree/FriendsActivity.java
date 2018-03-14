package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import static android.os.SystemClock.elapsedRealtimeNanos;


public class FriendsActivity extends MenuActivity {

    private  ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 1;


        ProgressBar prgBar = (ProgressBar) findViewById(R.id.prgBar);

        String[] string = new String[]{"No information !"};

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        final RecyclerView.Adapter adapter;

        recyclerView = (RecyclerView) findViewById(R.id.friendsList);


        if (SetDataClass.pictures != null) {
            adapter = new CustomAdapter(SetDataClass.names.toArray(new String[SetDataClass.names.size()]), SetDataClass.pictures.toArray(new Bitmap[SetDataClass.pictures.size()]), this);
            recyclerView.setAdapter(adapter);
        } else adapter = new CustomAdapter(string,null, this);


        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        prgBar.setVisibility(View.GONE);
    }

    public void createDialog(final int k) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

        ProgressBar pg = (ProgressBar) dialog.findViewById(R.id.pg_bar);
        pg.setVisibility(View.VISIBLE);

        LinearLayout linearLayout = (LinearLayout)dialog.findViewById(R.id.linearDialog);
        TextView nm = (TextView) dialog.findViewById(R.id.nameDialog);
        TextView sent = (TextView) dialog.findViewById(R.id.sentDialog);
        TextView received = (TextView) dialog.findViewById(R.id.receivedDialog);
        final TextView scor = (TextView) dialog.findViewById(R.id.levelDialog);
        ImageView pic = (ImageView) dialog.findViewById(R.id.picDialog);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("top");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String scr;

                if (dataSnapshot.hasChild(SetDataClass.ids.get(k)))
                    scr = dataSnapshot.child(SetDataClass.ids.get(k)).child("score").getValue(String.class);
                else scr = "0";

                scor.setText("Score:\n" + scr);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        linearLayout.setBackgroundColor(Color.parseColor(SetDataClass.color_dial));
        nm.setText(SetDataClass.names.get(k));
        pic.setImageBitmap(SetDataClass.pictures.get(k));
        sent.setText("You sent:\n"+ calculateSent(k));
        received.setText("You received:\n"+ calculateReceived(k));

        pic.setMinimumWidth(300);
        pic.setMinimumHeight(600);

        Button dialogButton = (Button) dialog.findViewById(R.id.dareButDialog);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                    createDareDialog(k);
            }
        });

        pg.setVisibility(View.GONE);

        dialog.show();
    }

    private void createDareDialog(final int k) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_dare);

        final ProgressBar prog = (ProgressBar) dialog.findViewById(R.id.progras_bar_dialog);
        prog.setVisibility(View.GONE);
        final EditText inputDare = (EditText) dialog.findViewById(R.id.inputDare);

        Button sendDare = (Button) dialog.findViewById(R.id.butonDare);
        LinearLayout linearLayout = (LinearLayout)dialog.findViewById(R.id.linearDialog);
        linearLayout.setBackgroundColor(Color.parseColor(SetDataClass.color_dial));

        sendDare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prog.setVisibility(View.VISIBLE);
                final boolean[] go = {false};

                final String dare = inputDare.getText().toString();

                String time = String.valueOf(elapsedRealtimeNanos());
                String idF = SetDataClass.ids.get(k);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference();


                ref.child(idF).child("blocked").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(SetDataClass.myId)) go[0] = true;

                        else {
                            prog.setVisibility(View.GONE);
                            Toast.makeText(FriendsActivity.this, "You are blocked!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                if (go[0]) {
                    DatabaseReference myRef1 = ref.child(SetDataClass.myId).child("sent").child(time);
                    final DatabaseReference myRef2 = ref.child(idF).child("received").child(time);

                    Map<String, Object> childrensTogether0 = new HashMap<>();

                    childrensTogether0.put("token", FirebaseInstanceId.getInstance().getToken());
                    childrensTogether0.put("name", SetDataClass.names.get(k));
                    childrensTogether0.put("dare", dare);
                    childrensTogether0.put("id", idF);
                    childrensTogether0.put("status", "SENT");

                    myRef1.updateChildren(childrensTogether0);


                    ref.child(idF).child("received").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s = dataSnapshot.child("token").getValue(String.class);

                            Map<String, Object> childrensTogether = new HashMap<>();
                            childrensTogether.put("token", s);
                            childrensTogether.put("name", SetDataClass.myName);
                            childrensTogether.put("dare", dare);
                            childrensTogether.put("id", SetDataClass.myId);
                            childrensTogether.put("status", "NEW");

                            myRef2.updateChildren(childrensTogether);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    //SetDataClass.dares2.add(dare);
                    //SetDataClass.ids2.add(idF);
                    //SetDataClass.status2.add("SENT");

                    prog.setVisibility(View.GONE);
                    dialog.dismiss();

                    Toast.makeText(FriendsActivity.this, "Challenge sent!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

        private int calculateSent(int k){

            int x=0;

            for(int i =0;i< SetDataClass.ids2.size();i++)
                if(SetDataClass.ids2.get(i).equals(SetDataClass.ids.get(k)))
                    x++;

            return x;
        }

        private int calculateReceived(int k){

            int x=0;

            for(int i =0;i< SetDataClass.ids3.size();i++)
                if(SetDataClass.ids3.get(i).equals(SetDataClass.ids.get(k)))
                    x++;

            return x;
        }


    @Override
    public void onBackPressed() {

    }

    public void createBlockDialog(final int position) {

        final String[] s = new String[1];
        final boolean[] block = new boolean[1];

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference().child(SetDataClass.myId).child("blocked");
        final DatabaseReference reff = database.getReference().child("blockedUsers");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(SetDataClass.ids.get(position))) {
                    s[0] = SetDataClass.names.get(position) + " is now blocked. Do you want to UNBLOCK " + SetDataClass.names.get(position) + "?";
                    block[0] = true;
                }else {
                    s[0] = "Are you sure you want to block " + SetDataClass.names.get(position);
                    block[0] = false;
                }



                final AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                alertDialog.setTitle("Block user");
                alertDialog.setMessage(s[0]);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if(block[0]) {
                                    ref.child(SetDataClass.ids.get(position)).removeValue();
                                    reff.child(SetDataClass.ids.get(position)).child(SetDataClass.myId).removeValue();
                                }
                                else {
                                    ref.child(SetDataClass.ids.get(position)).setValue("1");
                                    reff.child(SetDataClass.ids.get(position)).child(SetDataClass.myId).setValue("1");
                                }

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
