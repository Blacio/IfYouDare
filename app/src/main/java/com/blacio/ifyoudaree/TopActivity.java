package com.blacio.ifyoudaree;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;


public class TopActivity extends MenuActivity {

    static List<String> idTop;
    private List<String> namesTop;
    private List<String> scoresTop;
    private Context context;
    private TextView pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        ProgressBar prgBar = (ProgressBar) findViewById(R.id.prgBar);


        pos = (TextView)findViewById(R.id.myPosition);
        pos.setText(R.string.notTop);


        idTop = new ArrayList<>(101);
        namesTop = new ArrayList<>(101);
        scoresTop = new ArrayList<>(101);

        idTop.add("000");
        namesTop.add("000");
        scoresTop.add("-1");

        context = this;
        MenuActivity.sw = -1;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("top");


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChildren()) {

                    for (DataSnapshot node : dataSnapshot.getChildren())
                        insertPerson(Long.parseLong(node.child("score").getValue(String.class)), node.getKey(), node.child("name").getValue(String.class));

                    scoresTop.remove(scoresTop.size() - 1);
                    idTop.remove(idTop.size() - 1);
                    namesTop.remove(namesTop.size() - 1);

                }


                    if (idTop != null) {

                        RecyclerView recyclerView;
                        RecyclerView.LayoutManager layoutManager;
                        final RecyclerView.Adapter adapter;

                        recyclerView = (RecyclerView) findViewById(R.id.topList);
                        adapter = new CustomAdapter(namesTop.toArray(new String[namesTop.size()]),null, scoresTop.toArray(new String[scoresTop.size()]), context);

                        layoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                if(findPerson()!=0) pos.setText("Your place: "+ findPerson());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        prgBar.setVisibility(View.GONE);
    }

    private int findPerson(){

        for(int i =0 ; i<idTop.size();i++)
            if(idTop.get(i).equals(SetDataClass.myId))
                return i+1;

        return 0;
    }

    private void insertPerson(long sc,String idd,String nm){

        if(scoresTop.size()==0) {
            scoresTop.add(sc+"");
            namesTop.add(nm);
            idTop.add(idd);
        }

        for(int i =0;i<scoresTop.size();i++){
            if(sc>Long.parseLong(scoresTop.get(i))) {
                scoresTop.add(i, sc+"");
                namesTop.add(i, nm);
                idTop.add(i, idd);

                if(scoresTop.size()==101) {
                    scoresTop.remove(101);
                    namesTop.remove(101);
                    idTop.remove(101);
                }

                break;
            }
            if(i==(scoresTop.size()-1)) {
                scoresTop.set(i,sc+"");
                namesTop.set(i,nm);
                idTop.set(i,idd);

                if(scoresTop.size()==101) {
                    scoresTop.remove(101);
                    namesTop.remove(101);
                    idTop.remove(101);
                }
            }
        }
    }


}
