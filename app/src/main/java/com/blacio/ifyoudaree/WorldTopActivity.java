package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class WorldTopActivity extends MenuActivity {

    static List<String> idTop;
    private List<String> namesTop;
    private List<String> scoresTop;
    private Context context;
    private TextView pos;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_top);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        ProgressBar prgBar = (ProgressBar) findViewById(R.id.prgBar);
        pos = (TextView)findViewById(R.id.myWscore);
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
        DatabaseReference ref = database.getReference().child("worldDare").child("ids");


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChildren()) {

                    for (DataSnapshot node : dataSnapshot.getChildren())
                        insertPerson(node.getChildrenCount()-1,node.getKey(),node.child("name").getValue(String.class));

                    scoresTop.remove(scoresTop.size()-1);
                    idTop.remove(idTop.size()-1);
                    namesTop.remove(namesTop.size()-1);
                }

                if (scoresTop != null) {

                    RecyclerView recyclerView;
                    RecyclerView.LayoutManager layoutManager;
                    final RecyclerView.Adapter adapter;

                    recyclerView = (RecyclerView) findViewById(R.id.worldTopList);
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


    private int findPerson(){

        for(int i =0 ; i<idTop.size();i++)
            if(idTop.get(i).equals(SetDataClass.myId))
                return i+1;

        return 0;
    }


    public void createPictureDialog(int i){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_photo_text);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

        LinearLayout linearLayout = (LinearLayout)dialog.findViewById(R.id.linearDialog);
        linearLayout.setBackgroundColor(Color.parseColor(SetDataClass.color_dial));

        TextView dfn = (TextView)dialog.findViewById(R.id.dareFriendName);
        TextView nm = (TextView) dialog.findViewById(R.id.text_dialog);
        img = (ImageView) dialog.findViewById(R.id.dialog_photo);
        Button yes = (Button) dialog.findViewById(R.id.buton_yes);
        Button no = (Button) dialog.findViewById(R.id.buton_no);
        final ProgressBar progressBar = (ProgressBar)dialog.findViewById(R.id.progBarPhoto);

        dfn.setVisibility(View.GONE);
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        nm.setVisibility(View.GONE);

        String s = "worldTop/" + idTop.get(i) + "/myWorldPhoto" + ".png";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child(s);

        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into(new GlideDrawableImageViewTarget(img) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        progressBar.setVisibility(View.GONE);
                        super.onLoadFailed(e, errorDrawable);
                    }
                });

        img.setMinimumWidth(800);
        img.setMinimumHeight(1100);
        dialog.show();
    }

    @Override
    protected void onStop() {

        try {
            Glide.clear(img);
        }catch (Exception e){e.printStackTrace();}
        super.onStop();
    }

}
