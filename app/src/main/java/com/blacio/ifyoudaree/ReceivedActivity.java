package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;


public class ReceivedActivity extends MenuActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int k;
    private ProgressBar prgBar;
    static boolean visible;
    private ImageView img;
    private String[]index;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 3;
        visible = true;

        prgBar = (ProgressBar)findViewById(R.id.prgBar);

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        final RecyclerView.Adapter adapter;


        index = new String[2];
        index[0] = "LOAD a picture";
        index[1] = "TAKE a photo";

        if(SetDataClass.vec.size()>0){

            int count = SetDataClass.vec.size();

            String[] s1 = new String[count];
            Bitmap[] s2 = new Bitmap[count];
            String[] s3 = new String[count];

            for(int i=0;i<count;i++){
                s1[i] = SetDataClass.dares3.get(SetDataClass.vec.get(i));
                s2[i] = SetDataClass.pictures3.get(SetDataClass.vec.get(i));
                s3[i] = SetDataClass.status3.get(SetDataClass.vec.get(i));
            }

            SetDataClass.vec.clear();

            adapter = new CustomAdapter(s1,s2,s3,this);
        }


        else if (SetDataClass.pictures3 != null) {
            SetDataClass.swr=false;
            adapter = new CustomAdapter(SetDataClass.dares3.toArray(new String[SetDataClass.dares3.size()]),SetDataClass.pictures3.toArray(new Bitmap[SetDataClass.pictures3.size()]), SetDataClass.status3.toArray(new String[SetDataClass.status3.size()]), this);
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

        this.k=k;

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


        if(x==1 || (x==2 && SetDataClass.status3.get(k).equals("REFUSED"))) {

            pg.setVisibility(View.GONE);
            nm.setText(SetDataClass.dares3.get(k));
            img.setImageBitmap(SetDataClass.pictures3.get(k));
            name.setText(SetDataClass.names3.get(k));
        }
        else if(x==2 || x==3){


            String s2 = SetDataClass.ids3.get(k);
            String s3 = SetDataClass.times3.get(k);

            String s = s2 + "/" + SetDataClass.myId + "/" + s3 + ".png";

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


            if(x==2) {
                yes.setVisibility(View.GONE);
                no.setVisibility(View.GONE);
            }

            else if(x==3) {
                no.setVisibility(View.GONE);
                yes.setText("TRY AGAIN");
            }

            nm.setText(SetDataClass.dares3.get(k));
            name.setText(SetDataClass.names3.get(k));
        }


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                       if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String s2 = SetDataClass.ids3.get(k);
                final String s3 = SetDataClass.times3.get(k);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference();

                DatabaseReference myRef1 = ref.child(s2).child("sent").child(s3);
                DatabaseReference myRef2 = ref.child(SetDataClass.myId).child("received").child(s3);

                myRef1.child("status").setValue("REFUSED");
                myRef2.child("status").setValue("REFUSED");

                prgBar.setVisibility(View.GONE);
                dialog.dismiss();
                reset();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if ((requestCode == REQUEST_IMAGE_CAPTURE)&& resultCode == RESULT_OK) {


            Bitmap imageBitmap;

            final String s2 = SetDataClass.ids3.get(k);
            final String s3 = SetDataClass.times3.get(k);

            String s = s2 + "/" + SetDataClass.myId + "/" + s3 + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference ref = storageRef.child(s);

                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert imageBitmap != null;
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data1 = baos.toByteArray();

                UploadTask uploadTask = ref.putBytes(data1);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        prgBar.setVisibility(View.GONE);

                        Toast.makeText(ReceivedActivity.this, "Uploading failed !",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference();

                        DatabaseReference myRef1 = ref.child(s2).child("sent").child(s3);
                        DatabaseReference myRef2 = ref.child(SetDataClass.myId).child("received").child(s3);

                        myRef1.child("status").setValue("CHECK");
                        myRef2.child("status").setValue("CHECKING");

                        prgBar.setVisibility(View.GONE);

                        Toast.makeText(ReceivedActivity.this, "Sent !",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void reset(){
        this.recreate();
    }

    public void onBackPressed(){
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }

    @Override
    protected void onStop() {

        SetDataClass.swr=false;
        SetDataClass.vec.clear();

        try {
            Glide.clear(img);
        }catch (Exception e){e.printStackTrace();}
        super.onStop();
    }
}
