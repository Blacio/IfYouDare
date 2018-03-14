package com.blacio.ifyoudaree;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VoteActivity extends MenuActivity {

    static List<String> idWorldTop;
    private Context context;
    static boolean sw;
    private ProgressBar prgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        context  =this;
        MenuActivity.sw = -1;
        sw=false;

        prgBar = (ProgressBar)findViewById(R.id.prgBar);

        idWorldTop = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("worldDare").child("ids");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot node : dataSnapshot.getChildren())
                        idWorldTop.add(0, node.getKey());

                }


                if (idWorldTop != null) {

                    RecyclerView recyclerView;
                    RecyclerView.LayoutManager layoutManager;
                    final RecyclerView.Adapter adapter;

                    recyclerView = (RecyclerView) findViewById(R.id.voteList);
                    adapter = new CustomAdapter(context,prgBar);

                    layoutManager = new LinearLayoutManager(context);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        prgBar.setVisibility(View.GONE);
    }

    private void writeToDb(int i){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refD = database.getReference().child("worldDare").child("ids");

        refD.child(idWorldTop.get(i)).child(SetDataClass.myId).setValue("1");

    }

    public void createDialog(final int position){


        AlertDialog alertDialog = new AlertDialog.Builder(VoteActivity.this).create();
        alertDialog.setMessage("Are you sure you want to vote this photo ?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        writeToDb(position);
                        dialog.dismiss();

                        Toast.makeText(VoteActivity.this, "Voted!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        alertDialog.show();
    }

}
