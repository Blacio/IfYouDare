package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.graphics.Bitmap;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class SentActivity extends MenuActivity {

    private DatabaseReference myRef1,myRef2;
    private ImageView img;
    private ActionBar actionBar;
    private ProgressBar prgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 2;


        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        final RecyclerView.Adapter adapter;


        if(SetDataClass.vec.size()>0){

            int count = SetDataClass.vec.size();

            String[] s1 = new String[count];
            Bitmap[] s2 = new Bitmap[count];
            String[] s3 = new String[count];

            for(int i=0;i<count;i++){
                s1[i] = SetDataClass.dares2.get(SetDataClass.vec.get(i));
                s2[i] = SetDataClass.pictures2.get(SetDataClass.vec.get(i));
                s3[i] = SetDataClass.status2.get(SetDataClass.vec.get(i));
            }

            SetDataClass.vec.clear();
            adapter = new CustomAdapter(s1,s2,s3,this);
        }

        else if (SetDataClass.pictures2 != null) {
            SetDataClass.swr=false;
            adapter = new CustomAdapter(SetDataClass.dares2.toArray(new String[SetDataClass.dares2.size()]), SetDataClass.pictures2.toArray(new Bitmap[SetDataClass.pictures2.size()]), SetDataClass.status2.toArray(new String[SetDataClass.status2.size()]), this);
        }
        else adapter = null;


        recyclerView = (RecyclerView) findViewById(R.id.friendsList);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        prgBar.setVisibility(View.GONE);
    }

    public void createDialog(final int k,int x){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_photo_text);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

        LinearLayout linearLayout = (LinearLayout)dialog.findViewById(R.id.linearDialog);
        linearLayout.setBackgroundColor(Color.parseColor(SetDataClass.color_dial));
        TextView nm = (TextView) dialog.findViewById(R.id.text_dialog);
        img = (ImageView) dialog.findViewById(R.id.dialog_photo);
        Button yes = (Button) dialog.findViewById(R.id.buton_yes);
        Button no = (Button) dialog.findViewById(R.id.buton_no);
        final ProgressBar pg = (ProgressBar)dialog.findViewById(R.id.progBarPhoto);
        TextView name = (TextView) dialog.findViewById(R.id.dareFriendName);


        img.setMinimumHeight(700);
        img.setMinimumWidth(400);


        if(x==1 || (x==2 && SetDataClass.status2.get(k).equals("REFUSED"))) {

            yes.setVisibility(View.GONE);
            no.setVisibility(View.GONE);
            pg.setVisibility(View.GONE);

            nm.setText(SetDataClass.dares2.get(k));
            name.setText(SetDataClass.names2.get(k));
            img.setImageBitmap(SetDataClass.pictures2.get(k));
        }
        else if((x==2 && SetDataClass.status2.get(k).equals("CHECK")) || x==3){

            if(x==3){
                yes.setVisibility(View.GONE);
                no.setVisibility(View.GONE);
            }

            pg.setVisibility(View.VISIBLE);

                String s1 = SetDataClass.ids2.get(k);
                String s3 = SetDataClass.times2.get(k);

                String s = SetDataClass.myId + "/" + s1 + "/" + s3 + ".png";

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
                                pg.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                pg.setVisibility(View.GONE);
                                super.onLoadFailed(e, errorDrawable);
                            }
                        });


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference refD = database.getReference();

                myRef1 = refD.child(SetDataClass.myId).child("sent").child(s3);
                myRef2 = refD.child(s1).child("received").child(s3);

                yes.setVisibility(View.VISIBLE);
                no.setVisibility(View.VISIBLE);

            nm.setText(SetDataClass.dares2.get(k));
            name.setText(SetDataClass.names2.get(k));

            }


            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myRef1.child("status").setValue("DONE");
                    myRef2.child("status").setValue("SOLVED");
                    dialog.dismiss();
                    reset();
                }
            });


            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myRef1.child("status").setValue("DONE");
                    myRef2.child("status").setValue("REJECTED");
                    dialog.dismiss();
                    reset();
                }
            });

        dialog.show();
    }

    private void reset(){
        this.recreate();
    }

    public void onBackPressed(){

    }

    @Override
    protected void onStop() {

        SetDataClass.sws=false;
        SetDataClass.vec.clear();

        try {
            Glide.clear(img);
        }catch (Exception e){e.printStackTrace();}
        super.onStop();
    }


}